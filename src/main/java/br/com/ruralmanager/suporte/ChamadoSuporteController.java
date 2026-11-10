package br.com.ruralmanager.suporte;

import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.Paginacao;
import br.com.ruralmanager.suporte.ChamadoDtos.ChamadoRequest;
import br.com.ruralmanager.suporte.ChamadoDtos.ChamadoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suporte")
public class ChamadoSuporteController {

    private final ChamadoSuporteServico servico;

    public ChamadoSuporteController(ChamadoSuporteServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChamadoResponse criar(@Valid @RequestBody ChamadoRequest req) {
        return servico.criar(req);
    }

    /** Os chamados do usuario logado. */
    @GetMapping
    public List<ChamadoResponse> listarMeus() {
        return servico.listarMeus();
    }

    /** So pode alterar o proprio chamado, e so enquanto ainda estiver aberto. */
    @PutMapping("/{id}")
    public ChamadoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ChamadoRequest req) {
        return servico.atualizar(id, req);
    }

    /** So pode excluir o proprio chamado, e so enquanto ainda estiver aberto. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        servico.excluir(id);
    }

    /** Visao da equipe (ADMIN): todos os chamados. */
    @GetMapping("/todos")
    public Pagina<ChamadoResponse> listarTodos(@RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho) {
        return servico.listarTodos(Paginacao.de(pagina, tamanho));
    }

    @PutMapping("/{id}/resolver")
    public ChamadoResponse resolver(@PathVariable Long id) {
        return servico.resolver(id);
    }

    @PutMapping("/{id}/reabrir")
    public ChamadoResponse reabrir(@PathVariable Long id) {
        return servico.reabrir(id);
    }
}
