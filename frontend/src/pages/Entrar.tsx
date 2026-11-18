import { useEffect, useState, type FormEvent } from 'react';
import { acordarApi, api, ApiError } from '../api/client';
import { useAuth } from '../auth';
import { AvisoPrivacidade, CampoForm, CampoSenha, MensagemErro } from '../ui/componentes';

type Modo = 'entrar' | 'cadastro' | 'verificar' | 'esqueci' | 'redefinir';

export function Entrar() {
  const { entrar, cadastrar, confirmarEmail } = useAuth();
  const [modo, setModo] = useState<Modo>('entrar');
  const [v, setV] = useState({ nome: '', cpf: '', telefone: '', email: '', senha: '', codigo: '', novaSenha: '' });
  const [erro, setErro] = useState<unknown>(null);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [campos, setCampos] = useState<Record<string, string>>({});

  // A API gratuita dorme sem uso: cutuca ao abrir a tela de entrada para ela acordar enquanto a pessoa digita.
  useEffect(() => { acordarApi(); }, []);

  const muda =(k: keyof typeof v) => (e: { target: { value: string } }) => setV((x) => ({ ...x, [k]: e.target.value }));

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
        const r = await cadastrar(v);
        if (r.verificacaoPendente) {
          setModo('verificar');
          setMensagem(r.emailEnviado
            ? `Enviamos um código de 6 dígitos para ${r.email}. Ele vale por 15 minutos.`
            : 'Sua conta foi criada, mas não conseguimos enviar o e-mail agora. Toque em "Reenviar código" em instantes.');
        }
      } else if (modo === 'verificar') {
        await confirmarEmail(v.email, v.codigo);
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
      if (modo === 'entrar' && err instanceof ApiError && err.codigo === 'EMAIL_NAO_VERIFICADO') {
        // Senha certa, e-mail ainda não confirmado: leva direto para digitar o código.
        setModo('verificar');
        setMensagem('Falta confirmar seu e-mail. Digite o código de 6 dígitos que enviamos, ou peça um novo.');
        return;
      }
      setErro(err);
      if (err instanceof ApiError) setCampos(err.campos);
    } finally {
      setEnviando(false);
    }
  }

  async function reenviar() {
    setErro(null);
    setMensagem(null);
    setEnviando(true);
    try {
      await api.post('/api/auth/reenviar-codigo', { email: v.email });
      setMensagem('Pedido feito. Se houver uma conta aguardando confirmação nesse e-mail, enviamos um novo código.');
    } catch (err) {
      setErro(err);
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
              : modo === 'verificar' ? 'Confirmar e-mail'
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
          {modo === 'verificar' && (
            <CampoForm id="codigoVerif" rotulo="Código recebido por e-mail" erro={campos.codigo}>
              <input id="codigoVerif" inputMode="numeric" autoComplete="one-time-code" value={v.codigo}
                onChange={muda('codigo')} maxLength={6} required />
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
              : modo === 'verificar' ? 'Confirmar e-mail'
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
          {modo === 'verificar' && (
            <>
              <button type="button" onClick={reenviar} disabled={enviando}>Reenviar código</button>
              <button type="button" onClick={() => trocarModo('entrar')}>Voltar a entrar</button>
            </>
          )}
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
