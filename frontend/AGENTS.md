# Diretrizes do Frontend (Angular)

Este documento define a arquitetura, os padrões de código e as convenções obrigatórias para o módulo **`frontend/`** do projeto **PSI**.

---

## 1. Stack Tecnológica

- **Framework:** Angular `19.0.0` (100% *Standalone Components*)
- **UI / Componentes:** Angular Material `19.0.0` (`@angular/material` e `@angular/cdk`)
- **Linguagem:** TypeScript `~5.6.2`
- **Reatividade:** Angular **Signals** (`signal`, `computed`, `effect`) combinados com **RxJS** (`~7.8.0`) para chamadas HTTP
- **Cliente HTTP:** `provideHttpClient(withFetch())` configurado em `app.config.ts`
- **Estilização:** SCSS (`styles.scss` global e `.component.scss` escopado por componente)

---

## 2. Estrutura de Diretórios (`src/app/`)

Siga a organização modular exemplificada pelo cadastro de **`Estado`**:

```text
frontend/src/app/
├── app.config.ts                     # Providers globais (Router, HttpClient, Animations)
├── app.routes.ts                     # Definição centralizada de rotas
├── models/                           # Interfaces TypeScript (<entidade>.model.ts)
├── services/                         # Serviços HTTP + Estado com Signals (<entidade>.service.ts)
└── components/
    ├── layout/                       # Shell principal da aplicação (Sidebar/Header + RouterOutlet)
    └── <entidade>/
        ├── <entidade>-list/          # Tela de listagem, filtro e exclusão
        └── <entidade>-form/          # Tela de criação e edição (Reactive Forms)
```

---

## 3. Padrões de Implementação por Camada

### 1. Models (`src/app/models/<entidade>.model.ts`)
- Utilize `export interface <Entidade>` com tipagem forte espelhando o DTO do Backend.
- O campo `id?: number` deve ser opcional para permitir o uso da mesma interface na criação e na edição.

### 2. Services (`src/app/services/<entidade>.service.ts`)
- Anotado com `@Injectable({ providedIn: 'root' })`.
- Utilize injeção moderna via função `inject(HttpClient)`.
- Defina a URL base apontando para a API Quarkus: `private readonly apiUrl = 'http://localhost:8080/<entidades>';`.
- **Gerenciamento de Estado com Signals:**
  - Mantenha signals privados para a lista, carregamento e erro (`_<entidades>`, `_loading`, `_error`).
  - Exponha versões somente-leitura com `.asReadonly()`.
  - Métodos de mutação (`create`, `update`, `delete`) devem retornar `Observable` e usar o operador `tap` para atualizar o Signal local de forma reativa.

### 3. Componentes de Listagem (`<entidade>-list.component.ts`)
- Componente standalone (`standalone: true`) importando os módulos necessários do Angular Material (`MatTableModule`, `MatPaginatorModule`, `MatFormFieldModule`, `MatInputModule`, `MatButtonModule`, `MatIconModule`, `MatProgressBarModule`, `MatSnackBarModule`).
- Utilize `effect()` no construtor para sincronizar o Signal do Service com o `MatTableDataSource<<Entidade>>`.
- Exiba feedbacks de sucesso/erro utilizando `MatSnackBar` posicionado no topo/centro (`horizontalPosition: 'center'`, `verticalPosition: 'top'`, `panelClass: ['psi-snackbar']`).

### 4. Componentes de Formulário (`<entidade>-form.component.ts`)
- Componente único para **Criação** (`/novo`) e **Edição** (`/editar/:id`), verificando a presença do parâmetro `:id` via `ActivatedRoute`.
- Utilize **Reactive Forms** (`FormBuilder`, `FormGroup`, `Validators`) com as mesmas restrições de tamanho e obrigatoriedade definidas no Backend.
- Se o formulário for inválido no submit, chame `this.form.markAllAsTouched()` antes de interromper o envio.

### 5. Rotas (`src/app/app.routes.ts`)
- Novas telas devem ser registradas como rotas filhas (`children`) de `LayoutComponent`, definindo a propriedade `title: 'PSI - ...'`:
  - `<entidades>` -> `<Entidade>ListComponent`
  - `<entidades>/novo` -> `<Entidade>FormComponent`
  - `<entidades>/editar/:id` -> `<Entidade>FormComponent`

---

## 4. Boas Práticas e Anti-Patterns

- **Prefira `inject()`:** Use `private readonly service = inject(...)` em vez de injeção via construtor.
- **Proibido usar `any`:** Sempre crie ou utilize interfaces tipadas em `src/app/models/`.
- **Não crie `NgModules`:** Todo novo componente deve ser `standalone: true`.

---

## 5. Comandos Úteis

Execute a partir da pasta `frontend/`:
- **Servidor de Desenvolvimento (`http://localhost:4200`):** `npm start` (ou `npx ng serve`)
- **Build de Produção:** `npm run build`
- **Build em Modo Watch (Desenvolvimento):** `npm run watch`
