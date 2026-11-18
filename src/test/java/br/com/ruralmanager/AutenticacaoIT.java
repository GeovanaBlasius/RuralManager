package br.com.ruralmanager;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ruralmanager.auth.AuthDtos.AuthResponse;
import br.com.ruralmanager.auth.AuthDtos.CadastroRequest;
import br.com.ruralmanager.auth.AuthDtos.CadastroResponse;
import br.com.ruralmanager.auth.AuthDtos.ReenviarCodigoRequest;
import br.com.ruralmanager.auth.AuthDtos.VerificarEmailRequest;
import br.com.ruralmanager.auth.AuthDtos.EsqueciSenhaRequest;
import java.util.Map;
import org.mockito.Mockito;
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

    // ---- Verificacao de e-mail no cadastro ----

    private org.springframework.http.ResponseEntity<CadastroResponse> cadastrarSemConfirmar(String email, String cpf) {
        return post("/api/auth/cadastro", null,
                new CadastroRequest("Pendente", cpf, null, email, "senhaSegura123"), CadastroResponse.class);
    }

    @Test
    void cadastroNaoDaSessaoEExigeConfirmarOCodigoDoEmail() {
        String email = "pendente." + System.nanoTime() + "@teste.com";
        var cad = cadastrarSemConfirmar(email, novoCpf());
        assertThat(cad.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cad.getBody().verificacaoPendente()).isTrue();
        assertThat(cad.getBody().emailEnviado()).isTrue();
        assertThat(cad.getBody().sessao()).isNull();

        // Senha certa, mas e-mail nao confirmado: 403 com um codigo que a tela entende.
        var bloqueado = post("/api/auth/login", null, new LoginRequest(email, "senhaSegura123"), Map.class);
        assertThat(bloqueado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bloqueado.getBody().get("codigo")).isEqualTo("EMAIL_NAO_VERIFICADO");

        String certo = codigoEnviadoPara(email);
        String errado = certo.equals("000000") ? "111111" : "000000";
        assertThat(post("/api/auth/verificar-email", null, new VerificarEmailRequest(email, errado), Object.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        var ok = post("/api/auth/verificar-email", null, new VerificarEmailRequest(email, certo), AuthResponse.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody().token()).isNotBlank();

        assertThat(post("/api/auth/login", null, new LoginRequest(email, "senhaSegura123"), AuthResponse.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        // O codigo e de uso unico.
        assertThat(post("/api/auth/verificar-email", null, new VerificarEmailRequest(email, certo), Object.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void codigoDeVerificacaoExpiradoNaoServe() {
        String email = "vencido." + System.nanoTime() + "@teste.com";
        cadastrarSemConfirmar(email, novoCpf());
        Usuario u = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        u.exigirVerificacao(encoder.encode("123456"), LocalDateTime.now().minusMinutes(1));
        usuarios.save(u);
        assertThat(post("/api/auth/verificar-email", null, new VerificarEmailRequest(email, "123456"), Object.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void reenviarGeraCodigoNovoEOAntigoDeixaDeValer() {
        String email = "reenvia." + System.nanoTime() + "@teste.com";
        cadastrarSemConfirmar(email, novoCpf());
        Usuario u = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        u.exigirVerificacao(encoder.encode("111111"), LocalDateTime.now().plusMinutes(15));
        usuarios.save(u);

        assertThat(post("/api/auth/reenviar-codigo", null, new ReenviarCodigoRequest(email), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(post("/api/auth/verificar-email", null, new VerificarEmailRequest(email, "111111"), Object.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(post("/api/auth/verificar-email", null,
                new VerificarEmailRequest(email, codigoEnviadoPara(email)), AuthResponse.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void cadastroPendenteNaoSegueCpfNemEmailDeOutraPessoa() {
        String email = "ocupado." + System.nanoTime() + "@teste.com";
        String cpf = novoCpf();
        cadastrarSemConfirmar(email, cpf);          // alguem "ocupou" CPF e e-mail sem confirmar

        // O dono de verdade se cadastra com o mesmo CPF e e-mail: o pendente e substituido.
        var dono = cadastrarSemConfirmar(email, cpf);
        assertThat(dono.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(post("/api/auth/verificar-email", null,
                new VerificarEmailRequest(email, codigoEnviadoPara(email)), AuthResponse.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // Ja confirmado, ninguem mais consegue tomar o lugar.
        assertThat(cadastrarSemConfirmar(email, novoCpf()).getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void semEnvioDeEmailConfiguradoOCadastroEARecuperacaoFalamAVerdade() {
        Mockito.when(emailFalso.disponivel()).thenReturn(false);
        String email = "sememail." + System.nanoTime() + "@teste.com";
        assertThat(cadastrarSemConfirmar(email, novoCpf()).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(usuarios.findByEmailIgnoreCase(email)).isEmpty(); // nao cria conta que ninguem consegue ativar
        assertThat(post("/api/auth/esqueci-senha", null, new EsqueciSenhaRequest(email), Object.class).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
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
