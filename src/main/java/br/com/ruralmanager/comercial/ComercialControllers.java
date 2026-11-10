package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comercial.ComercialDtos.*;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.Paginacao;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

public final class ComercialControllers {

    private ComercialControllers() {
    }

    @RestController
    @RequestMapping("/api/clientes")
    public static class ClienteController {
        private final ClienteServico servico;

        public ClienteController(ClienteServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public Pagina<ClienteResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                              @RequestParam(defaultValue = "20") int tamanho) {
            return servico.listar(Paginacao.de(pagina, tamanho));
        }

        @GetMapping("/{id}")
        public ClienteResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ClienteResponse criar(@Valid @RequestBody ClienteRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/vendas")
    public static class FaturaController {
        private final FaturaServico servico;

        public FaturaController(FaturaServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public Pagina<FaturaResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "20") int tamanho) {
            return servico.listar(Paginacao.de(pagina, tamanho));
        }

        @GetMapping("/{id}")
        public FaturaResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public FaturaResponse criar(@Valid @RequestBody FaturaRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public FaturaResponse atualizar(@PathVariable Long id, @Valid @RequestBody FaturaRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }
}
