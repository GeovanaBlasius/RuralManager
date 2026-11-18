package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.comercial.ComercialDtos.ClienteResponse;
import br.com.ruralmanager.comum.Documentos;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.demo.DemoController.CarregarRequest;
import br.com.ruralmanager.demo.DemoServico.Estado;
import br.com.ruralmanager.demo.DemoServico.Resultado;
import br.com.ruralmanager.relatorio.RelatorioDtos.Painel;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

/** Carga dos dados de exemplo do trabalho: liberada por configuracao, confirmada, idempotente. */
@TestPropertySource(properties = "rm.demo.habilitado=true")
class DemoIT extends IntegrationTestBase {

    @Test
    void cargaExigeConfirmacao() {
        var s = cadastrar("Demo Sem Confirmar", null, "demo.sc." + System.nanoTime() + "@teste.com", "senhaSegura123");
        var resp = post("/api/demo/carregar", s.token(), new CarregarRequest(false), Object.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(get("/api/demo", s.token(), Estado.class).getBody().jaCarregado()).isFalse();
    }

    @Test
    void carregaOsDadosDoTrabalhoEEIdempotente() {
        var s = cadastrar("Demo", null, "demo." + System.nanoTime() + "@teste.com", "senhaSegura123");

        var primeira = post("/api/demo/carregar", s.token(), new CarregarRequest(true), Resultado.class);
        assertThat(primeira.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(primeira.getBody().carregou()).isTrue();
        assertThat(get("/api/demo", s.token(), Estado.class).getBody().jaCarregado()).isTrue();

        // Segunda chamada nao duplica nada.
        var segunda = post("/api/demo/carregar", s.token(), new CarregarRequest(true), Resultado.class);
        assertThat(segunda.getBody().carregou()).isFalse();

        var painel = get("/api/relatorios/painel", s.token(), Painel.class).getBody();
        assertThat(painel.totalSafras()).isEqualTo(3);
        // Soma dos 7 itens de fatura do SQL original: 51.400 + 29.480 + 64.990 + 92.832,50 + 58.247 + 19.740 + 1.200.
        assertThat(painel.receitaTotal()).isEqualByComparingTo("317889.50");

        var clientes = rest.exchange("/api/clientes", HttpMethod.GET, new HttpEntity<>(authHeaders(s.token())),
                new ParameterizedTypeReference<Pagina<ClienteResponse>>() {
                });
        assertThat(clientes.getBody().itens()).hasSize(5);
        // Nenhum documento de exemplo pode ser recusado se a pessoa for editar o cliente depois.
        assertThat(clientes.getBody().itens()).allSatisfy(c -> assertThat(Documentos.cpfOuCnpjValido(c.cpfCnpj())).isTrue());
    }

    @Test
    void operadorNaoCarrega() {
        var titular = cadastrar("Titular Demo", null, "demo.t." + System.nanoTime() + "@teste.com", "senhaSegura123");
        record Op(String nome, String email, String senha) {
        }
        String emailOp = "demo.op." + System.nanoTime() + "@teste.com";
        post("/api/usuarios/operadores", titular.token(), new Op("Op", emailOp, "senhaSegura123"), Object.class);
        var op = login(emailOp, "senhaSegura123");
        assertThat(post("/api/demo/carregar", op.token(), new CarregarRequest(true), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
