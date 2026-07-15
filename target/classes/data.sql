-- Seed de exemplo para testar o fluxo completo na apresentação.
-- Roda automaticamente na inicialização. Usa ON CONFLICT para não duplicar.
INSERT INTO setores (nome, quantidade_colaboradores, criado_em)
VALUES
    ('Financeiro', 18, now()),
    ('Recursos Humanos', 12, now()),
    ('Logistica', 25, now()),
    ('Comercial', 30, now())
ON CONFLICT (nome) DO NOTHING;


