---
name: quarkus-backend
description: >-
  Orienta a implementação e revisão de funcionalidades backend em Java 25 com
  Quarkus no PSI. Use esta skill sempre que criar ou alterar endpoints REST,
  Resources, Services, Mappers, Repositories, DTOs, validações, contratos HTTP,
  regras de negócio, autorização, isolamento entre tenants e transações.
---

# Implementação e Revisão de Backend Java/Quarkus (`quarkus-backend`)

Esta skill define a arquitetura em camadas, os contratos HTTP, as regras de isolamento de domínio e o fluxo de governança obrigatórios para implementar, evoluir e revisar funcionalidades no módulo **`backend/`** (Java 25 + Quarkus 3.39.2 + Jakarta REST + Bean Validation + Hibernate ORM Panache).

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou alteração de endpoints REST (`com.example.resource.<Entidade>Resource`);
- Implementação de interfaces e regras de serviço (`com.example.service.<Entidade>Service` e `impl.<Entidade>ServiceImpl`);
- Criação ou revisão de `record` DTOs (`com.example.dto.<Entidade>DTO`) e conversores (`com.example.mapper.<Entidade>Mapper`);
- Integração com repositórios (`com.example.repository.<Entidade>Repository`) e entidades (`com.example.model.<Entidade>`);
- Aplicação de validações (`@Valid`, `@NotBlank`, `@NotNull`, `@Size`), sanitização de dados e contratos HTTP;
- Gerenciamento de transações (`@Transactional`), autorização por perfil e isolamento multi-tenant.

---

## 2. Etapa Obrigatória de Inspeção Prévia

**Nunca** crie ou altere classes no Backend sem antes inspecionar a documentação viva e a estrutura existente do projeto:

1. **Diretrizes Arquiteturais:**
   - Leia [`AGENTS.md`](../../../AGENTS.md) e [`backend/AGENTS.md`](../../../backend/AGENTS.md).
2. **Documentação de Domínio e Status do Projeto:**
   - Leia [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) para consultar contratos, tamanhos de campo, validações, códigos HTTP e mensagens em português.
   - Leia [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) e [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) para distinguir o que já está implementado (`<<Entity>>`) do que ainda é proposta (`<<Proposto>>`).
3. **Identificação de Regras Aprovadas vs. Decisões Pendentes:**
   - Separe explicitamente as **regras aprovadas** (documentadas em `REGRAS_NEGOCIO.md`) das **decisões de domínio pendentes** (registradas nas seções de pendências ou notas do `modelo.plantuml`, como escopo multi-tenant, auditoria e vínculos em aberto).
4. **Inspeção da Referência Canônica (`Estado`):**
   - Consulte a implementação de referência antes de codificar novos módulos:
     - [`Estado.java`](../../../backend/src/main/java/com/example/model/Estado.java)
     - [`EstadoDTO.java`](../../../backend/src/main/java/com/example/dto/EstadoDTO.java)
     - [`EstadoMapper.java`](../../../backend/src/main/java/com/example/mapper/EstadoMapper.java)
     - [`EstadoRepository.java`](../../../backend/src/main/java/com/example/repository/EstadoRepository.java)
     - [`EstadoService.java`](../../../backend/src/main/java/com/example/service/EstadoService.java) e [`EstadoServiceImpl.java`](../../../backend/src/main/java/com/example/service/impl/EstadoServiceImpl.java)
     - [`EstadoResource.java`](../../../backend/src/main/java/com/example/resource/EstadoResource.java)

---

## 3. Limites Estritos e Governança (Regras de Bloqueio)

> **IMPORTANTE — LIMITES DE ESCOPO E APROVAÇÃO:**
> 1. **NÃO altere regras de negócio consolidadas, entidades JPA (`model/`) ou migrations do Flyway (`db/migration/`) sem aprovação explícita do usuário.**
> 2. **NUNCA invente endpoints, entidades, atributos ou regras de negócio** que não estejam especificados na documentação ou solicitados pelo usuário.
> 3. **NUNCA retorne nem receba entidades JPA (`@Entity`) diretamente nos métodos do `Resource`.** Toda comunicação externa da API deve ocorrer exclusivamente via `DTO`.

**Como proceder diante de decisões pendentes ou lacunas de especificação:**
- Pare antes de implementar suposições no código;
- Liste de forma objetiva para o usuário o que está aprovado e qual decisão pendente precisa de confirmação;
- Aguarde a aprovação explícita antes de criar/alterar entidades, migrations ou contratos.

---

## 4. Arquitetura em Camadas (`Resource -> Service -> Repository -> Entity`)

Mantenha a separação estrita de responsabilidades por domínio dentro do pacote base `com.example`:

