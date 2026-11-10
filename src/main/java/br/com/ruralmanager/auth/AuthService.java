package br.com.ruralmanager.auth;

import br.com.ruralmanager.auth.AuthDtos.*;
import br.com.ruralmanager.comum.Documentos;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.email.EmailServico;
import br.com.ruralmanager.produtor.Produtor;
import br.com.ruralmanager.produtor.ProdutorRepository;
import br.com.ruralmanager.seguranca.LimitadorDeTentativas;
import br.com.ruralmanager.seguranca.TokenService;
import br.com.ruralmanager.usuario.PerfilUsuario;
import br.com.ruralmanager.usuario.Usuario;
import br.com.ruralmanager.usuario.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String MSG_CREDENCIAIS = "E-mail ou senha incorretos.";
    private static final String MSG_CODIGO = "Codigo invalido ou expirado.";

    private final UsuarioRepository usuarios;
    private final ProdutorRepository produtores;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final LimitadorDeTentativas limitador;
    private final EmailServico email;
    /** Hash de uma senha qualquer, usado para gastar o mesmo tempo quando o e-mail nao existe. */
    private final String hashFalso;
    private final SecureRandom aleatorio = new SecureRandom();

    public AuthService(UsuarioRepository usuarios, ProdutorRepository produtores, PasswordEncoder encoder,
                       TokenService tokens, LimitadorDeTentativas limitador, EmailServico email) {
        this.usuarios = usuarios;
        this.produtores = produtores;
        this.encoder = encoder;
        this.tokens = tokens;
        this.limitador = limitador;
        this.email = email;
        this.hashFalso = encoder.encode("senha-inexistente-para-igualar-tempo");
    }

    @Transactional
    public AuthResponse cadastrar(CadastroRequest req) {
        String cpf = Documentos.somenteDigitos(req.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new RegraDeNegocioException("CPF invalido.");
        }
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (produtores.existsByCpf(cpf)) {
            throw new RegraDeNegocioException("Ja existe um cadastro com este CPF.");
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new RegraDeNegocioException("Ja existe um cadastro com este e-mail.");
        }
        Produtor produtor = new Produtor(cpf, req.nome());
        produtor.setTelefone(req.telefone());
        produtores.save(produtor);

        Usuario usuario = new Usuario(req.nome(), email, encoder.encode(req.senha()), PerfilUsuario.PRODUTOR, produtor);
        usuario.registrarAcesso(LocalDateTime.now());
        usuarios.save(usuario);
        return resposta(usuario);
    }

    @Transactional
    public AuthResponse entrar(LoginRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        limitador.exigirLiberado(email);

        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElse(null);
        // Sempre confere um hash, exista ou nao o e-mail, para o tempo de resposta nao revelar quem tem conta.
        boolean senhaOk = encoder.matches(req.senha(), usuario != null ? usuario.getSenhaHash() : hashFalso);

        if (usuario == null || !senhaOk || !usuario.isAtivo()) {
            limitador.registrarFalha(email);
            throw new BadCredentialsException(MSG_CREDENCIAIS);
        }
        limitador.registrarSucesso(email);
        usuario.registrarAcesso(LocalDateTime.now());
        return resposta(usuario);
    }

    /**
     * Sempre se comporta do mesmo jeito exista ou nao o e-mail, para nao revelar
     * quais contas existem. So quem recebe o codigo e quem tem acesso aquela caixa de entrada.
     */
    @Transactional
    public void esqueciSenha(EsqueciSenhaRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        limitador.exigirLiberado("recuperar:" + email);
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElse(null);
        if (usuario != null && usuario.isAtivo()) {
            String codigo = gerarCodigo();
            usuario.definirCodigoRecuperacao(encoder.encode(codigo), LocalDateTime.now().plusMinutes(15));
            this.email.enviar(usuario.getEmail(), "Codigo para redefinir sua senha",
                    "<p>Ola, " + usuario.getNome() + ".</p>"
                            + "<p>Use o codigo abaixo para redefinir sua senha no RuralManager. "
                            + "Ele vale por 15 minutos.</p>"
                            + "<p style=\"font-size:28px;font-weight:bold;letter-spacing:4px;\">" + codigo + "</p>"
                            + "<p>Se voce nao pediu isso, pode ignorar este e-mail.</p>");
        }
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        limitador.exigirLiberado("redefinir:" + email);
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElse(null);
        boolean codigoOk = usuario != null
                && usuario.getCodigoRecuperacaoHash() != null
                && usuario.getCodigoRecuperacaoExpira() != null
                && usuario.getCodigoRecuperacaoExpira().isAfter(LocalDateTime.now())
                && encoder.matches(req.codigo(), usuario.getCodigoRecuperacaoHash());
        if (!codigoOk) {
            limitador.registrarFalha("redefinir:" + email);
            throw new RegraDeNegocioException(MSG_CODIGO);
        }
        limitador.registrarSucesso("redefinir:" + email);
        usuario.setSenhaHash(encoder.encode(req.novaSenha()));
        usuario.limparCodigoRecuperacao();
    }

    private String gerarCodigo() {
        return String.format("%06d", aleatorio.nextInt(1_000_000));
    }

    @Transactional(readOnly = true)
    public UsuarioResumo eu(Long usuarioId) {
        return usuarios.findById(usuarioId).map(AuthService::resumo)
                .orElseThrow(() -> new BadCredentialsException("Sessao invalida."));
    }

    private AuthResponse resposta(Usuario u) {
        return new AuthResponse(tokens.emitir(u), "Bearer", tokens.segundosDeValidade(), resumo(u));
    }

    static UsuarioResumo resumo(Usuario u) {
        return new UsuarioResumo(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(),
                u.getProdutorId(), u.getProdutor() == null ? null : u.getProdutor().getNome());
    }
}
