# Regras de Negócio e Especificação de Domínio - PSI

Este documento centraliza o **o que o sistema deve fazer**: as entidades do domínio, seus atributos, restrições de integridade, validações e fluxos funcionais que devem permanecer sincronizados entre o **Backend (Quarkus)** e o **Frontend (Angular)**.

> **Instrução para Agentes:** Consulte este documento antes de criar ou alterar qualquer entidade, DTO, migration ou formulário. Toda nova regra de negócio implementada deve ser documentada aqui.

---

## 1. Regras Gerais do Domínio

1. **Idioma e Mensagens:** Todas as mensagens de validação (Jakarta Validation no Backend e erros de formulário/`MatSnackBar` no Frontend) devem ser apresentadas em **Português (pt-BR)**.
2. **Sanitização de Dados Textuais:**
   - Campos de texto devem ter espaços em branco nas extremidades removidos (`.trim()`) antes da persistência.
   - Códigos e siglas (como `sigla` de `Estado`) devem ser sempre convertidos e armazenados em letras **maiúsculas** (`UPPERCASE`), tanto na digitação em tela quanto no mapeamento do Backend (`Mapper`).
3. **Semântica de Operações CRUD:**
   - Buscas por `id` inexistente em leitura (`GET /{id}`), atualização (`PUT /{id}`) ou exclusão (`DELETE /{id}`) devem retornar erro `404 Not Found` com a mensagem `"<Entidade> com ID <id> não encontrado."`.
   - Exclusões na interface gráfica exigem confirmação explícita do usuário antes do disparo da requisição `DELETE`.

---

## 2. Catálogo de Entidades e Regras Específicas

### 2.1. Estado (`estado`)

Representa as Unidades Federativas (UF) brasileiras utilizadas nos cadastros do sistema.

#### Estrutura de Dados e Contratos

| Campo | Tipo (Java / TS) | Banco de Dados (`estado`) | Obrigatório | Tamanho / Formato | Transformação |
| :--- | :--- | :--- | :---: | :--- | :--- |
| `id` | `Long` / `number` | `BIGSERIAL PRIMARY KEY` | Gerado (Auto) | Inteiro 64-bits | Somente leitura após criação |
| `sigla` | `String` / `string` | `VARCHAR(2) NOT NULL` | Sim | Exatamente 2 caracteres (`min=2`, `max=2`) | `.trim().toUpperCase()` |
| `nome` | `String` / `string` | `VARCHAR(100) NOT NULL` | Sim | Entre 2 e 100 caracteres (`min=2`, `max=100`) | `.trim()` |

> **Nota de Compatibilidade com o Legado (`V1.0.1`):** No banco de dados compartilhado com o legado (`topicos2db`), a coluna legada `regiao` teve a restrição `NOT NULL` removida (`ALTER TABLE estado ALTER COLUMN regiao DROP NOT NULL`) para permitir que o modelo enxuto do PSI opere sem obrigatoriedade desse campo.

#### Regras de Validação (`EstadoDTO` <-> `EstadoFormComponent`)

- **RN-EST-01 (Sigla Obrigatória):** O campo `sigla` não pode ser nulo, vazio ou conter apenas espaços (`"A sigla é obrigatória"`).
- **RN-EST-02 (Tamanho da Sigla):** O campo `sigla` deve possuir exatamente 2 caracteres (`"A sigla deve conter exatamente 2 caracteres"`).
- **RN-EST-03 (Formatação da Sigla):** Durante a digitação no formulário e na conversão do `EstadoMapper`, a `sigla` é convertida automaticamente para letras maiúsculas.
- **RN-EST-04 (Nome Obrigatório):** O campo `nome` não pode ser nulo, vazio ou conter apenas espaços (`"O nome é obrigatório"`).
- **RN-EST-05 (Tamanho do Nome):** O campo `nome` deve conter no mínimo 2 e no máximo 100 caracteres (`"O nome deve conter entre 2 e 100 caracteres"`).

#### Endpoints REST (`/estados`)

- `GET /estados`: Retorna `200 OK` com a lista de todos os estados (`List<EstadoDTO>`).
- `GET /estados/{id}`: Retorna `200 OK` com o `EstadoDTO` correspondente ou `404 Not Found`.
- `POST /estados`: Valida o corpo (`@Valid EstadoDTO`), persiste o registro e retorna `201 Created` com header `Location: /estados/{id}` e o `EstadoDTO` criado.
- `PUT /estados/{id}`: Valida o corpo (`@Valid EstadoDTO`), atualiza a entidade existente e retorna `200 OK` com o `EstadoDTO` atualizado ou `404 Not Found`.
- `DELETE /estados/{id}`: Remove o registro pelo `id` e retorna `204 No Content` ou `404 Not Found`.

