package br.com.ruralmanager.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record NovoOperador(
            @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") @Size(max = 160) String email,
            @NotBlank(message = "Informe a senha")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String senha) {
    }

    public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil, boolean ativo,
                                  LocalDateTime ultimoAcesso) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo(),
                    u.getUltimoAcesso());
        }
    }
}
