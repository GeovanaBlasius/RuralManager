package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.comercial.ComercialDtos.ClienteRequest;
import br.com.ruralmanager.comercial.ComercialDtos.ClienteResponse;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaResponse;
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Busca e paginacao das listas usadas nos seletores (substitui o limite fixo de 100 itens):
 * a busca filtra no servidor, respeita o dono dos dados e nao aceita curingas do usuario.
 */
class BuscaIT extends IntegrationTestBase {

    @Test
    void buscaDeClientesFiltraPorNomePaginaEIsolaPorProdutor() {
        var a = cadastrar("Produtor Busca A", null, "busca.a." + System.nanoTime() + "@teste.com", "senhaSegura123");
        var b = cadastrar("Produtor Busca B", null, "busca.b." + System.nanoTime() + "@teste.com", "senhaSegura123");
        String[] docs = {"93541134780", "52998224725", "15350946056"};
        String[] nomes = {"Cooperativa Alfa", "Armazem Beta", "Cooperativa Gama"};
        for (int i = 0; i < 3; i++) {
            post("/api/clientes", a.token(), new ClienteRequest(nomes[i], docs[i], null, null), ClienteResponse.class);
        }
        post("/api/clientes", b.token(), new ClienteRequest("Cooperativa do B", docs[0], null, null), ClienteResponse.class);

        var tipo = new ParameterizedTypeReference<Pagina<ClienteResponse>>() {
        };
        var achados = rest.exchange("/api/clientes?busca=cooperativa", HttpMethod.GET,
                new HttpEntity<>(authHeaders(a.token())), tipo).getBody();
        assertThat(achados.itens()).extracting(ClienteResponse::nome)
                .containsExactly("Cooperativa Alfa", "Cooperativa Gama"); // nao inclui o cliente do produtor B

        var pagina1 = rest.exchange("/api/clientes?tamanho=2&pagina=0", HttpMethod.GET,
                new HttpEntity<>(authHeaders(a.token())), tipo).getBody();
        var pagina2 = rest.exchange("/api/clientes?tamanho=2&pagina=1", HttpMethod.GET,
                new HttpEntity<>(authHeaders(a.token())), tipo).getBody();
        assertThat(pagina1.itens()).hasSize(2);
        assertThat(pagina2.itens()).hasSize(1);

        // "%" digitado pelo usuario e texto comum, nao curinga.
        var curinga = rest.exchange("/api/clientes?busca=%25", HttpMethod.GET,
                new HttpEntity<>(authHeaders(a.token())), tipo).getBody();
        assertThat(curinga.itens()).isEmpty();
    }

    @Test
    void buscaDeColheitasPorProdutoEVariedade() {
        var p = cadastrar("Produtor Busca Colheita", null, "busca.c." + System.nanoTime() + "@teste.com", "senhaSegura123");
        Long soja = criarCatalogoSimples("/api/produtos", p.token(), "Soja-" + System.nanoTime());
        Long milho = criarCatalogoSimples("/api/produtos", p.token(), "Milho-" + System.nanoTime());
        Long unidadeId = criarUnidade(p.token(), "60");
        int ano = LocalDate.now().getYear() - 2;
        LocalDate plantio = LocalDate.of(ano, 10, 10);
        Long safraId = post("/api/safras", p.token(), new SafraRequest(ano, ano + 1, null), SafraResponse.class).getBody().id();
        for (Long produto : new Long[]{soja, milho}) {
            Long cultura = post("/api/culturas", p.token(),
                    new CulturaRequest(safraId, produto, "Var" + produto, new BigDecimal("5"), plantio),
                    CulturaResponse.class).getBody().id();
            post("/api/colheitas", p.token(),
                    new ColheitaRequest(cultura, unidadeId, new BigDecimal("10"), plantio.plusDays(100)), ColheitaResponse.class);
        }
        var tipo = new ParameterizedTypeReference<Pagina<ColheitaResponse>>() {
        };
        var soSoja = rest.exchange("/api/colheitas?busca=soja-", HttpMethod.GET,
                new HttpEntity<>(authHeaders(p.token())), tipo);
        assertThat(soSoja.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(soSoja.getBody().itens()).hasSize(1);
        assertThat(soSoja.getBody().itens().get(0).produtoNome()).startsWith("Soja-");

        var todas = rest.exchange("/api/colheitas", HttpMethod.GET, new HttpEntity<>(authHeaders(p.token())), tipo).getBody();
        assertThat(todas.itens()).hasSize(2);
    }
}
