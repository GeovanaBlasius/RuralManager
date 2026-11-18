package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.comercial.ComercialDtos.ClienteRequest;
import br.com.ruralmanager.comercial.ComercialDtos.ClienteResponse;
import br.com.ruralmanager.comercial.ComercialDtos.FaturaRequest;
import br.com.ruralmanager.comercial.ComercialDtos.FaturaResponse;
import br.com.ruralmanager.comercial.ComercialDtos.ItemFaturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.GastoRequest;
import br.com.ruralmanager.producao.ProducaoDtos.GastoResponse;
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * RN-transversal: produtor vem so do token, nunca de parametro do cliente; todo recurso
 * de outro produtor responde 404 (nunca 403, que entregaria que o registro existe).
 * Cobre Safra, Cultura, Colheita, Gasto, Cliente e Venda (fatura) -- "em todos os recursos".
 */
class IsolamentoEntreProdutoresIT extends IntegrationTestBase {

    private Sessao a;
    private Sessao b;
    private Long produtoId;
    private Long unidadeId;
    private Long categoriaId;

    private Long safraId;
    private Long culturaId;
    private Long colheitaId;
    private Long gastoId;
    private Long clienteId;
    private Long faturaId;

    @BeforeEach
    void montarCenario() {
        a = cadastrar("Produtor A", "52998224725", "produtor.a." + System.nanoTime() + "@teste.com", "senhaSegura123");
        b = cadastrar("Produtor B", "15350946056", "produtor.b." + System.nanoTime() + "@teste.com", "senhaSegura123");

        produtoId = criarCatalogoSimples("/api/produtos", a.token(), "Soja-" + System.nanoTime());
        categoriaId = criarCatalogoSimples("/api/categorias-gasto", a.token(), "Sementes-" + System.nanoTime());
        record UnidadeReq(String nome, String sigla, BigDecimal fatorKg) {
        }
        record UnidadeResp(Long id) {
        }
        unidadeId = post("/api/unidades", a.token(), new UnidadeReq("Saca-" + System.nanoTime(), "sc" + (System.nanoTime() % 1000), new BigDecimal("60")), UnidadeResp.class)
                .getBody().id();

        safraId = post("/api/safras", a.token(), new SafraRequest(2026, 2027, null), SafraResponse.class).getBody().id();
        culturaId = post("/api/culturas", a.token(),
                new CulturaRequest(safraId, produtoId, "Intacta", new BigDecimal("18.50"), LocalDate.now()),
                CulturaResponse.class).getBody().id();
        colheitaId = post("/api/colheitas", a.token(),
                new ColheitaRequest(culturaId, unidadeId, new BigDecimal("620"), LocalDate.now()),
                ColheitaResponse.class).getBody().id();
        gastoId = post("/api/gastos", a.token(),
                new GastoRequest(safraId, culturaId, null, categoriaId, "Adubo", new BigDecimal("100"), LocalDate.now()),
                GastoResponse.class).getBody().id();
        clienteId = post("/api/clientes", a.token(),
                new ClienteRequest("Cliente de A", "93541134780", null, null), ClienteResponse.class).getBody().id();
        faturaId = post("/api/vendas", a.token(),
                new FaturaRequest(clienteId, LocalDate.now(), List.of(new ItemFaturaRequest(colheitaId, new BigDecimal("10"), new BigDecimal("150")))),
                FaturaResponse.class).getBody().id();
    }

    @Test
    void produtorBNaoLeDadosDeA() {
        assertThat(get("/api/safras/" + safraId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/culturas/" + culturaId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/colheitas/" + colheitaId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/gastos/" + gastoId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/clientes/" + clienteId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/vendas/" + faturaId, b.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void produtorBNaoEditaNemExcluiDadosDeA() {
        var novaSafra = new SafraRequest(2030, 2031, "tentativa de B");
        assertThat(put("/api/safras/" + safraId, b.token(), novaSafra, Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/safras/" + safraId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/culturas/" + culturaId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/colheitas/" + colheitaId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/gastos/" + gastoId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/clientes/" + clienteId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete("/api/vendas/" + faturaId, b.token()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Confere que nada mudou: A ainda enxerga a safra com os dados originais.
        var ainda = get("/api/safras/" + safraId, a.token(), SafraResponse.class).getBody();
        assertThat(ainda.anoInicio()).isEqualTo(2026);
    }

    @Test
    void produtorBNaoCriaGastoApontandoParaRecursoDeA() {
        var req = new GastoRequest(safraId, culturaId, colheitaId, categoriaId, "Gasto injetado", new BigDecimal("50"), LocalDate.now());
        assertThat(post("/api/gastos", b.token(), req, Object.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void produtorANaoEAfetadoPelaContaDeB() {
        // B cria a propria safra com o mesmo periodo -- nao pode colidir com a de A (unicidade e por produtor).
        var resp = post("/api/safras", b.token(), new SafraRequest(2026, 2027, null), SafraResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().id()).isNotEqualTo(safraId);
    }
}
