package br.com.ruralmanager.producao;

import br.com.ruralmanager.comum.Datas;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.produtor.ProdutorRepository;
import br.com.ruralmanager.producao.ProducaoDtos.SafraRequest;
import br.com.ruralmanager.producao.ProducaoDtos.SafraResponse;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafraServico {

    private final SafraRepository safras;
    private final ProdutorRepository produtores;
    private final CulturaRepository culturas;
    private final GastoRepository gastos;
    private final ContextoUsuario contexto;
    private final Datas datas;

    public SafraServico(SafraRepository safras, ProdutorRepository produtores, CulturaRepository culturas,
                        GastoRepository gastos, ContextoUsuario contexto, Datas datas) {
        this.safras = safras;
        this.produtores = produtores;
        this.culturas = culturas;
        this.gastos = gastos;
        this.contexto = contexto;
        this.datas = datas;
    }

    /** Busca a safra garantindo que e do produtor logado; reutilizado pelos outros servicos. */
    public Safra buscar(Long id) {
        return safras.findByIdAndProdutor_Id(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Safra"));
    }

    @Transactional(readOnly = true)
    public List<SafraResponse> listar() {
        return safras.findByProdutor_IdOrderByAnoInicioDesc(contexto.produtorId())
                .stream().map(SafraResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public SafraResponse obter(Long id) {
        return SafraResponse.de(buscar(id));
    }

    @Transactional
    public SafraResponse criar(SafraRequest req) {
        Long pid = contexto.produtorId();
        validarPeriodo(req.anoInicio(), req.anoFim());
        if (safras.existsByProdutor_IdAndAnoInicioAndAnoFim(pid, req.anoInicio().shortValue(), req.anoFim().shortValue())) {
            throw new RegraDeNegocioException("Voce ja cadastrou a safra " + req.anoInicio() + "/" + req.anoFim() + ".");
        }
        Safra s = new Safra(produtores.getReferenceById(pid), req.anoInicio(), req.anoFim(), null);
        s.setDescricao(req.descricao());
        return SafraResponse.de(safras.save(s));
    }

    @Transactional
    public SafraResponse atualizar(Long id, SafraRequest req) {
        Safra s = buscar(id);
        boolean periodoMudou = s.getAnoInicio() != req.anoInicio() || s.getAnoFim() != req.anoFim();
        if (periodoMudou) {
            validarPeriodo(req.anoInicio(), req.anoFim());
            // Mudar o periodo com lavouras lancadas poderia deixar plantios fora da safra.
            if (culturas.existsBySafraId(id)) {
                throw new RegraDeNegocioException(
                        "Nao e possivel mudar o periodo de uma safra que ja tem lavouras. Crie outra safra.");
            }
            if (safras.existsByProdutor_IdAndAnoInicioAndAnoFimAndIdNot(s.getProdutorId(),
                    req.anoInicio().shortValue(), req.anoFim().shortValue(), id)) {
                throw new RegraDeNegocioException("Voce ja cadastrou a safra " + req.anoInicio() + "/" + req.anoFim() + ".");
            }
            s.definirPeriodo(req.anoInicio(), req.anoFim());
        }
        s.setDescricao(req.descricao());
        return SafraResponse.de(s);
    }

    @Transactional
    public void excluir(Long id) {
        Safra s = buscar(id);
        if (culturas.existsBySafraId(id)) {
            throw new RegraDeNegocioException("Esta safra tem lavouras. Exclua as lavouras antes.");
        }
        if (gastos.existsBySafraId(id)) {
            throw new RegraDeNegocioException("Esta safra tem gastos lancados. Exclua os gastos antes.");
        }
        safras.delete(s);
    }

    private void validarPeriodo(Integer inicio, Integer fim) {
        if (inicio < 1900) {
            throw new RegraDeNegocioException("O ano de inicio deve ser 1900 ou posterior.");
        }
        if (fim < inicio || fim > inicio + 1) {
            throw new RegraDeNegocioException("O ano de fim deve ser o mesmo ano de inicio ou o seguinte.");
        }
        if (inicio > datas.hoje().getYear() + 1) {
            throw new RegraDeNegocioException("A safra nao pode comecar mais de um ano a frente.");
        }
    }
}
