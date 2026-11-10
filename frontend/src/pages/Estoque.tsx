import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { EstoqueColheita } from '../api/types';
import { dataBr, numero } from '../lib/formato';
import { Carregando, MensagemErro, Vazio } from '../ui/componentes';

export function Estoque() {
  const q = useQuery({ queryKey: ['estoque'], queryFn: () => api.get<EstoqueColheita[]>('/api/relatorios/estoque') });
  return (
    <>
      <div className="cabecalho-pagina"><h1>Estoque</h1></div>
      <p className="aviso">Estoque = colhido − vendido − outras saídas, por colheita.</p>
      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error} />
      {q.data && q.data.length === 0 && <Vazio>Nenhuma colheita registrada.</Vazio>}
      <div className="lista">
        {q.data?.map((e) => {
          const usado = e.colhido > 0 ? ((e.vendido + e.destinado) / e.colhido) * 100 : 0;
          return (
            <div key={e.colheitaId} className="cartao">
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                <strong>{e.produto} · {e.variedade}</strong>
                <strong>{numero(e.estoque)} {e.unidade}</strong>
              </div>
              <div className="sub" style={{ color: 'var(--texto-suave)', fontSize: '0.85rem' }}>
                Safra {e.safra} · colhido em {dataBr(e.dataColheita)}
              </div>
              <div className="barra"><span style={{ width: `${Math.min(100, usado)}%` }} /></div>
              <div className="detalhes" style={{ display: 'flex', gap: 12, fontSize: '0.82rem', color: 'var(--texto-suave)', marginTop: 4 }}>
                <span>Colhido {numero(e.colhido)}</span><span>Vendido {numero(e.vendido)}</span><span>Outras saídas {numero(e.destinado)}</span>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
