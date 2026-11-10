package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.Produto;
import br.com.ruralmanager.catalogo.ProdutoRepository;
import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaRequest;
import br.com.ruralmanager.producao.ProducaoDtos.CulturaResponse;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CulturaServico {

    private static final BigDecimal AREA_MAXIMA = new BigDecimal("100000");

    private final CulturaRepository culturas;
    private final SafraServico safras;
    private final ProdutoRepository produtos;
    private final ColheitaRepository colheitas;
    private final GastoRepository gastos;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public CulturaServico(CulturaRepository culturas, SafraServico safras, ProdutoRepository produtos,
                          ColheitaRepository colheitas, GastoRepository gastos, ContextoUsuario contexto,
                          Datas datas) {
        this.culturas = culturas;
        this.safras = safras;
        this.produtos = produtos;
        this.colheitas = colheitas;
        this.gastos = gastos;
        this.contexto = contexto;
        this.datas = datas;
    }

    /** Busca a lavoura garantindo que e do produtor logado; reutilizado pelos outros servicos. */
    public Cultura buscar(Long id) {
        return culturas.findByIdAndSafra_Produtor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lavoura"));
    }

    @Transactional(readOnly = true)
    public List<CulturaResponse> listar(Long safraId) {
        Long pid = contexto.produtorId();
        List<Cultura> lista = safraId == null
                ? culturas.findBySafra_Produtor_IdOrderByDataPlantioDesc(pid)
                : culturas.findBySafraIdAndSafra_Produtor_IdOrderByDataPlantioDesc(safraId, pid);
        return lista.stream().map(CulturaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CulturaResponse obter(Long id) {
        return CulturaResponse.de(buscar(id));
    }

    @Transactional
    public CulturaResponse criar(CulturaRequest req) {
        Safra safra = safras.buscar(req.safraId());
        Produto produto = produto(req.produtoId());
        validarCampos(safra, req);
        if (culturas.existsBySafraIdAndProdutoIdAndVariedadeIgnoreCase(safra.getId(), produto.getId(), req.variedade().trim())) {
            throw new RegraDeNegocioException("Esta safra ja tem " + produto.getNome() + " da variedade "
                    + req.variedade().trim() + ".");
        }
        return CulturaResponse.de(culturas.save(
                new Cultura(safra, produto, req.variedade(), req.areaHa(), req.dataPlantio())));
    }

    @Transactional
    public CulturaResponse atualizar(Long id, CulturaRequest req) {
        Cultura c = buscar(id);
        if (!c.getSafra().getId().equals(req.safraId())) {
            throw new RegraDeNegocioException("Nao e possivel mover a lavoura para outra safra.");
        }
        Produto produto = produto(req.produtoId());
        validarCampos(c.getSafra(), req);
        if (culturas.existsBySafraIdAndProdutoIdAndVariedadeIgnoreCaseAndIdNot(
                c.getSafra().getId(), produto.getId(), req.variedade().trim(), id)) {
            throw new RegraDeNegocioException("Esta safra ja tem " + produto.getNome() + " da variedade "
                    + req.variedade().trim() + ".");
        }
        // O plantio nao pode passar a depois de uma colheita ja registrada.
        if (colheitas.existsByCulturaIdAndDataColheitaBefore(id, req.dataPlantio())) {
            throw new RegraDeNegocioException("Ja existe colheita desta lavoura anterior a essa data de plantio.");
        }
        c.setProduto(produto);
        c.setVariedade(req.variedade());
        c.setAreaHa(req.areaHa());
        c.setDataPlantio(req.dataPlantio());
        return CulturaResponse.de(c);
    }

    @Transactional
    public void excluir(Long id) {
        Cultura c = buscar(id);
        if (colheitas.existsByCulturaId(id)) {
            throw new RegraDeNegocioException("Esta lavoura tem colheitas. Exclua as colheitas antes.");
        }
        if (gastos.existsByCulturaId(id)) {
            throw new RegraDeNegocioException("Esta lavoura tem gastos vinculados. Exclua ou desvincule os gastos antes.");
        }
        culturas.delete(c);
    }

    private Produto produto(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto"));
    }

    private void validarCampos(Safra safra, CulturaRequest req) {
        if (req.areaHa().compareTo(AREA_MAXIMA) > 0) {
            throw new RegraDeNegocioException("Area acima do limite de 100.000 hectares.");
        }
        LocalDate plantio = req.dataPlantio();
        datas.naoFutura(plantio, "A data do plantio");
        // O plantio cai num dos dois anos civis da safra (ex.: safra 2025/2026 => 2025 ou 2026).
        if (plantio.getYear() < safra.getAnoInicio() || plantio.getYear() > safra.getAnoFim()) {
            throw new RegraDeNegocioException("O plantio precisa estar dentro da safra "
                    + safra.getRotulo() + " (anos " + safra.getAnoInicio() + " a " + safra.getAnoFim() + ").");
        }
    }
}
