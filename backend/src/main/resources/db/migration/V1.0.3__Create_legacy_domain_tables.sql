-- Migração Flyway: V1.0.3__Create_legacy_domain_tables.sql
-- Evolução do schema para suportar Região em Estado e as entidades Pessoa, Telefone, Paciente, Psicologo e Usuario

ALTER TABLE estado ADD COLUMN IF NOT EXISTS regiao BIGINT;
ALTER TABLE estado ALTER COLUMN regiao DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_estado_sigla ON estado (UPPER(sigla));

UPDATE estado SET regiao = 4 WHERE UPPER(sigla) IN ('SP', 'RJ', 'MG') AND regiao IS NULL;
UPDATE estado SET regiao = 2 WHERE UPPER(sigla) = 'TO' AND regiao IS NULL;

CREATE TABLE IF NOT EXISTS pessoa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(120) NOT NULL,
    data_nascimento DATE NOT NULL,
    genero VARCHAR(30),
    endereco VARCHAR(255),
    id_municipio BIGINT,
    CONSTRAINT uk_pessoa_cpf UNIQUE (cpf),
    CONSTRAINT uk_pessoa_email UNIQUE (email),
    CONSTRAINT fk_pessoa_municipio FOREIGN KEY (id_municipio) REFERENCES municipio (id) ON DELETE RESTRICT
);

ALTER TABLE pessoa ADD COLUMN IF NOT EXISTS id_municipio BIGINT;
ALTER TABLE pessoa ADD COLUMN IF NOT EXISTS data_nascimento DATE;

CREATE INDEX IF NOT EXISTS idx_pessoa_id_municipio ON pessoa (id_municipio);

CREATE TABLE IF NOT EXISTS telefone (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL,
    codigo_area VARCHAR(3) NOT NULL,
    numero VARCHAR(15) NOT NULL,
    whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_telefone_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_telefone_pessoa_id ON telefone (pessoa_id);

CREATE TABLE IF NOT EXISTS paciente (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL,
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_paciente_pessoa UNIQUE (pessoa_id),
    CONSTRAINT fk_paciente_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_paciente_pessoa_id ON paciente (pessoa_id);

CREATE TABLE IF NOT EXISTS psicologo (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL,
    crp VARCHAR(20) NOT NULL,
    especialidade VARCHAR(120) NOT NULL,
    bio TEXT,
    valor_consulta NUMERIC(10, 2) NOT NULL,
    duracao_consulta INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_psicologo_pessoa UNIQUE (pessoa_id),
    CONSTRAINT uk_psicologo_crp UNIQUE (crp),
    CONSTRAINT fk_psicologo_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE RESTRICT
);

ALTER TABLE psicologo ADD COLUMN IF NOT EXISTS valor_consulta NUMERIC(10, 2);
ALTER TABLE psicologo ADD COLUMN IF NOT EXISTS duracao_consulta INTEGER;

CREATE INDEX IF NOT EXISTS idx_psicologo_pessoa_id ON psicologo (pessoa_id);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(120) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    pessoa_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    perfis BIGINT[] NOT NULL,
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT uk_usuario_pessoa UNIQUE (pessoa_id),
    CONSTRAINT fk_usuario_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_usuario_pessoa_id ON usuario (pessoa_id);
