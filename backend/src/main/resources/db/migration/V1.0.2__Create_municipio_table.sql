-- Migração Flyway: V1.0.2__Create_municipio_table.sql
CREATE TABLE IF NOT EXISTS municipio (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(60) NOT NULL,
    id_estado BIGINT NOT NULL,
    CONSTRAINT fk_municipio_estado FOREIGN KEY (id_estado) REFERENCES estado (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_municipio_id_estado ON municipio (id_estado);

-- Carga inicial de dados (Seeds idempotentes)
INSERT INTO municipio (nome, id_estado)
SELECT 'Palmas', id FROM estado WHERE UPPER(sigla) = 'TO'
AND NOT EXISTS (SELECT 1 FROM municipio WHERE UPPER(nome) = 'PALMAS');

INSERT INTO municipio (nome, id_estado)
SELECT 'Araguaína', id FROM estado WHERE UPPER(sigla) = 'TO'
AND NOT EXISTS (SELECT 1 FROM municipio WHERE UPPER(nome) IN ('ARAGUAÍNA', 'ARAGUAINA'));

INSERT INTO municipio (nome, id_estado)
SELECT 'São Paulo', id FROM estado WHERE UPPER(sigla) = 'SP'
AND NOT EXISTS (SELECT 1 FROM municipio WHERE UPPER(nome) IN ('SÃO PAULO', 'SAO PAULO'));

INSERT INTO municipio (nome, id_estado)
SELECT 'Rio de Janeiro', id FROM estado WHERE UPPER(sigla) = 'RJ'
AND NOT EXISTS (SELECT 1 FROM municipio WHERE UPPER(nome) = 'RIO DE JANEIRO');

INSERT INTO municipio (nome, id_estado)
SELECT 'Belo Horizonte', id FROM estado WHERE UPPER(sigla) = 'MG'
AND NOT EXISTS (SELECT 1 FROM municipio WHERE UPPER(nome) = 'BELO HORIZONTE');
