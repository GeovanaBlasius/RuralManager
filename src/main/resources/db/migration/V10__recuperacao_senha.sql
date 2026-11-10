-- Recuperacao de senha: codigo de 6 digitos enviado por e-mail, valido por
-- 15 minutos. Guarda-se o hash do codigo (BCrypt), nunca o codigo em texto puro.

ALTER TABLE usuario ADD COLUMN codigo_recuperacao_hash   VARCHAR(100) NULL;
ALTER TABLE usuario ADD COLUMN codigo_recuperacao_expira TIMESTAMP    NULL;
