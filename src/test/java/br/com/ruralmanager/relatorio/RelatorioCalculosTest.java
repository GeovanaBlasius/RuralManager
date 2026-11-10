package br.com.ruralmanager.relatorio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.relatorio.RelatorioDtos.DesempenhoCultura;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Os mesmos numeros validados contra o banco: soja 620 sc60 em 18,5 ha, custo 4.500, receita 51.400. */
class RelatorioCalculosTest {

    @Test
    void produtividadeEmQuilosPorHectare() {
        assertThat(RelatorioServico.dividir(new BigDecimal("37200"), new BigDecimal("18.50")))
                .isEqualByComparingTo("2010.81");
    }

    @Test
    void divisaoPorZeroDevolveZeroEmVezDeEstourar() {
        assertThat(RelatorioServico.dividir(new BigDecimal("100"), BigDecimal.ZERO)).isEqualByComparingTo("0");
        assertThat(RelatorioServico.dividir(new BigDecimal("100"), null)).isEqualByComparingTo("0");
    }

    @Test
    void percentualComUmaCasa() {
        assertThat(RelatorioServico.percentual(new BigDecimal("46900"), new BigDecimal("4500")))
                .isEqualByComparingTo("1042.2");
        assertThat(RelatorioServico.percentual(new BigDecimal("1"), BigDecimal.ZERO)).isEqualByComparingTo("0");
    }

    @Test
    void desempenhoCompleto() {
        DesempenhoCultura d = RelatorioServico.desempenho(1L, "Soja", "BMX", new BigDecimal("18.50"),
                new BigDecimal("37200"), false, new BigDecimal("4500"), new BigDecimal("51400"));
        assertThat(d.lucro()).isEqualByComparingTo("46900");
        assertThat(d.produtividadeKgHa()).isEqualByComparingTo("2010.81");
        assertThat(d.custoPorHa()).isEqualByComparingTo("243.24");
        assertThat(d.roiPercentual()).isEqualByComparingTo("1042.2");
    }

    @Test
    void colheitaEmUnidadeSemPesoNaoGeraProdutividade() {
        DesempenhoCultura d = RelatorioServico.desempenho(2L, "Alho", "Roxo", new BigDecimal("2"),
                null, true, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThat(d.produtividadeKgHa()).isNull();
        assertThat(d.roiPercentual()).isNull(); // sem custo, retorno sobre o custo nao se define
    }
}
