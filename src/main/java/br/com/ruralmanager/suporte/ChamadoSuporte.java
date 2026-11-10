package br.com.ruralmanager.suporte;

import br.com.ruralmanager.comum.EntidadeAuditavel;
import br.com.ruralmanager.usuario.Usuario;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Relato de problema que o usuario manda para a equipe melhorar o sistema. */
@Entity
@Table(name = "chamado_suporte")
public class ChamadoSuporte extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusChamado status;

    protected ChamadoSuporte() {
    }

    public ChamadoSuporte(Usuario usuario, String titulo, String descricao) {
        this.usuario = usuario;
        this.titulo = titulo == null ? null : titulo.trim();
        this.descricao = descricao == null ? null : descricao.trim();
        this.status = StatusChamado.ABERTO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo == null ? null : titulo.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao == null ? null : descricao.trim();
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void resolver() {
        this.status = StatusChamado.RESOLVIDO;
    }

    public void reabrir() {
        this.status = StatusChamado.ABERTO;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChamadoSuporte c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
