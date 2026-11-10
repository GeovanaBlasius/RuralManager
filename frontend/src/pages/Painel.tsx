import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { Painel as PainelDados } from '../api/types';
import { useAuth } from '../auth';
import { moeda, numero } from '../lib/formato';
import { Carregando, Icones, MensagemErro, Metrica, tomDoValor, Vazio } from '../ui/componentes';

export function Painel() {
  const { sessao } = useAuth();
  const q = useQuery({ queryKey: ['painel'], queryFn: () => api.get<PainelDados>('/api/relatorios/painel') });
  const nome = sessao?.usuario.nome.split(' ')[0] ?? '';

  return (
    <>
      <div className="cabecalho-pagina"><h1>Olá, {nome}</h1></div>

      <div className="atalhos">
        <Link to="/gastos?novo=1">{Icones.gasto}Lançar gasto</Link>
        <Link to="/colheitas?novo=1">{Icones.colheita}Registrar colheita</Link>
        <Link to="/vendas?novo=1">{Icones.venda}Registrar venda</Link>
      </div>

      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error} />

      {q.data && (
        <>
          <div className="grade-metricas">
            <Metrica rotulo="Receita total" valor={moeda(q.data.receitaTotal)} />
            <Metrica rotulo="Custo total" valor={moeda(q.data.custoTotal)} />
            <Metrica rotulo="Resultado" valor={moeda(q.data.lucroTotal)} tom={tomDoValor(q.data.lucroTotal)} />
            <Metrica rotulo="Safras" valor={String(q.data.totalSafras)} />
          </div>

          <h2 className="secao">Suas safras</h2>
          {q.data.safras.length === 0 && (
            <Vazio>Comece cadastrando sua primeira safra em <Link to="/safras?novo=1">Safras</Link>.</Vazio>
          )}
          <div className="lista">
            {q.data.safras.map((s) => (
              <Link key={s.safraId} to={`/safras/${s.safraId}/relatorio`} className="item" style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="corpo">
                  <div className="titulo">Safra {s.rotulo}</div>
                  <div className="detalhes">
                    <span>{s.culturas} lavoura(s)</span><span>{numero(s.areaHa)} ha</span>
                    <span>Custo {moeda(s.custo)}</span><span>Receita {moeda(s.receita)}</span>
                  </div>
                </div>
                <div className={`destaque ${s.lucro >= 0 ? 'positivo' : 'negativo'}`}>{moeda(s.lucro)}</div>
              </Link>
            ))}
          </div>
        </>
      )}
    </>
  );
}
