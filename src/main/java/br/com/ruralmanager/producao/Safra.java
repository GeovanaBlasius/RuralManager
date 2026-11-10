package br.com.ruralmanager.producao;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import br.com.ruralmanager.produtor.Produtor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Safra: o periodo agricola de um produtor. Atravessa dois anos civis
 * (plantio no segundo semestre, colheita no primeiro do ano seguinte).
 */
@Entity
@Table(name = "safra")
public class Safra extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produtor_id", nullable = false, updatable = false)
    private Produtor produtor;

    @Column(name = "ano_inicio", nullable = false)
    private short anoInicio;

    @Column(name = "ano_fim", nullable = false)
    private short anoFim;

    @Size(max = 255)
    private String descricao;

    protected Safra() {
    }

    public Safra(Produtor produtor, int anoInicio, int anoFim, String descricao) {
        this.produtor = produtor;
        definirPeriodo(anoInicio, anoFim);
        this.descricao = descricao;
    }

    /** Espelha o CHECK do banco: ano_fim e o mesmo ano ou o seguinte ao de inicio. */
    public final void definirPeriodo(int anoInicio, int anoFim) {
        if (anoInicio < 1900) {
            throw new IllegalArgumentException("O ano de inicio da safra deve ser 1900 ou posterior");
        }
        if (anoFim < anoInicio || anoFim > anoInicio + 1) {
            throw new IllegalArgumentException("O ano de fim deve ser o mesmo ano de inicio ou o seguinte");
        }
        this.anoInicio = (short) anoInicio;
        this.anoFim = (short) anoFim;
    }

    public String getRotulo() {
        return anoInicio + "/" + anoFim;
    }

    public Long getId() { return id; }
    public Produtor getProdutor() { return produtor; }
    public Long getProdutorId() { return produtor.getId(); }
    public int getAnoInicio() { return anoInicio; }
    public int getAnoFim() { return anoFim; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) {
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Safra s && id != null && id.equals(s.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
