package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.comercial.ComercialDtos.ClienteRequest;
import br.com.ruralmanager.comercial.ComercialDtos.ClienteResponse;
import br.com.ruralmanager.comercial.ComercialDtos.FaturaRequest;
import br.com.ruralmanager.comercial.ComercialDtos.ItemFaturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import br.com.ruralmanager.producao.ProducaoDtos.SaidaRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Regras de data que o enunciado pede e que o banco nao expressa sozinho (dependem de
 * comparar com outra linha/tabela): plantio dentro da safra e nao futuro; colheita >=
 * plantio e nao futura; saida/venda >= colheita e nao futura.
 */
class RegrasDeDataIT extends IntegrationTestBase {

    private Sessao produtor;
    private Long produtoId;
    private Long unidadeId;
    private Long safraId;

    @BeforeEach
    void montarBase() {
        produtor = cadastrar("Produtor Datas", "03848335603", "datas." + System.nanoTime() + "@teste.com", "senhaSegura123");
        produtoId = criarCatalogoSimples("/api/produtos", produtor.token(), "Trigo-" + System.nanoTime());
        record UnidadeReq(String nome, String sigla, BigDecimal fatorKg) {
        }
        record UnidadeResp(Long id) {
        }
        unidadeId = post("/api/unidades", produtor.token(),
                new UnidadeReq("Ton-" + System.nanoTime(), "t" + (System.nanoTime() % 1000), new BigDecimal("1000")),
                UnidadeResp.class).getBody().id();
        // Safra fechada no ano passado e no retrasado, para poder testar "fora do periodo" nos dois sentidos.
        int anoPassado = LocalDate.now().getYear() - 2;
        safraId = post("/api/safras", produtor.token(), new SafraRequest(anoPassado, anoPassado + 1, null), SafraResponse.class)
                .getBody().id();
    }

    private CulturaResponse cultura(LocalDate plantio, boolean esperaSucesso) {
        var req = new CulturaRequest(safraId, produtoId, "V" + System.nanoTime(), new BigDecimal("5"), plantio);
        var resp = post("/api/culturas", produtor.token(), req, CulturaResponse.class);
        if (esperaSucesso) {
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        } else {
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return resp.getBody();
    }

    @Test
    void plantioForaDoPeriodoDaSafraFalha() {
        int anoPassado = LocalDate.now().getYear() - 2;
        cultura(LocalDate.of(anoPassado - 1, 6, 1), false); // antes do inicio da safra
        cultura(LocalDate.of(anoPassado + 2, 1, 1), false); // depois do fim da safra
    }

    @Test
    void plantioFuturoFalhaMesmoDentroDoPeriodoDaSafra() {
        // Safra futura: ano atual ate o proximo, com data de plantio futura.
        int anoAtual = LocalDate.now().getYear();
        var safraFutura = post("/api/safras", produtor.token(), new SafraRequest(anoAtual, anoAtual + 1, null), SafraResponse.class).getBody();
        var req = new CulturaRequest(safraFutura.id(), produtoId, "Futura", new BigDecimal("5"), LocalDate.now().plusDays(10));
        assertThat(post("/api/culturas", produtor.token(), req, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void colheitaAnteriorAoPlantioFalha() {
        int anoPassado = LocalDate.now().getYear() - 2;
        LocalDate plantio = LocalDate.of(anoPassado, 10, 10);
        var c = cultura(plantio, true);
        var colheitaAntes = new ColheitaRequest(c.id(), unidadeId, new BigDecimal("1"), plantio.minusDays(1));
        assertThat(post("/api/colheitas", produtor.token(), colheitaAntes, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        var colheitaOk = new ColheitaRequest(c.id(), unidadeId, new BigDecimal("1"), plantio.plusDays(90));
        assertThat(post("/api/colheitas", produtor.token(), colheitaOk, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void saidaEVendaAnterioresAColheitaFalham() {
        int anoPassado = LocalDate.now().getYear() - 2;
        LocalDate plantio = LocalDate.of(anoPassado, 10, 10);
        var c = cultura(plantio, true);
        LocalDate dataColheita = plantio.plusDays(90);
        var colheita = post("/api/colheitas", produtor.token(),
                new ColheitaRequest(c.id(), unidadeId, new BigDecimal("10"), dataColheita), ColheitaResponse.class).getBody();

        Long destinoId = criarCatalogoSimples("/api/destinos", produtor.token(), "Consumo-" + System.nanoTime());
        var saidaAntes = new SaidaRequest(colheita.id(), destinoId, new BigDecimal("1"), dataColheita.minusDays(1), null);
        assertThat(post("/api/saidas", produtor.token(), saidaAntes, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        Long clienteId = post("/api/clientes", produtor.token(),
                new ClienteRequest("Cliente Datas", "11144477735", null, null), ClienteResponse.class).getBody().id();
        var vendaAntes = new FaturaRequest(clienteId, dataColheita.minusDays(1),
                List.of(new ItemFaturaRequest(colheita.id(), new BigDecimal("1"), new BigDecimal("10"))));
        assertThat(post("/api/vendas", produtor.token(), vendaAntes, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
