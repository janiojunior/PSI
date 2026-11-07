# Regras de Negócio e Especificação de Domínio - PSI

Este documento centraliza **o que o sistema deve fazer**: as entidades do domínio, seus atributos, restrições de integridade, validações e fluxos funcionais que devem permanecer sincronizados entre o **Backend (Quarkus)** e o **Frontend (Angular)**.

> **Instrução para Agentes:** Consulte este documento antes de criar ou alterar qualquer entidade, DTO, migration ou formulário. Toda nova regra de negócio implementada deve ser documentada aqui.

---

## 1. Regras Gerais do Domínio

1. **Idioma e Mensagens:** Todas as mensagens de validação (Jakarta Validation no Backend e erros de formulário/`MatSnackBar` no Frontend) devem ser apresentadas em **Português (pt-BR)**.
2. **Sanitização de Dados Textuais:**
   - Campos de texto devem ter espaços em branco nas extremidades removidos (`.trim()`) antes da persistência.
   - Códigos e siglas (como `sigla` de `Estado` e `crp` de `Psicologo`) devem ser sempre convertidos e armazenados em letras **maiúsculas** (`UPPERCASE`).
   - E-mails e logins devem ser normalizados em letras **minúsculas** (`LOWERCASE`), e CPFs devem conter apenas os 11 dígitos numéricos.
3. **Semântica de Operações CRUD e Paginação:**
   - Buscas por `id` inexistente em leitura (`GET /{id}`), atualização (`PUT /{id}`) ou exclusão (`DELETE /{id}`) devem retornar erro `404 Not Found` com a mensagem `"<Entidade> com ID <id> não encontrado."`.
   - Endpoints paginados (`/paginado` e `/nome/{nome}`) recebem `@QueryParam("page") @DefaultValue("0")` e `@QueryParam("pageSize") @DefaultValue("10")` (entre 1 e 100) e retornam `PageResponse<DTO>`.
   - Exclusões na interface gráfica exigem confirmação explícita do usuário antes do disparo da requisição `DELETE`.

---

## 2. Catálogo de Entidades e Regras Específicas

### 2.1. Estado (`estado`) e Região (`Regiao`)

Representa as Unidades Federativas (UF) brasileiras e sua classificação macrorregional (`CENTRO_OESTE`, `NORTE`, `NORDESTE`, `SUDESTE`, `SUL`).

#### Estrutura de Dados e Contratos

| Campo | Tipo (Java / TS) | Banco de Dados (`estado`) | Obrigatório | Tamanho / Formato | Transformação |
| :--- | :--- | :--- | :---: | :--- | :--- |
| `id` | `Long` / `number` | `BIGSERIAL PRIMARY KEY` | Gerado (Auto) | Inteiro 64-bits | Somente leitura após criação |
| `sigla` | `String` / `string` | `VARCHAR(2) NOT NULL UNIQUE` | Sim | Exatamente 2 caracteres (`min=2`, `max=2`) | `.trim().toUpperCase()` |
| `nome` | `String` / `string` | `VARCHAR(100) NOT NULL` | Sim | Entre 2 e 100 caracteres (`min=2`, `max=100`) | `.trim()` |
| `regiao` | `Regiao` / `Regiao` | `BIGINT` (`RegiaoConverter`) | Opcional | Enum `Regiao` (`1` a `5`) | Conversão via ID ou objeto |

#### Regras de Validação (`EstadoDTO` <-> `EstadoServiceImpl`)

- **RN-EST-01 (Sigla Obrigatória):** O campo `sigla` não pode ser nulo, vazio ou conter apenas espaços (`"A sigla é obrigatória"`).
- **RN-EST-02 (Tamanho da Sigla):** O campo `sigla` deve possuir exatamente 2 caracteres (`"A sigla deve conter exatamente 2 caracteres"`).
- **RN-EST-03 (Formatação e Unicidade da Sigla):** A `sigla` é convertida para maiúsculas e não pode se repetir entre estados (`"Já existe um estado cadastrado com essa sigla."`).
- **RN-EST-04 (Nome Obrigatório):** O campo `nome` não pode ser nulo, vazio ou conter apenas espaços (`"O nome é obrigatório"`).
- **RN-EST-05 (Tamanho do Nome):** O campo `nome` deve conter entre 2 e 100 caracteres (`"O nome deve conter entre 2 e 100 caracteres"`).

