-- Hotel Iara - v3: base de demonstracao NOVA. Nao apaga tabelas.
-- Execute uma unica vez em MySQL 8.0.16+; requer permissao para criar a base.
-- Se a base ja possuir tabelas, pare e revise; nao use DROP para contornar.
-- Ajustes: numero do endereco textual; quarto numerico; categoria AUTO_INCREMENT;
-- quarto guarda somente DISPONIVEL/MANUTENCAO; pessoa exige telefone/nascimento.
-- Datas e coordenadas dos exemplos sao demonstrativas; nao confirmam dados reais.
CREATE DATABASE IF NOT EXISTS hotel_iara_demo DEFAULT CHARACTER SET utf8mb4;
USE hotel_iara_demo;

CREATE TABLE pessoa (
  id_pessoa   INT AUTO_INCREMENT PRIMARY KEY,
  tipo        ENUM('CLIENTE','FUNCIONARIO') NOT NULL DEFAULT 'CLIENTE',
  nome        VARCHAR(100) NOT NULL,
  cpf         VARCHAR(14)  NOT NULL UNIQUE,
  email       VARCHAR(100) NOT NULL UNIQUE,
  senha_hash  VARCHAR(255) NOT NULL,          -- NUNCA guardar a senha pura
  telefone    VARCHAR(20) NOT NULL,
  dt_nasc     DATE NOT NULL
);

-- Dados exclusivos de Funcionario (Cliente não precisa de tabela extra)
CREATE TABLE funcionario (
  id_pessoa  INT PRIMARY KEY,
  matricula  VARCHAR(10) NOT NULL UNIQUE,
  cargo      VARCHAR(50) NOT NULL,
  salario    DECIMAL(10,2),
  CONSTRAINT fk_funcionario_pessoa FOREIGN KEY (id_pessoa)
    REFERENCES pessoa (id_pessoa) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- HOTEL (endereço fica aqui; latitude/longitude alimentam a API de mapas)
-- ---------------------------------------------------------------------
CREATE TABLE hotel (
  id_hotel       INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  classificacao  TINYINT NOT NULL,            -- estrelas (1 a 5)
  telefone       VARCHAR(20),
  qtd_andares    INT,
  logradouro     VARCHAR(100) NOT NULL,
  numero         VARCHAR(20) NOT NULL,
  complemento    VARCHAR(100),
  cep            VARCHAR(9),
  cidade         VARCHAR(60) NOT NULL,
  estado         CHAR(2) NOT NULL,
  latitude       DECIMAL(9,6),
  longitude      DECIMAL(9,6),
  CONSTRAINT ck_hotel_classificacao CHECK (classificacao BETWEEN 1 AND 5)
);

-- ---------------------------------------------------------------------
-- CATEGORIA: "tipo" decide qual subclasse de Quarto o Java instancia
--   PADRAO -> QuartoPadrao | EXECUTIVO -> QuartoExecutivo
--   LUXO   -> QuartoLuxo   | SUITE     -> Suite
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
  id_categoria   INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  tipo           ENUM('PADRAO','EXECUTIVO','LUXO','SUITE') NOT NULL,
  capacidade     INT NOT NULL,
  valor_diaria   DECIMAL(10,2) NOT NULL,
  CONSTRAINT ck_categoria_valor CHECK (valor_diaria > 0)
);

