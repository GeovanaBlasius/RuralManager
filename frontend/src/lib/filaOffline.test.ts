import { beforeEach, describe, expect, it } from 'vitest';
import { ApiError } from '../api/client';
import { descartar, enfileirar, enviarFila, lerFila, type Armazenamento } from './filaOffline';

function memoria(): Armazenamento {
  const dados = new Map<string, string>();
  return { getItem: (k) => dados.get(k) ?? null, setItem: (k, v) => void dados.set(k, v) };
}

describe('fila offline', () => {
  let s: Armazenamento;
  beforeEach(() => { s = memoria(); });

  const item = (usuarioId: number, rotulo: string) => ({ usuarioId, caminho: '/api/gastos', corpo: { rotulo }, rotulo });

  it('envia em ordem e esvazia a fila', async () => {
    enfileirar(s, item(1, 'A'));
    enfileirar(s, item(1, 'B'));
    const enviados: string[] = [];
    const r = await enviarFila(s, 1, async (_c, corpo) => { enviados.push((corpo as { rotulo: string }).rotulo); });
    expect(enviados).toEqual(['A', 'B']);
    expect(r).toEqual({ enviados: 2, recusados: 0, interrompido: false });
    expect(lerFila(s)).toHaveLength(0);
  });

  it('nao envia lancamento de outra conta', async () => {
    enfileirar(s, item(2, 'de outro usuario'));
    const r = await enviarFila(s, 1, async () => { throw new Error('nao deveria enviar'); });
    expect(r.enviados).toBe(0);
    expect(lerFila(s)).toHaveLength(1);
  });

  it('marca como recusado (sem apagar) quando o servidor rejeita o dado', async () => {
    enfileirar(s, item(1, 'ruim'));
    const r = await enviarFila(s, 1, async () => { throw new ApiError(422, 'Data futura'); });
    expect(r.recusados).toBe(1);
    expect(lerFila(s)[0].erro).toBe('Data futura');
    // Item com erro nao e reenviado sozinho.
    const r2 = await enviarFila(s, 1, async () => { throw new Error('nao deveria reenviar'); });
    expect(r2.enviados).toBe(0);
  });

  it('para e mantem tudo quando a conexao cai, para tentar de novo depois', async () => {
    enfileirar(s, item(1, 'A'));
    enfileirar(s, item(1, 'B'));
    const r = await enviarFila(s, 1, async () => { throw new ApiError(0, 'Sem conexao'); });
    expect(r.interrompido).toBe(true);
    expect(lerFila(s)).toHaveLength(2);
  });

  it('permite descartar um item', () => {
    const a = enfileirar(s, item(1, 'A'));
    descartar(s, a.id);
    expect(lerFila(s)).toHaveLength(0);
  });
});
