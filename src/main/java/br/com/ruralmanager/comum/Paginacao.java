package br.com.ruralmanager.comum;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Monta a paginacao a partir de numeros simples. A ordenacao e sempre a da
 * consulta: aceitar "sort" do cliente permitiria ordenar por campo interno ou
 * inexistente e quebrar a consulta.
 */
public final class Paginacao {

    public static final int TAMANHO_MAXIMO = 100;

    private Paginacao() {
    }

    /**
     * Padrao LIKE ("%texto%", minusculo) para busca por parte do nome, ou null se nao ha texto.
     * Escapa % e _ com '!' (use "escape '!'" na consulta) para o usuario nao montar curingas.
     */
    public static String padraoDeBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }
        String limpo = busca.trim().toLowerCase(java.util.Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + limpo + "%";
    }

    public static Pageable de(int pagina, int tamanho) {
        return PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamanho), TAMANHO_MAXIMO));
    }
}
