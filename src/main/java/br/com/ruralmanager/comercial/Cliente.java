package br.com.ruralmanager.comercial;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import br.com.ruralmanager.produtor.Produtor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Comprador da producao. Pertence a um produtor: o documento e unico por produtor, nao global. */
@Entity
@Table(name = "cliente")
public class Cliente extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produtor_id", nullable = false, updatable = false)
    private Produtor produtor;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "informe um CPF (11 digitos) ou CNPJ (14 digitos)")
    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 160)
    @Column(length = 160)
    private String endereco;

    protected Cliente() {
    }

    public Cliente(Produtor produtor, String nome, String cpfCnpj) {
        this.produtor = produtor;
        setNome(nome);
        setCpfCnpj(cpfCnpj);
    }

    public Long getId() { return id; }
    public Produtor getProdutor() { return produtor; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome == null ? null : nome.trim(); }
    public String getCpfCnpj() { return cpfCnpj; }
    /** Guarda apenas digitos, para aceitar 12.345.678/0001-99 e 12345678000199 como o mesmo documento. */
    public void setCpfCnpj(String doc) { this.cpfCnpj = doc == null ? null : doc.replaceAll("\\D", ""); }
    public String getTelefone() { return telefone; }
    public void setTelefone(String t) { this.telefone = t == null || t.isBlank() ? null : t.trim(); }
    public String getEndereco() { return endereco; }
    public void setEndereco(String e) { this.endereco = e == null || e.isBlank() ? null : e.trim(); }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Cliente c && id != null && id.equals(c.id));
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