#### Endpoints REST (`/estados` e `/regioes`)

- `GET /estados`: Retorna `200 OK` com `List<EstadoDTO>`.
- `GET /estados/paginado?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<EstadoDTO>`.
- `GET /estados/nome/{nome}?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<EstadoDTO>` filtrado por nome.
- `GET /estados/sigla/{sigla}`: Retorna `200 OK` com o `EstadoDTO` ou `404 Not Found`.
- `GET /estados/{id}`: Retorna `200 OK` com o `EstadoDTO` correspondente ou `404 Not Found`.
- `POST /estados`: Valida o corpo (`@Valid EstadoDTO`), verifica unicidade da sigla, persiste e retorna `201 Created` com `Location: /estados/{id}`.
- `PUT /estados/{id}`: Valida o corpo (`@Valid EstadoDTO`), atualiza a entidade e retorna `200 OK` ou `404 Not Found`.
- `DELETE /estados/{id}`: Remove o registro pelo `id` e retorna `204 No Content` ou `404 Not Found`.
- `GET /regioes` e `GET /regioes/{id}`: Lista todas as regiões do enum `Regiao` ou busca por `id` (`404 Not Found` se inválido).

---

### 2.2. Município (`municipio`)

Representa os municípios brasileiros vinculados obrigatoriamente a uma Unidade Federativa (`Estado`).

#### Estrutura de Dados e Contratos

| Campo | Tipo (Java / TS) | Banco de Dados (`municipio`) | Obrigatório | Tamanho / Formato | Transformação |
| :--- | :--- | :--- | :---: | :--- | :--- |
| `id` | `Long` / `number` | `BIGSERIAL PRIMARY KEY` | Gerado (Auto) | Inteiro 64-bits | Somente leitura após criação |
| `nome` | `String` / `string` | `VARCHAR(60) NOT NULL` | Sim | Entre 2 e 60 caracteres (`min=2`, `max=60`) | `.trim()` |
| `estado` | `EstadoDTO` / `Estado` | `id_estado BIGINT NOT NULL REFERENCES estado(id)` | Sim | Objeto `Estado` com `id` válido | Associação verificada no banco |

#### Regras de Validação (`MunicipioDTO` <-> `MunicipioServiceImpl`)

- **RN-MUN-01 (Nome Obrigatório):** O campo `nome` não pode ser nulo, vazio ou conter apenas espaços (`"O nome é obrigatório"`).
- **RN-MUN-02 (Tamanho do Nome):** O campo `nome` deve conter entre 2 e 60 caracteres (`"O nome deve conter entre 2 e 60 caracteres"`).
- **RN-MUN-03 (Estado Obrigatório):** O vínculo com `estado` é obrigatório (`"O estado é obrigatório"` / `"O ID do estado é obrigatório."`).
- **RN-MUN-04 (Existência do Estado):** O `estado.id` informado deve corresponder a um `Estado` existente no banco (`404 Not Found` caso não exista).

#### Endpoints REST (`/municipios`)

- `GET /municipios`: Retorna `200 OK` com `List<MunicipioDTO>`.
- `GET /municipios/paginado?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<MunicipioDTO>`.
- `GET /municipios/nome/{nome}?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<MunicipioDTO>` filtrado por nome.
- `GET /municipios/{id}`, `POST /municipios`, `PUT /municipios/{id}`, `DELETE /municipios/{id}`.

---

### 2.3. Pessoa (`pessoa`) e Telefone (`telefone`)

Base cadastral compartilhada por `Paciente`, `Psicologo` e `Usuario`, permitindo que uma mesma pessoa física assuma múltiplos papéis no sistema sem duplicação de CPF/E-mail.

#### Estrutura de Dados (`pessoa` e `telefone`)

- `nome`: `VARCHAR(120) NOT NULL` (entre 2 e 120 caracteres).
- `cpf`: `VARCHAR(11) NOT NULL UNIQUE` (exatamente 11 dígitos).
- `email`: `VARCHAR(120) NOT NULL UNIQUE` (formato de e-mail válido, normalizado em minúsculas).
- `dataNascimento`: `DATE NOT NULL` (deve ser data no passado `@Past`).
- `genero`: `VARCHAR(30)` (opcional).
- `endereco`: `VARCHAR(255)` (opcional).
- `municipio`: `id_municipio BIGINT REFERENCES municipio(id)` (vínculo `@ManyToOne` opcional com `Municipio`).
- `telefones`: Composição `1..*` com `Telefone` (`codigoArea` de 2 a 3 caracteres, `numero` de 8 a 15 caracteres, `whatsapp` booleano obrigatório).

