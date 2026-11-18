-- Reproduz no Postgres as chaves estrangeiras compostas do trabalho academico
-- (docs/academico/ruralmanager_completo.sql): o gasto de uma cultura so pode
-- pertencer a safra daquela cultura, e o gasto de uma colheita so pode
-- pertencer a cultura daquela colheita. Antes so a camada de servico
-- garantia isso (GastoServico.vinculos); agora o banco tambem garante.
--
-- MATCH SIMPLE (o padrao do Postgres) deixa a checagem de lado quando
-- cultura_id ou colheita_id e nulo, ja que os dois sao opcionais em gasto
-- (custo geral da safra, sem lavoura nem colheita).
--
-- Verificado em producao antes de aplicar: nenhum gasto hoje tem cultura de
-- outra safra, nem colheita de outra cultura (consulta sem retorno).

ALTER TABLE cultura  ADD CONSTRAINT uk_cultura_safra_id   UNIQUE (safra_id, id);
ALTER TABLE colheita ADD CONSTRAINT uk_colheita_cultura_id UNIQUE (cultura_id, id);

ALTER TABLE gasto ADD CONSTRAINT fk_gasto_safra_cultura
    FOREIGN KEY (safra_id, cultura_id) REFERENCES cultura (safra_id, id) MATCH SIMPLE;

ALTER TABLE gasto ADD CONSTRAINT fk_gasto_cultura_colheita
    FOREIGN KEY (cultura_id, colheita_id) REFERENCES colheita (cultura_id, id) MATCH SIMPLE;
