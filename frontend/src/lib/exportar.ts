import type { RelatorioSafra } from '../api/types';

export type Celula = string | number | null | undefined;

/**
 * Texto de uma celula de CSV. Numeros saem com virgula decimal (o Excel em portugues le como numero).
 * Texto que comecaria com = + - @ ou tabulacao e prefixado com apostrofo: sem isso uma planilha
 * trataria o conteudo como formula (injecao de formula em CSV, ex.: um nome de cliente "=HYPERLINK(...)").
 */
export function celulaCsv(valor: Celula): string {
  if (valor === null || valor === undefined) return '';
  let texto: string;
  if (typeof valor === 'number') {
    texto = Number.isFinite(valor) ? String(valor).replace('.', ',') : '';
  } else {
    texto = /^[=+\-@\t\r]/.test(valor) ? `'${valor}` : valor;
  }
  return /[;"\r\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
}

/** CSV com ';' e quebra CRLF, o formato que o Excel brasileiro abre direto. */
export function gerarCsv(linhas: Celula[][]): string {
  return linhas.map((l) => l.map(celulaCsv).join(';')).join('\r\n') + '\r\n';
}

/** Linhas do relatorio de uma safra: resumo, culturas e gastos por categoria. */
export function linhasDoRelatorio(r: RelatorioSafra): Celula[][] {
  return [
    [`Relatório da safra ${r.rotulo}`],
    [],
    ['Resumo'],
    ['Receita (R$)', r.receitaTotal],
    ['Custo total (R$)', r.custoTotal],
    ['Custos gerais sem cultura (R$)', r.custoGeral],
    ['Resultado (R$)', r.lucro],
    ['Margem (%)', r.margemPercentual],
    ['Área total (ha)', r.areaTotalHa],
    [],
    ['Por cultura'],
    ['Produto', 'Variedade', 'Área (ha)', 'Colhido (kg)', 'Produtividade (kg/ha)', 'Custo direto (R$)',
      'Custo por ha (R$)', 'Receita (R$)', 'Receita por ha (R$)', 'Resultado (R$)', 'Retorno sobre o custo (%)'],
    ...r.culturas.map((c) => [c.produto, c.variedade, c.areaHa, c.colhidoKg, c.produtividadeKgHa, c.custo,
      c.custoPorHa, c.receita, c.receitaPorHa, c.lucro, c.roiPercentual]),
    [],
    ['Gastos por categoria'],
    ['Categoria', 'Total (R$)', '% do custo'],
    ...r.gastosPorCategoria.map((g) => [g.categoria, g.total, g.percentual]),
  ];
}

export function nomeDeArquivo(base: string): string {
  return base.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toLowerCase();
}

/** Baixa um arquivo de texto. O BOM faz o Excel reconhecer os acentos (UTF-8). */
export function baixarCsv(nome: string, linhas: Celula[][]): void {
  const blob = new Blob(['﻿', gerarCsv(linhas)], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${nomeDeArquivo(nome)}.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
