import { useState } from 'react';
import { useAuth } from '../auth';
import { CrudPagina } from '../resources/CrudPagina';
import { catalogoCategorias, catalogoDestinos, catalogoProdutos, catalogoUnidades } from '../resources/definicoes';

const ABAS = [
  { chave: 'produtos', rotulo: 'Produtos' },
  { chave: 'unidades', rotulo: 'Unidades' },
  { chave: 'categorias', rotulo: 'Categorias de gasto' },
  { chave: 'destinos', rotulo: 'Destinos' },
] as const;

export function Cadastros() {
  const { sessao } = useAuth();
  const [aba, setAba] = useState<(typeof ABAS)[number]['chave']>('produtos');
  const admin = sessao?.usuario.perfil === 'ADMIN';
  // Qualquer produtor pode cadastrar um item que falte; so o administrador altera ou apaga,
  // porque o catalogo e compartilhado entre todos os produtores.
  const podeCriar = sessao?.usuario.perfil !== 'OPERADOR';

  return (
    <>
      <div className="abas" role="tablist">
        {ABAS.map((a) => (
          <button key={a.chave} type="button" role="tab" aria-selected={aba === a.chave}
            className={aba === a.chave ? 'ativa' : undefined} onClick={() => setAba(a.chave)}>
            {a.rotulo}
          </button>
        ))}
      </div>
      {!admin && <p className="aviso">Estes cadastros são compartilhados por todos os produtores. Você pode incluir o que faltar; alterações e exclusões ficam com o administrador.</p>}
      {aba === 'produtos' && <CrudPagina key="p" recurso={catalogoProdutos} podeEditar={admin} podeCriar={podeCriar} />}
      {aba === 'unidades' && <CrudPagina key="u" recurso={catalogoUnidades} podeEditar={admin} podeCriar={podeCriar} />}
      {aba === 'categorias' && <CrudPagina key="c" recurso={catalogoCategorias} podeEditar={admin} podeCriar={podeCriar} />}
      {aba === 'destinos' && <CrudPagina key="d" recurso={catalogoDestinos} podeEditar={admin} podeCriar={podeCriar} />}
    </>
  );
}
