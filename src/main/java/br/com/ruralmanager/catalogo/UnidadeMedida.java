package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Unidade em que a producao e medida: saca de 60 quilos, caixa, quilograma.
 *
 * <p>O fator de conversao para quilos permite comparar colheitas registradas
 * em unidades diferentes. Unidades que nao tem peso definido, como "unidade"
 * ou "duzia", ficam com o fator nulo e por isso nao entram nos calculos de
 * produtividade em quilos por hectare.
 */
@Entity
@Table(name = "unidade_medida")
public class UnidadeMedida extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40, unique = true)
    private String nome;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10, unique = true)
    private String sigla;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "fator_kg", precision = 12, scale = 4)
    private BigDecimal fatorKg;

    protected UnidadeMedida() {
    }

    public UnidadeMedida(String nome, String sigla, BigDecimal fatorKg) {
        setNome(nome);
        setSigla(sigla);
        this.fatorKg = fatorKg;
    }

    /** Converte uma quantidade desta unidade para quilos, ou null se a unidade nao tem peso. */
    public BigDecimal paraQuilos(BigDecimal quantidade) {
        if (fatorKg == null || quantidade == null) {
            return null;
        }
        return quantidade.multiply(fatorKg);
    }

    public boolean temPeso() {
        return fatorKg != null;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome == null ? null : nome.trim();
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla == null ? null : sigla.trim();
    }

    public BigDecimal getFatorKg() {
        return fatorKg;
    }

    public void setFatorKg(BigDecimal fatorKg) {
        this.fatorKg = fatorKg;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof UnidadeMedida outra)) {
            return false;
        }
        return id != null && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return sigla;
    }
}
