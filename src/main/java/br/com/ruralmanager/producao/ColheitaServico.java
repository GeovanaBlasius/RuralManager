package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.UnidadeMedida;
import br.com.ruralmanager.catalogo.UnidadeMedidaRepository;
import br.com.ruralmanager.comercial.ItemFaturaRepository;
import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.ColheitaResponse;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ColheitaServico {

    private final ColheitaRepository colheitas;
    private final CulturaServico culturas;
    private final UnidadeMedidaRepository unidades;
    private final ItemFaturaRepository itens;
    private final SaidaRepository saidas;
    private final GastoRepository gastos;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public ColheitaServico(ColheitaRepository colheitas, CulturaServico culturas, UnidadeMedidaRepository unidades,
                           ItemFaturaRepository itens, SaidaRepository saidas, GastoRepository gastos,
                           ContextoUsuario contexto, Datas datas) {
        this.colheitas = colheitas;
        this.culturas = culturas;
        this.unidades = unidades;
        this.itens = itens;
        this.saidas = saidas;
        this.gastos = gastos;
        this.contexto = contexto;
        this.datas = datas;
    }

    /** Busca a colheita garantindo que e do produtor logado; reutilizado pelos outros servicos. */
    public Colheita buscar(Long id) {
        return colheitas.findByIdAndCulturaSafraProdutor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colheita"));
    }

    /** Igual a buscar, mas trava a linha para operar o estoque sem corrida com outra requisicao. */
    public Colheita buscarTravando(Long id) {
        buscar(id); // confirma a posse antes de travar
        return colheitas.bloquear(id).orElseThrow(() -> new RecursoNaoEncontradoException("Colheita"));
    }

    @Transactional(readOnly = true)
    public Pagina<ColheitaResponse> listar(Long culturaId, Pageable pageable) {
        Long pid = contexto.produtorId();
        Page<Colheita> pagina = culturaId == null
                ? colheitas.findByCulturaSafraProdutor_IdOrderByDataColheitaDesc(pid, pageable)
                : colheitas.findByCulturaIdAndCulturaSafraProdutor_IdOrderByDataColheitaDesc(culturaId, pid, pageable);
        return montar(pagina);
    }

    @Transactional(readOnly = true)
    public ColheitaResponse obter(Long id) {
        Colheita c = buscar(id);
        return ColheitaResponse.de(c, itens.totalVendido(id), saidas.totalDestinado(id));
    }

    @Transactional
    public ColheitaResponse criar(ColheitaRequest req) {
        Cultura cultura = culturas.buscar(req.culturaId());
        UnidadeMedida unidade = unidade(req.unidadeMedidaId());
        validarData(cultura, req);
        Colheita c = colheitas.save(new Colheita(cultura, unidade, req.quantidade(), req.dataColheita()));
        return ColheitaResponse.de(c, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional
    public ColheitaResponse atualizar(Long id, ColheitaRequest req) {
        Colheita c = buscarTravando(id);
        if (!c.getCultura().getId().equals(req.culturaId())) {
            throw new RegraDeNegocioException("Nao e possivel mover a colheita para outra lavoura.");
        }
        BigDecimal vendido = itens.totalVendido(id);
        BigDecimal destinado = saidas.totalDestinado(id);
        BigDecimal comprometido = vendido.add(destinado);
        boolean temMovimento = comprometido.signum() > 0;

        if (req.quantidade().compareTo(comprometido) < 0) {
            throw new RegraDeNegocioException("A quantidade nao pode ser menor que a ja vendida ou destinada ("
                    + comprometido.stripTrailingZeros().toPlainString() + ").");
        }
        // Trocar a unidade ou a data apos haver vendas/saidas deixaria os lancamentos incoerentes.
        if (temMovimento && !c.getUnidadeMedida().getId().equals(req.unidadeMedidaId())) {
            throw new RegraDeNegocioException("Nao e possivel trocar a unidade de uma colheita que ja teve vendas ou saidas.");
        }
        if (temMovimento && !c.getDataColheita().equals(req.dataColheita())) {
            throw new RegraDeNegocioException("Nao e possivel mudar a data de uma colheita que ja teve vendas ou saidas.");
        }
        validarData(c.getCultura(), req);
        c.setUnidadeMedida(unidade(req.unidadeMedidaId()));
        c.setQuantidade(req.quantidade());
        c.setDataColheita(req.dataColheita());
        return ColheitaResponse.de(c, vendido, destinado);
    }

    @Transactional
    public void excluir(Long id) {
        Colheita c = buscarTravando(id);
        if (itens.existsByColheitaId(id)) {
            throw new RegraDeNegocioException("Esta colheita tem vendas registradas. Exclua as vendas antes.");
        }
        if (saidas.existsByColheitaId(id)) {
            throw new RegraDeNegocioException("Esta colheita tem saidas registradas. Exclua as saidas antes.");
        }
        if (gastos.existsByColheitaId(id)) {
            throw new RegraDeNegocioException("Esta colheita tem gastos vinculados. Exclua ou desvincule os gastos antes.");
        }
        colheitas.delete(c);
    }

    // ---------- apoio ----------

    private UnidadeMedida unidade(Long id) {
        return unidades.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade de medida"));
    }

    private void validarData(Cultura cultura, ColheitaRequest req) {
        datas.naoFutura(req.dataColheita(), "A data da colheita");
        if (req.dataColheita().isBefore(cultura.getDataPlantio())) {
            throw new RegraDeNegocioException("A colheita nao pode ser anterior ao plantio ("
                    + cultura.getDataPlantio() + ").");
        }
    }

    /** Converte uma pagina somando vendido e destinado em duas consultas, e nao uma por linha. */
    private Pagina<ColheitaResponse> montar(Page<Colheita> pagina) {
        List<Long> ids = pagina.getContent().stream().map(Colheita::getId).toList();
        Map<Long, BigDecimal> vendidos = ids.isEmpty() ? Map.of() : somar(itens.totaisVendidos(ids));
        Map<Long, BigDecimal> destinados = ids.isEmpty() ? Map.of() : somar(saidas.totaisDestinados(ids));
        return Pagina.de(pagina, c -> ColheitaResponse.de(c,
                vendidos.getOrDefault(c.getId(), BigDecimal.ZERO),
                destinados.getOrDefault(c.getId(), BigDecimal.ZERO)));
    }

    private static Map<Long, BigDecimal> somar(Collection<TotalPorColheita> totais) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        totais.forEach(t -> mapa.put(t.getColheitaId(), t.getTotal()));
        return mapa;
    }
}
