package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.CategoriaGasto;
import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Despesa de uma safra. Cultura e colheita sao opcionais: custos gerais
 * (diesel, salario, arrendamento) nao pertencem a nenhuma lavoura. A
 * coerencia entre os vinculos e validada no servico, nao por FK composta.
 */
@Entity
@Table(name = "gasto")
public class Gasto extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "safra_id", nullable = false, updatable = false)
    private Safra safra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultura_id")
    private Cultura cultura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colheita_id")
    private Colheita colheita;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_gasto_id", nullable = false)
    private CategoriaGasto categoria;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String descricao;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @NotNull
    @Column(name = "data_gasto", nullable = false)
    private LocalDate dataGasto;

    protected Gasto() {
    }

    public Gasto(Safra safra, Cultura cultura, Colheita colheita, CategoriaGasto categoria,
                 String descricao, BigDecimal valor, LocalDate dataGasto) {
        this.safra = safra;
        this.cultura = cultura;
        this.colheita = colheita;
        this.categoria = categoria;
        setDescricao(descricao);
        this.valor = valor;
        this.dataGasto = dataGasto;
    }

    public Long getId() { return id; }
    public Safra getSafra() { return safra; }
    public Cultura getCultura() { return cultura; }
    public void setCultura(Cultura c) { this.cultura = c; }
    public Colheita getColheita() { return colheita; }
    public void setColheita(Colheita c) { this.colheita = c; }
    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto c) { this.categoria = c; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d == null ? null : d.trim(); }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public LocalDate getDataGasto() { return dataGasto; }
    public void setDataGasto(LocalDate d) { this.dataGasto = d; }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Gasto g && id != null && id.equals(g.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
