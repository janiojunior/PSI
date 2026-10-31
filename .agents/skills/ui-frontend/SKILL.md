---
name: ui-frontend
description: >-
  Orienta a criação e revisão da camada visual (UI/UX) do frontend Angular no
  PSI. Use esta skill sempre que criar ou revisar telas, templates HTML,
  estilos SCSS, componentes Angular Material/CDK, tabelas, diálogos,
  formulários, navegação, responsividade (desktop e mobile), acessibilidade e
  estados visuais.
---

# Camada Visual, Design System e Acessibilidade do Frontend (`ui-frontend`)

Esta skill define as diretrizes visuais, padrões de interface, responsividade e acessibilidade para criar, estilizar e revisar telas no módulo **`frontend/`** do projeto **PSI**, garantindo fidelidade ao Design System **Stitch (*Serene Clinical Architecture*)** e ao uso do **Angular Material / CDK**.

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou refatoração visual de telas, templates (`.component.html`) e folhas de estilo (`.component.scss` / `styles.scss`);
- Uso e estilização de componentes **Angular Material / CDK** (`MatTable`, `MatPaginator`, `MatDialog`, `MatSnackBar`, `MatProgressBar`, `MatFormField`, etc.);
- Construção de tabelas de listagem, barras de filtro, cards de formulário, modais/diálogos e cabeçalhos de página;
- Ajustes de navegação (sidebar, top header, breadcrumbs);
- Adaptação responsiva para **Desktop e Mobile**;
- Revisão de **acessibilidade (a11y)**: navegação por teclado, foco visível, vínculo de `<label>`, atributos `aria-*` e contraste de cores;
- Implementação e refinamento dos estados visuais (carregamento, estado vazio, erro, feedback de sucesso e prevenção de cliques duplicados).

---

## 2. Etapa Obrigatória de Inspeção Visual Prévia

**Nunca** crie estilos isolados, cores hexadecimais arbitrárias ou estruturas de layout sem antes inspecionar os padrões visuais já consolidados no projeto:

1. **Tokens Globais e Design System (`Stitch`):**
   - Leia [`frontend/src/styles.scss`](../../../frontend/src/styles.scss) para consultar as variáveis CSS (`--psi-primary`, `--psi-surface-*`, `--psi-on-surface`, `--psi-outline*`, `--psi-error*`), a tipografia (`'DM Sans'`) e a família de ícones (`'Material Symbols Outlined'`).
2. **Shell e Navegação Principal:**
   - Inspecione [`layout.component.html`](../../../frontend/src/app/components/layout/layout.component.html) e [`layout.component.scss`](../../../frontend/src/app/components/layout/layout.component.scss) para compreender a estrutura `.stitch-sidebar`, `.stitch-header`, `.breadcrumb-trail` e `.page-content`.
3. **Telas Canônicas de Referência (`Estado`):**
   - **Listagem e Tabela:** Consulte [`estado-list.component.html`](../../../frontend/src/app/components/estado/estado-list/estado-list.component.html) e [`estado-list.component.scss`](../../../frontend/src/app/components/estado/estado-list/estado-list.component.scss) (`.page-header`, `.filter-card`, `.table-card`, `.stitch-table`, `.empty-state`).
   - **Formulário de Cadastro/Edição:** Consulte [`estado-form.component.html`](../../../frontend/src/app/components/estado/estado-form/estado-form.component.html) e [`estado-form.component.scss`](../../../frontend/src/app/components/estado/estado-form/estado-form.component.scss) (`.form-header-context`, `.form-card`, `.form-section`, `.form-grid`, `.form-actions-bar`).

---

## 3. Limites Estritos de Atuação (Regra de Bloqueio)

> **IMPORTANTE — FRONTEIRA EXCLUSIVAMENTE VISUAL:**
> Ao atuar na camada visual (`ui-frontend`), você **NÃO DEVE**:
> 1. **Modificar regras de negócio** (validações de domínio, cálculos, transformações obrigatórias definidas em [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md));
> 2. **Alterar contratos de API** (nomes de propriedades nos `models/`, URLs de endpoints, métodos HTTP ou payloads enviados ao Backend);
> 3. **Alterar fluxos de autenticação ou autorização** (lógica de guards, verificação de papéis/permissões ou isolamento de sessão/tenant).

Se uma melhoria visual parecer exigir mudança em regra de negócio, contrato HTTP ou permissão, mantenha o contrato intacto e consulte o usuário antes de qualquer alteração estrutural.

---

## 4. Diretrizes de Consistência Visual (Design System *Stitch*)

1. **Uso Obrigatório de Tokens CSS (`--psi-*`):**
   - Utilize sempre as variáveis declaradas em `:root` (`styles.scss`):
     - Ações primárias e destaques: `var(--psi-primary)`, `var(--psi-primary-dark)`, `var(--psi-primary-fixed)`;
     - Superfícies e cards: `var(--psi-surface-alt)` (fundo da página), `var(--psi-surface-card)` (fundo de cards), `var(--psi-surface-container-low)` (inputs e cabeçalhos de tabela);
     - Textos e bordas: `var(--psi-on-surface)`, `var(--psi-on-surface-variant)`, `var(--psi-outline)`, `var(--psi-outline-variant)`;
     - Estados destrutivos/erro: `var(--psi-error)`, `var(--psi-error-container)`.
