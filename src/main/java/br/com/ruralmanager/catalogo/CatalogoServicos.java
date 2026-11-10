package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.seguranca.ContextoUsuario;
import org.springframework.stereotype.Service;

public final class CatalogoServicos {

    private CatalogoServicos() {
    }

    @Service
    public static class ProdutoServico extends CatalogoNomeadoServico<Produto> {
        public ProdutoServico(ProdutoRepository r, ContextoUsuario c) {
            super(r, c, Produto::new, "produto");
        }
    }

    @Service
    public static class CategoriaGastoServico extends CatalogoNomeadoServico<CategoriaGasto> {
        public CategoriaGastoServico(CategoriaGastoRepository r, ContextoUsuario c) {
            super(r, c, CategoriaGasto::new, "categoria de gasto");
        }
    }

    @Service
    public static class DestinoServico extends CatalogoNomeadoServico<Destino> {
        public DestinoServico(DestinoRepository r, ContextoUsuario c) {
            super(r, c, Destino::new, "destino");
        }
    }
}
