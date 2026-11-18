import { describe, expect, it } from 'vitest';
import { deveTentarDeNovo } from './client';

const base = { metodo: 'GET', falhouRede: false, semInternet: false };

describe('deveTentarDeNovo (servidor acordando)', () => {
  it('repete GET quando a conexao falha', () => {
    expect(deveTentarDeNovo({ ...base, falhouRede: true })).toBe(true);
  });

  it('nao repete POST que falhou na rede (poderia duplicar o lancamento)', () => {
    expect(deveTentarDeNovo({ ...base, metodo: 'POST', falhouRede: true })).toBe(false);
  });

  it('repete qualquer metodo quando o proxy responde 502/503/504 em HTML', () => {
    expect(deveTentarDeNovo({ ...base, metodo: 'POST', status: 503, tipoConteudo: 'text/html' })).toBe(true);
    expect(deveTentarDeNovo({ ...base, status: 502, tipoConteudo: null })).toBe(true);
  });

  it('nao repete 503 da propria API (JSON)', () => {
    expect(deveTentarDeNovo({ ...base, status: 503, tipoConteudo: 'application/problem+json' })).toBe(false);
  });

  it('nao repete sem internet nem em erro comum', () => {
    expect(deveTentarDeNovo({ ...base, falhouRede: true, semInternet: true })).toBe(false);
    expect(deveTentarDeNovo({ ...base, status: 422, tipoConteudo: 'application/problem+json' })).toBe(false);
    expect(deveTentarDeNovo({ ...base, status: 500, tipoConteudo: 'text/html' })).toBe(false);
  });
});
