package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comercial.ComercialDtos.FaturaRequest;
import br.com.ruralmanager.comercial.ComercialDtos.FaturaResponse;
import br.com.ruralmanager.comercial.ComercialDtos.ItemFaturaRequest;
import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.producao.Colheita;
import br.com.ruralmanager.producao.ColheitaServico;
import br.com.ruralmanager.producao.SaidaRepository;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Venda de producao. A regra central: a soma do que foi vendido e destinado
 * sem venda nunca ultrapassa o que foi colhido. Para isso valer mesmo com duas
 * pessoas vendendo ao mesmo tempo, cada colheita e travada antes da conferencia.
 */
@Service
public class FaturaServico {

    private final FaturaRepository faturas;
    private final ItemFaturaRepository itens;
    private final ClienteServico clientes;
    private final ColheitaServico colheitas;
    private final SaidaRepository saidas;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public FaturaServico(FaturaRepository faturas, ItemFaturaRepository itens, ClienteServico clientes,
                         ColheitaServico colheitas, SaidaRepository saidas, ContextoUsuario contexto, Datas datas) {
        this.faturas = faturas;
        this.itens = itens;
        this.clientes = clientes;
        this.colheitas = colheitas;
        this.saidas = saidas;
        this.contexto = contexto;
        this.datas = datas;
    }

    private Fatura buscar(Long id) {
        return faturas.findByIdAndClienteProdutorId(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda"));
    }

    @Transactional(readOnly = true)
    public Pagina<FaturaResponse> listar(Pageable pageable) {
        Page<Fatura> pagina = faturas.findByClienteProdutorIdOrderByDataCompraDescIdDesc(
                contexto.produtorId(), pageable);
        List<Long> ids = pagina.getContent().stream().map(Fatura::getId).toList();
        // Uma consulta para os itens da pagina inteira, em vez de uma por fatura.
        Map<Long, List<ItemFatura>> porFatura = ids.isEmpty() ? Map.of()
                : itens.findByFaturaIdIn(ids).stream().collect(Collectors.groupingBy(i -> i.getFatura().getId()));
        return Pagina.de(pagina, f -> FaturaResponse.de(f, porFatura.getOrDefault(f.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public FaturaResponse obter(Long id) {
        Fatura f = buscar(id);
        return FaturaResponse.de(f, itens.findByFaturaId(id));
    }

    @Transactional
    public FaturaResponse criar(FaturaRequest req) {
        Cliente cliente = clientes.buscar(req.clienteId());
        datas.naoFutura(req.dataCompra(), "A data da venda");
        validarItensDistintos(req.itens());
        Fatura f = faturas.save(new Fatura(cliente, req.dataCompra()));
        List<ItemFatura> gravados = gravarItens(f, req, null);
        return FaturaResponse.de(f, gravados);
    }

    @Transactional
    public FaturaResponse atualizar(Long id, FaturaRequest req) {
        Fatura f = buscar(id);
        if (!f.getCliente().getId().equals(req.clienteId())) {
            throw new RegraDeNegocioException("Nao e possivel trocar o cliente de uma venda. Exclua e lance de novo.");
        }
        datas.naoFutura(req.dataCompra(), "A data da venda");
        validarItensDistintos(req.itens());
        // As conferencias abaixo desconsideram os itens desta fatura, que serao substituidos.
        List<ItemFatura> novos = gravarItens(f, req, id);
        f.setDataCompra(req.dataCompra());
        return FaturaResponse.de(f, novos);
    }

    @Transactional
    public void excluir(Long id) {
        Fatura f = buscar(id);
        itens.excluirDaFatura(id);
        faturas.delete(f);
    }

    // ---------- apoio ----------

    private void validarItensDistintos(List<ItemFaturaRequest> lista) {
        Set<Long> vistos = new HashSet<>();
        for (ItemFaturaRequest i : lista) {
            if (!vistos.add(i.colheitaId())) {
                throw new RegraDeNegocioException(
                        "A mesma colheita aparece duas vezes na venda. Some as quantidades em um unico item.");
            }
        }
    }

    /**
     * Confere e grava os itens. Se faturaAtual != null, e uma edicao: os itens antigos
     * dela sao apagados depois das conferencias, e o estoque as ignora.
     */
    private List<ItemFatura> gravarItens(Fatura fatura, FaturaRequest req, Long faturaAtual) {
        // Ordem fixa por id da colheita: duas vendas que disputam as mesmas colheitas
        // travam na mesma ordem e nunca ficam esperando uma pela outra (deadlock).
        List<ItemFaturaRequest> ordenados = req.itens().stream()
                .sorted(Comparator.comparing(ItemFaturaRequest::colheitaId)).toList();

        List<Colheita> travadas = ordenados.stream()
                .map(i -> colheitas.buscarTravando(i.colheitaId())).toList();

        for (int k = 0; k < ordenados.size(); k++) {
            conferir(travadas.get(k), ordenados.get(k), req, faturaAtual);
        }
        if (faturaAtual != null) {
            itens.excluirDaFatura(faturaAtual);
        }
        return java.util.stream.IntStream.range(0, ordenados.size())
                .mapToObj(k -> itens.save(new ItemFatura(fatura, travadas.get(k),
                        ordenados.get(k).quantidade(), ordenados.get(k).precoUnitario())))
                .toList();
    }

    private void conferir(Colheita colheita, ItemFaturaRequest item, FaturaRequest req, Long faturaAtual) {
        if (req.dataCompra().isBefore(colheita.getDataColheita())) {
            throw new RegraDeNegocioException("A venda nao pode ser anterior a colheita ("
                    + colheita.getDataColheita() + ").");
        }
        BigDecimal vendidoPorOutras = faturaAtual == null
                ? itens.totalVendido(colheita.getId())
                : itens.totalVendidoExceto(colheita.getId(), faturaAtual);
        BigDecimal disponivel = colheita.getQuantidade()
                .subtract(vendidoPorOutras)
                .subtract(saidas.totalDestinado(colheita.getId()));
        if (item.quantidade().compareTo(disponivel) > 0) {
            throw new RegraDeNegocioException("Estoque insuficiente na colheita de "
                    + colheita.getCultura().getProduto().getNome() + ": restam "
                    + disponivel.stripTrailingZeros().toPlainString() + " "
                    + colheita.getUnidadeMedida().getSigla() + ".");
        }
    }
}
