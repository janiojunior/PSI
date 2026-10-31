# PSI - Diretrizes Globais do Monorepo

Este repositório utiliza uma arquitetura **Monorepo** contendo o Backend (`backend/`) e o Frontend (`frontend/`) do sistema **PSI**, além da integração para migração gradual do sistema legado (**TP2 / SGA**).

---

## 1. Estrutura do Monorepo

```text
PSI/
├── AGENTS.md                  # Diretrizes globais, arquitetura geral e fluxo de migração
├── docs/                      # Documentação viva de contexto, negócio e modelo de dados
│   ├── CONTEXTO_PROJETO.md    # Visão, decisões (ADRs), status da migração e pendências
│   ├── REGRAS_NEGOCIO.md      # Especificação funcional, entidades, validações e fluxos
│   └── modelo.plantuml        # Diagrama PlantUML do modelo de dados do Backend
├── backend/                   # API REST em Java 25 + Quarkus 3.39.2 + PostgreSQL + Flyway
│   └── AGENTS.md              # Regras e padrões exclusivos do Backend
├── frontend/                  # SPA em Angular 19 + Angular Material + Signals
│   └── AGENTS.md              # Regras e padrões exclusivos do Frontend
└── .agents/                   # Configuração de plugins e servidores MCP (legacy-bridge)
```

> **Importante:** Sempre consulte [`backend/AGENTS.md`](./backend/AGENTS.md) ao criar ou alterar arquivos no Backend e [`frontend/AGENTS.md`](./frontend/AGENTS.md) ao trabalhar no Frontend.

---

## 2. Documentação Viva do Projeto (`docs/`)

Os arquivos na pasta `docs/` representam a fonte da verdade sobre o domínio e o estado do projeto. Todo agente deve seguir este fluxo:

1. **Antes de implementar ou alterar funcionalidades:**
   - Leia [`docs/REGRAS_NEGOCIO.md`](./docs/REGRAS_NEGOCIO.md) para respeitar os contratos, tamanhos de campo e validações existentes.
   - Consulte [`docs/modelo.plantuml`](./docs/modelo.plantuml) para compreender as entidades e relacionamentos do Backend.
2. **Ao criar ou modificar entidades/migrations no Backend:**
   - Atualize [`docs/modelo.plantuml`](./docs/modelo.plantuml) refletindo as novas classes, atributos, tipos e multiplicidades.
   - Documente as novas regras e contratos em [`docs/REGRAS_NEGOCIO.md`](./docs/REGRAS_NEGOCIO.md).
3. **Ao concluir uma entrega ou migração do legado:**
   - Atualize a tabela de progresso e as pendências em [`docs/CONTEXTO_PROJETO.md`](./docs/CONTEXTO_PROJETO.md).

---

## 3. Sincronização Full-Stack (Backend <-> Frontend)

Ao criar ou alterar uma funcionalidade de ponta a ponta:
1. **Contrato de Dados:** Os campos e validações definidos no `DTO` do Backend (`com.example.dto.<Entidade>DTO`) devem refletir exatamente na interface TypeScript do Frontend (`frontend/src/app/models/<entidade>.model.ts`) e nos `Validators` do formulário reativo.
2. **Endpoints e CORS:** O Backend roda por padrão em `http://localhost:8080` e o Frontend em `http://localhost:4200` (CORS habilitado em `application.properties`).
3. **Banco de Dados:** Toda alteração de schema no Backend deve ser acompanhada de uma migration versionada do **Flyway** em `backend/src/main/resources/db/migration/`.

---

## 4. Diretrizes de Migração do Legado (TP2 / SGA -> PSI)

Ao migrar qualquer recurso, entidade ou tela dos projetos antigos para o `PSI`:
1. **Inspeção via MCP:** Utilize as ferramentas MCP (`sga-migration-expert` e `legacy-bridge`) para inspecionar o código original antes de implementar:
   - **Backend Legado:** `Quarkus/sga` (verifique entidades, regras de negócio, endpoints e relacionamentos).
   - **Frontend Legado:** `ProjetoAngular/hello-world-angular` (verifique campos de tela, fluxos de formulário e serviços).
2. **Modernização de Arquitetura:** Não copie código legado literalmente. Adapte-o para a arquitetura limpa do `PSI` (usando a implementação de `Estado` como referência canônica no Backend e no Frontend).

---

## 5. Convenções Gerais

- **Idioma:** Mensagens de validação, feedbacks de UI (`MatSnackBar`), comentários de código e documentação devem ser escritos em **Português (pt-BR)**.
- **Nomenclatura de Domínio:** Mantenha os nomes das entidades, atributos e rotas em português coerentes com o domínio de negócio (ex: `Estado`, `sigla`, `nome`, `/estados`).
- **Qualidade:** Não exponha entidades JPA diretamente na API REST e não utilize o tipo `any` no TypeScript.