#### Fluxos de Tela (`/estados`)

- **Listagem (`EstadoListComponent` - rota `/estados`):**
  - Exibe tabela paginada com colunas `ID`, `Sigla`, `Nome` e `Ações` (Editar / Excluir).
  - Permite filtragem textual rápida em tela e navegação para criação (`/estados/novo`) ou edição (`/estados/editar/:id`).
- **Formulário (`EstadoFormComponent` - rotas `/estados/novo` e `/estados/editar/:id`):**
  - Em modo criação (`/novo`), cadastra o estado, exibe `MatSnackBar` (`"Estado cadastrado com sucesso!"`) e redireciona para `/estados`.
  - Em modo edição (`/editar/:id`), carrega os dados atuais via `GET /estados/{id}`, permite salvar alterações (`PUT`) ou excluir o registro (`DELETE`) após confirmação, redirecionando para `/estados`.

---

### 2.2. Município (`municipio`)

Representa os municípios brasileiros vinculados obrigatoriamente a uma Unidade Federativa (`Estado`).

#### Estrutura de Dados e Contratos

| Campo | Tipo (Java / TS) | Banco de Dados (`municipio`) | Obrigatório | Tamanho / Formato | Transformação |
| :--- | :--- | :--- | :---: | :--- | :--- |
| `id` | `Long` / `number` | `BIGSERIAL PRIMARY KEY` | Gerado (Auto) | Inteiro 64-bits | Somente leitura após criação |
| `nome` | `String` / `string` | `VARCHAR(60) NOT NULL` | Sim | Entre 2 e 60 caracteres (`min=2`, `max=60`) | `.trim()` |
| `estado` | `EstadoDTO` / `Estado` | `id_estado BIGINT NOT NULL REFERENCES estado(id)` | Sim | Objeto `Estado` com `id` válido | Associação verificada no banco |

#### Regras de Validação (`MunicipioDTO` <-> `MunicipioFormComponent`)

- **RN-MUN-01 (Nome Obrigatório):** O campo `nome` não pode ser nulo, vazio ou conter apenas espaços (`"O nome é obrigatório"`).
- **RN-MUN-02 (Tamanho do Nome):** O campo `nome` deve conter no mínimo 2 e no máximo 60 caracteres (`"O nome deve conter entre 2 e 60 caracteres"`).
- **RN-MUN-03 (Estado Obrigatório):** O vínculo com `estado` é obrigatório (`"O estado é obrigatório"`).
- **RN-MUN-04 (Existência do Estado):** O `estado.id` informado na criação ou atualização deve corresponder a um `Estado` existente no banco de dados; caso contrário, retorna `404 Not Found` (`"Estado com ID <id> não encontrado."`).

#### Endpoints REST (`/municipios`)

- `GET /municipios`: Retorna `200 OK` com a lista de todos os municípios ordenados por nome e com o respectivo `estado` carregado (`List<MunicipioDTO>`).
- `GET /municipios/{id}`: Retorna `200 OK` com o `MunicipioDTO` correspondente ou `404 Not Found`.
- `POST /municipios`: Valida o corpo (`@Valid MunicipioDTO`), verifica a existência do `Estado`, persiste o registro e retorna `201 Created` com header `Location: /municipios/{id}` e o `MunicipioDTO` criado.
- `PUT /municipios/{id}`: Valida o corpo (`@Valid MunicipioDTO`), atualiza o nome e o estado do município existente e retorna `200 OK` com o `MunicipioDTO` atualizado ou `404 Not Found`.
- `DELETE /municipios/{id}`: Remove o registro pelo `id` e retorna `204 No Content` ou `404 Not Found`.

#### Fluxos de Tela (`/municipios`)

- **Listagem (`MunicipioListComponent` - rota `/municipios`):**
  - Exibe tabela paginada com colunas `UF` (badge), `Nome do Município`, `Unidade Federativa`, `Código` e `Ações` (Editar / Excluir).
  - Permite filtragem textual instantânea por nome do município, nome do estado ou sigla da UF, além de navegação para criação (`/municipios/novo`) ou edição (`/municipios/editar/:id`).
- **Formulário (`MunicipioFormComponent` - rotas `/municipios/novo` e `/municipios/editar/:id`):**
  - Carrega a lista de estados disponíveis via `EstadoService` para seleção em campo `<select>`.
  - Em modo criação (`/novo`), cadastra o município, exibe `MatSnackBar` (`"Município cadastrado com sucesso!"`) e redireciona para `/municipios`.
  - Em modo edição (`/editar/:id`), carrega os dados atuais via `GET /municipios/{id}`, permite salvar alterações (`PUT`) ou excluir o registro (`DELETE`) após confirmação, redirecionando para `/municipios`.
