import { describe, expect, it } from 'vitest';
import { dataBr, formatarCpfCnpj, lerDecimal, moeda, numero } from './formato';

describe('formato', () => {
  it('formata moeda em reais', () => {
    expect(moeda(51400)).toBe('R$ 51.400,00');
    expect(moeda(null)).toBe('R$ 0,00');
  });

  it('formata numero com casas fixas e traco para nulo', () => {
    expect(numero(3583.7837, 2)).toBe('3.583,78');
    expect(numero(null)).toBe('—');
  });

  it('converte data ISO sem deslocar o dia por fuso', () => {
    expect(dataBr('2026-03-14')).toBe('14/03/2026');
    expect(dataBr('2026-01-01')).toBe('01/01/2026');
    expect(dataBr(null)).toBe('—');
  });

  it('formata CPF e CNPJ', () => {
    expect(formatarCpfCnpj('52998224725')).toBe('529.982.247-25');
    expect(formatarCpfCnpj('11222333000181')).toBe('11.222.333/0001-81');
  });

  it('le decimais no formato brasileiro e no ponto', () => {
    expect(lerDecimal('1.234,56')).toBe(1234.56);
    expect(lerDecimal('1234,5')).toBe(1234.5);
    expect(lerDecimal('18.5')).toBe(18.5);
    expect(lerDecimal('')).toBeNull();
    expect(lerDecimal('abc')).toBeNull();
  });
});
