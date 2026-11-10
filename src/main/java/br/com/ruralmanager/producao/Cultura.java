package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.Produto;
import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Lavoura efetivamente plantada: a ocorrencia de um produto dentro de uma safra. */
@Entity
@Table(name = "cultura")
public class Cultura extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "safra_id", nullable = false, updatable = false)
    private Safra safra;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String variedade;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "area_ha", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHa;

    @NotNull
    @Column(name = "data_plantio", nullable = false)
    private LocalDate dataPlantio;

    protected Cultura() {
    }

    public Cultura(Safra safra, Produto produto, String variedade, BigDecimal areaHa, LocalDate dataPlantio) {
        this.safra = safra;
        this.produto = produto;
        setVariedade(variedade);
        this.areaHa = areaHa;
        this.dataPlantio = dataPlantio;
    }

    public Long getId() { return id; }
    public Safra getSafra() { return safra; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public String getVariedade() { return variedade; }
    public void setVariedade(String v) { this.variedade = v == null ? null : v.trim(); }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public LocalDate getDataPlantio() { return dataPlantio; }
    public void setDataPlantio(LocalDate d) { this.dataPlantio = d; }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Cultura c && id != null && id.equals(c.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
