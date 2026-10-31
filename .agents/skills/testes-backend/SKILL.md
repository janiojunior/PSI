---
name: testes-backend
description: >-
  Orienta a criação, revisão e execução de testes de backend Java/Quarkus. Use
  esta skill sempre que criar, revisar ou executar testes unitários, de
  integração, API REST Assured, persistência, autenticação/autorização,
  isolamento entre tenants, conflitos de agenda, histórico/imutabilidade,
  concorrência e análise de regressões.
---

# Testes de Backend Java/Quarkus (`testes-backend`)

Esta skill define a estratégia, os padrões de implementação, a matriz de cobertura obrigatória e o protocolo de execução e relato de testes no módulo **`backend/`** (Java 25 + Quarkus 3.39.2 + JUnit 5 + REST Assured + PostgreSQL).

---

## 1. Quando Utilizar Esta Skill

Ative e siga este fluxo sempre que a tarefa envolver:
- Criação ou revisão de testes unitários e de integração (`backend/src/test/java/com/example/...`);
- Testes de contrato e endpoints HTTP com **REST Assured** (`@QuarkusTest`);
- Testes de persistência, migrations Flyway, constraints e consultas em **PostgreSQL real via Quarkus Dev Services**;
- Testes de segurança, autenticação (`401`) e autorização por perfil/papel (`403`);
- Verificação de isolamento multi-tenant e bloqueio de referências cruzadas (*cross-tenant*);
- Testes de regras de negócio críticas: validações, transações/atomicidade, conflitos de agenda, histórico, imutabilidade e controle de concorrência;
- Investigação de bugs e criação de testes de regressão.

---

## 2. Etapa Obrigatória de Inspeção Prévia

Antes de escrever ou alterar qualquer teste, inspecione obrigatoriamente:
1. **Regras de Negócio:** [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) — contratos de entrada/saída, códigos HTTP esperados, mensagens de validação em português e regras específicas de cada entidade.
2. **Contexto e Arquitetura:** [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) e [`backend/AGENTS.md`](../../../backend/AGENTS.md).
3. **Modelo de Dados:** [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) e as migrations em `backend/src/main/resources/db/migration/`.
4. **Testes e Configurações Existentes:** Verifique a estrutura atual em `backend/src/test/`, as dependências no [`backend/pom.xml`](../../../backend/pom.xml) e a configuração de perfil `%test` em [`backend/src/main/resources/application.properties`](../../../backend/src/main/resources/application.properties).

---

## 3. Regra de Ouro: Execução Real e Transparência de Resultados

> **IMPORTANTE — PROIBIÇÃO DE APROVAÇÃO SEM EXECUÇÃO:**
> **NUNCA declare que os testes passaram ou que a suíte foi aprovada sem tê-los efetivamente executado via terminal (`./mvnw test`).**

Siga rigorosamente este protocolo ao trabalhar com testes:
1. **Sempre execute os testes** após criá-los ou modificá-los (`./mvnw test` ou `./mvnw test -Dtest=<ClasseDeTeste>` a partir de `backend/`).
2. **Relate os resultados reais executados:** Informe no resumo final:
   - O comando exato executado;
   - As classes/métodos de teste executados;
   - A contagem real reportada pelo Maven Surefire (`Tests run: X, Failures: Y, Errors: Z, Skipped: W`).
3. **Transparência em Caso de Bloqueio de Ambiente:**
   - Caso a execução falhe por limitação de infraestrutura local (ex: Docker/Testcontainers indisponível para o Dev Services ou PostgreSQL local desligado), **reporte claramente que os testes foram criados/atualizados mas NÃO puderam ser executados**, exibindo o erro real retornado pelo ambiente. Jamais simule ou presuma aprovação.

---

## 4. Princípios de Design de Testes

