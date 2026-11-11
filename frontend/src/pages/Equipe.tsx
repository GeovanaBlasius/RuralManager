import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { api, ApiError } from '../api/client';
import type { UsuarioConta } from '../api/types';
import { useAuth } from '../auth';
import { CampoForm, CampoSenha, Carregando, Folha, MensagemErro } from '../ui/componentes';

export function Equipe() {
  const { sessao } = useAuth();
  const qc = useQueryClient();
  const [novo, setNovo] = useState(false);
  const q = useQuery({ queryKey: ['equipe'], queryFn: () => api.get<UsuarioConta[]>('/api/usuarios'), enabled: sessao?.usuario.perfil === 'PRODUTOR' });
  const alternar = useMutation({
    mutationFn: (u: UsuarioConta) => api.put(`/api/usuarios/${u.id}/${u.ativo ? 'desativar' : 'ativar'}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['equipe'] }),
  });

  if (sessao?.usuario.perfil !== 'PRODUTOR') {
    return <p className="aviso">Só o produtor titular gerencia a equipe.</p>;
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Equipe</h1>
        <button className="btn" type="button" onClick={() => setNovo(true)}>+ Convidar</button>
      </div>
      <p className="aviso">Cônjuge, filho ou contador podem lançar dados na sua conta como <strong>operadores</strong>. Eles não gerenciam a equipe.</p>
      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error ?? alternar.error} />
      <div className="lista">
        {q.data?.map((u) => (
          <div key={u.id} className="item" style={{ cursor: 'default' }}>
            <div className="corpo">
              <div className="titulo">{u.nome}</div>
              <div className="sub">{u.email} · {u.perfil === 'PRODUTOR' ? 'Titular' : 'Operador'}{u.ativo ? '' : ' · desativado'}</div>
            </div>
            {u.perfil !== 'PRODUTOR' && (
              <button type="button" className="btn secundario pequeno" disabled={alternar.isPending} onClick={() => alternar.mutate(u)}>
                {u.ativo ? 'Desativar' : 'Reativar'}
              </button>
            )}
          </div>
        ))}
      </div>
      {novo && <NovoOperador aoFechar={() => setNovo(false)} />}
    </>
  );
}

function NovoOperador({ aoFechar }: { aoFechar: () => void }) {
  const qc = useQueryClient();
  const [v, setV] = useState({ nome: '', email: '', senha: '' });
  const criar = useMutation({
    mutationFn: () => api.post('/api/usuarios/operadores', v),
    onSuccess: async () => { await qc.invalidateQueries({ queryKey: ['equipe'] }); aoFechar(); },
  });
  const campos = criar.error instanceof ApiError ? criar.error.campos : {};
  const muda = (k: keyof typeof v) => (e: { target: { value: string } }) => setV((x) => ({ ...x, [k]: e.target.value }));
  const enviar = (e: FormEvent) => { e.preventDefault(); criar.mutate(); };

  return (
    <Folha titulo="Convidar operador" aoFechar={aoFechar}>
      <form onSubmit={enviar}>
        <CampoForm id="n" rotulo="Nome" erro={campos.nome}><input id="n" value={v.nome} onChange={muda('nome')} required /></CampoForm>
        <CampoForm id="e" rotulo="E-mail" erro={campos.email}><input id="e" type="email" value={v.email} onChange={muda('email')} required /></CampoForm>
        <CampoForm id="s" rotulo="Senha inicial" erro={campos.senha} dica="Mínimo de 8 caracteres. Combine a senha com a pessoa pessoalmente.">
          <CampoSenha id="s" value={v.senha} onChange={muda('senha')} autoComplete="new-password" />
        </CampoForm>
        <MensagemErro erro={criar.error} />
        <button className="btn cheio" type="submit" disabled={criar.isPending}>{criar.isPending ? 'Criando…' : 'Criar acesso'}</button>
      </form>
    </Folha>
  );
}