2. **Tipografia e Ícones:**
   - Mantenha a fonte `'DM Sans'` herdada globalmente (`font-family: inherit` em inputs e botões nativos).
   - Utilize ícones com `<span class="material-symbols-outlined">nome_do_icone</span>` alinhados verticalmente com o texto (`display: inline-flex; align-items: center; gap: 8px;`).
3. **Geometria de Cards, Tabelas e Botões:**
   - Cards (`.filter-card`, `.table-card`, `.form-card`): `border-radius: 14px`, borda suave `1px solid rgba(222, 228, 225, 0.7)` e sombra sutil.
   - Botões e campos de entrada: `border-radius: 10px` (ou `8px` em ações compactas de tabela) com transições suaves (`transition: all 0.2s`).

---

## 5. Responsividade (Desktop e Mobile)

Toda tela deve ser projetada e revisada para funcionar sem quebras de layout tanto em monitores amplos (**Desktop**) quanto em telas reduzidas (**Tablet / Mobile**):

1. **Cabeçalhos e Barras de Ação:**
   - Utilize `flex-wrap: wrap; gap: 12px;` em `.page-header`, `.filter-controls` e `.form-actions-bar` para que botões e filtros empilhem harmoniosamente em telas menores.
2. **Grids de Formulário:**
   - Em `.form-grid`, utilize CSS Grid para dispor campos lado a lado no Desktop (ex: `grid-template-columns: 180px 1fr` ou `repeat(2, 1fr)`) e colapse para coluna única em telas móveis (`@media (max-width: 640px) { grid-template-columns: 1fr; }`).
3. **Tabelas de Dados (`MatTable`):**
   - Envolva toda `<table mat-table>` em um contêiner `.table-responsive` com `overflow-x: auto; width: 100%;` para evitar corte horizontal de colunas ou estouro da viewport no mobile.
4. **Navegação Lateral (Sidebar):**
   - Respeite o breakpoint `@media (max-width: 900px)` do `LayoutComponent` (onde a sidebar recolhe para `72px` exibindo apenas ícones centralizados).

---

## 6. Acessibilidade (a11y), Navegação por Teclado e Contraste

1. **Labels e Associação Semântica:**
   - Todo campo de formulário (`<input>`, `<select>`, `<textarea>`) deve possuir um `<label for="id_do_campo" class="field-label">` explícito conectado ao atributo `id` do controle, sinalizando campos obrigatórios com `<span class="required-mark">*</span>`.
2. **Navegação por Teclado e Foco Visível:**
   - Todos os elementos interativos devem ser alcançáveis via tecla `Tab` em ordem lógica.
   - Mantenha estados `:focus` / `:focus-visible` nítidos (ex: `border-color: var(--psi-primary); box-shadow: 0 0 0 3px rgba(67, 98, 91, 0.15);`). Nunca remova o `outline` sem fornecer um indicador visual de foco equivalente.
   - Defina explicitamente `type="button"` em todos os botões que **não** submetem o formulário (ex: *Voltar*, *Limpar*, *Excluir*, *Editar*), reservando `type="submit"` apenas para a ação principal de salvar.
3. **Atributos ARIA e Botões de Ícone:**
   - Botões que exibem apenas ícones (como editar/excluir na tabela, limpar busca ou notificações) devem possuir `title="..."` e/ou `aria-label="..."` descritivos em português.
   - Configure `aria-label` no `<mat-paginator>` e `<nav aria-label="Navegação">` nos breadcrumbs.
4. **Contraste Visual:**
   - Garanta contraste legível entre texto e fundo (utilizando `--psi-on-surface` sobre `--psi-surface-card` e `#ffffff` sobre `--psi-primary` / `--psi-primary-dark`).

---

## 7. Estados Visuais, Mensagens e Prevenção de Ações Duplicadas

1. **Estado de Carregamento (`Loading`):**
   - Exiba `<mat-progress-bar mode="indeterminate" class="stitch-progress-bar"></mat-progress-bar>` no topo da listagem ou do card de formulário sempre que os dados estiverem sendo buscados ou processados.
2. **Estado Vazio (`Empty State`):**
   - Toda `MatTable` deve implementar a linha `*matNoDataRow` com o bloco `.empty-state`, exibindo ícone ilustrativo (`material-symbols-outlined`), título claro, subtítulo contextual (diferenciando lista vazia de filtro sem resultados) e botão para limpar o filtro quando houver busca ativa.
3. **Mensagens de Validação, Dicas e Erros:**
   - Abaixo de cada campo de formulário, alterne entre `.field-hint` (texto auxiliar orientativo quando válido/intocado) e `.error-msg` com ícone `error` em `var(--psi-error)` quando o controle estiver inválido e tocado (`invalid && touched`), aplicando a classe `.has-error` ao input.
   - Notificações globais via `MatSnackBar` devem ser claras, em português, posicionadas em `horizontalPosition: 'center'`, `verticalPosition: 'top'` e com `panelClass: ['psi-snackbar']` nos fluxos de sucesso.
4. **Prevenção de Ações Duplicadas e Confirmação de Exclusão:**
   - Botões de submissão (`.btn-save`) e ações de mutação devem permanecer desabilitados (`[disabled]="form.invalid || service.loading()"`) enquanto uma requisição estiver em andamento, com estilo visual `:disabled { opacity: 0.55; cursor: not-allowed; }`.
   - Toda ação destrutiva (ex: *Excluir*) deve exigir confirmação explícita (diálogo ou confirmação) antes de disparar a operação.
