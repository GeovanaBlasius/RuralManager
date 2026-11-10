import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import type { RelatorioSafra as Dados } from '../api/types';
import { moeda, numero } from '../lib/formato';
import { Carregando, MensagemErro, Metrica, tomDoValor } from '../ui/componentes';

export function RelatorioSafra() {
  const { id } = useParams();
  const q = useQuery({ queryKey: ['relatorio-safra', id], queryFn: () => api.get<Dados>(`/api/relatorios/safras/${id}`) });
  const r = q.data;

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Safra {r?.rotulo ?? ''}</h1>
        <Link className="btn secundario pequeno" to="/">← Início</Link>
      </div>
      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error} />
      {r && (
        <>
          <div className="grade-metricas">
            <Metrica rotulo="Receita" valor={moeda(r.receitaTotal)} />
            <Metrica rotulo="Custo total" valor={moeda(r.custoTotal)} />
            <Metrica rotulo="Resultado" valor={moeda(r.lucro)} tom={tomDoValor(r.lucro)} />
            <Metrica rotulo="Margem" valor={`${numero(r.margemPercentual, 1)}%`} tom={tomDoValor(r.margemPercentual)} />
          </div>
          <div className="aviso">
            Custos gerais da safra (sem cultura): <strong>{moeda(r.custoGeral)}</strong>. Eles entram no custo total e no resultado,
            mas não são divididos entre as culturas abaixo.
          </div>

          <h2 className="secao">Por cultura</h2>
          <div className="lista">
            {r.culturas.map((c) => (
              <div key={c.culturaId} className="cartao">
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                  <strong>{c.produto} · {c.variedade}</strong>
                  <span className={c.lucro >= 0 ? 'positivo' : 'negativo'}><strong>{moeda(c.lucro)}</strong></span>
                </div>
                <div className="linha-par"><span>Área</span><span>{numero(c.areaHa)} ha</span></div>
                <div className="linha-par">
                  <span>Produtividade</span>
                  <span>{c.produtividadeKgHa === null ? 'sem peso definido' : `${numero(c.produtividadeKgHa)} kg/ha`}</span>
                </div>
                <div className="linha-par"><span>Custo direto</span><span>{moeda(c.custo)} ({moeda(c.custoPorHa)}/ha)</span></div>
                <div className="linha-par"><span>Receita</span><span>{moeda(c.receita)} ({moeda(c.receitaPorHa)}/ha)</span></div>
                <div className="linha-par"><span>Retorno sobre o custo</span><span>{c.roiPercentual === null ? '—' : `${numero(c.roiPercentual, 1)}%`}</span></div>
              </div>
            ))}
          </div>

          <h2 className="secao">Gastos por categoria</h2>
          <div className="cartao">
            {r.gastosPorCategoria.length === 0 && <p>Nenhum gasto lançado.</p>}
            {r.gastosPorCategoria.map((g) => (
              <div key={g.categoria} style={{ marginBottom: 10 }}>
                <div className="linha-par"><span>{g.categoria}</span><span>{moeda(g.total)} · {numero(g.percentual, 1)}%</span></div>
                <div className="barra"><span style={{ width: `${Math.min(100, g.percentual)}%` }} /></div>
              </div>
            ))}
          </div>
        </>
      )}
    </>
  );
}
