package br.com.ruralmanager.suporte;

import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import br.com.ruralmanager.suporte.ChamadoDtos.ChamadoRequest;
import br.com.ruralmanager.suporte.ChamadoDtos.ChamadoResponse;
import br.com.ruralmanager.usuario.PerfilUsuario;
import br.com.ruralmanager.usuario.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChamadoSuporteServico {

    private final ChamadoSuporteRepository chamados;
    private final UsuarioRepository usuarios;
    private final ContextoUsuario contexto;

    public ChamadoSuporteServico(ChamadoSuporteRepository chamados, UsuarioRepository usuarios,
                                 ContextoUsuario contexto) {
        this.chamados = chamados;
        this.usuarios = usuarios;
        this.contexto = contexto;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest req) {
        var usuario = usuarios.getReferenceById(contexto.atual().id());
        return ChamadoResponse.de(chamados.save(new ChamadoSuporte(usuario, req.titulo(), req.descricao())));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarMeus() {
        return chamados.findByUsuarioIdOrderByCriadoEmDesc(contexto.atual().id()).stream()
                .map(ChamadoResponse::de).toList();
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest req) {
        ChamadoSuporte c = buscarMeu(id);
        exigirAberto(c);
        c.setTitulo(req.titulo());
        c.setDescricao(req.descricao());
        return ChamadoResponse.de(c);
    }

    /** So pode excluir o que ainda esta aberto: depois de resolvido, fica no historico da equipe. */
    @Transactional
    public void excluir(Long id) {
        ChamadoSuporte c = buscarMeu(id);
        exigirAberto(c);
        chamados.delete(c);
    }

    private ChamadoSuporte buscarMeu(Long id) {
        return chamados.findByIdAndUsuarioId(id, contexto.atual().id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado"));
    }

    private void exigirAberto(ChamadoSuporte c) {
        if (c.getStatus() != StatusChamado.ABERTO) {
            throw new RegraDeNegocioException("Este chamado ja foi resolvido pela equipe e nao pode mais ser alterado.");
        }
    }

    /** Visao da equipe: todos os chamados de todos os produtores. */
    @Transactional(readOnly = true)
    public Pagina<ChamadoResponse> listarTodos(Pageable pageable) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        return Pagina.de(chamados.findAllByOrderByCriadoEmDesc(pageable), ChamadoResponse::de);
    }

    @Transactional
    public ChamadoResponse resolver(Long id) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        ChamadoSuporte c = buscarQualquer(id);
        c.resolver();
        return ChamadoResponse.de(c);
    }

    @Transactional
    public ChamadoResponse reabrir(Long id) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        ChamadoSuporte c = buscarQualquer(id);
        c.reabrir();
        return ChamadoResponse.de(c);
    }

    private ChamadoSuporte buscarQualquer(Long id) {
        return chamados.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado"));
    }
}
