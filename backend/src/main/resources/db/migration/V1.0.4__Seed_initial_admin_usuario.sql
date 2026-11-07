-- Migração Flyway: V1.0.4__Seed_initial_admin_usuario.sql
-- 1. Compatibilidade com colunas legadas do banco compartilhado (topicos2db)
-- 2. Criação idempotente da Pessoa e do Usuário Administrador inicial (login: admin@psi.com.br / senha: Admin@Psi2026!Segura)

ALTER TABLE estado ALTER COLUMN nome TYPE VARCHAR(100);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_attribute
        WHERE attrelid = 'public.pessoa'::regclass AND attname = 'datanascimento' AND NOT attisdropped
    ) THEN
        EXECUTE 'UPDATE public.pessoa SET data_nascimento = datanascimento WHERE data_nascimento IS NULL AND datanascimento IS NOT NULL';
        EXECUTE 'ALTER TABLE public.pessoa ALTER COLUMN datanascimento DROP NOT NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_attribute
        WHERE attrelid = 'public.psicologo'::regclass AND attname = 'valorconsulta' AND NOT attisdropped
    ) THEN
        EXECUTE 'UPDATE public.psicologo SET valor_consulta = valorconsulta WHERE valor_consulta IS NULL AND valorconsulta IS NOT NULL';
        EXECUTE 'ALTER TABLE public.psicologo ALTER COLUMN valorconsulta DROP NOT NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_attribute
        WHERE attrelid = 'public.psicologo'::regclass AND attname = 'duracaoconsulta' AND NOT attisdropped
    ) THEN
        EXECUTE 'UPDATE public.psicologo SET duracao_consulta = duracaoconsulta WHERE duracao_consulta IS NULL AND duracaoconsulta IS NOT NULL';
        EXECUTE 'ALTER TABLE public.psicologo ALTER COLUMN duracaoconsulta DROP NOT NULL';
    END IF;
END $$;

UPDATE pessoa SET data_nascimento = DATE '1990-01-01' WHERE data_nascimento IS NULL;
ALTER TABLE pessoa ALTER COLUMN data_nascimento SET NOT NULL;

UPDATE psicologo SET valor_consulta = 150.00 WHERE valor_consulta IS NULL;
UPDATE psicologo SET duracao_consulta = 50 WHERE duracao_consulta IS NULL;
ALTER TABLE psicologo ALTER COLUMN valor_consulta SET NOT NULL;
ALTER TABLE psicologo ALTER COLUMN duracao_consulta SET NOT NULL;

INSERT INTO pessoa (nome, cpf, email, data_nascimento, genero, endereco, id_municipio)
SELECT
    'Dra. Helena Martins',
    '00000000000',
    'admin@psi.com.br',
    DATE '1985-04-15',
    'Feminino',
    'Sede Administrativa PSI Clínica',
    (SELECT id FROM municipio WHERE UPPER(nome) = 'PALMAS' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM pessoa WHERE cpf = '00000000000' OR LOWER(email) = 'admin@psi.com.br'
);

INSERT INTO telefone (pessoa_id, codigo_area, numero, whatsapp)
SELECT
    p.id,
    '63',
    '999990000',
    TRUE
FROM pessoa p
WHERE LOWER(p.email) = 'admin@psi.com.br'
  AND NOT EXISTS (
      SELECT 1 FROM telefone t WHERE t.pessoa_id = p.id
  );

INSERT INTO usuario (login, senha, pessoa_id, ativo, perfis)
SELECT
    'admin@psi.com.br',
    'pbkdf2-sha256-email-v1$600000$MDEyMzQ1Njc4OWFiY2RlZg==$qpO6kYSUf8PIi026yE4TeAzDfbN8DTi8/z6JjEolZgg=',
    p.id,
    TRUE,
    ARRAY[1]::BIGINT[]
FROM pessoa p
WHERE LOWER(p.email) = 'admin@psi.com.br'
  AND NOT EXISTS (
      SELECT 1 FROM usuario u WHERE LOWER(u.login) = 'admin@psi.com.br' OR u.pessoa_id = p.id
  );
