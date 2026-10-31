# Contexto do Projeto - PSI

Este documento registra a visão geral, as decisões arquiteturais (ADRs), o estado atual das entregas full-stack e as pendências da migração do sistema legado (**TP2 / SGA**) para o **PSI**.

> **Instrução para Agentes:** Atualize este documento sempre que uma nova entidade, tela, migration ou decisão arquitetural relevante for concluída.

---

## 1. Visão Geral e Objetivo

O **PSI** é uma evolução e modernização arquitetural do sistema legado (**TP2 / SGA** — composto anteriormente pelos repositórios `Quarkus/sga` no Backend e `ProjetoAngular/hello-world-angular` no Frontend).

O objetivo do monorepo é consolidar Backend e Frontend em uma arquitetura limpa, padronizada, fortemente tipada e orientada a contratos, eliminando acoplamentos do código legado (como exposição direta de entidades JPA ou gerenciamento de estado imperativo sem reatividade moderna).

---

## 2. Stack Tecnológica e Infraestrutura

| Camada | Tecnologia / Versão | Detalhes de Configuração |
| :--- | :--- | :--- |
| **Backend** | Java `25` + Quarkus `3.39.2` | Pacote base `com.example`, porta `8080`, Swagger UI em `/q/swagger-ui` |
| **Persistência** | Hibernate ORM Panache + PostgreSQL | Padrão *Repository* (`PanacheRepository`), banco `topicos2db` (`localhost:5432`) |
| **Migrations** | Flyway (`quarkus-flyway`) | Geração automática do Hibernate desativada (`database.generation=none`), baseline ativo |
| **Validação API** | Jakarta Bean Validation | Anotações `@NotBlank`, `@NotNull`, `@Size` diretamente nos `record` DTOs |
| **Frontend** | Angular `19.0.0` + TypeScript `~5.6.2` | 100% *Standalone Components*, porta `4200`, `provideHttpClient(withFetch())` |
| **UI / Design** | Angular Material `19.0.0` + SCSS | Tabelas paginadas (`MatTable`, `MatPaginator`), feedbacks via `MatSnackBar` |
| **Estado Front** | Angular Signals + RxJS | Estado reativo com `signal()`, `.asReadonly()` e sincronização via `effect()` |

---

## 3. Principais Decisões Arquiteturais (ADRs)

1. **Padrão Repository em vez de Active Record (Backend):**
   - As entidades em `com.example.model` são POJOs JPA puros e **não** estendem `PanacheEntity`. Toda operação de banco passa por `PanacheRepository<Entidade>`.
2. **Isolamento de Domínio com Java Records (Backend):**
   - Entidades JPA nunca são expostas nos endpoints REST. A comunicação utiliza exclusivamente `record` DTOs (`com.example.dto`) convertidos manualmente por classes `@ApplicationScoped` em `com.example.mapper`.
3. **Versionamento Estrito de Schema com Flyway (Banco de Dados):**
   - Nenhuma alteração de tabela é feita automaticamente pelo ORM. Mudanças estruturais ou ajustes de compatibilidade com o banco legado (`topicos2db`) são feitos via scripts versionados em `backend/src/main/resources/db/migration/`.
4. **Gerenciamento de Estado Reativo com Signals (Frontend):**
   - Serviços HTTP mantêm cache/estado local em `signal` privados (`_<entidades>`, `_loading`, `_error`) atualizados via operador `tap` do RxJS nas mutações (`create`, `update`, `delete`).
5. **Componente Único de Formulário para Criação e Edição (Frontend):**
   - As rotas `/<entidades>/novo` e `/<entidades>/editar/:id` reutilizam o mesmo componente `<Entidade>FormComponent`, alternando o comportamento pela presença do parâmetro `:id` na rota.

---

## 4. Status de Implementação e Migração (Legado -> PSI)

Abaixo está o acompanhamento ponta a ponta dos módulos do sistema:

| Módulo / Entidade | Migration (Flyway) | Backend (Quarkus) | Frontend (Angular) | Status Geral | Observações |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Layout Base (Shell)** | N/A | N/A | ✅ Concluído | **Concluído** | `LayoutComponent` com navegação principal e `router-outlet` |
| **Estado** | ✅ `V1.0.0` / `V1.0.1` | ✅ `/estados` | ✅ `/estados` | **Concluído** | Implementação de referência canônica (CRUD completo) |
| **Município** | ✅ `V1.0.2` | ✅ `/municipios` | ✅ `/municipios` | **Concluído** | Migrado do legado e adaptado ao padrão PSI (CRUD + vínculo com `Estado`) |

*(Novas entidades migradas do SGA/TP2 devem ser adicionadas à tabela acima conforme forem identificadas e implementadas).*

---

## 5. Pendências e Próximos Passos

- [ ] **Mapeamento das próximas entidades do legado (`SGA / TP2`):** Inspecionar as demais entidades dependentes (ex: `Municipio` / `Cidade` e demais cadastros do domínio) para planejar a próxima onda de migração.
- [ ] **Constraint de Unicidade de Sigla em `Estado`:** Avaliar adição de constraint `UNIQUE` na coluna `sigla` via migration Flyway e validação prévia no `EstadoServiceImpl` utilizando `EstadoRepository.findBySigla`.
- [ ] **Testes Automatizados:** Criar testes de integração com `@QuarkusTest` + REST Assured para `EstadoResource` e testes unitários no Frontend.
- [ ] **Containerização Local:** Adicionar `docker-compose.yml` na raiz para provisionamento padronizado do banco `topicos2db` em PostgreSQL.
