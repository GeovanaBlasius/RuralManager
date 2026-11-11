import { useState, type FormEvent } from 'react';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth';
import { AvisoPrivacidade, CampoForm, CampoSenha, MensagemErro } from '../ui/componentes';

type Modo = 'entrar' | 'cadastro' | 'esqueci' | 'redefinir';

export function Entrar() {
  const { entrar, cadastrar } = useAuth();
  const [modo, setModo] = useState<Modo>('entrar');
  const [v, setV] = useState({ nome: '', cpf: '', telefone: '', email: '', senha: '', codigo: '', novaSenha: '' });
  const [erro, setErro] = useState<unknown>(null);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [campos, setCampos] = useState<Record<string, string>>({});

  const muda = (k: keyof typeof v) => (e: { target: { value: string } }) => setV((x) => ({ ...x, [k]: e.target.value }));

  function trocarModo(novo: Modo) {
    setModo(novo);
    setErro(null);
    setMensagem(null);
    setCampos({});
  }

  async function enviar(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setMensagem(null);
    setCampos({});
    setEnviando(true);
    try {
      if (modo === 'entrar') {
        await entrar(v.email, v.senha);
      } else if (modo === 'cadastro') {
        await cadastrar(v);
      } else if (modo === 'esqueci') {
        await api.post('/api/auth/esqueci-senha', { email: v.email });
        setModo('redefinir');
        setMensagem('Se esse e-mail estiver cadastrado, enviamos um código de 6 dígitos. Ele vale por 15 minutos.');
      } else {
        await api.post('/api/auth/redefinir-senha', { email: v.email, codigo: v.codigo, novaSenha: v.novaSenha });
        trocarModo('entrar');
        setMensagem('Senha redefinida. Entre com a nova senha.');
      }
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
          <h2 style={{ marginBottom: 14 }}>
            {modo === 'entrar' ? 'Entrar' : modo === 'cadastro' ? 'Criar conta'
              : modo === 'esqueci' ? 'Recuperar senha' : 'Redefinir senha'}
          </h2>
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
          {(modo === 'entrar' || modo === 'cadastro') && (
            <CampoForm id="senha" rotulo="Senha" erro={campos.senha} dica={modo === 'cadastro' ? 'Mínimo de 8 caracteres.' : undefined}>
              <CampoSenha id="senha" value={v.senha} onChange={muda('senha')}
                autoComplete={modo === 'entrar' ? 'current-password' : 'new-password'} />
            </CampoForm>
          )}
          {modo === 'redefinir' && (
            <>
              <CampoForm id="codigo" rotulo="Código recebido por e-mail" erro={campos.codigo}>
                <input id="codigo" inputMode="numeric" value={v.codigo} onChange={muda('codigo')} maxLength={6} required />
              </CampoForm>
              <CampoForm id="novaSenha" rotulo="Nova senha" erro={campos.novaSenha} dica="Mínimo de 8 caracteres.">
                <CampoSenha id="novaSenha" value={v.novaSenha} onChange={muda('novaSenha')} autoComplete="new-password" />
              </CampoForm>
            </>
          )}
          {mensagem && <p style={{ color: 'var(--verde)', marginBottom: 10 }}>{mensagem}</p>}
          <MensagemErro erro={erro} />
          <button className="btn cheio" type="submit" disabled={enviando}>
            {enviando ? 'Aguarde…' : modo === 'entrar' ? 'Entrar' : modo === 'cadastro' ? 'Criar conta'
              : modo === 'esqueci' ? 'Enviar código' : 'Redefinir senha'}
          </button>
        </form>
        <div className="trocar">
          {modo === 'entrar' && (
            <>
              <button type="button" onClick={() => trocarModo('cadastro')}>Primeiro acesso? Criar conta</button>
              <button type="button" onClick={() => trocarModo('esqueci')}>Esqueceu a senha?</button>
            </>
          )}
          {modo === 'cadastro' && <button type="button" onClick={() => trocarModo('entrar')}>Já tenho conta</button>}
          {modo === 'esqueci' && <button type="button" onClick={() => trocarModo('entrar')}>Já lembrei, voltar a entrar</button>}
          {modo === 'redefinir' && (
            <>
              <button type="button" onClick={() => trocarModo('esqueci')}>Pedir um novo código</button>
              <button type="button" onClick={() => trocarModo('entrar')}>Voltar a entrar</button>
            </>
          )}
        </div>
      </div>
      <AvisoPrivacidade />
    </div>
  );
}
