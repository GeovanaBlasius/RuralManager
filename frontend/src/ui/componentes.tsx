import { useEffect, useState, type ChangeEvent, type ReactNode } from 'react';
import { ApiError } from '../api/client';

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return <div className="carregando" role="status">{texto}</div>;
}

export function Vazio({ children }: { children: ReactNode }) {
  return <div className="vazio">{children}</div>;
}

export function MensagemErro({ erro }: { erro: unknown }) {
  if (!erro) return null;
  const msg = erro instanceof ApiError ? erro.message : 'Algo deu errado. Tente novamente.';
  return <div className="aviso erro" role="alert">{msg}</div>;
}

interface CampoProps {
  id: string;
  rotulo: string;
  erro?: string;
  dica?: string;
  children: ReactNode;
}

export function CampoForm({ id, rotulo, erro, dica, children }: CampoProps) {
  return (
    <div className={`campo${erro ? ' invalido' : ''}`}>
      <label htmlFor={id}>{rotulo}</label>
      {children}
      {dica && !erro && <span className="dica">{dica}</span>}
      {erro && <span className="erro" role="alert">{erro}</span>}
    </div>
  );
}

/** Campo de senha com botao de olho para mostrar/esconder o texto digitado. */
export function CampoSenha({ id, value, onChange, autoComplete }: {
  id: string; value: string; onChange: (e: ChangeEvent<HTMLInputElement>) => void; autoComplete?: string;
}) {
  const [visivel, setVisivel] = useState(false);
  return (
    <div className="campo-senha">
      <input
        id={id}
        type={visivel ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        required
      />
      <button
        type="button"
        className="alternar-senha"
        onClick={() => setVisivel((v) => !v)}
        aria-label={visivel ? 'Esconder senha' : 'Mostrar senha'}
        aria-pressed={visivel}
      >
        {visivel ? Icones.olhoFechado : Icones.olho}
      </button>
    </div>
  );
}

/** Folha que sobe do rodape no celular e vira janela no desktop. Fecha com Esc. */
export function Folha({ titulo, aoFechar, children }: { titulo: string; aoFechar: () => void; children: ReactNode }) {
  useEffect(() => {
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && aoFechar();
    window.addEventListener('keydown', tecla);
    const antes = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      window.removeEventListener('keydown', tecla);
      document.body.style.overflow = antes;
    };
  }, [aoFechar]);

  return (
    <div className="folha-fundo" onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div className="folha" role="dialog" aria-modal="true" aria-label={titulo}>
        <div className="folha-topo">
          <h2>{titulo}</h2>
          <button type="button" onClick={aoFechar} aria-label="Fechar">×</button>
        </div>
        {children}
      </div>
    </div>
  );
}

const CHAVE_AVISO_PRIVACIDADE = 'rm:aviso-privacidade-visto';

/** Aviso de dados pessoais/armazenamento local, mostrado uma vez (LGPD). */
export function AvisoPrivacidade() {
  const [visivel, setVisivel] = useState(() => {
    try {
      return localStorage.getItem(CHAVE_AVISO_PRIVACIDADE) !== '1';
    } catch {
      return true;
    }
  });
  const [detalhe, setDetalhe] = useState(false);

  function fechar() {
    setVisivel(false);
    try {
      localStorage.setItem(CHAVE_AVISO_PRIVACIDADE, '1');
    } catch {
      // armazenamento indisponivel (navegacao privada etc.): so nao lembra na proxima visita
    }
  }

  if (!visivel) return null;

  return (
    <>
      <div className="aviso-privacidade" role="region" aria-label="Aviso de privacidade">
        <p>
          Guardamos sua sessão de login no armazenamento do navegador e tratamos os dados que você
          cadastra (nome, CPF, e-mail e a sua produção) para o sistema funcionar, conforme a LGPD.
        </p>
        <div className="aviso-privacidade-acoes">
          <button type="button" className="btn secundario pequeno" onClick={() => setDetalhe(true)}>Saiba mais</button>
          <button type="button" className="btn pequeno" onClick={fechar}>Entendi</button>
        </div>
      </div>
      {detalhe && (
        <Folha titulo="Privacidade e dados" aoFechar={() => setDetalhe(false)}>
          <p>
            Este aplicativo guarda sua sessão de login no armazenamento local do navegador (localStorage),
            não em cookies de terceiros, e não usa rastreadores de publicidade ou analytics externos.
          </p>
          <p style={{ marginTop: 12 }}>
            Para funcionar, coletamos e guardamos os dados que você cadastra: nome, CPF, e-mail, telefone
            (opcional) e as informações de safras, culturas, colheitas, gastos e vendas da sua produção.
            Esses dados ficam vinculados só à sua conta — nenhum outro produtor tem acesso a eles.
          </p>
          <p style={{ marginTop: 12 }}>
            Para pedir a exclusão da sua conta e dos seus dados, relate um chamado em <strong>Suporte</strong>,
            depois de entrar no sistema.
          </p>
          <button type="button" className="btn cheio" style={{ marginTop: 16 }} onClick={fechar}>Entendi</button>
        </Folha>
      )}
    </>
  );
}

