package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.catalogo.CatalogoDtos.UnidadeRequest;
import br.com.ruralmanager.catalogo.CatalogoDtos.UnidadeResponse;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import br.com.ruralmanager.usuario.PerfilUsuario;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnidadeMedidaServico {

    private final UnidadeMedidaRepository repo;
    private final ContextoUsuario contexto;

    public UnidadeMedidaServico(UnidadeMedidaRepository repo, ContextoUsuario contexto) {
        this.repo = repo;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listar() {
        return repo.findAllByOrderByNomeAsc().stream().map(UnidadeResponse::de).toList();
    }

    @Transactional
    public UnidadeResponse criar(UnidadeRequest req) {
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR, PerfilUsuario.ADMIN);
        if (repo.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new RegraDeNegocioException("Ja existe uma unidade com este nome.");
        }
        if (repo.existsBySiglaIgnoreCase(req.sigla().trim())) {
            throw new RegraDeNegocioException("Ja existe uma unidade com esta sigla.");
        }
        return UnidadeResponse.de(repo.save(new UnidadeMedida(req.nome(), req.sigla(), req.fatorKg())));
    }

    @Transactional
    public UnidadeResponse atualizar(Long id, UnidadeRequest req) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        UnidadeMedida u = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade"));
        if (repo.existsByNomeIgnoreCaseAndIdNot(req.nome().trim(), id)) {
            throw new RegraDeNegocioException("Ja existe uma unidade com este nome.");
        }
        if (repo.existsBySiglaIgnoreCaseAndIdNot(req.sigla().trim(), id)) {
            throw new RegraDeNegocioException("Ja existe uma unidade com esta sigla.");
        }
        u.setNome(req.nome());
        u.setSigla(req.sigla());
        u.setFatorKg(req.fatorKg());
        return UnidadeResponse.de(u);
    }

    @Transactional
    public void excluir(Long id) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        UnidadeMedida u = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade"));
        repo.delete(u);
        repo.flush();
    }
}
