package br.com.ruralmanager.auth;

import br.com.ruralmanager.auth.AuthDtos.*;
import br.com.ruralmanager.comum.Documentos;
import br.com.ruralmanager.comum.EmailNaoVerificadoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.comum.ServicoIndisponivelException;
import br.com.ruralmanager.email.EmailServico;
import br.com.ruralmanager.email.PropriedadesEmail;
import br.com.ruralmanager.produtor.Produtor;
import br.com.ruralmanager.produtor.ProdutorRepository;
import br.com.ruralmanager.seguranca.LimitadorDeTentativas;
import br.com.ruralmanager.seguranca.TokenService;
import br.com.ruralmanager.usuario.PerfilUsuario;
import br.com.ruralmanager.usuario.Usuario;
import br.com.ruralmanager.usuario.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final PropriedadesEmail propriedades;
    /** Hash de uma senha qualquer, usado para gastar o mesmo tempo quando o e-mail nao existe. */
    private final String hashFalso;
    private final SecureRandom aleatorio = new SecureRandom();

    public AuthService(UsuarioRepository usuarios, ProdutorRepository produtores, PasswordEncoder encoder,
                       TokenService tokens, LimitadorDeTentativas limitador, EmailServico email,
                       PropriedadesEmail propriedades) {
        this.propriedades = propriedades;
        this.usuarios = usuarios;
        this.produtores = produtores;
        this.encoder = encoder;
        this.tokens = tokens;
        this.limitador = limitador;
        this.email = email;
        this.hashFalso = encoder.encode("senha-inexistente-para-igualar-tempo");
    }

    /**
     * Com a verificacao ligada (padrao), a conta nasce pendente: so entra depois de confirmar o codigo de
     * 6 digitos enviado ao e-mail. Se nao houver como enviar e-mail, o cadastro e recusado com 503 em vez de
     * criar uma conta que ninguem consegue ativar. Cadastros pendentes nao seguram CPF nem e-mail: um novo
     * cadastro com o mesmo CPF ou e-mail substitui o pendente (quem nunca confirmou nao tem dado nenhum).
     */
    @Transactional
    public CadastroResponse cadastrar(CadastroRequest req) {
        String cpf = Documentos.somenteDigitos(req.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new RegraDeNegocioException("CPF invalido.");
        }
        String endereco = req.email().trim().toLowerCase(Locale.ROOT);
        boolean verificar = propriedades.exigirVerificacao();
        if (verificar && !email.disponivel()) {
            throw new ServicoIndisponivelException(
                    "Cadastro indisponivel no momento: o envio do e-mail de confirmacao nao esta configurado.");
        }
        if (verificar) {
            // Cada cadastro dispara um e-mail para um endereco qualquer: limita para nao virar spam.
            limitador.exigirLiberado("cadastro:" + endereco);
            limitador.registrarFalha("cadastro:" + endereco);
        }
        liberarCadastrosPendentes(cpf, endereco);
        if (produtores.existsByCpf(cpf)) {
            throw new RegraDeNegocioException("Ja existe um cadastro com este CPF.");
        }
        if (usuarios.existsByEmailIgnoreCase(endereco)) {
            throw new RegraDeNegocioException("Ja existe um cadastro com este e-mail.");
        }
        Produtor produtor = new Produtor(cpf, req.nome());
        produtor.setTelefone(req.telefone());
        produtores.save(produtor);

        Usuario usuario = new Usuario(req.nome(), endereco, encoder.encode(req.senha()), PerfilUsuario.PRODUTOR, produtor);
        if (!verificar) {
            usuario.registrarAcesso(LocalDateTime.now());
            usuarios.save(usuario);
            return new CadastroResponse(endereco, false, false, resposta(usuario));
        }
        String codigo = gerarCodigo();
        usuario.exigirVerificacao(encoder.encode(codigo), LocalDateTime.now().plusMinutes(15));
        usuarios.save(usuario);
        boolean enviado = enviarCodigoDeVerificacao(usuario, codigo);
        return new CadastroResponse(endereco, true, enviado, null);
    }

    /** Apaga cadastros que nunca confirmaram o e-mail e ocupam este CPF ou este e-mail. */
    private void liberarCadastrosPendentes(String cpf, String endereco) {
        List<Usuario> pendentes = new ArrayList<>(usuarios.findByEmailIgnoreCaseAndEmailVerificadoFalse(endereco));
        usuarios.findByProdutor_CpfAndEmailVerificadoFalse(cpf).stream()
                .filter(u -> !pendentes.contains(u)).forEach(pendentes::add);
        if (pendentes.isEmpty()) {
            return;
        }
        for (Usuario u : pendentes) {
            Produtor p = u.getProdutor();
            usuarios.delete(u);
            if (p != null) {
                produtores.delete(p);
            }
        }
        // O Hibernate faz DELETE depois de INSERT no mesmo flush; forca agora para liberar as chaves unicas.
        usuarios.flush();
    }

    private boolean enviarCodigoDeVerificacao(Usuario usuario, String codigo) {
        return email.enviar(usuario.getEmail(), "Confirme seu e-mail no RuralManager",
                "<p>Ola, " + usuario.getNome() + ".</p>"
                        + "<p>Use o codigo abaixo para confirmar seu e-mail e ativar sua conta no RuralManager. "
                        + "Ele vale por 15 minutos.</p>"
                        + "<p style=\"font-size:28px;font-weight:bold;letter-spacing:4px;\">" + codigo + "</p>"
                        + "<p>Se voce nao criou esta conta, pode ignorar este e-mail.</p>");
    }

    @Transactional
    public AuthResponse verificarEmail(VerificarEmailRequest req) {
        String endereco = req.email().trim().toLowerCase(Locale.ROOT);
        String chave = "verificar:" + endereco;
        limitador.exigirLiberado(chave);
        Usuario usuario = usuarios.findByEmailIgnoreCase(endereco).orElse(null);
        boolean codigoOk = usuario != null
                && usuario.isAtivo()
                && !usuario.isEmailVerificado()
                && usuario.getCodigoVerificacaoHash() != null
                && usuario.getCodigoVerificacaoExpira() != null
                && usuario.getCodigoVerificacaoExpira().isAfter(LocalDateTime.now())
                && encoder.matches(req.codigo(), usuario.getCodigoVerificacaoHash());
        if (!codigoOk) {
            limitador.registrarFalha(chave);
            throw new RegraDeNegocioException(MSG_CODIGO);
        }
        limitador.registrarSucesso(chave);
        usuario.confirmarEmail();
        usuario.registrarAcesso(LocalDateTime.now());
        return resposta(usuario);
    }

    /**
     * Gera um codigo novo para quem ainda nao confirmou. Responde sempre igual (204) exista ou nao a conta;
     * a unica excecao e o servico de e-mail nao estar configurado, o que nao depende de quem pede.
     */
    @Transactional
    public void reenviarCodigo(ReenviarCodigoRequest req) {
        String endereco = req.email().trim().toLowerCase(Locale.ROOT);
        String chave = "reenviar:" + endereco;
        limitador.exigirLiberado(chave);
        if (!email.disponivel()) {
            throw new ServicoIndisponivelException(
                    "Nao foi possivel enviar o codigo: o envio de e-mail nao esta configurado.");
        }
        limitador.registrarFalha(chave); // conta cada pedido: no maximo 10 reenvios por 15 minutos
        Usuario usuario = usuarios.findByEmailIgnoreCase(endereco).orElse(null);
        if (usuario != null && usuario.isAtivo() && !usuario.isEmailVerificado()) {
            String codigo = gerarCodigo();
            usuario.exigirVerificacao(encoder.encode(codigo), LocalDateTime.now().plusMinutes(15));
            enviarCodigoDeVerificacao(usuario, codigo);
        }
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
        if (!usuario.isEmailVerificado()) {
            // Senha certa, e-mail ainda nao confirmado: a tela leva a pessoa a digitar o codigo.
            throw new EmailNaoVerificadoException();
        }
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
        if (!this.email.disponivel()) {
            // Nao depende de quem pede nem de a conta existir, entao nao revela nada; evita dizer "enviamos" sem enviar.
            throw new ServicoIndisponivelException(
                    "Nao foi possivel enviar o codigo: o envio de e-mail nao esta configurado. Fale com o suporte.");
        }
        limitador.registrarFalha("recuperar:" + email); // conta cada pedido: evita usar o sistema para encher caixa de e-mail
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
