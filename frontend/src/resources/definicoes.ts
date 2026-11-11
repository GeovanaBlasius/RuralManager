import type { Chamado, Cliente, Colheita, Cultura, Gasto, Item, Safra, Saida, Unidade } from '../api/types';
import { dataBr, formatarCpfCnpj, hojeIso, lerDecimal, moeda, numero } from '../lib/formato';
import type { Recurso, Valores } from './tipos';

/** Numero do servidor -> texto do campo, com virgula decimal. */
const txt = (n: number | null | undefined): string => (n === null || n === undefined ? '' : String(n).replace('.', ','));
const num = (v: string): number | null => lerDecimal(v);
const id = (v: string): number | null => (v ? Number(v) : null);
const vazioParaNulo = (v: string): string | null => (v.trim() ? v.trim() : null);

export const safras: Recurso<Safra> = {
  chave: 'safras', titulo: 'Safras', singular: 'safra', caminho: '/api/safras', paginado: false,
  textoVazio: 'Nenhuma safra ainda. Comece cadastrando a primeira, por exemplo 2025/2026.',
  campos: [
    { nome: 'anoInicio', rotulo: 'Ano de início', tipo: 'numero', obrigatorio: true, dica: 'Ano em que o plantio começa. Ex.: 2025', maxLength: 4 },
    { nome: 'anoFim', rotulo: 'Ano de fim', tipo: 'numero', obrigatorio: true, dica: 'O mesmo ano ou o seguinte. Ex.: 2026', maxLength: 4 },
    { nome: 'descricao', rotulo: 'Descrição', tipo: 'texto', maxLength: 255 },
  ],
  linha: (s) => ({ titulo: `Safra ${s.rotulo}`, subtitulo: s.descricao ?? undefined }),
  paraFormulario: (s): Valores => ({
    anoInicio: s ? String(s.anoInicio) : String(new Date().getFullYear()),
    anoFim: s ? String(s.anoFim) : String(new Date().getFullYear() + 1),
    descricao: s?.descricao ?? '',
  }),
  paraPayload: (v) => ({ anoInicio: num(v.anoInicio), anoFim: num(v.anoFim), descricao: vazioParaNulo(v.descricao ?? '') }),
};

export const culturas: Recurso<Cultura> = {
  chave: 'culturas', titulo: 'Culturas', singular: 'cultura', caminho: '/api/culturas', paginado: false,
  textoVazio: 'Nenhuma cultura. Cadastre o que você plantou em cada safra.',
  filtro: { fonte: 'safras', parametro: 'safraId', rotulo: 'Safra' },
  campos: [
    { nome: 'safraId', rotulo: 'Safra', tipo: 'selecao', fonte: 'safras', obrigatorio: true, travadoAoEditar: true,
      criarRecurso: safras },
    { nome: 'produtoId', rotulo: 'Produto', tipo: 'selecao', fonte: 'produtos', obrigatorio: true,
      dica: 'O que você plantou, por exemplo Soja ou Milho. Não achou? Cadastre em Mais > Catálogos.' },
    { nome: 'variedade', rotulo: 'Variedade', tipo: 'texto', obrigatorio: true, maxLength: 60,
      dica: 'O cultivar específico, por exemplo Intacta RR2 (soja) ou Pioneer 30F53 (milho).' },
    { nome: 'areaHa', rotulo: 'Área plantada (hectares)', tipo: 'numero', obrigatorio: true },
    { nome: 'dataPlantio', rotulo: 'Data do plantio', tipo: 'data', obrigatorio: true },
  ],
  linha: (c) => ({
    titulo: `${c.produtoNome} · ${c.variedade}`, subtitulo: `Safra ${c.safraRotulo}`,
    destaque: `${numero(c.areaHa)} ha`, detalhes: [`Plantio ${dataBr(c.dataPlantio)}`],
  }),
  paraFormulario: (c): Valores => ({
    safraId: c ? String(c.safraId) : '', produtoId: c ? String(c.produtoId) : '', variedade: c?.variedade ?? '',
    areaHa: txt(c?.areaHa), dataPlantio: c?.dataPlantio ?? hojeIso(),
  }),
  paraPayload: (v) => ({
    safraId: id(v.safraId), produtoId: id(v.produtoId), variedade: v.variedade, areaHa: num(v.areaHa), dataPlantio: v.dataPlantio,
  }),
};

