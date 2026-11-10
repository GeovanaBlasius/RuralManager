package br.com.ruralmanager.producao;

import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.Paginacao;
import br.com.ruralmanager.producao.ProducaoDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

public final class ProducaoControllers {

    private ProducaoControllers() {
    }

    @RestController
    @RequestMapping("/api/safras")
    public static class SafraController {
        private final SafraServico servico;

        public SafraController(SafraServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public List<SafraResponse> listar() {
            return servico.listar();
        }

        @GetMapping("/{id}")
        public SafraResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public SafraResponse criar(@Valid @RequestBody SafraRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public SafraResponse atualizar(@PathVariable Long id, @Valid @RequestBody SafraRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/culturas")
    public static class CulturaController {
        private final CulturaServico servico;

        public CulturaController(CulturaServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public List<CulturaResponse> listar(@RequestParam(required = false) Long safraId) {
            return servico.listar(safraId);
        }

        @GetMapping("/{id}")
        public CulturaResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public CulturaResponse criar(@Valid @RequestBody CulturaRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public CulturaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CulturaRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/colheitas")
    public static class ColheitaController {
        private final ColheitaServico servico;

        public ColheitaController(ColheitaServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public Pagina<ColheitaResponse> listar(@RequestParam(required = false) Long culturaId,
                                               @RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho) {
            return servico.listar(culturaId, Paginacao.de(pagina, tamanho));
        }

        @GetMapping("/{id}")
        public ColheitaResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ColheitaResponse criar(@Valid @RequestBody ColheitaRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public ColheitaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColheitaRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/gastos")
    public static class GastoController {
        private final GastoServico servico;

        public GastoController(GastoServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public Pagina<GastoResponse> listar(@RequestParam(required = false) Long safraId,
                                            @RequestParam(defaultValue = "0") int pagina,
                                            @RequestParam(defaultValue = "20") int tamanho) {
            return servico.listar(safraId, Paginacao.de(pagina, tamanho));
        }

        @GetMapping("/{id}")
        public GastoResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public GastoResponse criar(@Valid @RequestBody GastoRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public GastoResponse atualizar(@PathVariable Long id, @Valid @RequestBody GastoRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }

    @RestController
    @RequestMapping("/api/saidas")
    public static class SaidaController {
        private final SaidaServico servico;

        public SaidaController(SaidaServico servico) {
            this.servico = servico;
        }

        @GetMapping
        public Pagina<SaidaResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                            @RequestParam(defaultValue = "20") int tamanho) {
            return servico.listar(Paginacao.de(pagina, tamanho));
        }

        @GetMapping("/{id}")
        public SaidaResponse obter(@PathVariable Long id) {
            return servico.obter(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public SaidaResponse criar(@Valid @RequestBody SaidaRequest req) {
            return servico.criar(req);
        }

        @PutMapping("/{id}")
        public SaidaResponse atualizar(@PathVariable Long id, @Valid @RequestBody SaidaRequest req) {
            return servico.atualizar(id, req);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void excluir(@PathVariable Long id) {
            servico.excluir(id);
        }
    }
}
