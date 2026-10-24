-- Esquema do banco do Bibliotech (MySQL 8).
-- Reconstituído a partir das consultas SQL dos DAOs da versão desktop, com
-- chaves estrangeiras e restrições adicionadas.
--
-- ATENÇÃO (usuario.senha): na versão desktop a senha era guardada em texto puro.
-- Agora a coluna guarda o HASH (PBKDF2). Usuários antigos precisam ser recriados
-- (use AutenticacaoServico.cadastrar) ou terão o login recusado.

CREATE DATABASE IF NOT EXISTS bibliotech CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bibliotech;

CREATE TABLE IF NOT EXISTS aluno (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    matricula  VARCHAR(30)  NOT NULL UNIQUE,
    serie      VARCHAR(30),
    contato    VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS funcionario (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    matricula  VARCHAR(30)  NOT NULL UNIQUE,
    funcao     VARCHAR(60)
);

CREATE TABLE IF NOT EXISTS livro (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titulo      VARCHAR(200) NOT NULL,
    autor       VARCHAR(120) NOT NULL,
    genero      VARCHAR(60),
    ano         INT NOT NULL,
    quantidade  INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_livro_quantidade CHECK (quantidade >= 0)
);

CREATE TABLE IF NOT EXISTS usuario (
    login  VARCHAR(50)  PRIMARY KEY,
    senha  VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS emprestimo (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_aluno         INT  NOT NULL,
    id_funcionario   INT  NOT NULL,
    data_emprestimo  DATE NOT NULL,
    data_prevista    DATE NOT NULL,
    CONSTRAINT fk_emprestimo_aluno       FOREIGN KEY (id_aluno)       REFERENCES aluno (id),
    CONSTRAINT fk_emprestimo_funcionario FOREIGN KEY (id_funcionario) REFERENCES funcionario (id)
);

CREATE TABLE IF NOT EXISTS item_emprestimo (
    id_emprestimo  INT NOT NULL,
    id_livro       INT NOT NULL,
    quantidade     INT NOT NULL,
    PRIMARY KEY (id_emprestimo, id_livro),
    CONSTRAINT fk_item_emprestimo FOREIGN KEY (id_emprestimo) REFERENCES emprestimo (id),
    CONSTRAINT fk_item_livro      FOREIGN KEY (id_livro)      REFERENCES livro (id)
);

CREATE TABLE IF NOT EXISTS devolucao (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_emprestimo   INT  NOT NULL UNIQUE,
    data_devolucao  DATE NOT NULL,
    dias_atraso     INT  NOT NULL,
    multa_total     DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_devolucao_emprestimo FOREIGN KEY (id_emprestimo) REFERENCES emprestimo (id)
);
