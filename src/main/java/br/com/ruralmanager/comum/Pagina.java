package br.com.ruralmanager.comum;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Pagina de resultados com formato proprio e estavel, independente do que o Spring serializa. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long total, int totalPaginas) {

    public static <E, T> Pagina<T> de(Page<E> page, Function<E, T> conversor) {
        return new Pagina<>(page.getContent().stream().map(conversor).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
