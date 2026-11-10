package br.com.ruralmanager.usuario;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import br.com.ruralmanager.produtor.Produtor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Usuario do sistema.
 *
 * Separa a autenticacao da entidade de dominio: o Produtor e quem tem CPF,
 * safras e lavouras; o Usuario e quem entra no sistema. Um produtor pode ter
 * mais de um usuario, situacao comum na agricultura familiar, em que o
 * lancamento dos dados costuma ser feito pelo conjuge, por um filho ou pelo
 * contador.
 *
 * O vinculo com o produtor determina a visibilidade dos dados: toda consulta
 * do sistema e filtrada pelo produtor do usuario autenticado.
 */
@Entity
@Table(name = "usuario")
public class Usuario extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Produtor dono dos dados que este usuario acessa.
     * Nulo apenas para o perfil ADMIN.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_id")
    private Produtor produtor;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 160)
    @Column(nullable = false, length = 160, unique = true)
    private String email;

    /**
     * Hash BCrypt da senha. Nunca a senha em texto puro, e nunca exposto
     * em DTO ou resposta da API.
     */
    @NotBlank
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    /** Construtor exigido pelo JPA. */
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, PerfilUsuario perfil, Produtor produtor) {
        setNome(nome);
        setEmail(email);
        setSenhaHash(senhaHash);
        setPerfil(perfil);
        setProdutor(produtor);
        validarVinculo();
    }

    /**
     * Garante a mesma regra que a restricao ck_usuario_vinculo aplica no banco:
     * ADMIN nao tem produtor, os demais perfis tem.
     */
    public void validarVinculo() {
        if (perfil == null) {
            throw new IllegalStateException("Perfil do usuario nao informado.");
        }
        if (perfil.exigeProdutor() && produtor == null) {
            throw new IllegalStateException(
                    "Usuario de perfil " + perfil + " precisa estar vinculado a um produtor.");
        }
        if (!perfil.exigeProdutor() && produtor != null) {
            throw new IllegalStateException(
                    "Usuario de perfil " + perfil + " nao pode estar vinculado a um produtor.");
        }
    }

    /** Identificador do produtor dono dos dados, ou nulo para ADMIN. */
    public Long getProdutorId() {
        return produtor == null ? null : produtor.getId();
    }

    public void registrarAcesso(LocalDateTime momento) {
        this.ultimoAcesso = momento;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public Produtor getProdutor() {
        return produtor;
    }

    public void setProdutor(Produtor produtor) {
        this.produtor = produtor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome == null ? null : nome.trim();
    }

    public String getEmail() {
        return email;
    }

    /** O e-mail e sempre guardado em minusculas, para servir de identificador estavel. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Usuario outro)) {
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
        return "Usuario{id=" + id + ", email='" + email + "', perfil=" + perfil + "}";
    }
}
