package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.UnidadeMedida;
import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Producao fisica retirada de uma lavoura. Nao guarda preco nem indicacao de
 * venda: a mesma colheita pode ser vendida em partes, a compradores e precos
 * diferentes (ItemFatura) ou ter saidas sem venda (Saida).
 */
@Entity
@Table(name = "colheita")
public class Colheita extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cultura_id", nullable = false, updatable = false)
    private Cultura cultura;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_medida_id", nullable = false)
    private UnidadeMedida unidadeMedida;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidade;

    @NotNull
    @Column(name = "data_colheita", nullable = false)
    private LocalDate dataColheita;

    protected Colheita() {
    }

    public Colheita(Cultura cultura, UnidadeMedida unidadeMedida, BigDecimal quantidade, LocalDate dataColheita) {
        this.cultura = cultura;
        this.unidadeMedida = unidadeMedida;
        this.quantidade = quantidade;
        this.dataColheita = dataColheita;
    }

    public Long getId() { return id; }
    public Cultura getCultura() { return cultura; }
    public UnidadeMedida getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(UnidadeMedida u) { this.unidadeMedida = u; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal q) { this.quantidade = q; }
    public LocalDate getDataColheita() { return dataColheita; }
    public void setDataColheita(LocalDate d) { this.dataColheita = d; }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Colheita c && id != null && id.equals(c.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
