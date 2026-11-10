package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comercial.ComercialDtos.ClienteRequest;
import br.com.ruralmanager.comercial.ComercialDtos.ClienteResponse;
import br.com.ruralmanager.comum.Documentos;
import br.com.ruralmanager.comum.Pagina;
import br.com.ruralmanager.comum.RecursoNaoEncontradoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.produtor.ProdutorRepository;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServico {

    private final ClienteRepository clientes;
    private final FaturaRepository faturas;
    private final ProdutorRepository produtores;
    private final ContextoUsuario contexto;

    public ClienteServico(ClienteRepository clientes, FaturaRepository faturas, ProdutorRepository produtores,
                          ContextoUsuario contexto) {
        this.clientes = clientes;
        this.faturas = faturas;
        this.produtores = produtores;
        this.contexto = contexto;
    }

    public Cliente buscar(Long id) {
        return clientes.findByIdAndProdutorId(id, contexto.produtorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente"));
    }

    @Transactional(readOnly = true)
    public Pagina<ClienteResponse> listar(Pageable pageable) {
        return Pagina.de(clientes.findByProdutorIdOrderByNome(contexto.produtorId(), pageable), ClienteResponse::de);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obter(Long id) {
        return ClienteResponse.de(buscar(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest req) {
        Long pid = contexto.produtorId();
        String doc = documento(req);
        if (clientes.existsByProdutorIdAndCpfCnpj(pid, doc)) {
            throw new RegraDeNegocioException("Voce ja cadastrou um cliente com este documento.");
        }
        Cliente c = new Cliente(produtores.getReferenceById(pid), req.nome(), doc);
        c.setTelefone(req.telefone());
        c.setEndereco(req.endereco());
        return ClienteResponse.de(clientes.save(c));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest req) {
        Cliente c = buscar(id);
        String doc = documento(req);
        if (clientes.existsByProdutorIdAndCpfCnpjAndIdNot(c.getProdutor().getId(), doc, id)) {
            throw new RegraDeNegocioException("Voce ja cadastrou um cliente com este documento.");
        }
        c.setNome(req.nome());
        c.setCpfCnpj(doc);
        c.setTelefone(req.telefone());
        c.setEndereco(req.endereco());
        return ClienteResponse.de(c);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente c = buscar(id);
        if (faturas.existsByClienteId(id)) {
            throw new RegraDeNegocioException("Este cliente tem vendas registradas e nao pode ser excluido.");
        }
        clientes.delete(c);
    }

    private String documento(ClienteRequest req) {
        String doc = Documentos.somenteDigitos(req.cpfCnpj());
        if (!Documentos.cpfOuCnpjValido(doc)) {
            throw new RegraDeNegocioException("CPF ou CNPJ invalido.");
        }
        return doc;
    }
}
