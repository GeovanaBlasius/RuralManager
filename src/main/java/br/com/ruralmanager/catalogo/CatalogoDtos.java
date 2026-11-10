package br.com.ruralmanager.catalogo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record NomeRequest(@NotBlank(message = "Informe o nome") @Size(max = 60) String nome) {
    }

    public record ItemResponse(Long id, String nome) {
        public static ItemResponse de(ItemNomeado i) {
            return new ItemResponse(i.getId(), i.getNome());
        }
    }

    public record UnidadeRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 40) String nome,
            @NotBlank(message = "Informe a sigla") @Size(max = 10) String sigla,
            @DecimalMin(value = "0.0001", message = "O fator deve ser maior que zero")
            @Digits(integer = 8, fraction = 4) BigDecimal fatorKg) {
    }

    public record UnidadeResponse(Long id, String nome, String sigla, BigDecimal fatorKg) {
        public static UnidadeResponse de(UnidadeMedida u) {
            return new UnidadeResponse(u.getId(), u.getNome(), u.getSigla(), u.getFatorKg());
        }
    }
}
