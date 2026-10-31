---
name: angular-frontend
description: >-
  Orienta a implementação e revisão de frontend Angular standalone no PSI. Use
  esta skill sempre que criar ou revisar componentes, rotas, formulários
  reativos, serviços HTTP, modelos TypeScript, guards, estado de sessão,
  listagens, inclusão, edição e integração com APIs.
---

# Implementação e Revisão de Frontend Angular Standalone (`angular-frontend`)

Esta skill define a arquitetura, os padrões de código, as regras de fidelidade a contratos e os limites operacionais para desenvolver e revisar funcionalidades no módulo **`frontend/`** (Angular 19 Standalone + Angular Material/CDK + Signals + RxJS).

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou manutenção de componentes standalone (`src/app/components/`);
- Configuração de rotas (`src/app/app.routes.ts`) e proteção via *functional guards* (`CanActivateFn`);
- Implementação de telas de listagem (`<entidade>-list`), inclusão e edição (`<entidade>-form`);
- Construção e validação de formulários reativos (`ReactiveFormsModule`, `FormBuilder`, `Validators`);
- Criação de interfaces/modelos TypeScript (`src/app/models/<entidade>.model.ts`);
- Implementação de serviços HTTP e estado reativo com Signals (`src/app/services/<entidade>.service.ts`);
- Gerenciamento de estado de sessão, autenticação, contexto de tenant e controle de permissões na UI;
- Integração com endpoints REST do Backend Quarkus, testes de frontend e validação de build.

---

## 2. Etapa Obrigatória de Inspeção Prévia e Reutilização de Padrões

Antes de criar ou editar qualquer arquivo em `frontend/`, inspecione obrigatoriamente:

1. **Documentos de Domínio e Diretrizes:**
   - [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) — campos, obrigatoriedades, tamanhos (`min`/`max`), transformações (ex: `UPPERCASE`, `.trim()`), fluxos de tela e mensagens em português.
   - [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) e [`frontend/AGENTS.md`](../../../frontend/AGENTS.md).
2. **Contratos Reais da API no Backend (Somente Leitura):**
   - Verifique os `record` DTOs em `backend/src/main/java/com/example/dto/` e os endpoints em `backend/src/main/java/com/example/resource/` para confirmar os nomes exatos dos campos, tipos, rotas e códigos de retorno HTTP.
3. **Implementação de Referência Canônica (`Estado`):**
   - Reutilize a estrutura, estilo visual (SCSS), componentes do Angular Material e padrão de Signals já estabelecidos em:
     - [`estado.model.ts`](../../../frontend/src/app/models/estado.model.ts)
     - [`estado.service.ts`](../../../frontend/src/app/services/estado.service.ts)
     - [`estado-list.component.ts`](../../../frontend/src/app/components/estado/estado-list/estado-list.component.ts)
     - [`estado-form.component.ts`](../../../frontend/src/app/components/estado/estado-form/estado-form.component.ts)
     - [`layout.component.ts`](../../../frontend/src/app/components/layout/layout.component.ts)

---

## 3. Limites Estritos e Proibição de Contratos Inventados

> **IMPORTANTE — REGRAS DE BLOQUEIO E ESCOPO:**
> 1. **NÃO altere arquivos do Backend (`backend/`), migrations ou contratos de API** durante uma tarefa de Frontend sem autorização explícita do usuário.
> 2. **NÃO instale, remova ou atualize dependências (`frontend/package.json`)** sem autorização explícita do usuário. Utilize exclusivamente Angular 19, Angular Material/CDK e RxJS já instalados.
> 3. **NUNCA invente endpoints, rotas HTTP, parâmetros de filtro ou campos de JSON** que não existam no DTO/Resource do Backend ou em `docs/REGRAS_NEGOCIO.md`.

**Como agir se o contrato necessário não existir no Backend:**
- Não crie *workarounds* silenciosos nem campos fictícios no `model.ts`;
- Informe claramente ao usuário qual endpoint ou atributo está ausente no contrato da API e solicite autorização antes de qualquer alteração no Backend ou no contrato.

---

## 4. Padrões de Arquitetura e Implementação

### 4.1. Componentes 100% Standalone e Injeção via `inject()`
- Todo componente deve declarar `standalone: true` e importar explicitamente suas dependências (`CommonModule`, `ReactiveFormsModule`, `RouterLink`, módulos do `@angular/material/*`).
- **Não crie `NgModules`.**
- Utilize sempre a função `inject()` (`private readonly fb = inject(FormBuilder);`) em vez de injeção via construtor.
- **Proibido usar `any`:** Mantenha tipagem estrita em todas as variáveis, parâmetros de eventos, respostas HTTP e formulários.

### 4.2. Modelos TypeScript (`src/app/models/<entidade>.model.ts`)
- Espelhe exatamente os nomes e tipos do `<Entidade>DTO` do Backend.
- Declare `id?: number;` como opcional para permitir reutilizar a mesma interface na criação (`POST`) e na edição (`PUT`).