---

### 2.4. Paciente (`paciente`)

Representa o perfil clínico de um paciente vinculado a uma `Pessoa`.

#### Regras de Negócio e Validações (`PacienteDTO` <-> `PacienteServiceImpl`)

- **RN-PAC-01 (Unicidade de CPF e E-mail):** Não é permitido cadastrar dois pacientes com o mesmo CPF, nem utilizar CPF ou e-mail pertencentes a outra pessoa. Caso o CPF já exista na tabela `pessoa` (ex: usuário já cadastrado), a `Pessoa` existente é reaproveitada e vinculada ao novo `Paciente`.
- **RN-PAC-02 (Proteção de Login Vinculado):** Se a `Pessoa` possuir um `Usuario` associado, a alteração do `email` é bloqueada caso divirja do `login` (`"O e-mail está vinculado ao login. A alteração exige um fluxo de troca de credenciais."`).
- **RN-PAC-03 (Campos Específicos):** `observacoes` (`TEXT`, opcional) e `ativo` (`BOOLEAN`, obrigatório).

#### Endpoints REST (`/pacientes`)

- `GET /pacientes`: Retorna `200 OK` com `List<PacienteDTO>`.
- `GET /pacientes/paginado?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<PacienteDTO>`.
- `GET /pacientes/nome/{nome}?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<PacienteDTO>`.
- `GET /pacientes/cpf/{cpf}`: Retorna `200 OK` com `PacienteDTO` (ou dados básicos da `Pessoa` caso já exista) ou `404 Not Found`.
- `GET /pacientes/{id}`, `POST /pacientes`, `PUT /pacientes/{id}`, `DELETE /pacientes/{id}`.

---

### 2.5. Psicólogo (`psicologo`)

Representa o profissional psicólogo vinculado a uma `Pessoa`.

#### Regras de Negócio e Validações (`PsicologoDTO` <-> `PsicologoServiceImpl`)

- **RN-PSI-01 (CRP Obrigatório e Único):** O campo `crp` (`VARCHAR(20) NOT NULL UNIQUE`) é obrigatório, normalizado em maiúsculas e único entre psicólogos (`"Já existe um psicólogo cadastrado com esse CRP."`).
- **RN-PSI-02 (Especialidade e Parâmetros de Consulta):**
  - `especialidade`: obrigatória, até 120 caracteres.
  - `bio`: texto livre opcional (`TEXT`).
  - `valorConsulta`: `BigDecimal` obrigatório e maior que zero (`@DecimalMin(value = "0.0", inclusive = false)`).
  - `duracaoConsulta`: `Integer` obrigatório e positivo (`@Positive`, em minutos).
  - `ativo`: `Boolean` obrigatório.
- **RN-PSI-03 (Reaproveitamento de Pessoa e Proteção de E-mail):** Segue a mesma regra de `Paciente` para reaproveitamento de `Pessoa` por CPF e proteção de e-mail vinculado a `Usuario`.

#### Endpoints REST (`/psicologos`)

- `GET /psicologos`: Retorna `200 OK` com `List<PsicologoDTO>`.
- `GET /psicologos/paginado?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<PsicologoDTO>`.
- `GET /psicologos/nome/{nome}?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<PsicologoDTO>`.
- `GET /psicologos/cpf/{cpf}` e `GET /psicologos/crp/{crp}`: Busca por CPF ou CRP (`200 OK` ou `404 Not Found`).
- `GET /psicologos/{id}`, `POST /psicologos`, `PUT /psicologos/{id}`, `DELETE /psicologos/{id}`.

---

### 2.6. Usuário (`usuario`), Perfil (`Perfil`) e Segurança de Senha (`SenhaService`)

Representa as credenciais de acesso e perfis de autorização vinculados a uma `Pessoa`.

#### Regras de Negócio e Validações (`UsuarioRequestDTO`, `UsuarioUpdateDTO`, `UsuarioDTO`)

