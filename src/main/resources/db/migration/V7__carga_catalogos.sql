-- Carga inicial dos catalogos. Sao dados de referencia do sistema, nao
-- dados de usuario, por isso vivem em migration e nao em script separado.

INSERT INTO produto (nome, created_at, updated_at) VALUES
    ('Soja', NOW(), NOW()), ('Milho', NOW(), NOW()), ('Feijao', NOW(), NOW()),
    ('Arroz', NOW(), NOW()), ('Trigo', NOW(), NOW()), ('Alho', NOW(), NOW()),
    ('Cebola', NOW(), NOW()), ('Fumo', NOW(), NOW()), ('Batata', NOW(), NOW()),
    ('Mandioca', NOW(), NOW());

INSERT INTO unidade_medida (nome, sigla, fator_kg, created_at, updated_at) VALUES
    ('Saca de 60 quilos',  'sc60',   60.000, NOW(), NOW()),
    ('Saca de 50 quilos',  'sc50',   50.000, NOW(), NOW()),
    ('Quilograma',         'kg',      1.000, NOW(), NOW()),
    ('Tonelada',           't',    1000.000, NOW(), NOW()),
    ('Caixa de 20 quilos', 'cx20',   20.000, NOW(), NOW()),
    ('Unidade',            'un',       NULL, NOW(), NOW()),
    ('Duzia',              'dz',       NULL, NOW(), NOW());

INSERT INTO categoria_gasto (nome, created_at, updated_at) VALUES
    ('Sementes', NOW(), NOW()), ('Fertilizantes', NOW(), NOW()),
    ('Defensivos', NOW(), NOW()), ('Mao de obra', NOW(), NOW()),
    ('Maquinario', NOW(), NOW()), ('Combustivel', NOW(), NOW()),
    ('Arrendamento', NOW(), NOW()), ('Energia eletrica', NOW(), NOW()),
    ('Transporte', NOW(), NOW()), ('Outros', NOW(), NOW());

INSERT INTO destino (nome, created_at, updated_at) VALUES
    ('Consumo proprio', NOW(), NOW()), ('Semente guardada', NOW(), NOW()),
    ('Alimentacao animal', NOW(), NOW()), ('Parceria ou meacao', NOW(), NOW()),
    ('Troca por insumos', NOW(), NOW()), ('Perda apos a colheita', NOW(), NOW()),
    ('Doacao', NOW(), NOW());