### 4.3. Serviços HTTP e Estado com Signals (`src/app/services/<entidade>.service.ts`)
- Anote com `@Injectable({ providedIn: 'root' })` e configure `private readonly apiUrl = 'http://localhost:8080/<recurso>';`.
- Mantenha signals privados para os dados, estado de carregamento e erro:
  - `private readonly _itens = signal<Entidade[]>([]);`
  - `private readonly _loading = signal<boolean>(false);`
  - `private readonly _error = signal<string | null>(null);`
- Exponha sinais somente-leitura (`readonly itens = this._itens.asReadonly();`, `loading`, `error`).
- Nas mutações (`create`, `update`, `delete`), retorne `Observable` e utilize `tap` para sincronizar imediatamente o signal local e atualizar `_loading` / `_error`.

### 4.4. Tratamento Obrigatório dos 4 Estados Visuais (Carregamento, Sucesso, Vazio e Erro)
Toda tela de listagem ou formulário que dependa de dados assíncronos deve tratar explicitamente:
1. **Carregamento (`loading`):**
   - Exiba indicador visual (`<mat-progress-bar mode="indeterminate">`) enquanto a requisição estiver em andamento;
   - Desabilite botões de submissão/exclusão durante operações pendentes para evitar cliques duplicados.
2. **Sucesso (`data`):**
   - Renderize os dados na `MatTable` (sincronizada via `effect()` com o Signal do serviço e paginada com `MatPaginator`) ou preencha o formulário via `patchValue`.
   - Em mutações bem-sucedidas (`create`, `update`, `delete`), exiba feedback em português via `MatSnackBar` (`horizontalPosition: 'center'`, `verticalPosition: 'top'`, `panelClass: ['psi-snackbar']`).
3. **Vazio (`empty state`):**
   - Quando a lista retornada estiver vazia ou quando o filtro não encontrar registros, exiba uma mensagem clara em português orientando o usuário (ex: *"Nenhum registro encontrado"* / botão para limpar filtro ou cadastrar novo), evitando tabelas em branco sem contexto.
4. **Erro (`error`):**
   - Em caso de falha HTTP (`400`, `403`, `404`, `409`, `500` ou falha de rede), interrompa o estado de carregamento (`loading = false`) e apresente mensagem clara em português no `MatSnackBar` ou banner de alerta na tela.

### 4.5. Formulários Reativos e Validações (`<entidade>-form`)
- Utilize um único componente `<Entidade>FormComponent` para criação (`/<recurso>/novo`) e edição (`/<recurso>/editar/:id`), detectando o modo pela presença de `this.route.snapshot.paramMap.get('id')`.
- Aplique nos controles do `FormBuilder` os mesmos validadores definidos no Backend (`Validators.required`, `Validators.minLength(...)`, `Validators.maxLength(...)`, `Validators.email`, `Validators.min(...)`).
- Exiba mensagens de erro específicas em português (no bloco `.error-msg` com `.has-error` no input conforme o padrão Stitch, ou `<mat-error>`) para cada regra violada.
- No método `salvar()`, se `this.form.invalid`, execute `this.form.markAllAsTouched()` e retorne imediatamente sem disparar requisição HTTP.
- Aplique transformações de entrada previstas nas regras de negócio (ex: converter siglas para maiúsculas em tempo de digitação e antes do envio).

### 4.6. Rotas, Guards, Estado de Sessão e Permissões
- Registre as rotas em `src/app/app.routes.ts` dentro de `children` do `LayoutComponent`, sempre definindo um `title` descritivo (`'PSI - ...'`) e atualizando a navegação lateral quando aplicável.
- Quando houver controle de sessão/autenticação e perfis de acesso:
  - Centralize o estado do usuário autenticado, token e tenant ativo em um serviço reativo baseado em Signals (`AuthService` / `SessionService`);
  - Utilize *functional route guards* (`CanActivateFn`) para proteger rotas autenticadas e rotas restritas por perfil (`ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO`, `PACIENTE`);
  - Oculte ou desabilite botões de ação (Criar, Editar, Excluir) na interface quando o perfil atual não possuir permissão, e trate respostas `401 Unauthorized` / `403 Forbidden` redirecionando ou exibindo aviso claro ao usuário.

---

## 5. Verificação de Build e Testes

Após implementar ou revisar qualquer código no Frontend:
1. **Verifique a compilação e tipagem TypeScript/Angular Templates:**
   - Execute `npm run build` na pasta `frontend/` para garantir zero erros de compilação, templates ou imports ausentes.
2. **Testes:**
   - Quando criar ou alterar testes unitários/componentes (`*.spec.ts`), execute a suíte de testes configurada e relate apenas os resultados realmente executados.
3. **Documentação:**
   - Se uma entrega de tela do projeto for concluída, atualize o status do Frontend em [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md).
