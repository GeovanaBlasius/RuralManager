package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Venda a um cliente. Os itens (ItemFatura) dizem quais colheitas, quanto e a que preco. */
@Entity
@Table(name = "fatura")
public class Fatura extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private Cliente cliente;

    @NotNull
    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    protected Fatura() {
    }

    public Fatura(Cliente cliente, LocalDate dataCompra) {
        this.cliente = cliente;
        this.dataCompra = dataCompra;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDate d) { this.dataCompra = d; }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Fatura f && id != null && id.equals(f.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
