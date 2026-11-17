-- =====================================================================
-- Hotel Iara - DADOS DE EXEMPLO para a demonstração
-- Rodar DEPOIS do sql/hotel_banco_v3.sql, uma única vez, na base vazia.
--
-- Origem: dados do hotel_banco_v2.sql (equipe), adaptados ao esquema v3 e ao Java:
--  * quarto.numero numérico; status só DISPONIVEL/MANUTENCAO (OCUPADO é calculado
--    pelas reservas no Java: Quarto.getStatus())
--  * CPF e telefone das pessoas só com dígitos (é assim que Pessoa guarda)
--  * hotel.numero como texto (Endereco.numero é String)
--  * reservas CONFIRMADAS que terminaram até 17/11 viraram CONCLUIDA
--
-- Login de teste: qualquer e-mail abaixo com a senha 123456
--   cliente:     ana.silva@email.com
--   funcionária: renata.faria@agencia.com
-- (SHA2(...,256) do MySQL gera o mesmo hash que SenhaUtil.gerarHash no Java)
-- =====================================================================

USE hotel_iara_demo;
SET NAMES utf8mb4; -- garante acentos certos (Suíte, José...) em qualquer cliente MySQL

-- 1. CATEGORIAS (o tipo decide a subclasse: PADRAO, EXECUTIVO, LUXO, SUITE)
INSERT INTO categoria (id_categoria, nome, tipo, capacidade, valor_diaria) VALUES
(1,  'Standard Solteiro',   'PADRAO',    1, 150.00),
(2,  'Standard Casal',      'PADRAO',    2, 220.00),
(3,  'Standard Duplo',      'PADRAO',    2, 240.00),
(4,  'Superior Solteiro',   'PADRAO',    1, 280.00),
(5,  'Superior Casal',      'PADRAO',    2, 330.00),
(6,  'Superior Triplo',     'PADRAO',    3, 390.00),
(7,  'Executivo Casal',     'EXECUTIVO', 2, 450.00),
(8,  'Executivo Twin',      'EXECUTIVO', 2, 470.00),
(9,  'Luxo Varanda',        'LUXO',      2, 550.00),
(10, 'Luxo Vista Mar',      'LUXO',      2, 620.00),
(11, 'Suíte Júnior',        'SUITE',     2, 750.00),
(12, 'Suíte Família',       'SUITE',     4, 820.00),
(13, 'Suíte Master',        'SUITE',     3, 980.00),
(14, 'Suíte Presidencial',  'SUITE',     4, 1500.00),
(15, 'Cobertura Duplex',    'SUITE',     6, 2200.00);

-- 2. HOTEIS (coordenadas aproximadas, só para o mapa)
INSERT INTO hotel (id_hotel, nome, classificacao, telefone, qtd_andares, logradouro, numero, complemento, cep, cidade, estado, latitude, longitude) VALUES
(1, 'Hotel Batel Plaza',         4, '(41) 3000-1001', 2, 'Av. Batel',            '1200', 'Bloco A - Principal', '80420-090', 'Curitiba', 'PR', -25.440000, -49.286000),
(2, 'Hotel Bispo Executive',     4, '(41) 3000-1002', 3, 'Rua Bispo Dom José',   '2100', 'Bloco B - Anexo',     '80440-080', 'Curitiba', 'PR', -25.438000, -49.292000),
(3, 'Hotel Carlos Business',     4, '(41) 3000-1003', 4, 'Al. Dr. Carlos',        '550', 'Bloco C - Executivo', '80420-180', 'Curitiba', 'PR', -25.431000, -49.277000),
(4, 'Hotel Buenos Aires Premium',5, '(41) 3000-1004', 6, 'Rua Buenos Aires',      '890', 'Bloco D - Premium',   '80250-070', 'Curitiba', 'PR', -25.436000, -49.285000);

