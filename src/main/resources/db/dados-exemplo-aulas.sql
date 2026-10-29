-- Dados de exemplo para testar o agendamento de aulas.
-- As tabelas são criadas pelo Hibernate (ddl-auto=update) ao subir a aplicação; rode este script depois disso.
-- Requer que tb_planos já tenha planos cadastrados.
-- ATENÇÃO: apaga e recria todas as categorias, endereços, ateliês, instrutores, aulas, horários e agendamentos.

TRUNCATE tb_agendamentos, tb_horarios_aula, tb_aulas_planos, tb_aulas, tb_instrutores, tb_parceiros,
         tb_enderecos, tb_categorias RESTART IDENTITY CASCADE;

-- Ícones da família MaterialCommunityIcons do @expo/vector-icons
INSERT INTO tb_categorias (nome, icone) VALUES
    ('Cerâmica & Argila', 'flower-outline'),
    ('Pintura & Aquarela', 'brush'),
    ('Marcenaria', 'hammer'),
    ('Gastronomia', 'chef-hat'),
    ('Costura & Bordado', 'needle');

INSERT INTO tb_enderecos (logradouro, numero, complemento, bairro, cidade, estado, cep) VALUES
    ('Rua Girassol', '123', NULL, 'Vila Madalena', 'São Paulo', 'SP', '05433000'),
    ('Alameda dos Ipês', '450', 'Sala 2', 'Pinheiros', 'São Paulo', 'SP', '05422010'),
    ('Rua Cardoso de Almeida', '980', NULL, 'Perdizes', 'São Paulo', 'SP', '05013001'),
    ('Rua Augusta', '1500', 'Loja 3', 'Consolação', 'São Paulo', 'SP', '01304001');

INSERT INTO tb_parceiros (nome, endereco_id) VALUES
    ('Ateliê Luz', 1),
    ('Espaço Criar', 2),
    ('Estúdio Oficina Viva', 3),
    ('Cozinha Afeto', 4);

INSERT INTO tb_instrutores (nome, especialidade, foto_url, parceiro_id) VALUES
    ('Sofia Martins', 'Mestre Ceramista', NULL, 1),
    ('Clara Mendes', 'Artista Botânica', NULL, 2),
    ('Rafael Costa', 'Marceneiro', NULL, 3),
    ('Mariana Santos', 'Chef Confeiteira', NULL, 4);

INSERT INTO tb_aulas (titulo, descricao, categoria_id, subcategoria, nivel, duracao_minutos, imagem_url, destaque, parceiro_id, instrutor_id) VALUES
    ('Cerâmica Iniciante: Torno & Modelagem',
     'Nesta aula você aprenderá técnicas básicas de modelagem manual, acabamento no torno, queima e introdução à esmaltação. Todo o material essencial está incluído e você leva sua peça final para casa após o ciclo no forno.',
     1, 'Torno & Modelagem', 'Básico', 120, NULL, true, 1, 1),
    ('Aquarela Botânica & Estudo de Cores',
     'Aprenda a observar e pintar folhas e flores em aquarela, explorando mistura de cores, transparências e camadas.',
     2, 'Aquarela', 'Básico', 90, NULL, true, 2, 2),
    ('Marcenaria Básica & Pequenos Objetos',
     'Introdução ao uso seguro de ferramentas manuais e elétricas, com a construção de um pequeno objeto em madeira.',
     3, 'Pequenos Objetos', 'Intermediário', 180, NULL, false, 3, 3),
    ('Pães de Fermentação Natural',
     'Do levain ao forno: prepare seu próprio pão de fermentação natural e entenda cada etapa do processo.',
     4, 'Panificação', 'Básico', 150, NULL, false, 4, 4);

-- Cerâmica e Pães: todos os planos | Aquarela: a partir do segundo plano mais barato | Marcenaria: só o mais caro
INSERT INTO tb_aulas_planos (aula_id, plano_id)
SELECT a.id, p.id FROM tb_aulas a CROSS JOIN tb_planos p
WHERE a.titulo IN ('Cerâmica Iniciante: Torno & Modelagem', 'Pães de Fermentação Natural');

