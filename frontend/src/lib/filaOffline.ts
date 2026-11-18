import { ApiError } from '../api/client';

/**
 * Fila de lancamentos feitos sem internet. So guarda criacoes (POST) de registros que nao dependem de
 * outro registro ainda nao enviado (gasto, colheita, saida); o envio acontece quando a conexao volta.
 * Cada item guarda o usuario que o criou: ninguem envia lancamento de outra conta.
 */
export interface ItemFila {
  id: string;
  usuarioId: number;
  caminho: string;
  corpo: unknown;
  /** Texto curto para a pessoa reconhecer o item (ex.: "Gasto: Adubo"). */
  rotulo: string;
  criadoEm: string;
  /** Preenchido quando o servidor recusou (regra de negocio): precisa de atencao da pessoa. */
  erro?: string;
}

export interface Armazenamento {
  getItem(chave: string): string | null;
  setItem(chave: string, valor: string): void;
}

export const CHAVE_FILA = 'rm:fila-offline';
export const EVENTO_FILA = 'rm:fila-mudou';

/** Recursos que podem entrar na fila: nao dependem de id criado offline. */
export const RECURSOS_NA_FILA = new Set(['gastos', 'colheitas', 'saidas']);

export function lerFila(s: Armazenamento): ItemFila[] {
  try {
    const bruto = s.getItem(CHAVE_FILA);
    const lista: unknown = bruto ? JSON.parse(bruto) : [];
    return Array.isArray(lista) ? (lista as ItemFila[]) : [];
  } catch {
    return [];
  }
}

function gravar(s: Armazenamento, fila: ItemFila[]): void {
  s.setItem(CHAVE_FILA, JSON.stringify(fila));
  if (typeof window !== 'undefined') window.dispatchEvent(new Event(EVENTO_FILA));
}

export function enfileirar(s: Armazenamento, item: Omit<ItemFila, 'id' | 'criadoEm'>): ItemFila {
  const novo: ItemFila = {
    ...item,
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    criadoEm: new Date().toISOString(),
  };
  gravar(s, [...lerFila(s), novo]);
  return novo;
}

export function descartar(s: Armazenamento, id: string): void {
  gravar(s, lerFila(s).filter((i) => i.id !== id));
}

/**
 * Erro que nao melhora tentando de novo (dado recusado). 401/408/429 sao temporarios; o resto de 4xx e definitivo.
 */
export function erroDefinitivo(e: unknown): e is ApiError {
  return e instanceof ApiError && e.status >= 400 && e.status < 500 && ![401, 408, 429].includes(e.status);
}

export interface ResumoEnvio {
  enviados: number;
  recusados: number;
  /** true se parou no meio por falta de conexao ou erro do servidor (tenta de novo depois). */
  interrompido: boolean;
}

/** Envia, em ordem, os itens pendentes deste usuario. Para no primeiro problema temporario para manter a ordem. */
export async function enviarFila(
  s: Armazenamento,
  usuarioId: number,
  postar: (caminho: string, corpo: unknown) => Promise<unknown>,
): Promise<ResumoEnvio> {
  const resumo: ResumoEnvio = { enviados: 0, recusados: 0, interrompido: false };
  for (const item of lerFila(s)) {
    if (item.usuarioId !== usuarioId || item.erro) continue;
    try {
      await postar(item.caminho, item.corpo);
      gravar(s, lerFila(s).filter((i) => i.id !== item.id));
      resumo.enviados++;
    } catch (e) {
      if (erroDefinitivo(e)) {
        gravar(s, lerFila(s).map((i) => (i.id === item.id ? { ...i, erro: e.message } : i)));
        resumo.recusados++;
      } else {
        resumo.interrompido = true;
        break;
      }
    }
  }
  return resumo;
}
