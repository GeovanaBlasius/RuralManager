import { useEffect, type ReactNode } from 'react';
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
  colheita: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 21V9" /><path d="M12 12C12 8 9 6 6 6c0 4 2 6 6 6z" /><path d="M12 9c0-3 2-5 5-5 0 3-2 5-5 5z" /></svg>,
  venda: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M6 6h15l-2 9H8L6 6zm0 0L5 3H2" /><circle cx="9" cy="20" r="1.2" /><circle cx="17" cy="20" r="1.2" /></svg>,
  mais: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="5" cy="12" r="1.3" /><circle cx="12" cy="12" r="1.3" /><circle cx="19" cy="12" r="1.3" /></svg>,
  safra: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M3 10h18M8 3v4M16 3v4" /></svg>,
  estoque: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 8l9-5 9 5v8l-9 5-9-5V8z" /><path d="M3 8l9 5 9-5M12 13v8" /></svg>,
  pessoas: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="9" cy="8" r="3.5" /><path d="M2.5 20c0-3.5 3-6 6.5-6s6.5 2.5 6.5 6M17 11a3 3 0 100-6M21.5 19c0-2.5-1.5-4.5-4-5" /></svg>,
  lista: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M8 6h13M8 12h13M8 18h13M3.5 6h.01M3.5 12h.01M3.5 18h.01" /></svg>,
  suporte: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12c0 4.4-4 8-9 8-1.1 0-2.2-.2-3.1-.5L3 21l1.6-4.2C3.6 15.5 3 13.8 3 12c0-4.4 4-8 9-8s9 3.6 9 8z" /><path d="M12 13v-.3c0-.9.6-1.3 1.2-1.7.6-.4 1.2-.9 1.2-1.7 0-1-.9-1.8-2.4-1.8s-2.4.8-2.4 1.8" /><circle cx="12" cy="16" r="0.1" fill="currentColor" /></svg>,
};
