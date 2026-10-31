---
name: testes-frontend
description: >-
  Orienta a criação, revisão e execução de testes do frontend Angular no PSI.
  Use esta skill sempre que criar, revisar ou executar testes de componentes
  standalone, serviços HTTP com Signals, formulários reativos, rotas, guards,
  interceptores, estado de sessão, fluxos de erro e regressões.
---

# Testes do Frontend Angular Standalone (`testes-frontend`)

Esta skill define a estratégia, os padrões de implementação, a matriz de cobertura obrigatória e o protocolo de execução e relato de testes no módulo **`frontend/`** (Angular 19 Standalone + Angular Material/CDK + Signals + RxJS).

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou revisão de testes de componentes standalone (`*.component.spec.ts`);
- Testes de serviços HTTP e gerenciamento de estado com Signals (`*.service.spec.ts`);
- Testes de formulários reativos (`ReactiveFormsModule`), validações e interações de usuário;
- Testes de roteamento (`RouterTestingHarness` / `provideRouter`), *functional guards* (`CanActivateFn`) e interceptores (`HttpInterceptorFn`);
- Testes de sessão, controle de permissões por perfil e expiração de sessão (`401` / `403`);
- Verificação dos estados visuais (carregamento, sucesso, estado vazio, erro e prevenção de cliques duplicados);
- Reprodução de bugs de interface e criação de testes de regressão.

---

## 2. Etapa Obrigatória de Inspeção Prévia

Antes de escrever, alterar ou executar qualquer teste no Frontend, inspecione obrigatoriamente:

1. **Configuração de Testes e Build do Projeto:**
   - Verifique [`frontend/package.json`](../../../frontend/package.json), [`frontend/angular.json`](../../../frontend/angular.json) e [`frontend/tsconfig.json`](../../../frontend/tsconfig.json) para identificar o *runner* configurado (ex: Karma/Jasmine, Web Test Runner ou Vitest) e os scripts disponíveis.
   - Liste os arquivos `*.spec.ts` existentes em `frontend/src/` para seguir as convenções já adotadas.
2. **Regras de Negócio e Contratos:**
   - Consulte [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) e [`frontend/AGENTS.md`](../../../frontend/AGENTS.md) para validar mensagens em português, limites de campos, rotas e comportamentos esperados.
3. **Código-Fonte Alvo:**
   - Leia o componente (`.ts` e `.html`), serviço (`.service.ts`) e modelo (`.model.ts`) antes de escrever as asserções, garantindo aderência aos seletores reais do template e aos sinais expostos.

---

## 3. Regra de Ouro: Execução Real e Proibição de Aprovação Sem Evidência

> **IMPORTANTE — EVIDÊNCIA OBRIGATÓRIA DE EXECUÇÃO:**
> **NUNCA declare cobertura, sucesso ou aprovação de testes sem tê-los efetivamente executado no terminal e obtido evidência real do resultado.**

Siga rigorosamente este protocolo:
1. **Execute os testes e o build:** Rode o comando de teste configurado em `frontend/` (e valide também a compilação com `npm run build`).
2. **Registre os resultados reais na resposta:**
   - Informe o comando exato executado;
   - Liste os arquivos `*.spec.ts` verificados;
   - Apresente o número real de testes executados, aprovados e falhos.
3. **Transparência em Caso de Ausência de Runner ou Bloqueio de Ambiente:**
   - Caso `frontend/angular.json` ou `frontend/package.json` ainda não possuam o *target* `test` / *runner* instalado, ou caso o navegador *headless* não esteja disponível no ambiente:
     - **NÃO instale dependências em `package.json` sem autorização explícita do usuário;**
     - **Reporte explicitamente** que os arquivos `.spec.ts` foram criados/revisados, mas que a execução depende da configuração/autorização do *test runner* (ou desbloqueio do ambiente), mostrando a saída real do terminal.

---

## 4. Princípios de Design de Testes no Angular 19

1. **Priorize o Comportamento Observável:**
   - Em **Componentes**: teste o que o usuário vê e faz — elementos renderizados no DOM, textos visíveis, classes de estado, preenchimento de `<input>`, cliques em `<button>`, abertura de `MatSnackBar` e navegação de rota.
   - Em **Serviços HTTP**: teste as chamadas HTTP reais produzidas pelo serviço usando `provideHttpClient()` + `provideHttpClientTesting()` e `HttpTestingController`, verificando o estado resultante nos Signals públicos (`estados()`, `loading()`, `error()`).
   - Evite testar métodos privados ou acoplar os testes a detalhes internos que não afetam o comportamento percebido pelo usuário ou pelo contrato da API.
2. **Configuração Standalone Limpa (`TestBed`):**
   - Importe diretamente o componente standalone em `TestBed.configureTestingModule({ imports: [ComponenteStandalone], providers: [...] })`.
   - Utilize `provideNoopAnimations()` (`@angular/platform-browser/animations/async` ou `@angular/platform-browser/animations`) para evitar atrasos ou falhas de animação do Angular Material nos testes.

---

## 5. Matriz Obrigatória de Cobertura de Testes

Ao criar ou revisar testes de uma funcionalidade do Frontend, cubra explicitamente os seguintes cenários aplicáveis:

### 5.1. Renderização e Estado Inicial
- Renderização correta do cabeçalho, título da página, subtítulo, breadcrumbs e estrutura inicial da tabela ou formulário.
- Distinção correta entre modo **Criação** (`/novo` -> título *"Cadastrar Novo..."*, campos vazios, botão *"Excluir"* oculto) e modo **Edição** (`/editar/:id` -> título *"Editar..."*, carregamento via `getById(id)`, preenchimento com `patchValue` e botão *"Excluir"* visível).

