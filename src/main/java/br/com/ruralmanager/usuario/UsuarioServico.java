package br.com.ruralmanager.usuario;

import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.produtor.ProdutorRepository;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import br.com.ruralmanager.usuario.UsuarioDtos.NovoOperador;
import br.com.ruralmanager.usuario.UsuarioDtos.UsuarioResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O produtor (perfil PRODUTOR) gerencia quem mais pode lancar dados na sua conta:
 * conjuge, filho, contador. Esses usuarios entram como OPERADOR, que nao gerencia ninguem.
 */
@Service
public class UsuarioServico {

    private final UsuarioRepository usuarios;
    private final ProdutorRepository produtores;
    private final PasswordEncoder encoder;
    private final ContextoUsuario contexto;

    public UsuarioServico(UsuarioRepository usuarios, ProdutorRepository produtores, PasswordEncoder encoder,
                          ContextoUsuario contexto) {
        this.usuarios = usuarios;
        this.produtores = produtores;
        this.encoder = encoder;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR);
        return usuarios.findByProdutor_IdOrderByNome(contexto.produtorId()).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional
    public UsuarioResponse criarOperador(NovoOperador req) {
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR);
        if (usuarios.existsByEmailIgnoreCase(req.email().trim())) {
            throw new RegraDeNegocioException("Ja existe um cadastro com este e-mail.");
        }
        Usuario u = new Usuario(req.nome(), req.email(), encoder.encode(req.senha()), PerfilUsuario.OPERADOR,
                produtores.getReferenceById(contexto.produtorId()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse alterarAtivo(Long id, boolean ativo) {
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR);
        // Filtra pelo produtor: nao se desativa usuario de outra conta.
        Usuario u = usuarios.findByIdAndProdutor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario"));
        if (u.getId().equals(contexto.atual().id())) {
            throw new RegraDeNegocioException("Voce nao pode desativar o proprio acesso.");
        }
        if (ativo) {
            u.ativar();
        } else {
            u.desativar();
        }
        return UsuarioResponse.de(u);
    }
}
