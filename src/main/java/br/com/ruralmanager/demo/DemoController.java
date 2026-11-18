package br.com.ruralmanager.demo;

import br.com.ruralmanager.demo.DemoServico.Estado;
import br.com.ruralmanager.demo.DemoServico.Resultado;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    public record CarregarRequest(boolean confirmar) {
    }

    private final DemoServico servico;

    public DemoController(DemoServico servico) {
        this.servico = servico;
    }

    /** Diz se a carga esta liberada neste ambiente e se a conta ja a recebeu. */
    @GetMapping
    public Estado estado() {
        return servico.estado();
    }

    @PostMapping("/carregar")
    public Resultado carregar(@RequestBody CarregarRequest req) {
        return servico.carregar(req.confirmar());
    }
}
