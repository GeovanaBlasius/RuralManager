export interface ItemGrafico {
  rotulo: string;
  valor: number;
  /** Texto mostrado ao lado da barra (ja formatado). */
  texto: string;
}

/**
 * Barras horizontais simples (sem biblioteca): leem bem no celular, imprimem no PDF e funcionam
 * com leitor de tela, pois cada linha tem rotulo e valor em texto. Valor negativo fica vermelho.
 */
export function Barras({ titulo, itens, vazio }: { titulo: string; itens: ItemGrafico[]; vazio?: string }) {
  const maximo = Math.max(0, ...itens.map((i) => Math.abs(i.valor)));
  return (
    <figure className="grafico">
      <figcaption>{titulo}</figcaption>
      {itens.length === 0 && <p className="grafico-vazio">{vazio ?? 'Sem dados ainda.'}</p>}
      <ul>
        {itens.map((i) => (
          <li key={i.rotulo}>
            <span className="grafico-rotulo">{i.rotulo}</span>
            <span className="grafico-trilho" aria-hidden="true">
              <span
                className={i.valor < 0 ? 'grafico-barra negativa' : 'grafico-barra'}
                style={{ width: maximo > 0 ? `${Math.max(2, (Math.abs(i.valor) / maximo) * 100)}%` : '0%' }}
              />
            </span>
            <span className="grafico-valor">{i.texto}</span>
          </li>
        ))}
      </ul>
    </figure>
  );
}
