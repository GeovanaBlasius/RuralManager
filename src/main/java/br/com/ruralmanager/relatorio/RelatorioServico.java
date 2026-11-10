package br.com.ruralmanager.relatorio;

import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.relatorio.RelatorioDtos.*;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Relatorios calculados no banco com SQL agregado, em poucas consultas por tela.
 * Carregar entidades e somar em Java faria uma consulta por lavoura e nao escalaria.
 *
 * Toda consulta filtra por produtor_id. O produtor vem do token, nunca da requisicao.
 */
@Service
public class RelatorioServico {

    private static final BigDecimal CEM = new BigDecimal("100");

    private final JdbcClient jdbc;
    private final ContextoUsuario contexto;

    public RelatorioServico(JdbcClient jdbc, ContextoUsuario contexto) {
        this.jdbc = jdbc;
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------ painel

    @Transactional(readOnly = true)
    public Painel painel() {
        List<ResumoSafra> safras = jdbc.sql("""
                SELECT s.id, s.ano_inicio, s.ano_fim,
                       (SELECT COUNT(*) FROM cultura cu WHERE cu.safra_id = s.id) AS culturas,
                       (SELECT COALESCE(SUM(cu.area_ha), 0) FROM cultura cu WHERE cu.safra_id = s.id) AS area,
                       (SELECT COALESCE(SUM(g.valor), 0) FROM gasto g WHERE g.safra_id = s.id) AS custo,
                       (SELECT COALESCE(SUM(i.quantidade * i.preco_unitario), 0)
                          FROM item_fatura i
                          JOIN colheita co ON co.id = i.colheita_id
                          JOIN cultura cu ON cu.id = co.cultura_id
                         WHERE cu.safra_id = s.id) AS receita
                  FROM safra s
                 WHERE s.produtor_id = :pid
                 ORDER BY s.ano_inicio DESC, s.ano_fim DESC
                """)
                .param("pid", contexto.produtorId())
                .query((rs, n) -> {
                    BigDecimal custo = rs.getBigDecimal("custo");
                    BigDecimal receita = rs.getBigDecimal("receita");
                    return new ResumoSafra(rs.getLong("id"), rs.getInt("ano_inicio") + "/" + rs.getInt("ano_fim"),
                            rs.getInt("culturas"), rs.getBigDecimal("area"), custo, receita, receita.subtract(custo));
                }).list();

        BigDecimal custo = safras.stream().map(ResumoSafra::custo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receita = safras.stream().map(ResumoSafra::receita).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Painel(safras.size(), custo, receita, receita.subtract(custo), safras);
    }

    // --------------------------------------------------------- relatorio de safra

    @Transactional(readOnly = true)
    public RelatorioSafra safra(Long safraId) {
        Long pid = contexto.produtorId();

        var safra = jdbc.sql("SELECT id, ano_inicio, ano_fim FROM safra WHERE id = :sid AND produtor_id = :pid")
                .param("sid", safraId).param("pid", pid)
                .query((rs, n) -> new Object[]{rs.getLong("id"), rs.getInt("ano_inicio") + "/" + rs.getInt("ano_fim")})
                .optional()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Safra"));

        // Cada subconsulta e restrita a esta safra, para nao agregar a tabela inteira.
        List<DesempenhoCultura> culturas = jdbc.sql("""
                SELECT c.id, p.nome AS produto, c.variedade, c.area_ha,
                       k.kg AS colhido_kg, COALESCE(k.sem_peso, FALSE) AS sem_peso,
                       COALESCE(g.total, 0) AS custo, COALESCE(r.total, 0) AS receita
                  FROM cultura c
                  JOIN produto p ON p.id = c.produto_id
                  LEFT JOIN (SELECT co.cultura_id,
                                    SUM(co.quantidade * u.fator_kg) FILTER (WHERE u.fator_kg IS NOT NULL) AS kg,
                                    BOOL_OR(u.fator_kg IS NULL) AS sem_peso
                               FROM colheita co
                               JOIN cultura cu ON cu.id = co.cultura_id
                               JOIN unidade_medida u ON u.id = co.unidade_medida_id
                              WHERE cu.safra_id = :sid
                              GROUP BY co.cultura_id) k ON k.cultura_id = c.id
                  LEFT JOIN (SELECT g2.cultura_id, SUM(g2.valor) AS total
                               FROM gasto g2
                              WHERE g2.safra_id = :sid AND g2.cultura_id IS NOT NULL
                              GROUP BY g2.cultura_id) g ON g.cultura_id = c.id
                  LEFT JOIN (SELECT co.cultura_id, SUM(i.quantidade * i.preco_unitario) AS total
                               FROM item_fatura i
                               JOIN colheita co ON co.id = i.colheita_id
                               JOIN cultura cu ON cu.id = co.cultura_id
                              WHERE cu.safra_id = :sid
                              GROUP BY co.cultura_id) r ON r.cultura_id = c.id
                 WHERE c.safra_id = :sid
                 ORDER BY p.nome, c.variedade
                """)
                .param("sid", safraId)
                .query((rs, n) -> desempenho(rs.getLong("id"), rs.getString("produto"), rs.getString("variedade"),
                        rs.getBigDecimal("area_ha"), rs.getBigDecimal("colhido_kg"), rs.getBoolean("sem_peso"),
                        rs.getBigDecimal("custo"), rs.getBigDecimal("receita")))
                .list();

        BigDecimal custoTotal = jdbc.sql("SELECT COALESCE(SUM(valor), 0) FROM gasto WHERE safra_id = :sid")
                .param("sid", safraId).query(BigDecimal.class).single();
        BigDecimal custoGeral = jdbc.sql(
                        "SELECT COALESCE(SUM(valor), 0) FROM gasto WHERE safra_id = :sid AND cultura_id IS NULL")
                .param("sid", safraId).query(BigDecimal.class).single();

        BigDecimal receitaTotal = culturas.stream().map(DesempenhoCultura::receita)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal area = culturas.stream().map(DesempenhoCultura::areaHa).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucro = receitaTotal.subtract(custoTotal);

        List<GastoPorCategoria> categorias = jdbc.sql("""
                SELECT cg.nome, SUM(g.valor) AS total
                  FROM gasto g JOIN categoria_gasto cg ON cg.id = g.categoria_gasto_id
                 WHERE g.safra_id = :sid
                 GROUP BY cg.nome
                 ORDER BY total DESC
                """)
                .param("sid", safraId)
                .query((rs, n) -> new GastoPorCategoria(rs.getString("nome"), rs.getBigDecimal("total"),
                        percentual(rs.getBigDecimal("total"), custoTotal)))
                .list();

        return new RelatorioSafra((Long) safra[0], (String) safra[1], area, custoTotal, custoGeral, receitaTotal,
                lucro, percentual(lucro, receitaTotal), culturas, categorias);
    }

    // ------------------------------------------------------------------ estoque

    @Transactional(readOnly = true)
    public List<EstoqueColheita> estoque(Long safraId) {
        return jdbc.sql("""
                SELECT co.id, p.nome AS produto, cu.variedade, sa.ano_inicio, sa.ano_fim, co.data_colheita,
                       u.sigla, co.quantidade AS colhido,
                       COALESCE((SELECT SUM(i.quantidade) FROM item_fatura i WHERE i.colheita_id = co.id), 0) AS vendido,
                       COALESCE((SELECT SUM(s.quantidade) FROM saida s WHERE s.colheita_id = co.id), 0) AS destinado
                  FROM colheita co
                  JOIN cultura cu ON cu.id = co.cultura_id
                  JOIN safra sa ON sa.id = cu.safra_id
                  JOIN produto p ON p.id = cu.produto_id
                  JOIN unidade_medida u ON u.id = co.unidade_medida_id
                 WHERE sa.produtor_id = :pid
                   AND (CAST(:sid AS BIGINT) IS NULL OR sa.id = :sid)
                 ORDER BY co.data_colheita DESC, co.id DESC
                """)
                .param("pid", contexto.produtorId())
                .param("sid", safraId, java.sql.Types.BIGINT)
                .query((rs, n) -> {
                    BigDecimal colhido = rs.getBigDecimal("colhido");
                    BigDecimal vendido = rs.getBigDecimal("vendido");
                    BigDecimal destinado = rs.getBigDecimal("destinado");
                    return new EstoqueColheita(rs.getLong("id"), rs.getString("produto"), rs.getString("variedade"),
                            rs.getInt("ano_inicio") + "/" + rs.getInt("ano_fim"),
                            rs.getDate("data_colheita").toLocalDate(), rs.getString("sigla"), colhido, vendido,
                            destinado, colhido.subtract(vendido).subtract(destinado));
                }).list();
    }

    // ------------------------------------------------------------------- apoio

    static DesempenhoCultura desempenho(Long id, String produto, String variedade, BigDecimal area,
                                        BigDecimal colhidoKg, boolean semPeso, BigDecimal custo, BigDecimal receita) {
        BigDecimal lucro = receita.subtract(custo);
        BigDecimal produtividade = (colhidoKg == null || semPeso) ? null : dividir(colhidoKg, area);
        return new DesempenhoCultura(id, produto, variedade, area, colhidoKg, semPeso, produtividade,
                custo, dividir(custo, area), receita, dividir(receita, area), lucro,
                custo.signum() > 0 ? percentual(lucro, custo) : null);
    }

    /** Divisao segura: zero no divisor devolve zero, em vez de estourar (a regra do projeto original). */
    static BigDecimal dividir(BigDecimal a, BigDecimal b) {
        if (b == null || b.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return a.divide(b, 2, RoundingMode.HALF_UP);
    }

    static BigDecimal percentual(BigDecimal parte, BigDecimal todo) {
        if (todo == null || todo.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CEM).divide(todo, 1, RoundingMode.HALF_UP);
    }
}
