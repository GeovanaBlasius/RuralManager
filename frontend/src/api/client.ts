import { limparSessao, lerSessao } from './sessao';

/** Vazio = mesma origem (proxy no dev, ou API atras do mesmo dominio). Defina VITE_API_URL para hospedar separado. */
const BASE: string = import.meta.env.VITE_API_URL ?? '';

export const EVENTO_SESSAO_EXPIRADA = 'rm:sessao-expirada';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public campos: Record<string, string> = {},
    public codigo?: string,
  ) {
    super(message);
  }
}

interface Problema {
  detail?: string;
  campos?: Record<string, string>;
  /** Código de erro de negócio que a tela entende, ex.: EMAIL_NAO_VERIFICADO. */
  codigo?: string;
}

export const EVENTO_API_ACORDANDO = 'rm:api-acordando';

/** Esperas entre tentativas enquanto o servidor acorda (hospedagem gratuita dorme sem uso): ~55 s no total. */
export const ESPERAS_MS = [2000, 3000, 5000, 8000, 10000, 12000, 15000];

export interface SituacaoDaResposta {
  metodo: string;
  /** fetch lancou erro (sem rede, DNS, conexao recusada). */
  falhouRede: boolean;
  /** Aparelho sem internet: esperar nao adianta. */
  semInternet: boolean;
  status?: number;
  tipoConteudo?: string | null;
}

/**
 * Decide se vale tentar de novo porque o servidor provavelmente esta acordando.
 * - Falha de rede: so em GET (um POST pode ter chegado ao servidor e repetir duplicaria o lancamento).
 * - 502/503/504 que NAO sao JSON: e o proxy da hospedagem, a requisicao nem chegou na API (seguro repetir
 *   qualquer metodo). Um 503 em JSON e resposta da propria API (ex.: e-mail nao configurado): nao repete.
 */
export function deveTentarDeNovo(s: SituacaoDaResposta): boolean {
  if (s.semInternet) return false;
  if (s.falhouRede) return s.metodo === 'GET';
  if (s.status !== undefined && [502, 503, 504].includes(s.status)) {
    return !(s.tipoConteudo ?? '').includes('json');
  }
  return false;
}

function avisarAcordando(acordando: boolean): void {
  window.dispatchEvent(new CustomEvent(EVENTO_API_ACORDANDO, { detail: acordando }));
}

const esperar = (ms: number) => new Promise<void>((r) => setTimeout(r, ms));

async function buscarComRetentativa(url: string, init: RequestInit): Promise<Response> {
  const metodo = (init.method ?? 'GET').toUpperCase();
  for (let tentativa = 0; ; tentativa++) {
    let resposta: Response | undefined;
    let falhouRede = false;
    try {
      resposta = await fetch(url, init);
    } catch {
      falhouRede = true;
    }
    const repetir = deveTentarDeNovo({
      metodo, falhouRede, semInternet: typeof navigator !== 'undefined' && navigator.onLine === false,
      status: resposta?.status, tipoConteudo: resposta?.headers.get('content-type'),
    });
    if (repetir && tentativa < ESPERAS_MS.length) {
      avisarAcordando(true);
      await esperar(ESPERAS_MS[tentativa]);
      continue;
    }
    avisarAcordando(false);
    if (!resposta) throw new ApiError(0, 'Sem conexão com o servidor. Verifique a internet e tente de novo.');
    return resposta;
  }
}

/** Cutuca a API logo ao abrir o app para ela acordar enquanto a pessoa digita. Sem CORS, sem resposta lida. */
export function acordarApi(): void {
  if (!BASE) return;
  fetch(`${BASE}/actuator/health/liveness`, { mode: 'no-cors' }).catch(() => undefined);
}

async function request<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const sessao = lerSessao();
  const cabecalhos: Record<string, string> = { Accept: 'application/json' };
  if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json';
  if (sessao) cabecalhos.Authorization = `Bearer ${sessao.token}`;

  const resposta = await buscarComRetentativa(`${BASE}${caminho}`, {
    method: metodo,
    headers: cabecalhos,
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  });

  if (resposta.status === 204) return undefined as T;

  const texto = await resposta.text();
  const json = texto ? safeJson(texto) : undefined;

  if (!resposta.ok) {
    const p = (json ?? {}) as Problema;
    // 401 fora do login = token vencido ou invalido: encerra a sessao.
    if (resposta.status === 401 && sessao) {
      limparSessao();
      window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA));
    }
    throw new ApiError(
      resposta.status,
      p.detail ?? mensagemPadrao(resposta.status),
      p.campos ?? {},
      p.codigo,
    );
  }
  return json as T;
}

function safeJson(texto: string): unknown {
  try {
    return JSON.parse(texto);
  } catch {
    return undefined;
  }
}

function mensagemPadrao(status: number): string {
  if (status === 401) return 'Sua sessão expirou. Entre novamente.';
  if (status === 403) return 'Você não tem permissão para isso.';
  if (status === 404) return 'Registro não encontrado.';
  if (status >= 500) return 'Erro no servidor. Tente novamente em instantes.';
  return 'Não foi possível concluir a operação.';
}

export const api = {
  get: <T>(caminho: string) => request<T>('GET', caminho),
  post: <T>(caminho: string, corpo?: unknown) => request<T>('POST', caminho, corpo ?? {}),
  put: <T>(caminho: string, corpo?: unknown) => request<T>('PUT', caminho, corpo ?? {}),
  del: (caminho: string) => request<void>('DELETE', caminho),
};
