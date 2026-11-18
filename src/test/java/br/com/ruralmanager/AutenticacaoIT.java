package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.auth.AuthDtos.AuthResponse;
import br.com.ruralmanager.auth.AuthDtos.EsqueciSenhaRequest;
import br.com.ruralmanager.auth.AuthDtos.LoginRequest;
import br.com.ruralmanager.auth.AuthDtos.RedefinirSenhaRequest;
import br.com.ruralmanager.usuario.Usuario;
import br.com.ruralmanager.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Login, recuperacao de senha e o limitador de tentativas (forca bruta). */
class AutenticacaoIT extends IntegrationTestBase {

    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private PasswordEncoder encoder;

    @Test
    void loginComSenhaCertaFunciona() {
        String email = "login.ok." + System.nanoTime() + "@teste.com";
        cadastrar("Login OK", "52998224725", email, "senhaSegura123");
        var resp = post("/api/auth/login", null, new LoginRequest(email, "senhaSegura123"), AuthResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().token()).isNotBlank();
    }

    @Test
    void loginComSenhaErradaFalhaComMensagemGenerica() {
        String email = "login.errado." + System.nanoTime() + "@teste.com";
        cadastrar("Login Errado", "15350946056", email, "senhaSegura123");
        var resp = post("/api/auth/login", null, new LoginRequest(email, "senhaMuitoErrada"), Object.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginComEmailInexistenteDevolveAMesmaMensagemDeSenhaErrada() {
        // Nao pode dar pra saber, pela resposta, se o e-mail existe ou nao.
        var resp = post("/api/auth/login", null,
                new LoginRequest("ninguem.aqui." + System.nanoTime() + "@teste.com", "qualquerSenha"), Object.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void dezUmaTentativasErradasSeguidasBloqueiaATentativaSeguinte() {
        String email = "bruteforce." + System.nanoTime() + "@teste.com";
        cadastrar("Forca Bruta", "93541134780", email, "senhaSegura123");

        HttpStatusCode ultimoStatus = null;
        for (int i = 0; i < 10; i++) {
            ultimoStatus = post("/api/auth/login", null, new LoginRequest(email, "senhaErrada"), Object.class).getStatusCode();
        }
        assertThat(ultimoStatus).isEqualTo(HttpStatus.UNAUTHORIZED); // ainda dentro do limite

        var decimaPrimeira = post("/api/auth/login", null, new LoginRequest(email, "senhaErrada"), Object.class);
        assertThat(decimaPrimeira.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // bloqueado

        // Mesmo com a senha CERTA, continua bloqueado enquanto a janela nao passar.
        var comSenhaCerta = post("/api/auth/login", null, new LoginRequest(email, "senhaSegura123"), Object.class);
        assertThat(comSenhaCerta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void esqueciSenhaSempreDevolveNoContentExistaOuNaoOEmail() {
        String email = "recupera." + System.nanoTime() + "@teste.com";
        cadastrar("Recupera Senha", "03848335603", email, "senhaSegura123");
        assertThat(post("/api/auth/esqueci-senha", null, new EsqueciSenhaRequest(email), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(post("/api/auth/esqueci-senha", null,
                new EsqueciSenhaRequest("inexistente." + System.nanoTime() + "@teste.com"), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void esqueciSenhaGeraCodigoEQueRedefineComOCodigoCertoFunciona() {
        String email = "redefine." + System.nanoTime() + "@teste.com";
        cadastrar("Redefine Senha", "11144477735", email, "senhaSegura123");

        post("/api/auth/esqueci-senha", null, new EsqueciSenhaRequest(email), Void.class);
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(usuario.getCodigoRecuperacaoHash()).isNotNull();
        assertThat(usuario.getCodigoRecuperacaoExpira()).isAfter(LocalDateTime.now());

        // O e-mail de verdade nao sai no teste (sem chave do Resend): fixa um codigo conhecido
        // direto no banco, do jeito que o servico faria, so pra testar o endpoint de redefinicao.
        usuario.definirCodigoRecuperacao(encoder.encode("123456"), LocalDateTime.now().plusMinutes(15));
        usuarios.save(usuario);

        var codigoErrado = post("/api/auth/redefinir-senha", null,
                new RedefinirSenhaRequest(email, "000000", "novaSenhaSegura1"), Object.class);
        assertThat(codigoErrado.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        var codigoCerto = post("/api/auth/redefinir-senha", null,
                new RedefinirSenhaRequest(email, "123456", "novaSenhaSegura1"), Void.class);
        assertThat(codigoCerto.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var loginComNova = post("/api/auth/login", null, new LoginRequest(email, "novaSenhaSegura1"), AuthResponse.class);
        assertThat(loginComNova.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void redefinirComCodigoExpiradoFalha() {
        String email = "expirado." + System.nanoTime() + "@teste.com";
        cadastrar("Codigo Expirado", "15350946056", email, "outraSenhaSegura1");
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        usuario.definirCodigoRecuperacao(encoder.encode("654321"), LocalDateTime.now().minusMinutes(1));
        usuarios.save(usuario);

        var resp = post("/api/auth/redefinir-senha", null,
                new RedefinirSenhaRequest(email, "654321", "outraSenhaNova1"), Object.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
