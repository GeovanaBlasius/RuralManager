import type { ReactNode } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth';
import { Icones } from './componentes';

const PRINCIPAIS: { para: string; rotulo: string; icone: ReactNode; fim?: boolean }[] = [
  { para: '/', rotulo: 'Início', icone: Icones.inicio, fim: true },
  { para: '/gastos', rotulo: 'Gastos', icone: Icones.gasto },
  { para: '/colheitas', rotulo: 'Colheitas', icone: Icones.colheita },
  { para: '/vendas', rotulo: 'Vendas', icone: Icones.venda },
  { para: '/mais', rotulo: 'Mais', icone: Icones.mais },
];

const SECUNDARIOS: { para: string; rotulo: string; icone: ReactNode }[] = [
  { para: '/safras', rotulo: 'Safras', icone: Icones.safra },
  { para: '/lavouras', rotulo: 'Culturas', icone: Icones.colheita },
  { para: '/saidas', rotulo: 'Outras saídas', icone: Icones.estoque },
  { para: '/estoque', rotulo: 'Estoque', icone: Icones.estoque },
  { para: '/clientes', rotulo: 'Clientes', icone: Icones.pessoas },
  { para: '/cadastros', rotulo: 'Cadastros', icone: Icones.lista },
  { para: '/suporte', rotulo: 'Suporte', icone: Icones.suporte },
];

export const LINKS_SECUNDARIOS = SECUNDARIOS;

export function Layout() {
  const { sessao, sair } = useAuth();
  const { pathname } = useLocation();
  const ehProdutor = sessao?.usuario.perfil === 'PRODUTOR';
  const titulo = [...PRINCIPAIS, ...SECUNDARIOS].find((l) => (l.para === '/' ? pathname === '/' : pathname.startsWith(l.para)))?.rotulo ?? 'RuralManager';

  const classe = ({ isActive }: { isActive: boolean }) => (isActive ? 'ativo' : undefined);

  return (
    <div className="app">
      <nav className="nav-lateral" aria-label="Principal">
        <div className="marca">RuralManager</div>
        {[...PRINCIPAIS.filter((l) => l.para !== '/mais'), ...SECUNDARIOS].map((l) => (
          <NavLink key={l.para} to={l.para} end={l.para === '/'} className={classe}>{l.icone}{l.rotulo}</NavLink>
        ))}
        {ehProdutor && <NavLink to="/equipe" className={classe}>{Icones.pessoas}Equipe</NavLink>}
        <button type="button" className="btn secundario" style={{ marginTop: 'auto' }} onClick={sair}>Sair</button>
      </nav>

      <div className="principal">
        <header className="topo">
          <h1>{pathname === '/' ? <span className="marca">RuralManager</span> : titulo}</h1>
          <button type="button" onClick={sair} aria-label="Sair">Sair</button>
        </header>
        <main className="conteudo"><Outlet /></main>
      </div>

      <nav className="nav-inferior" aria-label="Principal">
        {PRINCIPAIS.map((l) => (
          <NavLink key={l.para} to={l.para} end={l.fim} className={classe}>{l.icone}{l.rotulo}</NavLink>
        ))}
      </nav>
    </div>
  );
}
