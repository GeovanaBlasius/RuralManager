package br.com.ruralmanager.producao;

import java.math.BigDecimal;

/** Projecao de soma agrupada por colheita, para calcular o estoque de uma pagina inteira de uma vez. */
public interface TotalPorColheita {
    Long getColheitaId();

    BigDecimal getTotal();
}
