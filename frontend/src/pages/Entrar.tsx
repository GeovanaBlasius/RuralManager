import { useState, type FormEvent } from 'react';
import { ApiError } from '../api/client';
import { useAuth } from '../auth';
import { CampoForm, MensagemErro } from '../ui/componentes';

export function Entrar() {
  const { entrar, cadastrar } = useAuth();
  const [modo, setModo] = useState<'entrar' | 'cadastro'>('entrar');
  const [v, setV] = useState({ nome: '', cpf: '', telefone: '', email: '', senha: '' });
  const [erro, setErro] = useState<unknown>(null);
  const [enviando, setEnviando] = useState(false);
  const [campos, setCampos] = useState<Record<string, string>>({});

  const muda = (k: keyof typeof v) => (e: { target: { value: string } }) => setV((x) => ({ ...x, [k]: e.target.value }));

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setCampos({});
    setEnviando(true);
    try {
      if (modo === 'entrar') await entrar(v.email, v.senha);
      else await cadastrar(v);
    } catch (err) {
      setErro(err);
      if (err instanceof ApiError) setCampos(err.campos);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login">
      <div className="caixa">
        <div className="marca-grande">
          <img src="/icon-192.png" alt="" />
          <div>
            <h1>RuralManager</h1>
            <p style={{ color: 'var(--texto-suave)' }}>Safras, custos e vendas da sua cultura.</p>
          </div>
        </div>
        <form className="cartao" onSubmit={enviar}>
          <h2 style={{ marginBottom: 14 }}>{modo === 'entrar' ? 'Entrar' : 'Criar conta'}</h2>
          {modo === 'cadastro' && (
            <>
              <CampoForm id="nome" rotulo="Seu nome" erro={campos.nome}>
                <input id="nome" value={v.nome} onChange={muda('nome')} autoComplete="name" required />
              </CampoForm>
              <CampoForm id="cpf" rotulo="CPF" erro={campos.cpf} dica="Identifica você como produtor. Só os números.">
                <input id="cpf" inputMode="numeric" value={v.cpf} onChange={muda('cpf')} maxLength={14} required />
              </CampoForm>
              <CampoForm id="tel" rotulo="Telefone (opcional)" erro={campos.telefone}>
                <input id="tel" inputMode="tel" value={v.telefone} onChange={muda('telefone')} autoComplete="tel" />
              </CampoForm>
            </>
          )}
          <CampoForm id="email" rotulo="E-mail" erro={campos.email}>
            <input id="email" type="email" value={v.email} onChange={muda('email')} autoComplete="email" required />
          </CampoForm>
          <CampoForm id="senha" rotulo="Senha" erro={campos.senha} dica={modo === 'cadastro' ? 'Mínimo de 8 caracteres.' : undefined}>
            <input id="senha" type="password" value={v.senha} onChange={muda('senha')}
              autoComplete={modo === 'entrar' ? 'current-password' : 'new-password'} required />
          </CampoForm>
          <MensagemErro erro={erro} />
          <button className="btn cheio" type="submit" disabled={enviando}>
            {enviando ? 'Aguarde…' : modo === 'entrar' ? 'Entrar' : 'Criar conta'}
          </button>
        </form>
        <div className="trocar">
          <button type="button" onClick={() => { setModo(modo === 'entrar' ? 'cadastro' : 'entrar'); setErro(null); setCampos({}); }}>
            {modo === 'entrar' ? 'Primeiro acesso? Criar conta' : 'Já tenho conta'}
          </button>
        </div>
      </div>
    </div>
  );
}