export const colheitas: Recurso<Colheita> = {
  chave: 'colheitas', titulo: 'Colheitas', singular: 'colheita', caminho: '/api/colheitas', paginado: true,
  textoVazio: 'Nenhuma colheita registrada ainda.',
  filtro: { fonte: 'culturas', parametro: 'culturaId', rotulo: 'Cultura' },
  campos: [
    { nome: 'culturaId', rotulo: 'Cultura', tipo: 'selecao', fonte: 'culturas', obrigatorio: true, travadoAoEditar: true,
      criarRecurso: culturas },
    { nome: 'unidadeMedidaId', rotulo: 'Unidade de medida', tipo: 'selecao', fonte: 'unidades', obrigatorio: true },
    { nome: 'quantidade', rotulo: 'Quantidade colhida', tipo: 'numero', obrigatorio: true },
    { nome: 'dataColheita', rotulo: 'Data da colheita', tipo: 'data', obrigatorio: true },
  ],
  linha: (c) => ({
    titulo: `${c.produtoNome} · ${c.variedade}`, subtitulo: `Safra ${c.safraRotulo} · ${dataBr(c.dataColheita)}`,
    destaque: `${numero(c.quantidade)} ${c.unidadeSigla}`,
    detalhes: [`Vendido ${numero(c.vendido)}`, `Outras saídas ${numero(c.destinado)}`, `Em estoque ${numero(c.estoque)}`],
  }),
  paraFormulario: (c): Valores => ({
    culturaId: c ? String(c.culturaId) : '', unidadeMedidaId: c ? String(c.unidadeMedidaId) : '',
    quantidade: txt(c?.quantidade), dataColheita: c?.dataColheita ?? hojeIso(),
  }),
  paraPayload: (v) => ({
    culturaId: id(v.culturaId), unidadeMedidaId: id(v.unidadeMedidaId), quantidade: num(v.quantidade), dataColheita: v.dataColheita,
  }),
};

export const gastos: Recurso<Gasto> = {
  chave: 'gastos', titulo: 'Gastos', singular: 'gasto', caminho: '/api/gastos', paginado: true,
  textoVazio: 'Nenhum gasto lançado. Registre sementes, adubo, diesel, mão de obra…',
  filtro: { fonte: 'safras', parametro: 'safraId', rotulo: 'Safra' },
  campos: [
    { nome: 'safraId', rotulo: 'Safra', tipo: 'selecao', fonte: 'safras', obrigatorio: true, travadoAoEditar: true,
      limpa: ['culturaId', 'colheitaId'], criarRecurso: safras },
    { nome: 'culturaId', rotulo: 'Cultura', tipo: 'selecao', fonte: 'culturas', limpa: ['colheitaId'],
      dica: 'Deixe vazio para custo geral da safra (diesel, salário, arrendamento).',
      filtrar: (o, v) => !!v.safraId && o.dados?.safraId === v.safraId, criarRecurso: culturas },
    { nome: 'colheitaId', rotulo: 'Colheita', tipo: 'selecao', fonte: 'colheitas',
      dica: 'Opcional. Escolha a cultura primeiro.',
      filtrar: (o, v) => !!v.culturaId && o.dados?.culturaId === v.culturaId },
    { nome: 'categoriaGastoId', rotulo: 'Categoria', tipo: 'selecao', fonte: 'categorias', obrigatorio: true },
    { nome: 'descricao', rotulo: 'Descrição', tipo: 'texto', obrigatorio: true, maxLength: 160 },
    { nome: 'valor', rotulo: 'Valor (R$)', tipo: 'dinheiro', obrigatorio: true },
    { nome: 'dataGasto', rotulo: 'Data', tipo: 'data', obrigatorio: true },
  ],
  linha: (g) => ({
    titulo: g.descricao, subtitulo: `${g.categoriaNome} · ${g.culturaNome ?? 'Custo geral'}`,
    destaque: moeda(g.valor), detalhes: [dataBr(g.dataGasto), `Safra ${g.safraRotulo}`],
  }),
  paraFormulario: (g): Valores => ({
    safraId: g ? String(g.safraId) : '', culturaId: g?.culturaId ? String(g.culturaId) : '',
    colheitaId: g?.colheitaId ? String(g.colheitaId) : '', categoriaGastoId: g ? String(g.categoriaGastoId) : '',
    descricao: g?.descricao ?? '', valor: txt(g?.valor), dataGasto: g?.dataGasto ?? hojeIso(),
  }),
  paraPayload: (v) => ({
    safraId: id(v.safraId), culturaId: id(v.culturaId), colheitaId: id(v.colheitaId),
    categoriaGastoId: id(v.categoriaGastoId), descricao: v.descricao, valor: num(v.valor), dataGasto: v.dataGasto,
  }),
};