INSERT INTO tb_aulas_planos (aula_id, plano_id)
SELECT a.id, p.id FROM tb_aulas a CROSS JOIN tb_planos p
WHERE a.titulo = 'Aquarela Botânica & Estudo de Cores'
  AND p.id <> (SELECT id FROM tb_planos ORDER BY preco_mensal ASC LIMIT 1);

INSERT INTO tb_aulas_planos (aula_id, plano_id)
SELECT a.id, (SELECT id FROM tb_planos ORDER BY preco_mensal DESC LIMIT 1)
FROM tb_aulas a WHERE a.titulo = 'Marcenaria Básica & Pequenos Objetos';

-- Horários relativos à data atual no fuso de Brasília (o container do Postgres usa UTC)
INSERT INTO tb_horarios_aula (aula_id, data_hora, capacidade)
SELECT a.id, date_trunc('day', NOW() AT TIME ZONE 'America/Sao_Paulo') + h.deslocamento, h.capacidade
FROM tb_aulas a
JOIN (VALUES
    -- Cerâmica: um horário lotado (capacidade 1) e um a menos de 24h
    ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '2 days 10 hours', 1),
    ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '2 days 18 hours', 8),
    ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '4 days 19 hours', 8),
    ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '6 days 14 hours', 8),
    ('Aquarela Botânica & Estudo de Cores', INTERVAL '3 days 19 hours', 10),
    ('Aquarela Botânica & Estudo de Cores', INTERVAL '5 days 10 hours', 10),
    -- Começa durante a Cerâmica das 19h do mesmo dia: útil para testar o conflito de horário
    ('Aquarela Botânica & Estudo de Cores', INTERVAL '4 days 20 hours', 10),
    ('Marcenaria Básica & Pequenos Objetos', INTERVAL '4 days 14 hours', 6),
    ('Marcenaria Básica & Pequenos Objetos', INTERVAL '7 days 9 hours', 6),
    ('Pães de Fermentação Natural', INTERVAL '3 days 9 hours', 12),
    ('Pães de Fermentação Natural', INTERVAL '8 days 15 hours', 12)
) AS h(titulo, deslocamento, capacidade) ON h.titulo = a.titulo;

-- Horário que começa em menos de 24h (permite agendar; não permite reagendar; cancelar não devolve o crédito)
INSERT INTO tb_horarios_aula (aula_id, data_hora, capacidade)
SELECT id, date_trunc('hour', NOW() AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '5 hours', 10
FROM tb_aulas WHERE titulo = 'Cerâmica Iniciante: Torno & Modelagem';

-- Horários que já passaram, usados no histórico de aulas concluídas
INSERT INTO tb_horarios_aula (aula_id, data_hora, capacidade)
SELECT id, date_trunc('day', NOW() AT TIME ZONE 'America/Sao_Paulo') - INTERVAL '3 days' + INTERVAL '14 hours', 8
FROM tb_aulas WHERE titulo = 'Cerâmica Iniciante: Torno & Modelagem'
UNION ALL
SELECT id, date_trunc('day', NOW() AT TIME ZONE 'America/Sao_Paulo') - INTERVAL '6 days' + INTERVAL '19 hours', 8
FROM tb_aulas WHERE titulo = 'Aquarela Botânica & Estudo de Cores';

-- Aulas concluídas para todo aluno com assinatura ativa, para a aba "Concluídas" ter conteúdo
INSERT INTO tb_agendamentos (usuario_id, horario_id, data_agendamento, creditos_pagos, status)
SELECT ass.usuario_id, h.id, h.data_hora - INTERVAL '5 days', 1, 'AGENDADO'
FROM tb_assinaturas ass
JOIN tb_horarios_aula h ON h.data_hora < NOW() AT TIME ZONE 'America/Sao_Paulo'
JOIN tb_aulas_planos ap ON ap.aula_id = h.aula_id AND ap.plano_id = ass.plano_id
WHERE ass.status = 'ATIVA';
