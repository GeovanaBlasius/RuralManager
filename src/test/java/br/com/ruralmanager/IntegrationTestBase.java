package br.com.ruralmanager;

import br.com.ruralmanager.auth.AuthDtos.AuthResponse;
import br.com.ruralmanager.auth.AuthDtos.CadastroRequest;
import br.com.ruralmanager.auth.AuthDtos.LoginRequest;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes de integracao: sobe um Postgres real via Testcontainers (as migrations
 * do Flyway rodam na subida do contexto, de V1 ate a mais recente) e expoe atalhos para
 * cadastro/login e chamadas HTTP autenticadas contra a API real, de ponta a ponta.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    /**
     * Um unico Postgres para toda a execucao (padrao "singleton container"): o Spring reaproveita
     * o contexto entre as classes de teste, entao o banco nao pode ser derrubado ao fim de cada
     * classe. O Ryuk do Testcontainers o remove quando a JVM termina. Cada teste cria os proprios
     * produtores (com CPF e e-mail unicos), entao os dados nao se misturam.
     */
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected TestRestTemplate rest;

    /** Sessao de um usuario autenticado: token pronto para as chamadas seguintes. */
    protected record Sessao(String token, Long usuarioId, Long produtorId) {
    }

    private static final AtomicLong SEQUENCIA_CPF = new AtomicLong(123_456_001);

    /** CPF valido (digitos verificadores certos) e diferente a cada chamada. */
    protected static String novoCpf() {
        String base = String.format("%09d", SEQUENCIA_CPF.getAndIncrement());
        int d1 = digito(base, 10);
        int d2 = digito(base + d1, 11);
        return base + d1 + d2;
    }

    private static int digito(String digitos, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** O CPF do parametro e ignorado: cada cadastro usa um novo, para os testes nao colidirem no mesmo banco. */
    protected Sessao cadastrar(String nome, String cpfIgnorado, String email, String senha) {
        var resp = rest.postForEntity(
                "/api/auth/cadastro", new CadastroRequest(nome, novoCpf(), null, email, senha), AuthResponse.class);
        if (resp.getBody() == null || resp.getBody().usuario() == null) {
            throw new IllegalStateException("Cadastro falhou: HTTP " + resp.getStatusCode());
        }
        AuthResponse corpo = resp.getBody();
        return new Sessao(corpo.token(), corpo.usuario().id(), corpo.usuario().produtorId());
    }

    protected Sessao login(String email, String senha) {
        AuthResponse resp = rest.postForObject(
                "/api/auth/login", new LoginRequest(email, senha), AuthResponse.class);
        return new Sessao(resp.token(), resp.usuario().id(), resp.usuario().produtorId());
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        if (token != null) {
            h.setBearerAuth(token);
        }
        return h;
    }

    protected <T> ResponseEntity<T> get(String path, String token, Class<T> tipo) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(authHeaders(token)), tipo);
    }

    protected <T> ResponseEntity<T> post(String path, String token, Object corpo, Class<T> tipo) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(corpo, authHeaders(token)), tipo);
    }

    protected <T> ResponseEntity<T> put(String path, String token, Object corpo, Class<T> tipo) {
        return rest.exchange(path, HttpMethod.PUT, new HttpEntity<>(corpo, authHeaders(token)), tipo);
    }

    protected ResponseEntity<Void> delete(String path, String token) {
        return rest.exchange(path, HttpMethod.DELETE, new HttpEntity<>(authHeaders(token)), Void.class);
    }

    private static final AtomicLong SEQUENCIA_UNIDADE = new AtomicLong(1);

    /** Cria uma unidade de medida com nome e sigla unicos (o banco e compartilhado entre as classes de teste). */
    protected Long criarUnidade(String token, String fatorKg) {
        record UnidadeReq(String nome, String sigla, java.math.BigDecimal fatorKg) {
        }
        record UnidadeResp(Long id) {
        }
        long n = SEQUENCIA_UNIDADE.getAndIncrement();
        return post("/api/unidades", token, new UnidadeReq("Unidade de teste " + n, "ut" + n, new java.math.BigDecimal(fatorKg)),
                UnidadeResp.class).getBody().id();
    }

    /** Cria um catalogo simples (produto, categoria de gasto ou destino) e devolve o id. */
    protected Long criarCatalogoSimples(String caminho, String token, String nome) {
        record NomeReq(String nome) {
        }
        record ItemResp(Long id, String nome) {
        }
        return post(caminho, token, new NomeReq(nome), ItemResp.class).getBody().id();
    }
}
