package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.catalogo.CatalogoDtos.ItemResponse;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import br.com.ruralmanager.usuario.PerfilUsuario;
import java.util.List;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras comuns dos catalogos compartilhados. Qualquer produtor pode CADASTRAR
 * um item que falte (uma cultura nova, uma unidade regional), mas so o
 * administrador ALTERA ou APAGA, porque o item e visto por todos os produtores.
 */
public abstract class CatalogoNomeadoServico<E extends ItemNomeado> {

    private final RepositorioNomeado<E> repo;
    private final ContextoUsuario contexto;
    private final Function<String, E> fabrica;
    private final String rotulo;

    protected CatalogoNomeadoServico(RepositorioNomeado<E> repo, ContextoUsuario contexto,
                                     Function<String, E> fabrica, String rotulo) {
        this.repo = repo;
        this.contexto = contexto;
        this.fabrica = fabrica;
        this.rotulo = rotulo;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listar() {
        return repo.findAllByOrderByNomeAsc().stream().map(ItemResponse::de).toList();
    }

    @Transactional
    public ItemResponse criar(String nome) {
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR, PerfilUsuario.ADMIN);
        String limpo = nome.trim();
        if (repo.existsByNomeIgnoreCase(limpo)) {
            throw new RegraDeNegocioException("Ja existe " + rotulo + " com este nome.");
        }
        return ItemResponse.de(repo.save(fabrica.apply(limpo)));
    }

    @Transactional
    public ItemResponse atualizar(Long id, String nome) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        E item = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(rotulo));
        String limpo = nome.trim();
        if (repo.existsByNomeIgnoreCaseAndIdNot(limpo, id)) {
            throw new RegraDeNegocioException("Ja existe " + rotulo + " com este nome.");
        }
        item.setNome(limpo);
        return ItemResponse.de(item);
    }

    @Transactional
    public void excluir(Long id) {
        contexto.exigirPerfil(PerfilUsuario.ADMIN);
        E item = repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(rotulo));
        repo.delete(item);
        repo.flush(); // forca a FK a reclamar aqui, resultando em 409 "ainda em uso"
    }
}
