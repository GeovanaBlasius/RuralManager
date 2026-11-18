-- Verificacao de e-mail no cadastro: codigo de 6 digitos enviado por e-mail, valido por
-- 15 minutos (so o hash BCrypt do codigo e guardado). Quem ja existe antes desta migration
-- fica como verificado (DEFAULT TRUE); o servico marca como FALSE so os cadastros novos.

ALTER TABLE usuario ADD COLUMN email_verificado          BOOLEAN      NOT NULL DEFAULT TRUE;
ALTER TABLE usuario ADD COLUMN codigo_verificacao_hash   VARCHAR(100) NULL;
ALTER TABLE usuario ADD COLUMN codigo_verificacao_expira TIMESTAMP    NULL;
