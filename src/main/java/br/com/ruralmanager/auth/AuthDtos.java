package br.com.ruralmanager.auth;

import br.com.ruralmanager.usuario.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") String email,
            @NotBlank(message = "Informe a senha") String senha) {
    }

    /**
     * Cadastro do produtor e do primeiro usuario dele, em um passo so.
     * A senha tem no maximo 72 caracteres porque o BCrypt ignora o que passa disso.
     */
    public record CadastroRequest(
            @NotBlank(message = "Informe seu nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o CPF") String cpf,
            @Size(max = 20) String telefone,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") @Size(max = 160) String email,
            @NotBlank(message = "Informe a senha")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String senha) {
    }

    public record UsuarioResumo(Long id, String nome, String email, PerfilUsuario perfil,
                                Long produtorId, String produtorNome) {
    }

    public record AuthResponse(String token, String tipo, long expiraEmSegundos, UsuarioResumo usuario) {
    }
}