1. **Priorize o Comportamento Observável:**
   - Teste o sistema através de suas fronteiras públicas (endpoints REST via REST Assured ou contratos públicos do `Service`/`Repository`) verificando respostas HTTP, headers, payloads JSON e o estado persistido no banco de dados.
   - Evite testes frágeis acoplados a detalhes internos de implementação ou excesso de *mocks* que mascaram erros de SQL, mapeamento JPA ou serialização Jackson.
2. **PostgreSQL Real via Quarkus Dev Services:**
   - Testes de integração (`@QuarkusTest`) devem rodar contra uma instância real do **PostgreSQL** (preferencialmente provisionada automaticamente pelo **Quarkus Dev Services** no perfil `%test`, com o Flyway aplicando todas as migrations de `db/migration/`).
   - Atenção ao `application.properties`: se `quarkus.datasource.jdbc.url` estiver fixo sem escopo de perfil, configure o perfil de teste (`%test`) para permitir que o Dev Services assuma ou aponte para um banco de teste isolado, nunca poluindo dados de produção/desenvolvimento sem controle.
3. **Independência e Determinismo:**
   - Cada teste deve preparar seu próprio cenário (dados de entrada) e não depender da ordem de execução de outros métodos `@Test`.
   - Use nomes de métodos descritivos em português ou padrão claro (ex: `deveCriarEstadoComSucessoEConsultarPorId()`, `deveRetornar400QuandoSiglaForInvalida()`).

---

## 5. Matriz Obrigatória de Cobertura de Testes

Ao criar ou revisar a suíte de testes de um recurso ou funcionalidade, garanta cobertura explícita para as seguintes dimensões aplicáveis:

### 5.1. Sucesso (Caminho Feliz e Transformações)
- Criação (`POST` -> `201 Created`), presença do header `Location` e retorno do DTO com `id` gerado.
- Consulta individual (`GET /{id}` -> `200 OK`) e listagem (`GET` -> `200 OK`) refletindo exatamente os dados persistidos.
- Atualização (`PUT /{id}` -> `200 OK`) e verificação de que a alteração persistiu no banco.
- Exclusão (`DELETE /{id}` -> `204 No Content`) seguida de `GET /{id}` retornando `404 Not Found`.
- Sanitização e normalização exigidas pelas regras de negócio (ex: remoção de espaços com `.trim()` e conversão de siglas/códigos para `UPPERCASE`).

### 5.2. Erros e Casos Limítrofes
- Busca, atualização ou exclusão de `id` inexistente retornando `404 Not Found` com mensagem em português (`"<Entidade> com ID <id> não encontrado."`).
- Violação de restrições de negócio ou integridade (ex: exclusão de registro referenciado por outra tabela ou duplicidade de chave única).

### 5.3. Validações de Entrada (Jakarta Bean Validation)
- Campos obrigatórios nulos (`null`), vazios (`""`) ou preenchidos apenas com espaços (`"   "`) -> `400 Bad Request`.
- Violação de limites mínimos e máximos de caracteres (`@Size`), valores numéricos fora da faixa ou formatos inválidos -> `400 Bad Request`.
- Verificação de que o payload de erro contém as mensagens de validação em português definidas no DTO.

### 5.4. Autenticação e Autorização (Quando o Recurso for Protegido)
- **Não Autenticado (`401 Unauthorized`):** Requisições sem token/credencial ou com token expirado/inválido.
- **Não Autorizado (`403 Forbidden`):** Usuário autenticado com papel/perfil sem permissão para a operação (ex: perfil de leitura tentando executar `POST`, `PUT` ou `DELETE`).
- **Autorizado (`2xx`):** Usuário com o papel correto executando a ação permitida.

### 5.5. Isolamento entre Tenants e Prevenção de Referências Cruzadas
- **Isolamento de Leitura e Escrita:** Dados criados no **Tenant A** não devem aparecer na listagem do **Tenant B**, e tentativas do **Tenant B** de acessar (`GET /{id}`), editar (`PUT /{id}`) ou excluir (`DELETE /{id}`) um registro do **Tenant A** devem ser negadas (`404 Not Found` ou `403 Forbidden`).
- **Bloqueio de Referências Cruzadas (*Cross-Tenant*):** Tentativa de criar ou atualizar uma entidade no **Tenant A** referenciando o `id` de uma entidade associada que pertence ao **Tenant B** deve falhar e não persistir vínculo cruzado no banco.