- **RN-USU-01 (Isolamento de Senha):** A senha nunca é exposta no DTO de leitura (`UsuarioDTO`). No `UsuarioRequestDTO` (criação), a `senha` deve ter entre 15 e 128 caracteres.
- **RN-USU-02 (Hash Criptográfico PBKDF2):** `SenhaService` gera hashes no formato `pbkdf2-sha256-email-v1$600000$<saltBase64>$<hashBase64>`, combinando salt aleatório de 16 bytes com o e-mail normalizado como contexto.
- **RN-USU-03 (Perfis de Acesso):** `perfis` é armazenado como `BIGINT[]` no PostgreSQL via `PerfilConverter`, aceitando um ou mais valores de `Perfil`: `1 = ADMINISTRADOR`, `2 = ATENDENTE`, `3 = PACIENTE`, `4 = PSICOLOGO`.

#### Endpoints REST (`/usuarios`)

- `GET /usuarios`: Retorna `200 OK` com `List<UsuarioDTO>`.
- `GET /usuarios/paginado?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<UsuarioDTO>`.
- `GET /usuarios/nome/{nome}?page=0&pageSize=10`: Retorna `200 OK` com `PageResponse<UsuarioDTO>`.
- `GET /usuarios/cpf/{cpf}` e `GET /usuarios/{id}`: Retorna `200 OK` com `UsuarioDTO` ou `404 Not Found`.
- `POST /usuarios`: Recebe `UsuarioRequestDTO`, cria/atualiza `Pessoa`, gera hash de senha e retorna `201 Created` com `Location: /usuarios/{id}` e `UsuarioDTO`.
- `PUT /usuarios/{id}`: Recebe `UsuarioUpdateDTO` (atualização de senha opcional, `ativo` e `perfis`) e retorna `200 OK` com `UsuarioDTO`.
- `DELETE /usuarios/{id}`: Remove o usuário e retorna `204 No Content`.

---

### 2.7. Autenticação e Autorização JWT (`AuthResource` / `TokenJwtService` / `AuthService`)

Implementa autenticação stateless via **SmallRye JWT** (`RS256` assinado com `privateKey.pem` e verificado com `publicKey.pem`, `issuer = "psi-jwt"`, expiração padrão de `28800` segundos / 8 horas).

#### Contratos de Autenticação (`/auth`)

- `POST /auth/login` (`@PermitAll`):
  - Recebe `@Valid AuthRequestDTO` (`login` e `senha`).
  - Verifica se o usuário existe, está `ativo = true` e se o hash PBKDF2-SHA256 confere via `SenhaService.verificar`.
  - Caso inválido ou inativo, retorna `401 Unauthorized` com `{"message": "E-mail ou senha inválidos, ou usuário inativo."}`.
  - Caso válido, retorna `200 OK` com o header `Authorization: Bearer <token>` e o corpo `AuthResponseDTO` (`token`, `tokenType: "Bearer"`, `expiresIn: 28800`, `usuario: UsuarioDTO`).
- `GET /auth/me` (`@Authenticated`):
  - Retorna `200 OK` com o `UsuarioDTO` correspondente ao `upn` (`login`) presente no token JWT ativo.

#### Matriz de Autorização (`@RolesAllowed`)

| Recurso | Leitura (`GET`) | Escrita (`POST`, `PUT`, `DELETE`) |
| :--- | :--- | :--- |
| `/auth/login` | N/A | Público (`@PermitAll`) |
| `/auth/me` | Qualquer perfil autenticado (`@Authenticated`) | N/A |
| `/usuarios` | `ADMINISTRADOR` | `ADMINISTRADOR` |
| `/pacientes` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO` |
| `/psicologos` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO` |
| `/estados` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO`, `PACIENTE` | `ADMINISTRADOR`, `ATENDENTE` |
| `/municipios` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO`, `PACIENTE` | `ADMINISTRADOR`, `ATENDENTE` |
| `/regioes` | `ADMINISTRADOR`, `ATENDENTE`, `PSICOLOGO`, `PACIENTE` | N/A |

#### Credenciais Iniciais (Migration `V1.0.4__Seed_initial_admin_usuario.sql`)

- **Login:** `admin@psi.com.br`
- **Senha:** `Admin@Psi2026!Segura`
- **Perfil:** `ADMINISTRADOR` (`1`)
