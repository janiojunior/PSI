# Diretrizes de Migração do Legado (TP2) para o PSI

Ao migrar qualquer recurso, classe ou componente dos projetos antigos para este projeto (`PSI`):

## 1. Padrões do Backend (Quarkus 3.15)
- Inspecione a classe correspondente no backend antigo (`Quarkus/sga`) utilizando as ferramentas MCP de leitura e busca (`read_file`, `search_files`).
- No `PSI`, siga a arquitetura limpa em camadas exemplificada pela entidade `Estado`:
  - **Model:** `com.example.model.<Entidade>` (estendendo `PanacheEntityBase` com ID e campos com anotações JPA).
  - **DTO:** `com.example.dto.<Entidade>DTO` (records ou classes com validações Bean Validation como `@NotBlank`, `@NotNull`, etc.).
  - **Mapper:** `com.example.mapper.<Entidade>Mapper` (conversão entre Entity e DTO).
  - **Repository:** `com.example.repository.<Entidade>Repository` (implementando `PanacheRepositoryBase<Entidade, Long>`).
  - **Service:** `com.example.service.<Entidade>Service` (interface) e `impl.<Entidade>ServiceImpl` (`@ApplicationScoped`).
  - **Resource:** `com.example.resource.<Entidade>Resource` (REST endpoints padronizados com `@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE` e códigos HTTP adequados).

## 2. Padrões do Frontend (Angular 19)
- Inspecione os componentes, rotas e serviços no frontend antigo (`ProjetoAngular/hello-world-angular`) via MCP.
- No `PSI/frontend`, utilize:
  - **Componentes Standalone:** Padrão Angular moderno (`standalone: true`).
  - **Angular Material:** Componentes de UI modernos (`@angular/material`).
  - **Tipagem Forte:** Models e interfaces em `src/app/models/`.
  - **Services com RxJS / Signals:** Comunicação HTTP centralizada em `src/app/services/`.
  - **Rotas:** Mapeamento limpo em `src/app/app.routes.ts`.