export const saidas: Recurso<Saida> = {
  chave: 'saidas', titulo: 'Outras saídas', singular: 'saída', caminho: '/api/saidas', paginado: true,
  textoVazio: 'Nenhuma saída. Registre aqui o que não foi vendido: consumo, semente guardada, ração, perda, doação.',
  campos: [
    { nome: 'colheitaId', rotulo: 'Colheita', tipo: 'selecao', fonte: 'colheitas', obrigatorio: true, travadoAoEditar: true,
      criarRecurso: colheitas },
    { nome: 'destinoId', rotulo: 'Destino', tipo: 'selecao', fonte: 'destinos', obrigatorio: true },
    { nome: 'quantidade', rotulo: 'Quantidade', tipo: 'numero', obrigatorio: true, dica: 'Na mesma unidade da colheita.' },
    { nome: 'dataSaida', rotulo: 'Data', tipo: 'data', obrigatorio: true },
    { nome: 'observacao', rotulo: 'Observação', tipo: 'texto', maxLength: 160 },
  ],
  linha: (s) => ({
    titulo: `${s.produtoNome} → ${s.destinoNome}`, subtitulo: dataBr(s.dataSaida),
    destaque: `${numero(s.quantidade)} ${s.unidadeSigla}`, detalhes: s.observacao ? [s.observacao] : undefined,
  }),
  paraFormulario: (s): Valores => ({
    colheitaId: s ? String(s.colheitaId) : '', destinoId: s ? String(s.destinoId) : '',
    quantidade: txt(s?.quantidade), dataSaida: s?.dataSaida ?? hojeIso(), observacao: s?.observacao ?? '',
  }),
  paraPayload: (v) => ({
    colheitaId: id(v.colheitaId), destinoId: id(v.destinoId), quantidade: num(v.quantidade),
    dataSaida: v.dataSaida, observacao: vazioParaNulo(v.observacao ?? ''),
  }),
};

export const clientes: Recurso<Cliente> = {
  chave: 'clientes', titulo: 'Clientes', singular: 'cliente', caminho: '/api/clientes', paginado: true,
  textoVazio: 'Nenhum cliente. Cadastre quem compra a sua produção (pessoa ou empresa).',
  campos: [
    { nome: 'nome', rotulo: 'Nome ou razão social', tipo: 'texto', obrigatorio: true, maxLength: 120 },
    { nome: 'cpfCnpj', rotulo: 'CPF ou CNPJ', tipo: 'documento', obrigatorio: true, maxLength: 18, dica: 'Só os números ou com pontuação.' },
    { nome: 'telefone', rotulo: 'Telefone', tipo: 'texto', maxLength: 20 },
    { nome: 'endereco', rotulo: 'Endereço', tipo: 'texto', maxLength: 160 },
  ],
  linha: (c) => ({ titulo: c.nome, subtitulo: formatarCpfCnpj(c.cpfCnpj), detalhes: c.telefone ? [c.telefone] : undefined }),
  paraFormulario: (c): Valores => ({
    nome: c?.nome ?? '', cpfCnpj: c ? formatarCpfCnpj(c.cpfCnpj) : '', telefone: c?.telefone ?? '', endereco: c?.endereco ?? '',
  }),
  paraPayload: (v) => ({
    nome: v.nome, cpfCnpj: v.cpfCnpj, telefone: vazioParaNulo(v.telefone ?? ''), endereco: vazioParaNulo(v.endereco ?? ''),
  }),
};

