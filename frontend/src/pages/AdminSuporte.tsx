import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { api } from '../api/client';
import type { Chamado, Pagina } from '../api/types';
import { useAuth } from '../auth';
import { dataBr } from '../lib/formato';
import { Carregando, MensagemErro } from '../ui/componentes';

export function AdminSuporte() {
  const { sessao } = useAuth();
  const qc = useQueryClient();
  const [pagina, setPagina] = useState(0);

  const q = useQuery({
    queryKey: ['admin-suporte', pagina],
    queryFn: () => api.get<Pagina<Chamado>>(`/api/suporte/todos?pagina=${pagina}&tamanho=20`),
    enabled: sessao?.usuario.perfil === 'ADMIN',
  });

  const alternar = useMutation({
    mutationFn: (c: Chamado) => api.put(`/api/suporte/${c.id}/${c.status === 'ABERTO' ? 'resolver' : 'reabrir'}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-suporte'] }),
  });

  if (sessao?.usuario.perfil !== 'ADMIN') {
    return <p className="aviso">Só a equipe (administradores) vê os chamados de todos os usuários.</p>;
  }

  const itens = q.data?.itens ?? [];
  const abertos = itens.filter((c) => c.status === 'ABERTO').length;

  return (
    <>
      <div className="cabecalho-pagina"><h1>Painel de suporte</h1></div>
      <p className="aviso">
        {q.data ? `${abertos} aberto(s) nesta página de ${q.data.total} chamado(s) no total.` : 'Chamados que os usuários relataram, mais recentes primeiro.'}
      </p>

      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error ?? alternar.error} />
      {!q.isLoading && !q.error && itens.length === 0 && <p className="aviso">Nenhum chamado ainda.</p>}

      <div className="lista">
        {itens.map((c) => (
          <div key={c.id} className="item" style={{ cursor: 'default', flexDirection: 'column', alignItems: 'stretch', gap: 6 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
              <div className="titulo">{c.titulo}</div>
              <strong className={c.status === 'ABERTO' ? 'negativo' : undefined} style={{ whiteSpace: 'nowrap' }}>
                {c.status === 'ABERTO' ? 'Aberto' : 'Resolvido'}
              </strong>
            </div>
            <div className="sub">{c.nomeUsuario} · {c.emailUsuario} · {dataBr(c.criadoEm)}</div>
            <p style={{ whiteSpace: 'pre-wrap' }}>{c.descricao}</p>
            <button type="button" className="btn secundario pequeno" style={{ alignSelf: 'flex-end' }}
              disabled={alternar.isPending} onClick={() => alternar.mutate(c)}>
              {c.status === 'ABERTO' ? 'Marcar resolvido' : 'Reabrir'}
            </button>
          </div>
        ))}
      </div>

      {(q.data?.totalPaginas ?? 1) > 1 && (
        <div className="paginacao">
          <button className="btn secundario pequeno" type="button" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>
            ← Anteriores
          </button>
          <span>Página {pagina + 1} de {q.data?.totalPaginas}</span>
          <button className="btn secundario pequeno" type="button"
            disabled={pagina + 1 >= (q.data?.totalPaginas ?? 1)} onClick={() => setPagina(pagina + 1)}>
            Próximas →
          </button>
        </div>
      )}
    </>
  );
}
