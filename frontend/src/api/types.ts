export interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: import('./sessao').Usuario;
}

export interface Item { id: number; nome: string }
export interface Unidade { id: number; nome: string; sigla: string; fatorKg: number | null }

export interface Safra { id: number; anoInicio: number; anoFim: number; rotulo: string; descricao: string | null }
export interface Cultura {
  id: number; safraId: number; safraRotulo: string; produtoId: number; produtoNome: string;
  variedade: string; areaHa: number; dataPlantio: string;
}
export interface Colheita {
  id: number; culturaId: number; produtoNome: string; variedade: string; safraRotulo: string;
  unidadeMedidaId: number; unidadeSigla: string; quantidade: number; dataColheita: string;
  vendido: number; destinado: number; estoque: number;
}
export interface Gasto {
  id: number; safraId: number; safraRotulo: string; culturaId: number | null; culturaNome: string | null;
  colheitaId: number | null; categoriaGastoId: number; categoriaNome: string; descricao: string;
  valor: number; dataGasto: string;
}
export interface Saida {
  id: number; colheitaId: number; produtoNome: string; unidadeSigla: string; destinoId: number;
  destinoNome: string; quantidade: number; dataSaida: string; observacao: string | null;
}
export interface Cliente { id: number; nome: string; cpfCnpj: string; telefone: string | null; endereco: string | null }
export interface ItemVenda {
  id: number; colheitaId: number; produtoNome: string; variedade: string; unidadeSigla: string;
  quantidade: number; precoUnitario: number; subtotal: number;
}
export interface Venda { id: number; clienteId: number; clienteNome: string; dataCompra: string; total: number; itens: ItemVenda[] }
export interface UsuarioConta { id: number; nome: string; email: string; perfil: string; ativo: boolean; ultimoAcesso: string | null }

export interface ResumoSafra {
  safraId: number; rotulo: string; culturas: number; areaHa: number; custo: number; receita: number; lucro: number;
}
export interface Painel { totalSafras: number; custoTotal: number; receitaTotal: number; lucroTotal: number; safras: ResumoSafra[] }
export interface DesempenhoCultura {
  culturaId: number; produto: string; variedade: string; areaHa: number; colhidoKg: number | null;
  possuiColheitaSemPeso: boolean; produtividadeKgHa: number | null; custo: number; custoPorHa: number;
  receita: number; receitaPorHa: number; lucro: number; roiPercentual: number | null;
}
export interface GastoPorCategoria { categoria: string; total: number; percentual: number }
export interface RelatorioSafra {
  safraId: number; rotulo: string; areaTotalHa: number; custoTotal: number; custoGeral: number;
  receitaTotal: number; lucro: number; margemPercentual: number;
  culturas: DesempenhoCultura[]; gastosPorCategoria: GastoPorCategoria[];
}
export interface EstoqueColheita {
  colheitaId: number; produto: string; variedade: string; safra: string; dataColheita: string;
  unidade: string; colhido: number; vendido: number; destinado: number; estoque: number;
}