-- 3. PESSOAS: clientes 1-15, funcionários 16-17
INSERT INTO pessoa (id_pessoa, tipo, nome, cpf, email, senha_hash, telefone, dt_nasc) VALUES
(1,  'CLIENTE', 'Ana Silva',      '11122233301', 'ana.silva@email.com',      SHA2('123456',256), '41991011001', '1990-05-12'),
(2,  'CLIENTE', 'Bruno Souza',    '11122233302', 'bruno.souza@email.com',    SHA2('123456',256), '41991011002', '1985-08-23'),
(3,  'CLIENTE', 'Carla Mendes',   '11122233303', 'carla.mendes@email.com',   SHA2('123456',256), '41991011003', '1992-11-04'),
(4,  'CLIENTE', 'Diego Alves',    '11122233304', 'diego.alves@email.com',    SHA2('123456',256), '41991011004', '1988-02-19'),
(5,  'CLIENTE', 'Elena Costa',    '11122233305', 'elena.costa@email.com',    SHA2('123456',256), '41991011005', '1995-07-30'),
(6,  'CLIENTE', 'Fabio Lima',     '11122233306', 'fabio.lima@email.com',     SHA2('123456',256), '41991011006', '1979-12-15'),
(7,  'CLIENTE', 'Gisele Rocha',   '11122233307', 'gisele.rocha@email.com',   SHA2('123456',256), '41991011007', '1998-03-22'),
(8,  'CLIENTE', 'Hugo Martins',   '11122233308', 'hugo.martins@email.com',   SHA2('123456',256), '41991011008', '1983-09-10'),
(9,  'CLIENTE', 'Igor Pereira',   '11122233309', 'igor.pereira@email.com',   SHA2('123456',256), '41991011009', '1991-06-05'),
(10, 'CLIENTE', 'Julia Nunes',    '11122233310', 'julia.nunes@email.com',    SHA2('123456',256), '41991011010', '1994-10-18'),
(11, 'CLIENTE', 'Lucas Ribeiro',  '11122233311', 'lucas.ribeiro@email.com',  SHA2('123456',256), '41991011011', '1987-01-27'),
(12, 'CLIENTE', 'Mariana Dias',   '11122233312', 'mariana.dias@email.com',   SHA2('123456',256), '41991011012', '1996-04-14'),
(13, 'CLIENTE', 'Nicolas Gomes',  '11122233313', 'nicolas.gomes@email.com',  SHA2('123456',256), '41991011013', '1982-11-29'),
(14, 'CLIENTE', 'Olivia Castro',  '11122233314', 'olivia.castro@email.com',  SHA2('123456',256), '41991011014', '1993-08-08'),
(15, 'CLIENTE', 'Pedro Cardoso',  '11122233315', 'pedro.cardoso@email.com',  SHA2('123456',256), '41991011015', '1989-12-01'),
(16, 'FUNCIONARIO', 'Renata Faria',    '22233344416', 'renata.faria@agencia.com',   SHA2('123456',256), '41992012001', '1984-03-17'),
(17, 'FUNCIONARIO', 'Marcos Teixeira', '22233344417', 'marcos.teixeira@agencia.com',SHA2('123456',256), '41992012002', '1991-09-25');

INSERT INTO funcionario (id_pessoa, matricula, cargo, salario) VALUES
(16, 'F001', 'Gerente',    6500.00),
(17, 'F002', 'Atendente',  3200.00);

-- 4. QUARTOS
INSERT INTO quarto (id_quarto, id_hotel, id_categoria, numero, andar, status) VALUES
(1,  1, 1,  101, 1, 'DISPONIVEL'),
(2,  1, 2,  102, 1, 'DISPONIVEL'),
(3,  1, 3,  103, 1, 'DISPONIVEL'),
(4,  1, 4,  201, 2, 'MANUTENCAO'),
(5,  1, 5,  202, 2, 'DISPONIVEL'),
(6,  2, 6,  203, 2, 'DISPONIVEL'),
(7,  2, 7,  301, 3, 'DISPONIVEL'),
(8,  2, 8,  302, 3, 'DISPONIVEL'),
(9,  2, 9,  303, 3, 'DISPONIVEL'),
(10, 3, 10, 401, 4, 'DISPONIVEL'),
(11, 3, 11, 402, 4, 'DISPONIVEL'),
(12, 3, 12, 403, 4, 'DISPONIVEL'),
(13, 4, 13, 501, 5, 'DISPONIVEL'),
(14, 4, 14, 502, 5, 'DISPONIVEL'),
(15, 4, 15, 601, 6, 'DISPONIVEL');

