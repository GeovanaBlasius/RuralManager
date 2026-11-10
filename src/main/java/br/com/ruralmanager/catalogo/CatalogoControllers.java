package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.catalogo.CatalogoDtos.ItemResponse;
import br.com.ruralmanager.catalogo.CatalogoDtos.NomeRequest;
import br.com.ruralmanager.catalogo.CatalogoServicos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

public final class CatalogoControllers {

    private CatalogoControllers() {
    }

    /** Verbos HTTP dos catalogos simples; cada subclasse so define o caminho. */
    public abstract static class Base {
        private final CatalogoNomeadoServico<?> servico;

        protected Base(CatalogoNomeadoServico<?> servico) {
            this.servico = servico;
        }

        @GetMapping
        public List<ItemResponse> listar() {
            return servico.listar();
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ItemResponse criar(@Valid @RequestBody NomeRequest req) {
            return servico.criar(req.nome());
        }

        @PutMapping("/{id}")
        public ItemResponse atualizar(@PathVariable Long id, @Valid @RequestBody NomeRequest req) {
            return servico.atualizar(id, req.nome());
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/produtos")
    public static class ProdutoController extends Base {
        public ProdutoController(ProdutoServico s) {
            super(s);
        }
    }

    @RestController
    @RequestMapping("/api/categorias-gasto")
    public static class CategoriaGastoController extends Base {
        public CategoriaGastoController(CategoriaGastoServico s) {
            super(s);
        }
    }

    @RestController
    @RequestMapping("/api/destinos")
    public static class DestinoController extends Base {
        public DestinoController(DestinoServico s) {
            super(s);
        }
    }
}
