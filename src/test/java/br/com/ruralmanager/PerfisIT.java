package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import br.com.ruralmanager.usuario.PerfilUsuario;
import br.com.ruralmanager.usuario.Usuario;
import br.com.ruralmanager.usuario.UsuarioDtos.NovoOperador;
import br.com.ruralmanager.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

/** O que cada perfil pode e nao pode: ADMIN (sem produtor), PRODUTOR (titular) e OPERADOR. */
class PerfisIT extends IntegrationTestBase {

    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private PasswordEncoder encoder;

    private Sessao admin;
    private Sessao produtor;
    private Sessao operador;
    private Long produtoId;

    @BeforeEach
    void montarPerfis() {
        String sufixo = String.valueOf(System.nanoTime());

        // Admin nao se cria pela API (so pelo AdminInicial na subida): grava direto, como ele faz.
        String emailAdmin = "admin." + sufixo + "@teste.com";
        usuarios.save(new Usuario("Admin", emailAdmin, encoder.encode("adminSenhaForte1"), PerfilUsuario.ADMIN, null));
        admin = login(emailAdmin, "adminSenhaForte1");

        produtor = cadastrar("Titular", "52998224725", "titular." + sufixo + "@teste.com", "senhaSegura123");
        String emailOperador = "operador." + sufixo + "@teste.com";
        post("/api/usuarios/operadores", produtor.token(), new NovoOperador("Operador", emailOperador, "senhaSegura123"), Object.class);
        operador = login(emailOperador, "senhaSegura123");

        produtoId = criarCatalogoSimples("/api/produtos", produtor.token(), "Produto-" + sufixo);
    }

    @Test
    void operadorTrabalhaNosDadosDoTitularMasNaoGerenciaEquipe() {
        // Cria dado na conta do titular...
        var safra = post("/api/safras", operador.token(), new SafraRequest(2026, 2027, null), SafraResponse.class);
        assertThat(safra.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // ...e o titular enxerga: e a mesma conta.
        assertThat(get("/api/safras/" + safra.getBody().id(), produtor.token(), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(get("/api/usuarios", operador.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(post("/api/usuarios/operadores", operador.token(),
                new NovoOperador("Outro", "outro." + System.nanoTime() + "@teste.com", "senhaSegura123"), Object.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void titularGerenciaAEquipe() {
        assertThat(get("/api/usuarios", produtor.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void soAdminAlteraEApagaCatalogoCompartilhado() {
        record Nome(String nome) {
        }
        assertThat(put("/api/produtos/" + produtoId, produtor.token(), new Nome("Renomeado"), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(delete("/api/produtos/" + produtoId, operador.token()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(put("/api/produtos/" + produtoId, admin.token(), new Nome("Renomeado pelo admin"), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(delete("/api/produtos/" + produtoId, admin.token()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void operadorNaoCriaItemDeCatalogo() {
        record Nome(String nome) {
        }
        assertThat(post("/api/produtos", operador.token(), new Nome("Do operador"), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void soAdminVeETrataChamadosDeTodos() {
        assertThat(get("/api/suporte/todos", admin.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get("/api/suporte/todos", produtor.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(get("/api/suporte/todos", operador.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminNaoTemProdutorEPortantoNaoAcessaDadosDeProdutor() {
        assertThat(get("/api/safras", admin.token(), Object.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void operadorDesativadoNaoEntraMais() {
        record Ativo(Long id) {
        }
        var lista = get("/api/usuarios", produtor.token(), Ativo[].class).getBody();
        Long idOperador = null;
        for (Ativo u : lista) {
            if (!u.id().equals(produtor.usuarioId())) {
                idOperador = u.id();
            }
        }
        assertThat(put("/api/usuarios/" + idOperador + "/desativar", produtor.token(), null, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        String email = usuarios.findById(idOperador).orElseThrow().getEmail();
        var tentativa = post("/api/auth/login", null, new br.com.ruralmanager.auth.AuthDtos.LoginRequest(email, "senhaSegura123"), Object.class);
        assertThat(tentativa.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
