import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Pagina } from '../api/types';
import { DEFINICOES_BUSCA, type FonteBuscavel } from '../lib/fontes';

const POR_PAGINA = 20;

function useAtrasado<T>(valor: T, ms: number): T {
  const [atual, setAtual] = useState(valor);
  useEffect(() => {
    const t = setTimeout(() => setAtual(valor), ms);
    return () => clearTimeout(t);
  }, [valor, ms]);
  return atual;
}

interface Props {
  id: string;
  fonte: FonteBuscavel;
  valor: string;
  aoMudar: (valor: string) => void;
  desabilitado?: boolean;
  /** Filtros extras da listagem (ex.: so as colheitas de uma cultura). null = ainda nao da para listar. */
  parametros?: Record<string, string> | null;
  textoBloqueado?: string;
  obrigatorio?: boolean;
}

/**
 * Seletor para listas que podem crescer (colheitas, clientes): busca pelo nome no servidor e carrega
 * de 20 em 20, em vez de baixar so os primeiros 100. O item ja escolhido continua aparecendo mesmo
 * que fique fora do resultado da busca.
 */
export function SelecaoBusca({ id, fonte, valor, aoMudar, desabilitado, parametros, textoBloqueado, obrigatorio }: Props) {
  const def = DEFINICOES_BUSCA[fonte];
  const [texto, setTexto] = useState('');
  const busca = useAtrasado(texto.trim(), 300);
  const bloqueado = parametros === null;

  const q = useInfiniteQuery({
    queryKey: ['busca', fonte, busca, parametros ?? {}],
    enabled: !bloqueado,
    staleTime: 15_000,
    initialPageParam: 0,
    queryFn: ({ pageParam }) => {
      const p = new URLSearchParams({ pagina: String(pageParam), tamanho: String(POR_PAGINA) });
      if (busca) p.set('busca', busca);
      for (const [k, v] of Object.entries(parametros ?? {})) p.set(k, v);
      return api.get<Pagina<unknown>>(`${def.caminho}?${p.toString()}`);
    },
    getNextPageParam: (ultima) => (ultima.pagina + 1 < ultima.totalPaginas ? ultima.pagina + 1 : undefined),
  });

  const opcoes = q.data?.pages.flatMap((pg) => pg.itens.map(def.mapa)) ?? [];
  const escolhidoForaDaLista = !!valor && !opcoes.some((o) => o.valor === valor);
  const escolhido = useQuery({
    queryKey: ['busca-um', fonte, valor],
    enabled: escolhidoForaDaLista,
    staleTime: 60_000,
    queryFn: async () => def.mapa(await api.get<unknown>(`${def.caminho}/${valor}`)),
  });
  const lista = escolhidoForaDaLista && escolhido.data ? [escolhido.data, ...opcoes] : opcoes;
  const total = q.data?.pages[0]?.total ?? 0;

  return (
    <div className="selecao-busca">
      <input
        type="search"
        aria-label="Buscar pelo nome"
        placeholder="Buscar pelo nome…"
        value={texto}
        disabled={desabilitado || bloqueado}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => { if (e.key === 'Enter') e.preventDefault(); }} // Enter nao envia o formulario
      />
      <select id={id} value={valor} disabled={desabilitado || bloqueado} onChange={(e) => aoMudar(e.target.value)}>
        <option value="">
          {bloqueado ? (textoBloqueado ?? 'Preencha o campo anterior') : q.isLoading ? 'Carregando…' : obrigatorio ? 'Selecione…' : '— nenhum —'}
        </option>
        {lista.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
      </select>
      {!bloqueado && !q.isLoading && (
        <div className="selecao-busca-rodape">
          <span>{busca ? `${total} encontrado(s)` : `${total} no total`}</span>
          {q.hasNextPage && (
            <button type="button" className="btn secundario pequeno" disabled={q.isFetchingNextPage}
              onClick={() => void q.fetchNextPage()}>
              {q.isFetchingNextPage ? 'Carregando…' : 'Mostrar mais'}
            </button>
          )}
        </div>
      )}
    </div>
  );
}
