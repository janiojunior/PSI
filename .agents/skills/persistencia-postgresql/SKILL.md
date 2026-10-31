---
name: persistencia-postgresql
description: >-
  Orienta alterações de persistência com PostgreSQL, Hibernate ORM, JPA,
  Panache Repository e Flyway. Use esta skill sempre que criar ou alterar
  entidades, mapeamentos JPA, repositories, consultas PostgreSQL, migrations,
  índices, restrições, transações, isolamento entre tenants e controle de
  concorrência.
---

# Persistência com PostgreSQL, Hibernate ORM, Panache Repository e Flyway (`persistencia-postgresql`)

Esta skill define os padrões arquiteturais, regras de integridade e procedimentos obrigatórios para criar, alterar ou revisar a camada de persistência do Backend (`backend/`) utilizando **PostgreSQL**, **Jakarta Persistence (JPA) / Hibernate ORM**, **Quarkus Panache Repository** e **Flyway**.

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou alteração de entidades JPA (`com.example.model.<Entidade>`);
- Mapeamentos de colunas, relacionamentos (`@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`) e tipos de dados JPA;
- Criação ou evolução de repositórios (`com.example.repository.<Entidade>Repository`) com `PanacheRepository`;
- Escrita ou otimização de consultas HQL/JPQL ou SQL nativo para PostgreSQL;
- Criação de migrations versionadas no Flyway (`backend/src/main/resources/db/migration/`);
- Definição de índices (`INDEX`), restrições de integridade (`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `CHECK`, `NOT NULL`);
- Gerenciamento de transações (`@Transactional`) e garantia de atomicidade;
- Isolamento de dados multi-tenant (tenant obrigatório e prevenção de relações cruzadas);
- Controle de concorrência (bloqueio otimista com `@Version`, bloqueio pessimista com `FOR UPDATE` e prevenção de condições de corrida).

---

## 2. Etapa Obrigatória de Inspeção Prévia

**Nunca** crie ou modifique entidades, repositórios ou scripts SQL sem antes inspecionar a base de conhecimento e o código atual do projeto:

1. **Regras de Negócio:** Leia [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) para verificar obrigatoriedades, limites de caracteres, formatos, unicidades e regras de domínio.
2. **Contexto do Projeto:** Leia [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) para compreender as decisões arquiteturais (ADRs), banco configurado (`topicos2db`) e pendências.
3. **Diagrama de Classes UML:** Consulte [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) para validar nomes de atributos, tipos, visibilidade, relacionamentos e multiplicidades.
4. **Entidades e Migrations Existentes:** Inspecione `backend/src/main/java/com/example/model/`, `backend/src/main/java/com/example/repository/` e `backend/src/main/resources/db/migration/` (tome `Estado` e `EstadoRepository` como referência canônica).

---

## 3. Regra de Bloqueio: Novas Decisões de Domínio Exigem Aprovação

> **IMPORTANTE — REGRA DE GOVERNANÇA DE DOMÍNIO:**
> **NÃO crie nem altere entidades JPA (`model/`), schema do banco ou migrations do Flyway (`db/migration/`) sem aprovação explícita do usuário quando a mudança representar uma nova decisão de domínio.**

Caracterizam **nova decisão de domínio** (exigindo alinhamento e aprovação prévia):
- Inclusão ou remoção de entidades, tabelas ou atributos de negócio não previstos em `docs/REGRAS_NEGOCIO.md` ou `docs/modelo.plantuml`;
- Alteração de cardinalidade/multiplicidade (ex: transformar `1..*` em `*..*` ou alterar obrigatoriedade `NOT NULL` <-> `NULL`);
- Definição de escopo de unicidade (global vs. por tenant) quando não especificado;
- Escolha da política de exclusão/integridade referencial (`RESTRICT`, `CASCADE`, `SET NULL` ou exclusão lógica) não documentada.

**Fluxo em caso de decisão de domínio pendente:**
1. Pare antes de gerar migrations ou alterar entidades;
2. Apresente ao usuário os pontos em aberto, o impacto no modelo (`docs/modelo.plantuml`) e no schema PostgreSQL;
3. Aguarde a confirmação explícita antes de prosseguir com a implementação.

---

## 4. Diretrizes de Modelagem de Entidades JPA (`com.example.model`)

1. **POJO Puro (Sem Active Record):**
   - Toda entidade deve ser anotada com `@Entity` e `@Table(name = "<nome_tabela>")`.
   - **Proibido** estender `PanacheEntity` ou `PanacheEntityBase`. O acesso a dados deve ocorrer exclusivamente via `PanacheRepository`.
2. **Atributos Privados e Acessores Explícitos:**
   - Todos os atributos da entidade devem ser declarados como `private`.
   - Implemente construtor padrão público (`public <Entidade>() {}`), construtor parametrizado quando aplicável, e métodos `get...()` e `set...()` explícitos para todos os campos.
   - Não exponha coleções mutáveis sem controle; inicialize coleções (`new ArrayList<>()`) e prefira métodos utilitários para manter a coerência bidirecional quando houver `@OneToMany`.
3. **Identificador e Igualdade (`equals` / `hashCode`):**
   - Chave primária `private Long id;` anotada com `@Id` e `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
   - Implemente `equals` e `hashCode` baseados exclusivamente no `id` (usando `Objects.equals(id, other.id)` e `Objects.hash(id)`).
4. **Mapeamento Explícito de Colunas e Relacionamentos:**
   - Declare sempre `@Column(name = "...", length = ..., nullable = ...)` em aderência exata à migration SQL e ao `docs/REGRAS_NEGOCIO.md`.
   - Em relacionamentos `@ManyToOne` e `@OneToOne`, defina explicitamente `fetch = FetchType.LAZY` (evitando carregamento ansioso desnecessário) e `@JoinColumn(name = "<entidade>_id", nullable = ...)`.

---

## 5. Migrations SQL Versionadas (Flyway) e Integridade Referencial

1. **Versionamento Estrito (`database.generation=none`):**
   - O Hibernate nunca deve criar ou alterar tabelas automaticamente.
   - Toda alteração estrutural exige um novo arquivo SQL em `backend/src/main/resources/db/migration/` seguindo o padrão:
     `V1.X.Y__Descricao_clara_da_mudanca.sql` (ex: `V1.0.2__Create_municipio_table.sql`).
   - **Imutabilidade de Migrations:** Nunca edite ou apague uma migration que já foi executada. Crie sempre uma nova versão incremental (`ALTER TABLE ...`).
2. **Integridade Referencial e Restrições (Constraints):**
   - **Primary Key:** Toda tabela deve possuir chave primária explícita (`id BIGSERIAL PRIMARY KEY` ou `BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY`).
   - **Foreign Keys:** Todo relacionamento deve possuir constraint `FOREIGN KEY` explícita no banco com nomenclatura padronizada (`fk_<tabela_origem>_<tabela_destino>`) e ação referencial consciente (`ON DELETE RESTRICT` por padrão para proteger histórico, ou `ON DELETE CASCADE` apenas em composições estritas).
   - **Unicidade e Domínio:** Utilize constraints nomeadas (`uk_<tabela>_<colunas>`, `ck_<tabela>_<regra>`) e `NOT NULL` em todas as colunas obrigatórias.
3. **Índices (`INDEX`) no PostgreSQL:**
   - O PostgreSQL **não** cria índices automaticamente em colunas de chave estrangeira (`FOREIGN KEY`). Crie sempre índices explícitos para todas as FKs:
     `CREATE INDEX idx_<tabela>_<coluna_fk> ON <tabela> (<coluna_fk>);`
   - Crie índices compostos ou funcionais (ex: `UPPER(sigla)`) para colunas frequentemente utilizadas em buscas, ordenações ou filtros por tenant.

---

## 6. Isolamento entre Tenants e Prevenção de Relações Cruzadas

Quando a entidade pertencer a um escopo multi-tenant (organização, unidade ou locatário):

1. **Tenant Obrigatório:**
   - A coluna identificadora do tenant (ex: `tenant_id` ou `organizacao_id`) deve ser mapeada com `nullable = false` na entidade JPA e `NOT NULL` com `FOREIGN KEY` na migration SQL.
   - Unicidades de negócio dentro do tenant devem ser modeladas como constraints compostas: `UNIQUE (tenant_id, <campo_unico>)`.
   - Índices de busca e chave estrangeira devem priorizar o prefixo `tenant_id` (ex: `idx_<tabela>_tenant_<campo>`).
2. **Isolamento nas Consultas e Operações (`PanacheRepository`):**
   - Toda consulta (`findById`, listagens, buscas por código/nome, verificações de existência e exclusões) em entidades *tenant-scoped* deve filtrar obrigatoriamente pelo `tenantId` do contexto atual (`find("id = ?1 and tenant.id = ?2", id, tenantId)`).
3. **Prevenção de Relações Cruzadas entre Tenants (*Cross-Tenant References*):**
   - Uma entidade do **Tenant A** jamais pode referenciar uma entidade pertencente ao **Tenant B**.
   - **Na Camada de Serviço (`@Transactional`):** Ao associar duas entidades (ex: vincular uma entidade filha a uma entidade pai/relacionada), valide explicitamente que ambas pertencem ao mesmo `tenantId` antes de persistir; caso contrário, rejeite a operação.
   - **No Banco de Dados (Defesa em Profundidade):** Para tabelas críticas *tenant-scoped*, utilize chaves únicas compostas `UNIQUE (tenant_id, id)` na tabela referenciada e chaves estrangeiras compostas `FOREIGN KEY (tenant_id, <relacionamento>_id) REFERENCES <tabela_destino>(tenant_id, id)` para tornar relações cruzadas impossíveis no nível do PostgreSQL.

---

## 7. Panache Repository e Consultas PostgreSQL (`com.example.repository`)

1. **Estrutura do Repository:**
   - Anote com `@ApplicationScoped` e implemente `PanacheRepository<<Entidade>>`.
   - Métodos de consulta única devem retornar `Optional<<Entidade>>` (`firstResultOptional()`).
2. **Consultas Seguras e Eficientes:**
   - Use sempre parâmetros posicionais (`?1`, `?2`) ou nomeados (`:param`) — **nunca** concatene strings em queries.
   - Para buscas textuais insensíveis a maiúsculas/minúsculas, padronize com `UPPER(campo) = UPPER(?1)` ou `LOWER(campo) LIKE LOWER(?1)`.
   - Previna o problema de *N+1 queries* em listagens que necessitem de associações `LAZY` utilizando `JOIN FETCH` na consulta do repositório.

---

## 8. Transações, Atomicidade e Controle de Concorrência

1. **Fronteira Transacional e Atomicidade (`@Transactional`):**
   - A anotação `@Transactional` deve residir nos métodos de negócio da camada **Service (`com.example.service.impl.<Entidade>ServiceImpl`)**, nunca no `Resource` (Controller).
   - Toda operação de escrita que envolva múltiplas validações e mutações deve executar dentro de uma única transação atômica, garantindo *rollback* completo em caso de exceção.
2. **Controle de Concorrência Otimista (`@Version`):**
   - Em entidades sujeitas a edições simultâneas por múltiplos usuários/processos, utilize coluna de versionamento:
     - No banco: `version BIGINT NOT NULL DEFAULT 0`
     - Na entidade: `@Version @Column(name = "version", nullable = false) private Long version;`
   - Trate `OptimisticLockException` convertendo para resposta semântica (`409 Conflict`) com mensagem clara em português.
3. **Controle de Concorrência Pessimista e Condições de Corrida:**
   - Quando uma regra de negócio exigir leitura seguida de atualização crítica (ex: reserva de vagas, abatimento de saldo, geração de sequencial lógico), utilize bloqueio pessimista no repositório:
     `find("id = ?1 and tenant.id = ?2", LockModeType.PESSIMISTIC_WRITE, id, tenantId).firstResultOptional()` (`SELECT ... FOR UPDATE` no PostgreSQL).
   - **Unicidade Concorrente:** A verificação prévia no Service (`findBySigla`, `existsBy...`) oferece feedback amigável, mas **não substitui** a constraint `UNIQUE` no PostgreSQL, que é a única garantia real contra inserções concorrentes (*race conditions*).

---

## 9. Sincronização Pós-Implementação

Após implementar ou alterar qualquer item de persistência (com aprovação de domínio quando aplicável):
1. Verifique se [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) reflete fielmente a entidade, atributos privados, tipos e cardinalidades;
2. Atualize [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) e [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md);
3. Valide a compilação e os testes executando `./mvnw clean compile` ou `./mvnw test` na pasta `backend/`.