-- 5. RESERVAS (valor_total segue as regras das subclasses de Quarto)
INSERT INTO reserva (id_reserva, id_cliente, id_quarto, data_reserva, check_in, check_out, num_hospedes, status, valor_total) VALUES
(1,  1,  1,  '2026-09-25', '2026-10-01', '2026-10-03', 1, 'CONCLUIDA',  300.00),
(2,  2,  3,  '2026-09-28', '2026-10-03', '2026-10-06', 2, 'CONCLUIDA',  720.00),
(3,  3,  6,  '2026-09-30', '2026-10-05', '2026-10-07', 3, 'CONCLUIDA',  780.00),
(4,  4,  4,  '2026-10-02', '2026-10-08', '2026-10-10', 1, 'CONCLUIDA',  560.00),
(5,  5,  8,  '2026-10-05', '2026-10-10', '2026-10-12', 2, 'CONCLUIDA',  940.00),
(6,  6,  9,  '2026-10-07', '2026-10-12', '2026-10-14', 2, 'CONCLUIDA', 1210.00),
(7,  7,  11, '2026-10-09', '2026-10-15', '2026-10-18', 2, 'CONCLUIDA', 2490.00),
(8,  8,  13, '2026-10-12', '2026-10-18', '2026-10-20', 2, 'CONCLUIDA', 2120.00),
(9,  9,  15, '2026-10-14', '2026-10-20', '2026-10-23', 4, 'CONCLUIDA', 6840.00),
(10, 10, 7,  '2026-11-01', '2026-11-10', '2026-11-17', 2, 'CONCLUIDA' , 2835.00),
(11, 11, 2,  '2026-11-05', '2026-11-12', '2026-11-15', 2, 'CONCLUIDA' ,  660.00),
(12, 12, 5,  '2026-11-04', '2026-11-11', '2026-11-14', 2, 'CONCLUIDA' ,  990.00),
(13, 13, 10, '2026-11-08', '2026-11-13', '2026-11-16', 2, 'CONCLUIDA' , 2046.00),
(14, 14, 12, '2026-11-03', '2026-11-09', '2026-11-14', 3, 'CONCLUIDA' , 4500.00),
(15, 15, 14, '2026-11-02', '2026-11-10', '2026-11-15', 2, 'CONCLUIDA' , 7900.00),
(16, 1,  1,  '2026-11-10', '2026-12-20', '2026-12-23', 1, 'PENDENTE',    450.00),
(17, 3,  13, '2026-11-12', '2026-12-26', '2026-12-30', 2, 'CONFIRMADA', 4240.00),
(18, 5,  6,  '2026-10-30', '2026-12-01', '2026-12-04', 2, 'CANCELADA',  1170.00);

-- 6. PAGAMENTOS
INSERT INTO pagamento (id_pagamento, id_reserva, forma, valor, status, data_pagamento, parcelas, data_vencimento) VALUES
(1,  1,  'PIX',    300.00,  'APROVADO',  '2026-10-01', NULL, NULL),
(2,  2,  'CARTAO', 720.00,  'APROVADO',  '2026-10-03', 2,    NULL),
(3,  3,  'CARTAO', 780.00,  'APROVADO',  '2026-10-05', 1,    NULL),
(4,  4,  'PIX',    560.00,  'APROVADO',  '2026-10-08', NULL, NULL),
(5,  5,  'BOLETO', 940.00,  'APROVADO',  '2026-10-10', NULL, '2026-10-12'),
(6,  6,  'CARTAO', 1210.00, 'APROVADO',  '2026-10-12', 3,    NULL),
(7,  7,  'PIX',    2490.00, 'APROVADO',  '2026-10-15', NULL, NULL),
(8,  8,  'CARTAO', 2120.00, 'APROVADO',  '2026-10-18', 4,    NULL),
(9,  9,  'BOLETO', 6840.00, 'APROVADO',  '2026-10-20', NULL, '2026-10-20'),
(10, 10, 'PIX',    2835.00, 'APROVADO',  '2026-11-10', NULL, NULL),
(11, 11, 'PIX',    660.00,  'APROVADO',  '2026-11-12', NULL, NULL),
(12, 12, 'CARTAO', 990.00,  'APROVADO',  '2026-11-11', 1,    NULL),
(13, 13, 'CARTAO', 2046.00, 'APROVADO',  '2026-11-13', 2,    NULL),
(14, 14, 'BOLETO', 4500.00, 'APROVADO',  '2026-11-09', NULL, '2026-11-09'),
(15, 15, 'PIX',    7900.00, 'APROVADO',  '2026-11-10', NULL, NULL),
(16, 17, 'CARTAO', 2120.00, 'APROVADO',  '2026-11-12', 1,    NULL),   -- sinal de 50% (restante ainda a pagar)
(17, 16, 'BOLETO', 450.00,  'PENDENTE',   NULL,        NULL, '2026-12-10'),
(18, 18, 'PIX',    1170.00, 'ESTORNADO', '2026-11-02', NULL, NULL);   -- reserva cancelada

-- 7. AVALIACOES (só reservas concluídas)
INSERT INTO avaliacao (id_avaliacao, id_reserva, nota, comentario, data_avaliacao) VALUES
(1, 1, 4, 'Quarto simples e limpo, ótima localização.',               '2026-10-04'),
(2, 2, 5, 'Atendimento excelente, voltaria com certeza.',             '2026-10-07'),
(3, 3, 4, 'Quarto espaçoso para três pessoas.',                       '2026-10-08'),
(4, 4, 3, 'Quarto um pouco antigo, mas confortável.',                 '2026-10-11'),
(5, 5, 5, 'Perfeito para viagem de trabalho.',                        '2026-10-13'),
(6, 6, 4, 'Varanda muito agradável.',                                 '2026-10-15'),
(7, 7, 5, 'Suíte impecável e café da manhã ótimo.',                   '2026-10-19'),
(8, 8, 5, 'Hotel premium, equipe atenciosa.',                         '2026-10-21'),
(9, 9, 5, 'A cobertura foi a melhor parte da viagem.',                '2026-10-24');
