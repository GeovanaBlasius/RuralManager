package br.com.ruralmanager.relatorio;

import br.com.ruralmanager.relatorio.RelatorioDtos.*;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioServico servico;

    public RelatorioController(RelatorioServico servico) {
        this.servico = servico;
    }

    @GetMapping("/painel")
    public Painel painel() {
        return servico.painel();
    }

    @GetMapping("/safras/{id}")
    public RelatorioSafra safra(@PathVariable Long id) {
        return servico.safra(id);
    }

    @GetMapping("/estoque")
    public List<EstoqueColheita> estoque(@RequestParam(required = false) Long safraId) {
        return servico.estoque(safraId);
    }
}
