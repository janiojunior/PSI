-- Ajuste para compatibilidade com o banco legado
ALTER TABLE estado ALTER COLUMN regiao DROP NOT NULL;
