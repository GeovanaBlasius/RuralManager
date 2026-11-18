import { describe, expect, it } from 'vitest';
import { celulaCsv, gerarCsv, nomeDeArquivo } from './exportar';

describe('exportar CSV', () => {
  it('usa virgula decimal e ponto e virgula como separador', () => {
    expect(gerarCsv([['Soja', 2010.81, 4500]])).toBe('Soja;2010,81;4500\r\n');
  });

  it('protege aspas, separador e quebra de linha', () => {
    expect(celulaCsv('a;b')).toBe('"a;b"');
    expect(celulaCsv('diz "oi"')).toBe('"diz ""oi"""');
  });

  it('neutraliza formulas vindas de texto digitado', () => {
    expect(celulaCsv('=HYPERLINK("http://x")')).toBe(`"'=HYPERLINK(""http://x"")"`);
    expect(celulaCsv('+55')).toBe("'+55");
    expect(celulaCsv('@cmd')).toBe("'@cmd");
  });

  it('mantem numero negativo como numero e vazio como vazio', () => {
    expect(celulaCsv(-12.5)).toBe('-12,5');
    expect(celulaCsv(null)).toBe('');
    expect(celulaCsv(undefined)).toBe('');
  });

  it('gera nome de arquivo seguro', () => {
    expect(nomeDeArquivo('Relatório safra 2025/2026')).toBe('relatorio-safra-2025-2026');
  });
});
