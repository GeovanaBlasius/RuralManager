package br.com.ruralmanager.usuario;

import br.com.ruralmanager.usuario.UsuarioDtos.NovoOperador;
import br.com.ruralmanager.usuario.UsuarioDtos.UsuarioResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServico servico;

    public UsuarioController(UsuarioServico servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return servico.listar();
    }

    @PostMapping("/operadores")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody NovoOperador req) {
        return servico.criarOperador(req);
    }

    @PutMapping("/{id}/ativar")
    public UsuarioResponse ativar(@PathVariable Long id) {
        return servico.alterarAtivo(id, true);
    }

    @PutMapping("/{id}/desativar")
    public UsuarioResponse desativar(@PathVariable Long id) {
        return servico.alterarAtivo(id, false);
    }
}
