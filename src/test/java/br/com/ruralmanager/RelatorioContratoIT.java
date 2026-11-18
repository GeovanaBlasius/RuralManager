package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.producao.ProducaoDtos.ColheitaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.GastoRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import br.com.ruralmanager.relatorio.RelatorioDtos.RelatorioSafra;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Teste de contrato dos relatorios com os numeros do trabalho de Banco de Dados:
 * soja em 18,5 ha, colheita de 620 sc60 (60 kg cada) = 37.200 kg => 2.010,81 kg/ha;
 * custo direto de R$ 4.500 => R$ 243,24 por hectare. Passa pela API e pelo banco de
 * verdade, inclusive a conversao de saca para quilo (o teste unitario so cobre a conta).
 */
class RelatorioContratoIT extends IntegrationTestBase {

    @Test
    void sojaDoTrabalhoDa2010eOitentaEUmQuilosPorHectare() {
        var produtor = cadastrar("Produtor Relatorio", "93541134780", "relatorio." + System.nanoTime() + "@teste.com", "senhaSegura123");
        Long produtoId = criarCatalogoSimples("/api/produtos", produtor.token(), "Soja-" + System.nanoTime());
        Long categoriaId = criarCatalogoSimples("/api/categorias-gasto", produtor.token(), "Sementes-" + System.nanoTime());
        record UnidadeReq(String nome, String sigla, BigDecimal fatorKg) {
        }
        record UnidadeResp(Long id) {
        }
        Long unidadeId = post("/api/unidades", produtor.token(),
                new UnidadeReq("Saca 60 kg " + System.nanoTime(), "s" + (System.nanoTime() % 1000), new BigDecimal("60")),
                UnidadeResp.class).getBody().id();

        int ano = LocalDate.now().getYear() - 2;
        LocalDate plantio = LocalDate.of(ano, 10, 10);
        Long safraId = post("/api/safras", produtor.token(), new SafraRequest(ano, ano + 1, null), SafraResponse.class).getBody().id();
        Long culturaId = post("/api/culturas", produtor.token(),
                new CulturaRequest(safraId, produtoId, "BMX", new BigDecimal("18.50"), plantio), CulturaResponse.class).getBody().id();
        post("/api/colheitas", produtor.token(),
                new ColheitaRequest(culturaId, unidadeId, new BigDecimal("620"), plantio.plusDays(120)), Object.class);
        post("/api/gastos", produtor.token(),
                new GastoRequest(safraId, culturaId, null, categoriaId, "Sementes de soja", new BigDecimal("4500"), plantio), Object.class);

        var resp = get("/api/relatorios/safras/" + safraId, produtor.token(), RelatorioSafra.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        var cultura = resp.getBody().culturas().get(0);
        assertThat(cultura.areaHa()).isEqualByComparingTo("18.50");
        assertThat(cultura.colhidoKg()).isEqualByComparingTo("37200");
        assertThat(cultura.produtividadeKgHa()).isEqualByComparingTo("2010.81");
        assertThat(cultura.custo()).isEqualByComparingTo("4500");
        assertThat(cultura.custoPorHa()).isEqualByComparingTo("243.24");
    }
}