### 5.2. Interação do Usuário e Transformações em Tela
- Digitação em campos de filtro atualizando imediatamente os itens exibidos na `MatTable` e funcionamento do botão *"Limpar"*.
- Transformações de entrada durante a digitação (ex: conversão automática da `sigla` para letras maiúsculas em `onSiglaInput`).

### 5.3. Validações de Formulários Reativos
- Campo obrigatório vazio (`Validators.required`) marcando o controle como inválido.
- Violação de tamanho mínimo e máximo (`Validators.minLength`, `Validators.maxLength`) e formatos específicos.
- Exibição da mensagem de erro em português (`.error-msg`) e da classe `.has-error` quando o campo é tocado (`touched`) ou quando o usuário tenta submeter o formulário inválido (`markAllAsTouched()`).
- Garantia de que nenhuma requisição HTTP (`POST`/`PUT`) é disparada enquanto `form.invalid` for verdadeiro.

### 5.4. Chamadas HTTP e Sincronização de Signals (`HttpTestingController`)
- `loadAll()` (`GET /<recurso>`): verifica que `loading()` passa para `true` durante a chamada e volta para `false` após o `flush`, populando o signal da lista.
- `create()` (`POST /<recurso>`): verifica URL, método `POST`, corpo JSON enviado e inclusão reativa do novo item no signal local via `tap`.
- `update()` (`PUT /<recurso>/:id`): verifica URL com `:id`, método `PUT`, corpo enviado e substituição do item correspondente no signal local.
- `delete()` (`DELETE /<recurso>/:id`): verifica método `DELETE` e remoção do item do signal local.
- Chamada obrigatória a `httpMock.verify()` no `afterEach` para garantir que não existem requisições HTTP pendentes ou inesperadas.

### 5.5. Estados Visuais: Carregamento, Sucesso, Vazio e Erro
- **Carregamento (`loading`):** Presença do `<mat-progress-bar>` no DOM enquanto `loading()` é `true`.
- **Sucesso:** Renderização das linhas na tabela, disparo do `MatSnackBar` com a mensagem em português esperada e redirecionamento de rota (`router.navigate`).
- **Estado Vazio (`empty state`):** Exibição do bloco `.empty-state` (`*matNoDataRow`) tanto quando a lista inicial está vazia quanto quando um filtro textual não retorna resultados (incluindo o botão *"Limpar filtro de busca"*).
- **Fluxos de Erro:** Simulação de erro HTTP (`req.flush('Erro', { status: 400 | 404 | 500, statusText: '...' })`) comprovando que:
  - `loading()` retorna para `false`;
  - `error()` registra a mensagem de falha;
  - O usuário recebe feedback claro via `MatSnackBar`;
  - Na falha ao carregar um registro para edição (`GET /:id` -> `404`), o usuário é notificado e redirecionado com segurança para a listagem.

### 5.6. Navegação, Rotas, Guards, Autorização e Sessão Expirada
- **Navegação:** Clique em *"+ Novo..."*, botão *"Editar"* da linha da tabela e botão *"Voltar para Lista"* acionando as rotas corretas.
- **Guards e Autorização (`CanActivateFn`):**
  - Usuário autenticado e com perfil autorizado consegue ativar a rota (`true`);
  - Usuário não autenticado ou sem o perfil exigido é bloqueado e redirecionado (`UrlTree` para login ou página de acesso negado);
  - Elementos de ação restritos na tela respeitam as permissões do usuário logado.
- **Sessão Expirada (`401 Unauthorized` / Interceptor):**
  - Quando uma requisição HTTP recebe `401 Unauthorized`, o estado de sessão é limpo e o usuário é redirecionado para a tela de autenticação com feedback apropriado.

### 5.7. Prevenção de Envio Duplicado e Confirmação Destrutiva
- Verificação de que o botão de salvar (`button[type="submit"]`) permanece com `disabled = true` enquanto `loading()` estiver ativo ou `form.invalid`, impedindo submissões duplicadas.
- Verificação de que a exclusão só dispara `service.delete(id)` se a confirmação do usuário retornar `true` (e cancela sem chamada HTTP se retornar `false`).

---

## 6. Exemplo de Referência: Teste de Serviço com `HttpTestingController`

```typescript
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EstadoService } from './estado.service';
import { Estado } from '../models/estado.model';

describe('EstadoService', () => {
  let service: EstadoService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/estados';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        EstadoService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(EstadoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve carregar a lista de estados e atualizar os signals loading e estados', () => {
    const mockEstados: Estado[] = [
      { id: 1, sigla: 'TO', nome: 'Tocantins' },
      { id: 2, sigla: 'SP', nome: 'São Paulo' }
    ];

    service.loadAll();
    expect(service.loading()).toBeTrue();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockEstados);

    expect(service.loading()).toBeFalse();
    expect(service.error()).toBeNull();
    expect(service.estados()).toEqual(mockEstados);
  });

  it('deve registrar erro e desativar loading quando a chamada HTTP falhar', () => {
    service.loadAll();
    expect(service.loading()).toBeTrue();

    const req = httpMock.expectOne(apiUrl);
    req.flush('Falha interna', { status: 500, statusText: 'Server Error' });

    expect(service.loading()).toBeFalse();
    expect(service.error()).toBe('Erro ao carregar lista de estados do servidor');
  });
});
```
