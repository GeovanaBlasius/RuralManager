package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.Destino;
import br.com.ruralmanager.catalogo.DestinoRepository;
import br.com.ruralmanager.comercial.ItemFaturaRepository;
import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.producao.ProducaoDtos.SaidaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SaidaResponse;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Producao que sai do estoque sem ser venda: consumo, semente, ração, perda, doacao. */
@Service
public class SaidaServico {

    private final SaidaRepository saidas;
    private final ColheitaServico colheitas;
    private final DestinoRepository destinos;
    private final ItemFaturaRepository itens;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public SaidaServico(SaidaRepository saidas, ColheitaServico colheitas, DestinoRepository destinos,
                        ItemFaturaRepository itens, ContextoUsuario contexto, Datas datas) {
        this.saidas = saidas;
        this.colheitas = colheitas;
        this.destinos = destinos;
        this.itens = itens;
        this.contexto = contexto;
        this.datas = datas;
    }

    private Saida buscar(Long id) {
        return saidas.findByIdAndColheitaCulturaSafraProdutor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Saida"));
    }

    @Transactional(readOnly = true)
    public Pagina<SaidaResponse> listar(Pageable pageable) {
        return Pagina.de(saidas.findByColheitaCulturaSafraProdutor_IdOrderByDataSaidaDesc(
                contexto.produtorId(), pageable), SaidaResponse::de);
    }

    @Transactional(readOnly = true)
    public SaidaResponse obter(Long id) {
        return SaidaResponse.de(buscar(id));
    }

    @Transactional
    public SaidaResponse criar(SaidaRequest req) {
        Colheita colheita = colheitas.buscarTravando(req.colheitaId());
        Destino destino = destino(req.destinoId());
        validar(colheita, req);
        BigDecimal outros = itens.totalVendido(colheita.getId()).add(saidas.totalDestinado(colheita.getId()));
        exigirEstoque(colheita, outros, req.quantidade());
        return SaidaResponse.de(saidas.save(
                new Saida(colheita, destino, req.quantidade(), req.dataSaida(), req.observacao())));
    }

    @Transactional
    public SaidaResponse atualizar(Long id, SaidaRequest req) {
        Saida s = buscar(id);
        if (!s.getColheita().getId().equals(req.colheitaId())) {
            throw new RegraDeNegocioException("Nao e possivel mover a saida para outra colheita.");
        }
        Colheita colheita = colheitas.buscarTravando(req.colheitaId());
        validar(colheita, req);
        // O estoque disponivel desconta tudo, menos esta mesma saida (que esta sendo reescrita).
        BigDecimal outros = itens.totalVendido(colheita.getId())
                .add(saidas.totalDestinadoExceto(colheita.getId(), id));
        exigirEstoque(colheita, outros, req.quantidade());
        s.setDestino(destino(req.destinoId()));
        s.setQuantidade(req.quantidade());
        s.setDataSaida(req.dataSaida());
        s.setObservacao(req.observacao());
        return SaidaResponse.de(s);
    }

    @Transactional
    public void excluir(Long id) {
        Saida s = buscar(id);
        colheitas.buscarTravando(s.getColheita().getId());
        saidas.delete(s);
    }

    // ---------- apoio ----------

    private Destino destino(Long id) {
        return destinos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Destino"));
    }

    private void validar(Colheita colheita, SaidaRequest req) {
        datas.naoFutura(req.dataSaida(), "A data da saida");
        if (req.dataSaida().isBefore(colheita.getDataColheita())) {
            throw new RegraDeNegocioException("A saida nao pode ser anterior a colheita ("
                    + colheita.getDataColheita() + ").");
        }
    }

    private void exigirEstoque(Colheita colheita, BigDecimal jaComprometido, BigDecimal pedido) {
        BigDecimal disponivel = colheita.getQuantidade().subtract(jaComprometido);
        if (pedido.compareTo(disponivel) > 0) {
            throw new RegraDeNegocioException("Estoque insuficiente: restam "
                    + disponivel.stripTrailingZeros().toPlainString() + " "
                    + colheita.getUnidadeMedida().getSigla() + " nesta colheita.");
        }
    }
}
