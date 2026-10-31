# Diretrizes do Backend (Quarkus)

Este documento define a arquitetura, os padrões de código e as convenções obrigatórias para o módulo **`backend/`** do projeto **PSI**.

---

## 1. Stack Tecnológica

- **Linguagem:** Java 25 (`maven.compiler.release: 25`)
- **Framework:** Quarkus `3.39.2`
- **Web / REST:** `quarkus-rest-jackson` (Jakarta REST / JAX-RS)
- **Persistência:** `quarkus-hibernate-orm-panache` (Padrão *Repository*) + `quarkus-jdbc-postgresql`
- **Migrations:** `quarkus-flyway` (`quarkus.hibernate-orm.database.generation=none`)
- **Validação:** `quarkus-hibernate-validator` (Jakarta Bean Validation)
- **Documentação da API:** `quarkus-smallrye-openapi` (Swagger UI sempre ativo em `/q/swagger-ui`)
- **Testes:** JUnit 5 (`quarkus-junit5`) + REST Assured (`rest-assured`)

---

## 2. Arquitetura em Camadas (Pacote Base: `com.example`)

Toda entidade ou recurso deve seguir rigorosamente a arquitetura em 6 camadas exemplificada pela entidade **`Estado`**:

```text
backend/src/main/java/com/example/
├── model/          # Entidades JPA (<Entidade>.java)
├── dto/            # Objetos de transferência em Java Record (<Entidade>DTO.java)
├── mapper/         # Conversores entre Entity e DTO (<Entidade>Mapper.java)
├── repository/     # Repositórios Panache (<Entidade>Repository.java)
├── service/        # Interfaces de serviço (<Entidade>Service.java)
│   └── impl/       # Implementações de negócio (<Entidade>ServiceImpl.java)
└── resource/       # Endpoints REST (<Entidade>Resource.java)
```

### Detalhamento por Camada

1. **Model (`com.example.model.<Entidade>`)**
   - POJO anotado com `@Entity` e `@Table(name = "<tabela>")`.
   - Chave primária `Long id` com `@Id` e `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
   - Mapeamento explícito de colunas com `@Column(name = "...", length = ..., nullable = ...)`.
   - Deve implementar `equals` e `hashCode` baseados apenas no `id`.
   - **Nota:** Não estenda `PanacheEntity` nem `PanacheEntityBase`; utilize sempre o padrão *Repository*.

2. **DTO (`com.example.dto.<Entidade>DTO`)**
   - Implementado como Java `record`.
   - Validações com anotações do Jakarta Validation (`@NotBlank`, `@NotNull`, `@Size`, etc.) contendo mensagens claras em português (`message = "..."`).
   - Pode incluir métodos utilitários imutáveis como `withId(Long generatedId)`.

3. **Mapper (`com.example.mapper.<Entidade>Mapper`)**
   - Classe anotada com `@ApplicationScoped`.
   - Métodos padronizados:
     - `toDTO(<Entidade> entity)`: Converte a entidade para DTO.
     - `toEntity(<Entidade>DTO dto)`: Cria uma nova entidade a partir do DTO (aplicando `.trim()`, `.toUpperCase()` quando aplicável).
     - `updateEntityFromDTO(<Entidade>DTO dto, <Entidade> entity)`: Atualiza os campos de uma entidade gerenciada.

4. **Repository (`com.example.repository.<Entidade>Repository`)**
   - Classe anotada com `@ApplicationScoped` implementando `PanacheRepository<<Entidade>>`.
   - Contém consultas customizadas retornando `Optional<<Entidade>>` ou `List<<Entidade>>`.

5. **Service (`com.example.service.<Entidade>Service` e `impl.<Entidade>ServiceImpl`)**
   - Interface definindo o contrato (`findAll`, `findById`, `create`, `update`, `delete`) operando apenas com DTOs.
   - Implementação anotada com `@ApplicationScoped`, injetando Repository e Mapper via `@Inject`.
   - Métodos de escrita (`create`, `update`, `delete`) devem ser anotados com `@Transactional`.
   - Caso um registro não seja encontrado, lance `jakarta.ws.rs.NotFoundException` com mensagem descritiva.

6. **Resource (`com.example.resource.<Entidade>Resource`)**
   - Anotado com `@Path("/<recursos-no-plural>")`, `@Produces(MediaType.APPLICATION_JSON)` e `@Consumes(MediaType.APPLICATION_JSON)`.
   - Injeta apenas a interface `<Entidade>Service`.
   - Usa `@Valid` nos parâmetros de corpo (`POST` e `PUT`) e retorna `jakarta.ws.rs.core.Response` com os códigos HTTP semânticos:
     - `GET`: `200 OK` (`Response.ok(...).build()`)
     - `POST`: `201 Created` (`Response.created(URI.create(...)).entity(created).build()`)
     - `PUT`: `200 OK` (`Response.ok(updated).build()`)
     - `DELETE`: `204 No Content` (`Response.noContent().build()`)

---

## 3. Banco de Dados e Migrations (Flyway)

- O Hibernate **não** gera tabelas automaticamente (`database.generation=none`).
- Toda nova tabela ou alteração de coluna exige a criação de um script SQL em `src/main/resources/db/migration/` seguindo o padrão de nomenclatura do Flyway:
  - `V1.X.Y__Descricao_da_mudanca.sql` (ex: `V1.0.0__Create_estado_table.sql`).

---

## 4. Anti-Patterns (O que NÃO fazer)

- **Nunca** retorne ou receba uma classe `model` (`@Entity`) diretamente no `Resource`.
- **Nunca** coloque regras de negócio ou acesso direto ao `Repository` dentro do `Resource`.
- **Nunca** altere arquivos de migration do Flyway que já foram executados; crie uma nova versão incremental (`V1.0.X__...`).

---

## 5. Comandos Úteis

Execute a partir da pasta `backend/`:
- **Modo de Desenvolvimento (Live Reload):** `./mvnw quarkus:dev`
- **Compilar o projeto:** `./mvnw clean compile`
- **Executar testes automatizados:** `./mvnw test`
- **Gerar pacote de produção:** `./mvnw package`
