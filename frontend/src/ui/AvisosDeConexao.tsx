import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useState } from 'react';
import { api, EVENTO_API_ACORDANDO } from '../api/client';
import { useAuth } from '../auth';
import {
  descartar, enviarFila, EVENTO_FILA, lerFila, type ItemFila,
} from '../lib/filaOffline';

function armazenamento(): Storage | null {
  try {
    return window.localStorage;
  } catch {
    return null;
  }
}

/**
 * Avisos de conexao no topo do app:
 *  - "servidor acordando" enquanto a API (hospedagem gratuita) liga e as chamadas sao repetidas sozinhas;
 *  - lancamentos feitos sem internet que aguardam envio, com envio automatico quando a rede volta.
 */
export function AvisosDeConexao() {
  const { sessao } = useAuth();
  const qc = useQueryClient();
  const usuarioId = sessao?.usuario.id;
  const [acordando, setAcordando] = useState(false);
  const [fila, setFila] = useState<ItemFila[]>([]);
  const [enviando, setEnviando] = useState(false);

  const recarregar = useCallback(() => {
    const s = armazenamento();
    setFila(s && usuarioId !== undefined ? lerFila(s).filter((i) => i.usuarioId === usuarioId) : []);
  }, [usuarioId]);

  const enviar = useCallback(async () => {
    const s = armazenamento();
    if (!s || usuarioId === undefined || !navigator.onLine) return;
    if (!lerFila(s).some((i) => i.usuarioId === usuarioId && !i.erro)) return;
    setEnviando(true);
    try {
      const r = await enviarFila(s, usuarioId, (caminho, corpo) => api.post(caminho, corpo));
      if (r.enviados > 0) await qc.invalidateQueries();
    } finally {
      setEnviando(false);
      recarregar();
    }
  }, [usuarioId, qc, recarregar]);

  useEffect(() => {
    const aoAcordar = (e: Event) => setAcordando((e as CustomEvent<boolean>).detail);
    window.addEventListener(EVENTO_API_ACORDANDO, aoAcordar);
    return () => window.removeEventListener(EVENTO_API_ACORDANDO, aoAcordar);
  }, []);

  useEffect(() => {
    recarregar();
    void enviar();
    const aoMudar = () => { recarregar(); void enviar(); };
    window.addEventListener(EVENTO_FILA, recarregar);
    window.addEventListener('online', aoMudar);
    const relogio = window.setInterval(() => void enviar(), 30_000);
    return () => {
      window.removeEventListener(EVENTO_FILA, recarregar);
      window.removeEventListener('online', aoMudar);
      window.clearInterval(relogio);
    };
  }, [recarregar, enviar]);

  const pendentes = fila.filter((i) => !i.erro);
  const recusados = fila.filter((i) => i.erro);

  return (
    <>
      {acordando && (
        <div className="aviso aviso-conexao" role="status">
          O servidor estava dormindo e está acordando. Isso leva até um minuto na primeira visita; vamos tentar de novo sozinhos.
        </div>
      )}
      {pendentes.length > 0 && (
        <div className="aviso aviso-conexao" role="status">
          {pendentes.length} lançamento(s) salvo(s) neste aparelho aguardando envio:{' '}
          {pendentes.map((i) => i.rotulo).join(', ')}.{' '}
          {navigator.onLine
            ? <button type="button" className="btn secundario pequeno" disabled={enviando} onClick={() => void enviar()}>
                {enviando ? 'Enviando…' : 'Enviar agora'}
              </button>
            : 'Eles serão enviados quando a internet voltar.'}
        </div>
      )}
      {recusados.map((i) => (
        <div key={i.id} className="aviso erro" role="alert">
          O servidor não aceitou &quot;{i.rotulo}&quot;: {i.erro}{' '}
          <button type="button" className="btn secundario pequeno"
            onClick={() => { const s = armazenamento(); if (s) descartar(s, i.id); }}>
            Descartar
          </button>
        </div>
      ))}
    </>
  );
}
