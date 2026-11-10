import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import { Cadastros } from './pages/Cadastros';
import { Entrar } from './pages/Entrar';
import { Equipe } from './pages/Equipe';
import { Estoque } from './pages/Estoque';
import { Mais } from './pages/Mais';
import { Painel } from './pages/Painel';
import { RelatorioSafra } from './pages/RelatorioSafra';
import { Vendas } from './pages/Vendas';
import { CrudPagina } from './resources/CrudPagina';
import { clientes, colheitas, culturas, gastos, safras, saidas, suporte } from './resources/definicoes';
import { Layout } from './ui/Layout';

export function App() {
  const { sessao } = useAuth();
  if (!sessao) return <Entrar />;

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Painel />} />
        <Route path="safras" element={<CrudPagina recurso={safras} />} />
        <Route path="safras/:id/relatorio" element={<RelatorioSafra />} />
        <Route path="lavouras" element={<CrudPagina recurso={culturas} />} />
        <Route path="colheitas" element={<CrudPagina recurso={colheitas} />} />
        <Route path="gastos" element={<CrudPagina recurso={gastos} />} />
        <Route path="saidas" element={<CrudPagina recurso={saidas} />} />
        <Route path="clientes" element={<CrudPagina recurso={clientes} />} />
        <Route path="vendas" element={<Vendas />} />
        <Route path="estoque" element={<Estoque />} />
        <Route path="cadastros" element={<Cadastros />} />
        <Route path="equipe" element={<Equipe />} />
        <Route path="suporte" element={<CrudPagina recurso={suporte} />} />
        <Route path="mais" element={<Mais />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