### 5.6. Transações e Atomicidade
- Em operações que alteram múltiplas tabelas ou executam validações após mutações intermediárias, simule uma falha no meio do fluxo e comprove que o `@Transactional` efetuou *rollback* completo (nenhum registro órfão ou alteração parcial permaneceu no PostgreSQL).

### 5.7. Conflitos de Agenda (Quando Envolver Horários / Reservas / Sessões)
- Teste todas as variações de sobreposição de intervalo para o mesmo profissional/recurso/paciente:
  - Mesmo horário exato de início e fim;
  - Novo agendamento iniciando durante um agendamento existente;
  - Novo agendamento terminando durante um agendamento existente;
  - Novo agendamento englobando totalmente um existente (e vice-versa);
  - Horários imediatamente adjacentes (ex: `09:00-10:00` e `10:00-11:00` — verificando se a regra permite encaixe exato sem sobreposição);
  - Agendamentos cancelados/inativos não devem bloquear novos agendamentos no mesmo horário (se previsto na regra).

### 5.8. Histórico e Imutabilidade
- Garanta que atributos imutáveis após a criação (ex: `id`, `tenantId`, `criadoEm`, autor original ou registros de prontuário/auditoria finalizados) não sejam alterados mesmo que enviados em um `PUT`.
- Verifique se alterações críticas geram os registros de histórico/auditoria esperados e se registros históricos são preservados (imutáveis).

### 5.9. Controle de Concorrência
- **Concorrência Otimista (`@Version`):** Simule duas leituras simultâneas do mesmo registro seguidas de duas atualizações; a primeira deve ter sucesso e a segunda deve ser rejeitada (`409 Conflict` / `OptimisticLockException`), evitando *lost updates*.
- **Condições de Corrida (*Race Conditions*):** Utilize threads concorrentes (`ExecutorService` + `CountDownLatch`) para disparar requisições simultâneas conflitantes (ex: dois cadastros com a mesma sigla/documento único ou dois agendamentos para o mesmo horário) e comprove que apenas uma vence e a outra é rejeitada de forma consistente.

---

## 6. Exemplo de Estrutura de Teste com REST Assured (`@QuarkusTest`)

```java
package com.example.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EstadoResourceTest {

    @Test
    void deveCriarConsultarAtualizarEExcluirEstadoComSucesso() {
        // 1. POST (Criação com sanitização de espaços e minúsculas)
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                      {
                        "sigla": " pr ",
                        "nome": " Paraná "
                      }
                      """)
                .when()
                .post("/estados")
                .then()
                .statusCode(201)
                .header("Location", containsString("/estados/"))
                .body("id", notNullValue())
                .body("sigla", equalTo("PR"))
                .body("nome", equalTo("Paraná"))
                .extract()
                .path("id");

        // 2. GET por ID
        given()
                .when()
                .get("/estados/{id}", id)
                .then()
                .statusCode(200)
                .body("sigla", equalTo("PR"))
                .body("nome", equalTo("Paraná"));

        // 3. PUT (Atualização)
        given()
                .contentType(ContentType.JSON)
                .body("""
                      {
                        "sigla": "PR",
                        "nome": "Estado do Paraná"
                      }
                      """)
                .when()
                .put("/estados/{id}", id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Estado do Paraná"));

        // 4. DELETE (Exclusão)
        given()
                .when()
                .delete("/estados/{id}", id)
                .then()
                .statusCode(204);

        // 5. Confirma 404 após exclusão
        given()
                .when()
                .get("/estados/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void deveRetornar400QuandoDadosForemInvalidos() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                      {
                        "sigla": "ABC",
                        "nome": " "
                      }
                      """)
                .when()
                .post("/estados")
                .then()
                .statusCode(400);
    }
}
```
