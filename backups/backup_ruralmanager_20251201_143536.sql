-- Backup do banco de dados: ruralmanager
-- Data: 2025-12-01T14:35:36.483425300
-- Gerado por RuralManager


-- Backup da tabela: produtor
DROP TABLE IF EXISTS produtor;
CREATE TABLE `produtor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `telefone` varchar(15) NOT NULL,
  `endereco` varchar(200) DEFAULT NULL,
  `data_cadastro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cpf` (`cpf`),
  KEY `idx_cpf` (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Backup da tabela: safra
DROP TABLE IF EXISTS safra;
CREATE TABLE `safra` (
  `id` int NOT NULL AUTO_INCREMENT,
  `produtor_id` int NOT NULL,
  `ano` int NOT NULL,
  `descricao` text,
  `data_cadastro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_produtor_ano` (`produtor_id`,`ano`),
  CONSTRAINT `safra_ibfk_1` FOREIGN KEY (`produtor_id`) REFERENCES `produtor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Backup da tabela: cultura
DROP TABLE IF EXISTS cultura;
CREATE TABLE `cultura` (
  `id` int NOT NULL AUTO_INCREMENT,
  `safra_id` int NOT NULL,
  `nome` varchar(100) NOT NULL,
  `variedade` varchar(100) NOT NULL,
  `area_plantada` decimal(10,2) NOT NULL,
  `data_cadastro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_safra_nome` (`safra_id`,`nome`),
  CONSTRAINT `cultura_ibfk_1` FOREIGN KEY (`safra_id`) REFERENCES `safra` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cultura_chk_1` CHECK ((`area_plantada` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Backup da tabela: gasto
DROP TABLE IF EXISTS gasto;
CREATE TABLE `gasto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cultura_id` int NOT NULL,
  `descricao` varchar(100) NOT NULL,
  `categoria` varchar(50) NOT NULL,
  `valor` decimal(10,2) NOT NULL,
  `data` date NOT NULL,
  `data_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cultura_id` (`cultura_id`),
  CONSTRAINT `gasto_ibfk_1` FOREIGN KEY (`cultura_id`) REFERENCES `cultura` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Backup da tabela: colheita
DROP TABLE IF EXISTS colheita;
CREATE TABLE `colheita` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cultura_id` int NOT NULL,
  `quantidade` decimal(10,2) NOT NULL,
  `preco_venda` decimal(10,2) DEFAULT '0.00',
  `vendida` tinyint(1) DEFAULT '0',
  `data_colheita` date NOT NULL,
  `data_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cultura_id` (`cultura_id`),
  CONSTRAINT `colheita_ibfk_1` FOREIGN KEY (`cultura_id`) REFERENCES `cultura` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Fim do backup
