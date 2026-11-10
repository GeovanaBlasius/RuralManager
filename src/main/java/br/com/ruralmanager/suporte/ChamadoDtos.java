package br.com.ruralmanager.suporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class ChamadoDtos {

    private ChamadoDtos() {
    }

    public record ChamadoRequest(
            @NotBlank(message = "Informe um titulo") @Size(max = 120) String titulo,
            @NotBlank(message = "Descreva o problema") @Size(max = 2000) String descricao) {
    }

    public record ChamadoResponse(Long id, String titulo, String descricao, StatusChamado status,
                                  String nomeUsuario, String emailUsuario, LocalDateTime criadoEm) {
        public static ChamadoResponse de(ChamadoSuporte c) {
            return new ChamadoResponse(c.getId(), c.getTitulo(), c.getDescricao(), c.getStatus(),
                    c.getUsuario().getNome(), c.getUsuario().getEmail(), c.getCriadoEm());
        }
    }
}
