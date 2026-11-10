package br.com.ruralmanager.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class RelatorioDtos {

    private RelatorioDtos() {
    }

    /** Uma linha por safra no painel inicial. */
    public record ResumoSafra(Long safraId, String rotulo, int culturas, BigDecimal areaHa,
                              BigDecimal custo, BigDecimal receita, BigDecimal lucro) {
    }

    public record Painel(int totalSafras, BigDecimal custoTotal, BigDecimal receitaTotal, BigDecimal lucroTotal,
                         List<ResumoSafra> safras) {
    }

    /**
     * Desempenho de uma lavoura. Custo e apenas o direto (gastos ligados a ela);
     * os custos gerais da safra aparecem a parte, sem rateio. Rateio exigiria um
     * criterio (por area? por receita?) que e escolha do produtor, nao do sistema.
     *
     * produtividadeKgHa e nulo quando alguma colheita esta em unidade sem peso (unidade, duzia).
     */
    public record DesempenhoCultura(Long culturaId, String produto, String variedade, BigDecimal areaHa,
                                    BigDecimal colhidoKg, boolean possuiColheitaSemPeso,
                                    BigDecimal produtividadeKgHa, BigDecimal custo, BigDecimal custoPorHa,
                                    BigDecimal receita, BigDecimal receitaPorHa, BigDecimal lucro,
                                    BigDecimal roiPercentual) {
    }

    public record GastoPorCategoria(String categoria, BigDecimal total, BigDecimal percentual) {
    }

    public record RelatorioSafra(Long safraId, String rotulo, BigDecimal areaTotalHa, BigDecimal custoTotal,
                                 BigDecimal custoGeral, BigDecimal receitaTotal, BigDecimal lucro,
                                 BigDecimal margemPercentual, List<DesempenhoCultura> culturas,
                                 List<GastoPorCategoria> gastosPorCategoria) {
    }

    public record EstoqueColheita(Long colheitaId, String produto, String variedade, String safra,
                                  LocalDate dataColheita, String unidade, BigDecimal colhido,
                                  BigDecimal vendido, BigDecimal destinado, BigDecimal estoque) {
    }
}
