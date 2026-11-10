package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import br.com.ruralmanager.producao.Colheita;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Parte de uma colheita vendida em uma fatura. O preco e por unidade de medida da colheita. */
@Entity
@Table(name = "item_fatura")
public class ItemFatura extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fatura_id", nullable = false, updatable = false)
    private Fatura fatura;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colheita_id", nullable = false, updatable = false)
    private Colheita colheita;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    protected ItemFatura() {
    }

    public ItemFatura(Fatura fatura, Colheita colheita, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.fatura = fatura;
        this.colheita = colheita;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return quantidade.multiply(precoUnitario);
    }

    public Long getId() { return id; }
    public Fatura getFatura() { return fatura; }
    public Colheita getColheita() { return colheita; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal q) { this.quantidade = q; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal p) { this.precoUnitario = p; }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof ItemFatura i && id != null && id.equals(i.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
