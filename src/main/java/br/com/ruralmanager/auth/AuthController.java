package br.com.ruralmanager.auth;

import br.com.ruralmanager.auth.AuthDtos.*;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService servico;
    private final ContextoUsuario contexto;

    public AuthController(AuthService servico, ContextoUsuario contexto) {
        this.servico = servico;
        this.contexto = contexto;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastro(@Valid @RequestBody CadastroRequest req) {
        return servico.cadastrar(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return servico.entrar(req);
    }

    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest req) {
        servico.esqueciSenha(req);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest req) {
        servico.redefinirSenha(req);
    }

    /** Dados do usuario do token atual. Exige autenticacao, apesar de ficar sob /api/auth. */
    @GetMapping("/eu")
    public UsuarioResumo eu() {
        return servico.eu(contexto.atual().id());
    }
}
