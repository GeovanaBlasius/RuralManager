import type { Fonte, Opcao } from '../lib/fontes';

export type TipoCampo = 'texto' | 'textarea' | 'numero' | 'dinheiro' | 'data' | 'selecao' | 'documento';
export type Valores = Record<string, string>;

export interface Campo {
  nome: string;
  rotulo: string;
  tipo: TipoCampo;
  obrigatorio?: boolean;
  dica?: string;
  maxLength?: number;
  /** Para tipo 'selecao': de onde vem a lista. */
  fonte?: Fonte;
  /** Restringe as opcoes conforme o que ja foi preenchido (ex.: lavouras da safra escolhida). */
  filtrar?: (opcao: Opcao, valores: Valores) => boolean;
  /** So na criacao: depois de salvo o campo fica travado (o servidor tambem recusa mudar). */
  travadoAoEditar?: boolean;
  /** Campos que devem ser esvaziados quando este muda (ex.: trocar a safra invalida a lavoura escolhida). */
  limpa?: string[];
}

export interface Linha {
  titulo: string;
  subtitulo?: string;
  destaque?: string;
  detalhes?: string[];
}

export interface Recurso<T extends { id: number }> {
  chave: string;
  titulo: string;
  singular: string;
  caminho: string;
  paginado: boolean;
  textoVazio: string;
  campos: Campo[];
  linha: (r: T) => Linha;
  paraFormulario: (r: T | undefined) => Valores;
  paraPayload: (v: Valores) => unknown;
  /** Filtro de listagem por um select no topo (ex.: so gastos de uma safra). */
  filtro?: { fonte: Fonte; parametro: string; rotulo: string };
}
