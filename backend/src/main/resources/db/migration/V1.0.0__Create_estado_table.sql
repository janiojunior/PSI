-- Migração Flyway: V1.0.0__Create_estado_table.sql
CREATE TABLE estado (
    id BIGSERIAL PRIMARY KEY,
    sigla VARCHAR(2) NOT NULL,
    nome VARCHAR(100) NOT NULL
);

-- Carga inicial de dados (Seeds)
INSERT INTO estado (sigla, nome) VALUES ('SP', 'São Paulo');
INSERT INTO estado (sigla, nome) VALUES ('RJ', 'Rio de Janeiro');
INSERT INTO estado (sigla, nome) VALUES ('MG', 'Minas Gerais');
INSERT INTO estado (sigla, nome) VALUES ('TO', 'Tocantins');
