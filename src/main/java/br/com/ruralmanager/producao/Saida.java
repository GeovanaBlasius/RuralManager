package br.com.ruralmanager.producao;

import br.com.ruralmanager.catalogo.Destino;
import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Producao que deixa o estoque sem ser venda: consumo proprio, semente, perda, doacao. */
@Entity
@Table(name = "saida")
public class Saida extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colheita_id", nullable = false, updatable = false)
    private Colheita colheita;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Destino destino;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidade;

    @NotNull
    @Column(name = "data_saida", nullable = false)
    private LocalDate dataSaida;

    @Size(max = 160)
    @Column(length = 160)
    private String observacao;

    protected Saida() {
    }

    public Saida(Colheita colheita, Destino destino, BigDecimal quantidade, LocalDate dataSaida, String observacao) {
        this.colheita = colheita;
        this.destino = destino;
        this.quantidade = quantidade;
        this.dataSaida = dataSaida;
        setObservacao(observacao);
    }

    public Long getId() { return id; }
    public Colheita getColheita() { return colheita; }
    public Destino getDestino() { return destino; }
    public void setDestino(Destino d) { this.destino = d; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal q) { this.quantidade = q; }
    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate d) { this.dataSaida = d; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String o) { this.observacao = o == null || o.isBlank() ? null : o.trim(); }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Saida s && id != null && id.equals(s.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
