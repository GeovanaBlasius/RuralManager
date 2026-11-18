package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Producao so deve expor o que e necessario: so o health check publico, nenhum outro actuator, nenhum
 * Swagger/OpenAPI (desligados por padrao), e a API sempre com cabecalhos de seguranca.
 */
class SuperficieExpostaIT extends IntegrationTestBase {

    private static final List<String> NAO_DEVEM_EXISTIR = List.of(
            "/actuator", "/actuator/env", "/actuator/beans", "/actuator/mappings", "/actuator/heapdump",
            "/actuator/configprops", "/actuator/loggers", "/actuator/metrics", "/actuator/info",
            "/v3/api-docs", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-ui/index.html",
            "/h2-console", "/api/auth/qualquer-coisa");

    @Test
    void soOHealthCheckEPublico() {
        assertThat(get("/actuator/health/readiness", null, String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        for (String caminho : NAO_DEVEM_EXISTIR) {
            var sem = get(caminho, null, String.class).getStatusCode();
            assertThat(sem.is2xxSuccessful()).as("anonimo em %s deu %s", caminho, sem).isFalse();
        }
    }

    @Test
    void nemLogadoEnxergaActuatorOuSwagger() {
        var s = cadastrar("Produtor Superficie", null, "superficie." + System.nanoTime() + "@teste.com", "senhaSegura123");
        for (String caminho : NAO_DEVEM_EXISTIR) {
            var logado = get(caminho, s.token(), String.class).getStatusCode();
            assertThat(logado.is2xxSuccessful()).as("logado em %s deu %s", caminho, logado).isFalse();
        }
    }

    @Test
    void healthNaoRevelaDetalhes() {
        String corpo = get("/actuator/health/readiness", null, String.class).getBody();
        assertThat(corpo).contains("UP").doesNotContain("jdbc").doesNotContain("postgres").doesNotContain("password");
    }

    @Test
    void respostasDaApiTemCabecalhosDeSeguranca() {
        var h = get("/actuator/health/readiness", null, String.class).getHeaders();
        assertThat(h.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(h.getFirst("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(h.getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(h.getFirst("Content-Security-Policy")).contains("default-src 'none'").contains("frame-ancestors 'none'");
        assertThat(h.getFirst("Strict-Transport-Security")).isNull(); // em HTTP puro nao se envia HSTS
    }

    @Test
    void hstsVaiQuandoOProxyDizQueEHttps() {
        var cab = new HttpHeaders();
        cab.set("X-Forwarded-Proto", "https");
        var resp = rest.exchange("/actuator/health/readiness", HttpMethod.GET, new HttpEntity<>(cab), String.class);
        assertThat(resp.getHeaders().getFirst("Strict-Transport-Security"))
                .contains("max-age=31536000").contains("includeSubDomains");
    }

    @Test
    void erroNaoVazaRastroDePilha() {
        var s = cadastrar("Produtor Erro", null, "erro." + System.nanoTime() + "@teste.com", "senhaSegura123");
        var resp = rest.exchange("/api/safras/abc", HttpMethod.GET, new HttpEntity<>(authHeaders(s.token())), String.class);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
        assertThat(resp.getBody()).doesNotContain("Exception").doesNotContain("at br.com").doesNotContain("org.springframework");
    }
}
