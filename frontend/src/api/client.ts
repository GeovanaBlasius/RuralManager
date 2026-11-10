import { limparSessao, lerSessao } from './sessao';

/** Vazio = mesma origem (proxy no dev, ou API atras do mesmo dominio). Defina VITE_API_URL para hospedar separado. */
const BASE: string = import.meta.env.VITE_API_URL ?? '';

export const EVENTO_SESSAO_EXPIRADA = 'rm:sessao-expirada';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public campos: Record<string, string> = {},
  ) {
    super(message);
  }
}

interface Problema {
  detail?: string;
  campos?: Record<string, string>;
}

async function request<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const sessao = lerSessao();
  const cabecalhos: Record<string, string> = { Accept: 'application/json' };
  if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json';
  if (sessao) cabecalhos.Authorization = `Bearer ${sessao.token}`;

  let resposta: Response;
  try {
    resposta = await fetch(`${BASE}${caminho}`, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiError(0, 'Sem conexão com o servidor. Verifique a internet e tente de novo.');
  }

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
