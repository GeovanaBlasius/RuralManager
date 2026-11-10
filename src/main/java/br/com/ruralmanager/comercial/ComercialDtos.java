package br.com.ruralmanager.comercial;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ComercialDtos {

    private ComercialDtos() {
    }

    // ---------------- Cliente ----------------
    public record ClienteRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o CPF ou CNPJ") String cpfCnpj,
            @Size(max = 20) String telefone,
            @Size(max = 160) String endereco) {
    }

    public record ClienteResponse(Long id, String nome, String cpfCnpj, String telefone, String endereco) {
        public static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNome(), c.getCpfCnpj(), c.getTelefone(), c.getEndereco());
        }
    }

    // ---------------- Fatura ----------------
    public record ItemFaturaRequest(
            @NotNull(message = "Informe a colheita") Long colheitaId,
            @NotNull(message = "Informe a quantidade")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
            @Digits(integer = 10, fraction = 2) BigDecimal quantidade,
            @NotNull(message = "Informe o preco")
            @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero")
            @Digits(integer = 10, fraction = 2) BigDecimal precoUnitario) {
    }

    public record FaturaRequest(
            @NotNull(message = "Informe o cliente") Long clienteId,
            @NotNull(message = "Informe a data da venda") LocalDate dataCompra,
            @NotEmpty(message = "Informe ao menos um item") @Valid List<ItemFaturaRequest> itens) {
    }

    public record ItemFaturaResponse(Long id, Long colheitaId, String produtoNome, String variedade,
                                     String unidadeSigla, BigDecimal quantidade, BigDecimal precoUnitario,
                                     BigDecimal subtotal) {
        public static ItemFaturaResponse de(ItemFatura i) {
            var c = i.getColheita();
            return new ItemFaturaResponse(i.getId(), c.getId(), c.getCultura().getProduto().getNome(),
                    c.getCultura().getVariedade(), c.getUnidadeMedida().getSigla(), i.getQuantidade(),
                    i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public record FaturaResponse(Long id, Long clienteId, String clienteNome, LocalDate dataCompra,
                                 BigDecimal total, List<ItemFaturaResponse> itens) {
        public static FaturaResponse de(Fatura f, List<ItemFatura> itens) {
            List<ItemFaturaResponse> linhas = itens.stream().map(ItemFaturaResponse::de).toList();
            BigDecimal total = linhas.stream().map(ItemFaturaResponse::subtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new FaturaResponse(f.getId(), f.getCliente().getId(), f.getCliente().getNome(),
                    f.getDataCompra(), total, linhas);
        }
    }
}