### 4.1. Resource (`com.example.resource.<Entidade>Resource`)
- Anotado com `@Path("/<recursos-no-plural>")`, `@Produces(MediaType.APPLICATION_JSON)` e `@Consumes(MediaType.APPLICATION_JSON)`.
- Injeta **apenas** a interface `<Entidade>Service` (`@Inject <Entidade>Service service;`).
- **Proibido** injetar `Repository`, `EntityManager` ou `Mapper` diretamente no `Resource`.
- **Proibido** anotar métodos do `Resource` com `@Transactional` ou incluir lógica de negócio na camada web.
- Valide corpos de entrada (`POST` e `PUT`) com `@Valid` e retorne `jakarta.ws.rs.core.Response` com semântica HTTP exata:
  - `GET /<recursos>`: `200 OK` (`Response.ok(lista).build()`)
  - `GET /<recursos>/{id}`: `200 OK` ou `404 Not Found`
  - `POST /<recursos>`: `201 Created` (`Response.created(URI.create("/<recursos>/" + created.id())).entity(created).build()`)
  - `PUT /<recursos>/{id}`: `200 OK` (`Response.ok(updated).build()`) ou `404 Not Found`
  - `DELETE /<recursos>/{id}`: `204 No Content` (`Response.noContent().build()`) ou `404 Not Found`

### 4.2. DTO (`com.example.dto.<Entidade>DTO`)
- Implementado como Java `record` imutável.
- Concentra as anotações do Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Positive`, etc.) sempre com `message = "..."` clara em **Português (pt-BR)**.
- Nunca exponha campos internos sensíveis (como hash de senha) em DTOs de leitura/resposta.

### 4.3. Mapper (`com.example.mapper.<Entidade>Mapper`)
- Bean `@ApplicationScoped` responsável pela conversão isolada entre `Entity` e `DTO`:
  - `toDTO(<Entidade> entity)`
  - `toEntity(<Entidade>DTO dto)`
  - `updateEntityFromDTO(<Entidade>DTO dto, <Entidade> entity)`
- Aplique aqui as higienizações determinísticas de dados previstas nas regras de negócio (ex: `.trim()` em textos e `.toUpperCase()` em siglas/códigos).

### 4.4. Service (`com.example.service.<Entidade>Service` e `impl.<Entidade>ServiceImpl`)
- A interface `<Entidade>Service` define o contrato de negócio operando exclusivamente com DTOs e tipos de domínio desacoplados da camada HTTP.
- A classe `<Entidade>ServiceImpl` (`@ApplicationScoped`) orquestra regras de negócio, validações de integridade/unicidade, verificação de tenant e chamadas ao `Repository` e `Mapper`.
- Demarque `@Transactional` nos métodos de mutação (`create`, `update`, `delete` e fluxos transacionais compostos).
- Lance `jakarta.ws.rs.NotFoundException("<Entidade> com ID " + id + " não encontrado.")` quando o recurso buscado, atualizado ou excluído não existir.

### 4.5. Repository (`com.example.repository.<Entidade>Repository`) e Entity (`com.example.model.<Entidade>`)
- O `Repository` (`@ApplicationScoped implements PanacheRepository<<Entidade>>`) encapsula todas as consultas ao PostgreSQL (para diretrizes detalhadas de JPA, SQL e Flyway, consulte também a skill `persistencia-postgresql`).
- A `Entity` é um POJO JPA puro com atributos `private`, acessores explícitos e `equals`/`hashCode` baseados apenas no `id`.

---

## 5. Autorização e Isolamento entre Tenants

1. **Validação de Autorização:**
   - Em recursos protegidos, valide a identidade e o perfil do usuário autenticado (`ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO`, `PACIENTE`), garantindo `401 Unauthorized` para requisições não autenticadas e `403 Forbidden` para papéis sem permissão na operação.
2. **Isolamento Multi-Tenant e Bloqueio de Relações Cruzadas:**
   - Em entidades *tenant-scoped*, todas as operações do `Service` e consultas do `Repository` devem restringir o escopo ao `tenantId` ativo da sessão/contexto;
   - Nunca permita que uma operação de criação ou atualização associe entidades pertencentes a tenants diferentes (*cross-tenant reference*); valide a titularidade de todas as entidades relacionadas dentro da mesma transação.

---

## 6. Execução de Testes, Build e Registro de Evidências

Ao concluir qualquer implementação ou revisão no Backend:

1. **Compile e Execute os Testes Reais:**
   - Execute `./mvnw test` (ou `./mvnw clean compile` quando verificar apenas compilação) a partir do diretório `backend/` (seguindo também a skill `testes-backend`).
2. **Registre Evidências Reais:**
   - Informe na resposta final os comandos executados e o resultado real obtido no terminal (`Tests run`, `Failures`, `Errors`, status do build).
   - **Nunca** declare que testes passaram ou que o build foi validado sem tê-los efetivamente executado. Se houver bloqueio de ambiente (ex: banco/Docker indisponível), relate o erro real de forma transparente.
3. **Atualização da Documentação Viva:**
   - Quando uma funcionalidade aprovada for concluída, mantenha [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md), [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) e [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) sincronizados.
