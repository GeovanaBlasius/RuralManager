package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.catalogo.CatalogoDtos.UnidadeRequest;
import br.com.ruralmanager.catalogo.CatalogoDtos.UnidadeResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeMedidaController {

    private final UnidadeMedidaServico servico;

    public UnidadeMedidaController(UnidadeMedidaServico servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<UnidadeResponse> listar() {
        return servico.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadeResponse criar(@Valid @RequestBody UnidadeRequest req) {
        return servico.criar(req);
    }

    @PutMapping("/{id}")
    public UnidadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody UnidadeRequest req) {
        return servico.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        servico.excluir(id);
    }
}
