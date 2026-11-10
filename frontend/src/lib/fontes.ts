import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { Cliente, Colheita, Cultura, Item, Pagina, Safra, Unidade } from '../api/types';
import { dataBr, numero } from './formato';

export interface Opcao {
  valor: string;
  rotulo: string;
  /** Dados extras usados para filtrar um select a partir de outro (ex.: lavouras de uma safra). */
  dados?: Record<string, string>;
}

export type Fonte =
  | 'safras' | 'produtos' | 'unidades' | 'culturas' | 'colheitas' | 'categorias' | 'destinos' | 'clientes';

interface Definicao<T> {
  caminho: string;
  paginada: boolean;
  mapa: (x: T) => Opcao;
}

function def<T>(d: Definicao<T>): Definicao<unknown> {
  return d as Definicao<unknown>;
}

const simples = (x: Item): Opcao => ({ valor: String(x.id), rotulo: x.nome });

const FONTES: Record<Fonte, Definicao<unknown>> = {
  safras: def<Safra>({ caminho: '/api/safras', paginada: false, mapa: (x) => ({ valor: String(x.id), rotulo: x.rotulo }) }),
  produtos: def<Item>({ caminho: '/api/produtos', paginada: false, mapa: simples }),
  categorias: def<Item>({ caminho: '/api/categorias-gasto', paginada: false, mapa: simples }),
  destinos: def<Item>({ caminho: '/api/destinos', paginada: false, mapa: simples }),
  unidades: def<Unidade>({
    caminho: '/api/unidades', paginada: false,
    mapa: (x) => ({ valor: String(x.id), rotulo: `${x.nome} (${x.sigla})` }),
  }),
  culturas: def<Cultura>({
    caminho: '/api/culturas', paginada: false,
    mapa: (x) => ({
      valor: String(x.id), rotulo: `${x.produtoNome} · ${x.variedade} (${x.safraRotulo})`,
      dados: { safraId: String(x.safraId) },
    }),
  }),
  colheitas: def<Colheita>({
    caminho: '/api/colheitas?tamanho=100', paginada: true,
    mapa: (x) => ({
      valor: String(x.id),
      rotulo: `${x.produtoNome} · ${x.variedade} · ${dataBr(x.dataColheita)} · restam ${numero(x.estoque)} ${x.unidadeSigla}`,
      dados: { culturaId: String(x.culturaId) },
    }),
  }),
  clientes: def<Cliente>({
    caminho: '/api/clientes?tamanho=100', paginada: true,
    mapa: (x) => ({ valor: String(x.id), rotulo: x.nome }),
  }),
};

export function useOpcoes(fonte: Fonte): { opcoes: Opcao[]; carregando: boolean } {
  const d = FONTES[fonte];
  const q = useQuery({
    queryKey: ['opcoes', fonte],
    staleTime: 30_000,
    queryFn: async (): Promise<Opcao[]> => {
      const r = await api.get<unknown[] | Pagina<unknown>>(d.caminho);
      const itens = Array.isArray(r) ? r : r.itens;
      return itens.map(d.mapa);
    },
  });
  return { opcoes: q.data ?? [], carregando: q.isLoading };
}
