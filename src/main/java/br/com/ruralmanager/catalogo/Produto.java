package br.com.ruralmanager.catalogo;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Especie cultivada: soja, milho, feijao. Catalogo compartilhado por todos os produtores, para que o nome seja registrado uma unica vez e reaproveitado em todas as lavouras. */
@Entity
@Table(name = "produto")
public class Produto extends EntidadeAuditavel implements ItemNomeado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60, unique = true)
    private String nome;

    protected Produto() {
    }

    public Produto(String nome) {
        setNome(nome);
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

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Produto outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