export const suporte: Recurso<Chamado> = {
  chave: 'suporte', titulo: 'Suporte', singular: 'chamado', caminho: '/api/suporte', paginado: false,
  textoVazio: 'Nenhum chamado ainda. Relate um problema para a gente melhorar o sistema.',
  campos: [
    { nome: 'titulo', rotulo: 'Título', tipo: 'texto', obrigatorio: true, maxLength: 120,
      dica: 'Resuma o problema em poucas palavras.' },
    { nome: 'descricao', rotulo: 'Descrição', tipo: 'textarea', obrigatorio: true, maxLength: 2000,
      dica: 'Conte o que você esperava que acontecesse e o que aconteceu de errado.' },
  ],
  linha: (c) => ({
    titulo: c.titulo,
    subtitulo: `${c.status === 'RESOLVIDO' ? 'Resolvido' : 'Aberto'} · ${dataBr(c.criadoEm)}`,
    detalhes: [c.descricao],
  }),
  paraFormulario: (c): Valores => ({ titulo: c?.titulo ?? '', descricao: c?.descricao ?? '' }),
  paraPayload: (v) => ({ titulo: v.titulo, descricao: v.descricao }),
};

// ---------------------- catalogos compartilhados ----------------------

function catalogo(chave: string, titulo: string, singular: string, caminho: string): Recurso<Item> {
  return {
    chave, titulo, singular, caminho, paginado: false, textoVazio: `Nenhum item em ${titulo.toLowerCase()}.`,
    campos: [{ nome: 'nome', rotulo: 'Nome', tipo: 'texto', obrigatorio: true, maxLength: 60 }],
    linha: (i) => ({ titulo: i.nome }),
    paraFormulario: (i): Valores => ({ nome: i?.nome ?? '' }),
    paraPayload: (v) => ({ nome: v.nome }),
  };
}

export const catalogoProdutos = catalogo('cat-produtos', 'Produtos', 'produto', '/api/produtos');
export const catalogoCategorias = catalogo('cat-categorias', 'Categorias de gasto', 'categoria', '/api/categorias-gasto');
export const catalogoDestinos = catalogo('cat-destinos', 'Destinos', 'destino', '/api/destinos');

export const catalogoUnidades: Recurso<Unidade> = {
  chave: 'cat-unidades', titulo: 'Unidades de medida', singular: 'unidade', caminho: '/api/unidades', paginado: false,
  textoVazio: 'Nenhuma unidade.',
  campos: [
    { nome: 'nome', rotulo: 'Nome', tipo: 'texto', obrigatorio: true, maxLength: 40 },
    { nome: 'sigla', rotulo: 'Sigla', tipo: 'texto', obrigatorio: true, maxLength: 10 },
    { nome: 'fatorKg', rotulo: 'Quanto pesa, em quilos', tipo: 'numero',
      dica: 'Ex.: saca de 60 kg → 60. Deixe vazio para unidade sem peso (unidade, dúzia).' },
  ],
  linha: (u) => ({ titulo: u.nome, subtitulo: u.sigla, destaque: u.fatorKg ? `${numero(u.fatorKg, 0)} kg` : 'sem peso' }),
  paraFormulario: (u): Valores => ({ nome: u?.nome ?? '', sigla: u?.sigla ?? '', fatorKg: txt(u?.fatorKg) }),
  paraPayload: (v) => ({ nome: v.nome, sigla: v.sigla, fatorKg: num(v.fatorKg ?? '') }),
};