export function Metrica({ rotulo, valor, tom }: { rotulo: string; valor: string; tom?: 'positivo' | 'negativo' }) {
  return (
    <div className="metrica">
      <div className="rotulo">{rotulo}</div>
      <div className={`valor${tom ? ` ${tom}` : ''}`}>{valor}</div>
    </div>
  );
}

export function tomDoValor(n: number): 'positivo' | 'negativo' | undefined {
  return n > 0 ? 'positivo' : n < 0 ? 'negativo' : undefined;
}

export const Icones = {
  inicio: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 11l9-8 9 8" /><path d="M5 10v10h14V10" /></svg>,
  gasto: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="6" width="18" height="13" rx="2" /><path d="M3 10h18M7 15h3" /></svg>,
  colheita: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M2 15l4-7h8a3 3 0 013 3v4" /><path d="M8 11l-1.5 3.5M10.5 9.5L9 13M13 8.5l-1.5 4" /><path d="M2 15h19" /><circle cx="7" cy="17.5" r="2" /><circle cx="16" cy="17.5" r="2.5" /></svg>,
  cultura: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 21V9" /><path d="M12 12C12 8 9 6 6 6c0 4 2 6 6 6z" /><path d="M12 9c0-3 2-5 5-5 0 3-2 5-5 5z" /></svg>,
  venda: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M6 6h15l-2 9H8L6 6zm0 0L5 3H2" /><circle cx="9" cy="20" r="1.2" /><circle cx="17" cy="20" r="1.2" /></svg>,
  mais: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="5" cy="12" r="1.3" /><circle cx="12" cy="12" r="1.3" /><circle cx="19" cy="12" r="1.3" /></svg>,
  safra: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M3 10h18M8 3v4M16 3v4" /></svg>,
  estoque: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 8l9-5 9 5v8l-9 5-9-5V8z" /><path d="M3 8l9 5 9-5M12 13v8" /></svg>,
  pessoas: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="9" cy="8" r="3.5" /><path d="M2.5 20c0-3.5 3-6 6.5-6s6.5 2.5 6.5 6M17 11a3 3 0 100-6M21.5 19c0-2.5-1.5-4.5-4-5" /></svg>,
  lista: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M8 6h13M8 12h13M8 18h13M3.5 6h.01M3.5 12h.01M3.5 18h.01" /></svg>,
  suporte: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12c0 4.4-4 8-9 8-1.1 0-2.2-.2-3.1-.5L3 21l1.6-4.2C3.6 15.5 3 13.8 3 12c0-4.4 4-8 9-8s9 3.6 9 8z" /><path d="M12 13v-.3c0-.9.6-1.3 1.2-1.7.6-.4 1.2-.9 1.2-1.7 0-1-.9-1.8-2.4-1.8s-2.4.8-2.4 1.8" /><circle cx="12" cy="16" r="0.1" fill="currentColor" /></svg>,
  olho: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12z" /><circle cx="12" cy="12" r="3" /></svg>,
  olhoFechado: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 3l18 18" /><path d="M10.6 5.2C11 5.1 11.5 5 12 5c7 0 10.5 7 10.5 7a17.6 17.6 0 01-3.2 4.1M6.6 6.6C3.4 8.6 1.5 12 1.5 12s3.5 7 10.5 7c1.4 0 2.7-.3 3.8-.7" /><path d="M9.9 9.9a3 3 0 104.2 4.2" /></svg>,
};
