package br.com.ruralmanager.producao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ProducaoDtos {

    private ProducaoDtos() {
    }

    // ---------------- Safra ----------------
    public record SafraRequest(
            @NotNull(message = "Informe o ano de inicio") Integer anoInicio,
            @NotNull(message = "Informe o ano de fim") Integer anoFim,
            @Size(max = 255) String descricao) {
    }

    public record SafraResponse(Long id, int anoInicio, int anoFim, String rotulo, String descricao) {
        public static SafraResponse de(Safra s) {
            return new SafraResponse(s.getId(), s.getAnoInicio(), s.getAnoFim(), s.getRotulo(), s.getDescricao());
        }
    }

    // ---------------- Cultura ----------------
    public record CulturaRequest(
            @NotNull(message = "Informe a safra") Long safraId,
            @NotNull(message = "Informe o produto") Long produtoId,
            @NotBlank(message = "Informe a variedade") @Size(max = 60) String variedade,
            @NotNull(message = "Informe a area")
            @DecimalMin(value = "0.01", message = "A area deve ser maior que zero")
            @Digits(integer = 8, fraction = 2) BigDecimal areaHa,
            @NotNull(message = "Informe a data do plantio") LocalDate dataPlantio) {
    }

    public record CulturaResponse(Long id, Long safraId, String safraRotulo, Long produtoId, String produtoNome,
                                  String variedade, BigDecimal areaHa, LocalDate dataPlantio) {
        public static CulturaResponse de(Cultura c) {
            return new CulturaResponse(c.getId(), c.getSafra().getId(), c.getSafra().getRotulo(),
                    c.getProduto().getId(), c.getProduto().getNome(), c.getVariedade(),
                    c.getAreaHa(), c.getDataPlantio());
        }
    }

    // ---------------- Colheita ----------------
    public record ColheitaRequest(
            @NotNull(message = "Informe a lavoura") Long culturaId,
            @NotNull(message = "Informe a unidade de medida") Long unidadeMedidaId,
            @NotNull(message = "Informe a quantidade")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
            @Digits(integer = 10, fraction = 2) BigDecimal quantidade,
            @NotNull(message = "Informe a data da colheita") LocalDate dataColheita) {
    }

    public record ColheitaResponse(Long id, Long culturaId, String produtoNome, String variedade, String safraRotulo,
                                   Long unidadeMedidaId, String unidadeSigla, BigDecimal quantidade,
                                   LocalDate dataColheita, BigDecimal vendido, BigDecimal destinado,
                                   BigDecimal estoque) {
        public static ColheitaResponse de(Colheita c, BigDecimal vendido, BigDecimal destinado) {
            return new ColheitaResponse(c.getId(), c.getCultura().getId(),
                    c.getCultura().getProduto().getNome(), c.getCultura().getVariedade(),
                    c.getCultura().getSafra().getRotulo(), c.getUnidadeMedida().getId(),
                    c.getUnidadeMedida().getSigla(), c.getQuantidade(), c.getDataColheita(),
                    vendido, destinado, c.getQuantidade().subtract(vendido).subtract(destinado));
        }
    }

    // ---------------- Gasto ----------------
    public record GastoRequest(
            @NotNull(message = "Informe a safra") Long safraId,
            Long culturaId,
            Long colheitaId,
            @NotNull(message = "Informe a categoria") Long categoriaGastoId,
            @NotBlank(message = "Informe a descricao") @Size(max = 160) String descricao,
            @NotNull(message = "Informe o valor")
            @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
            @Digits(integer = 10, fraction = 2) BigDecimal valor,
            @NotNull(message = "Informe a data do gasto") LocalDate dataGasto) {
    }

    public record GastoResponse(Long id, Long safraId, String safraRotulo, Long culturaId, String culturaNome,
                                Long colheitaId, Long categoriaGastoId, String categoriaNome, String descricao,
                                BigDecimal valor, LocalDate dataGasto) {
        public static GastoResponse de(Gasto g) {
            Cultura c = g.getCultura();
            return new GastoResponse(g.getId(), g.getSafra().getId(), g.getSafra().getRotulo(),
                    c == null ? null : c.getId(),
                    c == null ? null : c.getProduto().getNome() + " - " + c.getVariedade(),
                    g.getColheita() == null ? null : g.getColheita().getId(),
                    g.getCategoria().getId(), g.getCategoria().getNome(), g.getDescricao(),
                    g.getValor(), g.getDataGasto());
        }
    }

    // ---------------- Saida ----------------
    public record SaidaRequest(
            @NotNull(message = "Informe a colheita") Long colheitaId,
            @NotNull(message = "Informe o destino") Long destinoId,
            @NotNull(message = "Informe a quantidade")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
            @Digits(integer = 10, fraction = 2) BigDecimal quantidade,
            @NotNull(message = "Informe a data") LocalDate dataSaida,
            @Size(max = 160) String observacao) {
    }

    public record SaidaResponse(Long id, Long colheitaId, String produtoNome, String unidadeSigla, Long destinoId,
                                String destinoNome, BigDecimal quantidade, LocalDate dataSaida, String observacao) {
        public static SaidaResponse de(Saida s) {
            Colheita c = s.getColheita();
            return new SaidaResponse(s.getId(), c.getId(), c.getCultura().getProduto().getNome(),
                    c.getUnidadeMedida().getSigla(), s.getDestino().getId(), s.getDestino().getNome(),
                    s.getQuantidade(), s.getDataSaida(), s.getObservacao());
        }
    }
}
