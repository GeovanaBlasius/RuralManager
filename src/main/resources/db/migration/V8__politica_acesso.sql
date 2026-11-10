-- Politica de acesso no Postgres.
--
-- O banco fica hospedado no Supabase, que publica o schema public como API
-- REST. Sem politica, qualquer pessoa com a chave publica do projeto leria
-- as tabelas direto, sem passar pela aplicacao. Habilitar RLS sem criar
-- nenhuma politica fecha esse caminho: os papeis anon e authenticated nao
-- leem nem escrevem nada.
--
-- A aplicacao nao e afetada, porque conecta pelo JDBC com o papel dono do
-- banco, que ignora RLS. Quem autoriza o acesso aos dados e a camada de
-- servico do Spring, filtrando por produtor -- e nao o banco.

ALTER TABLE produtor        ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario         ENABLE ROW LEVEL SECURITY;
ALTER TABLE produto         ENABLE ROW LEVEL SECURITY;
ALTER TABLE unidade_medida  ENABLE ROW LEVEL SECURITY;
ALTER TABLE categoria_gasto ENABLE ROW LEVEL SECURITY;
ALTER TABLE destino         ENABLE ROW LEVEL SECURITY;
ALTER TABLE safra           ENABLE ROW LEVEL SECURITY;
ALTER TABLE cultura         ENABLE ROW LEVEL SECURITY;
ALTER TABLE colheita        ENABLE ROW LEVEL SECURITY;
ALTER TABLE gasto           ENABLE ROW LEVEL SECURITY;
ALTER TABLE saida           ENABLE ROW LEVEL SECURITY;
ALTER TABLE cliente         ENABLE ROW LEVEL SECURITY;
ALTER TABLE fatura          ENABLE ROW LEVEL SECURITY;
ALTER TABLE item_fatura     ENABLE ROW LEVEL SECURITY;
