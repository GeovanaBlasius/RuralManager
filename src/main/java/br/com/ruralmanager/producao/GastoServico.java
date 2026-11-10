package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.CategoriaGasto;
import br.com.ruralmanager.catalogo.CategoriaGastoRepository;
import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.producao.ProducaoDtos.GastoRequest;
import br.com.ruralmanager.producao.ProducaoDtos.GastoResponse;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Todo gasto pertence a uma safra; lavoura e colheita sao opcionais. Como o banco
 * usa chaves simples, e este servico que garante a coerencia entre os tres vinculos.
 */
@Service
public class GastoServico {

    private final GastoRepository gastos;
    private final SafraServico safras;
    private final CulturaServico culturas;
    private final ColheitaServico colheitas;
    private final CategoriaGastoRepository categorias;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public GastoServico(GastoRepository gastos, SafraServico safras, CulturaServico culturas,
                        ColheitaServico colheitas, CategoriaGastoRepository categorias,
                        ContextoUsuario contexto, Datas datas) {
        this.gastos = gastos;
        this.safras = safras;
        this.culturas = culturas;
        this.colheitas = colheitas;
        this.categorias = categorias;
        this.contexto = contexto;
        this.datas = datas;
    }

    private Gasto buscar(Long id) {
        return gastos.findByIdAndSafra_Produtor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gasto"));
    }

    @Transactional(readOnly = true)
    public Pagina<GastoResponse> listar(Long safraId, Pageable pageable) {
        Long pid = contexto.produtorId();
        var pagina = safraId == null
                ? gastos.findBySafra_Produtor_IdOrderByDataGastoDesc(pid, pageable)
                : gastos.findBySafraIdAndSafra_Produtor_IdOrderByDataGastoDesc(safraId, pid, pageable);
        return Pagina.de(pagina, GastoResponse::de);
    }

    @Transactional(readOnly = true)
    public GastoResponse obter(Long id) {
        return GastoResponse.de(buscar(id));
    }

    @Transactional
    public GastoResponse criar(GastoRequest req) {
        Safra safra = safras.buscar(req.safraId());
        Vinculos v = vinculos(safra, req);
        validarData(safra, req);
        Gasto g = new Gasto(safra, v.cultura(), v.colheita(), categoria(req.categoriaGastoId()),
                req.descricao(), req.valor(), req.dataGasto());
        return GastoResponse.de(gastos.save(g));
    }

    @Transactional
    public GastoResponse atualizar(Long id, GastoRequest req) {
        Gasto g = buscar(id);
        if (!g.getSafra().getId().equals(req.safraId())) {
            throw new RegraDeNegocioException("Nao e possivel mover o gasto para outra safra.");
        }
        Vinculos v = vinculos(g.getSafra(), req);
        validarData(g.getSafra(), req);
        g.setCultura(v.cultura());
        g.setColheita(v.colheita());
        g.setCategoria(categoria(req.categoriaGastoId()));
        g.setDescricao(req.descricao());
        g.setValor(req.valor());
        g.setDataGasto(req.dataGasto());
        return GastoResponse.de(g);
    }

    @Transactional
    public void excluir(Long id) {
        gastos.delete(buscar(id));
    }

    // ---------- apoio ----------

    private record Vinculos(Cultura cultura, Colheita colheita) {
    }

    /**
     * Regras de coerencia:
     *  - a lavoura, se informada, e da mesma safra do gasto;
     *  - a colheita, se informada, e da lavoura informada (ou, se nao houver lavoura, define a lavoura).
     */
    private Vinculos vinculos(Safra safra, GastoRequest req) {
        Cultura cultura = null;
        Colheita colheita = null;
        if (req.culturaId() != null) {
            cultura = culturas.buscar(req.culturaId());
            if (!cultura.getSafra().getId().equals(safra.getId())) {
                throw new RegraDeNegocioException("A lavoura informada nao pertence a safra do gasto.");
            }
        }
        if (req.colheitaId() != null) {
            colheita = colheitas.buscar(req.colheitaId());
            Cultura daColheita = colheita.getCultura();
            if (cultura == null) {
                cultura = daColheita;
                if (!cultura.getSafra().getId().equals(safra.getId())) {
                    throw new RegraDeNegocioException("A colheita informada nao pertence a safra do gasto.");
                }
            } else if (!daColheita.getId().equals(cultura.getId())) {
                throw new RegraDeNegocioException("A colheita informada nao pertence a lavoura do gasto.");
            }
        }
        return new Vinculos(cultura, colheita);
    }

    private CategoriaGasto categoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de gasto"));
    }

    private void validarData(Safra safra, GastoRequest req) {
        datas.naoFutura(req.dataGasto(), "A data do gasto");
        // Insumos sao comprados antes do plantio e contas fecham depois da colheita: tolera um ano para cada lado.
        int ano = req.dataGasto().getYear();
        if (ano < safra.getAnoInicio() - 1 || ano > safra.getAnoFim() + 1) {
            throw new RegraDeNegocioException("A data do gasto esta muito distante da safra " + safra.getRotulo() + ".");
        }
    }
}