-- ---------------------------------------------------------------------
-- QUARTO (o número só precisa ser único DENTRO do hotel)
-- Status: situação física do quarto. A disponibilidade POR DATA é calculada
-- pela tabela reserva (veja a consulta de exemplo no fim do arquivo).
-- ---------------------------------------------------------------------
CREATE TABLE quarto (
  id_quarto     INT AUTO_INCREMENT PRIMARY KEY,
  id_hotel      INT NOT NULL,
  id_categoria  INT NOT NULL,
  numero        INT NOT NULL,
  andar         INT,
  status        ENUM('DISPONIVEL','MANUTENCAO') NOT NULL DEFAULT 'DISPONIVEL',
  CONSTRAINT uq_quarto_hotel_numero UNIQUE (id_hotel, numero),
  CONSTRAINT fk_quarto_hotel     FOREIGN KEY (id_hotel)     REFERENCES hotel (id_hotel)         ON DELETE RESTRICT,
  CONSTRAINT fk_quarto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- RESERVA (antes não existia: faltavam datas, status e total)
-- Regra adotada: 1 reserva = 1 quarto
-- ---------------------------------------------------------------------
CREATE TABLE reserva (
  id_reserva     INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente     INT NOT NULL,
  id_quarto      INT NOT NULL,
  data_reserva   DATE NOT NULL,
  check_in       DATE NOT NULL,
  check_out      DATE NOT NULL,
  num_hospedes   INT NOT NULL DEFAULT 1,
  status         ENUM('PENDENTE','CONFIRMADA','CANCELADA','CONCLUIDA') NOT NULL DEFAULT 'PENDENTE',
  valor_total    DECIMAL(10,2) NOT NULL,
  CONSTRAINT ck_reserva_datas    CHECK (check_out > check_in),
  CONSTRAINT ck_reserva_hospedes CHECK (num_hospedes > 0),
  CONSTRAINT fk_reserva_cliente  FOREIGN KEY (id_cliente) REFERENCES pessoa (id_pessoa)  ON DELETE RESTRICT,
  CONSTRAINT fk_reserva_quarto   FOREIGN KEY (id_quarto)  REFERENCES quarto (id_quarto)  ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- PAGAMENTO (1 reserva pode ter vários pagamentos: sinal + restante, estorno...)
-- Colunas opcionais conforme a subclasse:
--   PIX    -> nenhuma extra | CARTAO -> parcelas | BOLETO -> data_vencimento
-- ---------------------------------------------------------------------
CREATE TABLE pagamento (
  id_pagamento     INT AUTO_INCREMENT PRIMARY KEY,
  id_reserva       INT NOT NULL,
  forma            ENUM('PIX','CARTAO','BOLETO') NOT NULL,
  valor            DECIMAL(10,2) NOT NULL,
  status           ENUM('PENDENTE','APROVADO','ESTORNADO') NOT NULL DEFAULT 'PENDENTE',
  data_pagamento   DATE,
  parcelas         INT,
  data_vencimento  DATE,
  CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
  CONSTRAINT fk_pagamento_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- AVALIACAO (só quem teve uma reserva pode avaliar; 1 avaliação por reserva)
-- Cliente e hotel são descobertos pela reserva (evita dado repetido - 3FN)
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao (
  id_avaliacao    INT AUTO_INCREMENT PRIMARY KEY,
  id_reserva      INT NOT NULL UNIQUE,
  nota            TINYINT NOT NULL,
  comentario      VARCHAR(500),
  data_avaliacao  DATE NOT NULL,
  CONSTRAINT ck_avaliacao_nota CHECK (nota BETWEEN 1 AND 5),
  CONSTRAINT fk_avaliacao_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE
);

-- =====================================================================
-- Dados de exemplo omitidos da entrega publica. Cadastre dados de teste localmente.

-- CONSULTAS ÚTEIS PARA O SISTEMA (exemplos para os DAOs)
-- =====================================================================

-- Quartos DISPONÍVEIS de um hotel em um período (troque os ? no PreparedStatement):
-- Dois períodos se sobrepõem quando  check_in_existente < nova_saida  E  check_out_existente > nova_entrada
--
-- SELECT q.*, c.nome AS categoria, c.tipo, c.valor_diaria
--   FROM quarto q
--   JOIN categoria c ON c.id_categoria = q.id_categoria
--  WHERE q.id_hotel = ?
--    AND q.status <> 'MANUTENCAO'
--    AND NOT EXISTS (
--          SELECT 1 FROM reserva r
--           WHERE r.id_quarto = q.id_quarto
--             AND r.status IN ('PENDENTE','CONFIRMADA')
--             AND r.check_in  < ?     -- data de saída desejada
--             AND r.check_out > ?);   -- data de entrada desejada

-- Nota média de cada hotel (usado na tela de pesquisa):
CREATE OR REPLACE VIEW vw_nota_media_hotel AS
SELECT h.id_hotel, h.nome, ROUND(AVG(a.nota), 1) AS nota_media, COUNT(a.id_avaliacao) AS qtd_avaliacoes
  FROM hotel h
  LEFT JOIN quarto q   ON q.id_hotel = h.id_hotel
  LEFT JOIN reserva r  ON r.id_quarto = q.id_quarto
  LEFT JOIN avaliacao a ON a.id_reserva = r.id_reserva
 GROUP BY h.id_hotel, h.nome;

-- Histórico de reservas do cliente (usado em consultarHistorico()):
CREATE OR REPLACE VIEW vw_historico_reservas AS
SELECT r.id_reserva, p.id_pessoa AS id_cliente, p.nome AS cliente,
       h.nome AS hotel, q.numero AS quarto, c.nome AS categoria,
       r.check_in, r.check_out, r.status, r.valor_total
  FROM reserva r
  JOIN pessoa p    ON p.id_pessoa  = r.id_cliente
  JOIN quarto q    ON q.id_quarto  = r.id_quarto
  JOIN hotel h     ON h.id_hotel   = q.id_hotel
  JOIN categoria c ON c.id_categoria = q.id_categoria;

SELECT * FROM vw_nota_media_hotel;
SELECT * FROM vw_historico_reservas ORDER BY check_in;
