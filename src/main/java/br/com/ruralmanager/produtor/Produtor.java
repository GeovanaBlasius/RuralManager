package br.com.ruralmanager.produtor;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Produtor rural. E a raiz de todos os dados do sistema: safras, clientes e,
 * por consequencia, lavouras, colheitas, gastos e faturas pertencem sempre a
 * um produtor. Toda consulta feita a partir da sessao do usuario precisa
 * passar pelo identificador do produtor.
 */
@Entity
@Table(name = "produtor")
public class Produtor extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "o CPF deve conter exatamente 11 digitos, sem pontos ou tracos")
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 200)
    @Column(length = 200)
    private String endereco;

    protected Produtor() {
    }

    public Produtor(String cpf, String nome) {
        setCpf(cpf);
        setNome(nome);
    }

    public Long getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    /** Guarda apenas os digitos, de modo que 123.456.789-01 e 12345678901 sejam o mesmo CPF. */
    public void setCpf(String cpf) {
        this.cpf = cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome == null ? null : nome.trim();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone == null || telefone.isBlank() ? null : telefone.trim();
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco == null || endereco.isBlank() ? null : endereco.trim();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Produtor outro)) {
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
