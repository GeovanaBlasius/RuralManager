import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth';
import { LINKS_SECUNDARIOS } from '../ui/Layout';
import { Icones } from '../ui/componentes';

interface EventoInstalacao extends Event {
  prompt: () => Promise<void>;
}

export function Mais() {
  const { sessao, sair } = useAuth();
  const [instalar, setInstalar] = useState<EventoInstalacao | null>(null);

  useEffect(() => {
    const aoOferecer = (e: Event) => {
      e.preventDefault();
      setInstalar(Object.assign(e, { prompt: (e as unknown as { prompt: () => Promise<void> }).prompt }) as EventoInstalacao);
    };
    window.addEventListener('beforeinstallprompt', aoOferecer);
    return () => window.removeEventListener('beforeinstallprompt', aoOferecer);
  }, []);

  const emStandalone = window.matchMedia('(display-mode: standalone)').matches;
  const ios = /iphone|ipad|ipod/i.test(navigator.userAgent);

  const grupos = ['Cadastros', 'Operação', 'Conta'] as const;

  return (
    <>
      <div className="cabecalho-pagina"><h1>Mais</h1></div>
      {grupos.map((grupo) => (
        <div key={grupo}>
          <h2 className="secao">{grupo}</h2>
          <div className="lista">
            {LINKS_SECUNDARIOS.filter((l) => l.grupo === grupo).map((l) => (
              <Link key={l.para} to={l.para} className="item" style={{ textDecoration: 'none', color: 'inherit' }}>
                <span style={{ width: 24, height: 24, color: 'var(--verde)' }}>{l.icone}</span>
                <div className="corpo"><div className="titulo">{l.rotulo}</div></div>
              </Link>
            ))}
            {grupo === 'Conta' && sessao?.usuario.perfil === 'PRODUTOR' && (
              <Link to="/equipe" className="item" style={{ textDecoration: 'none', color: 'inherit' }}>
                <span style={{ width: 24, height: 24, color: 'var(--verde)' }}>{Icones.pessoas}</span>
                <div className="corpo"><div className="titulo">Equipe</div><div className="sub">Quem mais lança dados na sua conta</div></div>
              </Link>
            )}
          </div>
        </div>
      ))}

      {!emStandalone && (
        <div className="cartao" style={{ marginTop: 18 }}>
          <h2>Instalar no celular</h2>
          {instalar ? (
            <button type="button" className="btn cheio" style={{ marginTop: 10 }} onClick={() => void instalar.prompt()}>Instalar o app</button>
          ) : ios ? (
            <p style={{ marginTop: 6 }}>No Safari, toque em <strong>Compartilhar</strong> e depois em <strong>Adicionar à Tela de Início</strong>.</p>
          ) : (
            <p style={{ marginTop: 6 }}>No menu do navegador, escolha <strong>Instalar app</strong> ou <strong>Adicionar à tela inicial</strong>.</p>
          )}
        </div>
      )}

      <div className="cartao" style={{ marginTop: 18 }}>
        <div className="linha-par"><span>Conectado como</span><span>{sessao?.usuario.nome}</span></div>
        <div className="linha-par"><span>E-mail</span><span>{sessao?.usuario.email}</span></div>
        <button type="button" className="btn perigo cheio" style={{ marginTop: 12 }} onClick={sair}>Sair</button>
      </div>
    </>
  );
}
