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
| **Validação API** | Jakarta Bean Validation | Anotações `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Past` diretamente nos `record` DTOs |
| **Frontend** | Angular `19.0.0` + TypeScript `~5.6.2` | 100% *Standalone Components*, porta `4200`, `provideHttpClient(withFetch())` |
| **UI / Design** | Angular Material `19.0.0` + SCSS | Tabelas paginadas (`MatTable`, `MatPaginator`), feedbacks via `MatSnackBar` |
| **Estado Front** | Angular Signals + RxJS | Estado reativo com `signal()`, `.asReadonly()` e sincronização via `effect()` |

---

## 3. Principais Decisões Arquiteturais (ADRs)

1. **Padrão Repository em vez de Active Record (Backend):**
   - As entidades em `com.example.model` são POJOs JPA puros (sem herança de `DefaultEntity` ou `PanacheEntity`). Toda operação de banco passa por `PanacheRepository<Entidade>`.
2. **Isolamento de Domínio com Java Records (Backend):**
   - Entidades JPA nunca são expostas nos endpoints REST. A comunicação utiliza exclusivamente `record` DTOs (`com.example.dto`) convertidos por classes `@ApplicationScoped` em `com.example.mapper`.
3. **Versionamento Estrito de Schema com Flyway (Banco de Dados):**
   - Nenhuma alteração de tabela é feita automaticamente pelo ORM. Mudanças estruturais ou ajustes de compatibilidade com o banco legado (`topicos2db`) são feitos via scripts versionados em `backend/src/main/resources/db/migration/` (`V1.0.0` a `V1.0.3`).
4. **Vínculo Formal entre `Pessoa` e `Municipio`:**
   - `Pessoa` possui associação `@ManyToOne(fetch = FetchType.LAZY)` para `Municipio` (`id_municipio`), mantendo `endereco` para complemento/logradouro e unicidade global para `cpf`, `email`, `crp`, `login` e `sigla`.
5. **Gerenciamento de Estado Reativo com Signals (Frontend):**
   - Serviços HTTP mantêm cache/estado local em `signal` privados (`_<entidades>`, `_loading`, `_error`) atualizados via operador `tap` do RxJS nas mutações (`create`, `update`, `delete`).
6. **Componente Único de Formulário para Criação e Edição (Frontend):**
   - As rotas `/<entidades>/novo` e `/<entidades>/editar/:id` reutilizam o mesmo componente `<Entidade>FormComponent`, alternando o comportamento pela presença do parâmetro `:id` na rota.

---

## 4. Status de Implementação e Migração (Legado -> PSI)

Abaixo está o acompanhamento ponta a ponta dos módulos do sistema:

| Módulo / Entidade | Migration (Flyway) | Backend (Quarkus) | Frontend (Angular) | Status Geral | Observações |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Layout Base (Shell)** | N/A | N/A | ✅ Concluído | **Concluído** | `LayoutComponent` com navegação principal e `router-outlet` |
| **Região (`Regiao`)** | ✅ `V1.0.3` | ✅ `/regioes` | ⏳ Pendente | **Backend Concluído** | Enum + `RegiaoConverter` + `RegiaoResource` |
| **Estado (`Estado`)** | ✅ `V1.0.0` / `V1.0.1` / `V1.0.3` | ✅ `/estados` | ✅ `/estados` | **Concluído** | Inclui `Regiao`, unicidade de `sigla`, busca por sigla/nome e paginação |
| **Município (`Municipio`)** | ✅ `V1.0.2` | ✅ `/municipios` | ✅ `/municipios` | **Concluído** | CRUD completo + paginação e filtro por nome |
| **Pessoa e Telefone** | ✅ `V1.0.3` | ✅ Interno (Composição) | ⏳ Pendente | **Backend Concluído** | Base compartilhada com vínculo `@ManyToOne` para `Municipio` |
| **Paciente (`Paciente`)** | ✅ `V1.0.3` | ✅ `/pacientes` | ⏳ Pendente | **Backend Concluído** | CRUD + busca por CPF/nome + paginação em 6 camadas |
| **Psicólogo (`Psicologo`)** | ✅ `V1.0.3` | ✅ `/psicologos` | ⏳ Pendente | **Backend Concluído** | CRUD + busca por CPF/CRP/nome + paginação em 6 camadas |
| **Usuário e Perfil (`Usuario`)** | ✅ `V1.0.3` | ✅ `/usuarios` | ⏳ Pendente | **Backend Concluído** | CRUD + `SenhaService` (PBKDF2-SHA256) + `PerfilConverter` |
| **Autenticação JWT (`Auth`)** | ✅ `V1.0.4` | ✅ `/auth/login`, `/auth/me` | ✅ `/login` + `AuthService` + `authGuard` + `authInterceptor` | **Concluído** | SmallRye JWT (`RS256`, 8h) + controle de acesso `@RolesAllowed` e sessão reativa no Angular |

---

## 5. Pendências e Próximos Passos

- [x] **Refatoração e Migração Completa do Backend Legado (`SGA / TP2` -> `PSI`):** Todas as entidades, conversores JPA, repositórios, mappers, services, resources e testes unitários migrados para a arquitetura em 6 camadas do PSI.
- [ ] **Migração das Telas Restantes do Frontend (`Pacientes`, `Psicólogos`, `Usuários`):** Implementar os componentes standalone Angular 19 + Signals consumindo os novos endpoints do Backend.
- [ ] **Testes de Integração `@QuarkusTest` com Banco Real:** Expandir a suíte de testes de integração REST Assured quando o container PostgreSQL estiver ativo.
- [ ] **Containerização Local:** Adicionar `docker-compose.yml` na raiz para provisionamento padronizado do banco `topicos2db` em PostgreSQL.
