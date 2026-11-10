package br.com.ruralmanager.comum;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base das entidades do sistema: carrega as datas de criacao e de ultima
 * alteracao, preenchidas automaticamente pelo Spring Data.
 *
 * Exige que a aplicacao tenha a auditoria habilitada, o que e feito em
 * {@code br.com.ruralmanager.config.JpaAuditingConfig}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeAuditavel {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime atualizadoEm;

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
