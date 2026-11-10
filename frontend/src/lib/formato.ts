const moedaBR = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export function moeda(valor: number | null | undefined): string {
  return moedaBR.format(valor ?? 0);
}

export function numero(valor: number | null | undefined, casas = 2): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas });
}

/**
 * "2026-03-14" -> "14/03/2026", sem passar por Date: new Date('2026-03-14') e lido
 * como UTC e, no fuso do Brasil, mostraria o dia anterior.
 */
export function dataBr(iso: string | null | undefined): string {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return a && m && d ? `${d}/${m}/${a}` : iso;
}

export function hojeIso(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

export function formatarCpfCnpj(valor: string): string {
  const d = somenteDigitos(valor);
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  return valor;
}

/** Aceita "1.234,56", "1234,56" e "1234.56". Devolve null se nao for numero. */
export function lerDecimal(texto: string): number | null {
  const t = texto.trim();
  if (!t) return null;
  const normal = t.includes(',') ? t.replace(/\./g, '').replace(',', '.') : t;
  const n = Number(normal);
  return Number.isFinite(n) ? n : null;
}
