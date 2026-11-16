CREATE TABLE pessoa (
  id_pessoa   INT AUTO_INCREMENT PRIMARY KEY,
  tipo        ENUM('CLIENTE','FUNCIONARIO') NOT NULL DEFAULT 'CLIENTE',
  nome        VARCHAR(100) NOT NULL,
  cpf         VARCHAR(14)  NOT NULL UNIQUE,
  email       VARCHAR(100) NOT NULL UNIQUE,
  senha_hash  VARCHAR(255) NOT NULL,
  telefone    VARCHAR(20) NOT NULL,
  dt_nasc     DATE NOT NULL
);


CREATE TABLE funcionario (
  id_pessoa  INT PRIMARY KEY,
  matricula  VARCHAR(10) NOT NULL UNIQUE,
  cargo      VARCHAR(50) NOT NULL,
  salario    DECIMAL(10,2),
  CONSTRAINT fk_funcionario_pessoa FOREIGN KEY (id_pessoa)
    REFERENCES pessoa (id_pessoa) ON DELETE CASCADE
);




CREATE TABLE hotel (
  id_hotel       INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  classificacao  TINYINT NOT NULL,
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






CREATE TABLE categoria (
  id_categoria   INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(100) NOT NULL,
  tipo           ENUM('PADRAO','EXECUTIVO','LUXO','SUITE') NOT NULL,
  capacidade     INT NOT NULL,
  valor_diaria   DECIMAL(10,2) NOT NULL,
  CONSTRAINT ck_categoria_valor CHECK (valor_diaria > 0)
);






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





CREATE TABLE avaliacao (
  id_avaliacao    INT AUTO_INCREMENT PRIMARY KEY,
  id_reserva      INT NOT NULL UNIQUE,
  nota            TINYINT NOT NULL,
  comentario      VARCHAR(500),
  data_avaliacao  DATE NOT NULL,
  CONSTRAINT ck_avaliacao_nota CHECK (nota BETWEEN 1 AND 5),
  CONSTRAINT fk_avaliacao_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE
);

