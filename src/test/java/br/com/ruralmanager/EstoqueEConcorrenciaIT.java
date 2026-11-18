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
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import br.com.ruralmanager.producao.ProducaoDtos.SaidaRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

/**
 * RN10: vendido + destinado nunca pode ultrapassar o colhido. A regra e do servico (nao
 * da pra expressar SUM entre tabelas em CHECK puro), protegida por trava de linha
 * (ColheitaRepository.bloquear) para duas vendas da mesma colheita nao passarem juntas.
 */
class EstoqueEConcorrenciaIT extends IntegrationTestBase {

    private Sessao produtor;
    private Long colheitaId;
    private Long clienteId;
    private Long destinoId;

    @BeforeEach
    void montarColheitaComEstoque() {
        produtor = cadastrar("Produtor Estoque", "93541134780", "estoque." + System.nanoTime() + "@teste.com", "senhaSegura123");
        Long produtoId = criarCatalogoSimples("/api/produtos", produtor.token(), "Milho-" + System.nanoTime());
        destinoId = criarCatalogoSimples("/api/destinos", produtor.token(), "Consumo-" + System.nanoTime());
        Long unidadeId = criarUnidade(produtor.token(), "60");

        Long safraId = post("/api/safras", produtor.token(), new SafraRequest(2026, 2027, null), SafraResponse.class).getBody().id();
        Long culturaId = post("/api/culturas", produtor.token(),
                new CulturaRequest(safraId, produtoId, "Precoce", new BigDecimal("10"), LocalDate.now()),
                CulturaResponse.class).getBody().id();
        colheitaId = post("/api/colheitas", produtor.token(),
                new ColheitaRequest(culturaId, unidadeId, new BigDecimal("100"), LocalDate.now()),
                ColheitaResponse.class).getBody().id();
        clienteId = post("/api/clientes", produtor.token(),
                new ClienteRequest("Comprador", "15350946056", null, null), ClienteResponse.class).getBody().id();
    }

    private ResponseEntity<FaturaResponse> venderQuantidade(BigDecimal quantidade) {
        var req = new FaturaRequest(clienteId, LocalDate.now(),
                List.of(new ItemFaturaRequest(colheitaId, quantidade, new BigDecimal("50"))));
        return post("/api/vendas", produtor.token(), req, FaturaResponse.class);
    }

    @Test
    void vendaDentroDoEstoqueFunciona() {
        assertThat(venderQuantidade(new BigDecimal("40")).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void vendaAcimaDoEstoqueDisponivelFalha() {
        assertThat(venderQuantidade(new BigDecimal("100")).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Estoque ja zerado: qualquer nova venda, por menor que seja, estoura a regra.
        var segunda = venderQuantidade(new BigDecimal("0.01"));
        assertThat(segunda.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void somaDeVendaEDestinoRespeitaOEstoque() {
        assertThat(venderQuantidade(new BigDecimal("60")).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Restam 40; tentar destinar 50 (saida) deve falhar.
        var saida = new SaidaRequest(colheitaId, destinoId, new BigDecimal("50"), LocalDate.now(), null);
        assertThat(post("/api/saidas", produtor.token(), saida, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        // Mas destinar os 40 que restam funciona.
        var saidaOk = new SaidaRequest(colheitaId, destinoId, new BigDecimal("40"), LocalDate.now(), null);
        assertThat(post("/api/saidas", produtor.token(), saidaOk, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void duasVendasConcorrentesNaMesmaColheitaSoUmaPassa() throws Exception {
        // Estoque = 100; duas vendas de 60 ao mesmo tempo somam 120 > 100: so uma pode passar.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        Callable<HttpStatusCode> tarefa = () -> {
            largada.await();
            return venderQuantidade(new BigDecimal("60")).getStatusCode();
        };
        List<Future<HttpStatusCode>> futuros = IntStream.range(0, 2)
                .mapToObj(i -> pool.submit(tarefa)).toList();
        largada.countDown();

        long sucessos = 0;
        long falhas = 0;
        for (Future<HttpStatusCode> f : futuros) {
            HttpStatusCode status = f.get(20, TimeUnit.SECONDS);
            if (status == HttpStatus.CREATED) {
                sucessos++;
            } else if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                falhas++;
            }
        }
        pool.shutdown();

        assertThat(sucessos).isEqualTo(1);
        assertThat(falhas).isEqualTo(1);
    }
}
