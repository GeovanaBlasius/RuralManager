import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, EVENTO_SESSAO_EXPIRADA } from './api/client';
import { gravarSessao, lerSessao, limparSessao, type Sessao } from './api/sessao';
import type { AuthResponse, CadastroResponse } from './api/types';

export interface DadosCadastro {
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
  senha: string;
}

interface ContextoAuth {
  sessao: Sessao | null;
  entrar: (email: string, senha: string) => Promise<void>;
  /** Com verificação de e-mail ligada, a sessão só nasce depois de confirmar o código (ver confirmarEmail). */
  cadastrar: (dados: DadosCadastro) => Promise<CadastroResponse>;
  confirmarEmail: (email: string, codigo: string) => Promise<void>;
  sair: () => void;
}

const Ctx = createContext<ContextoAuth | null>(null);

function deResposta(r: AuthResponse): Sessao {
  return { token: r.token, expiraEm: Date.now() + r.expiraEmSegundos * 1000, usuario: r.usuario };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => lerSessao());

  const aplicar = useCallback((r: AuthResponse) => {
    const s = deResposta(r);
    gravarSessao(s);
    setSessao(s);
  }, []);

  const sair = useCallback(() => {
    limparSessao();
    setSessao(null);
  }, []);

  useEffect(() => {
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, sair);
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, sair);
  }, [sair]);

  const valor = useMemo<ContextoAuth>(
    () => ({
      sessao,
      sair,
      entrar: async (email, senha) => aplicar(await api.post<AuthResponse>('/api/auth/login', { email, senha })),
      cadastrar: async (d) => {
        const r = await api.post<CadastroResponse>('/api/auth/cadastro', {
          nome: d.nome,
          cpf: d.cpf,
          telefone: d.telefone || null,
          email: d.email,
          senha: d.senha,
        });
        if (r.sessao) aplicar(r.sessao);
        return r;
      },
      confirmarEmail: async (email, codigo) =>
        aplicar(await api.post<AuthResponse>('/api/auth/verificar-email', { email, codigo })),
    }),
    [sessao, sair, aplicar],
  );
  return <Ctx.Provider value={valor}>{children}</Ctx.Provider>;
}

export function useAuth(): ContextoAuth {
  const c = useContext(Ctx);
  if (!c) throw new Error('useAuth fora do AuthProvider');
  return c;
}
