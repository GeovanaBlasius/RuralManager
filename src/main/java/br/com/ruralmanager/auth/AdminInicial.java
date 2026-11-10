package br.com.ruralmanager.auth;

import br.com.ruralmanager.usuario.PerfilUsuario;
import br.com.ruralmanager.usuario.Usuario;
import br.com.ruralmanager.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador, e so quando RM_ADMIN_EMAIL e RM_ADMIN_SENHA
 * estao definidos. Nao existe senha padrao: sem as variaveis, nenhum admin nasce.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
                        @Value("${RM_ADMIN_EMAIL:}") String email,
                        @Value("${RM_ADMIN_SENHA:}") String senha) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank()) {
            return;
        }
        if (senha.length() < 12) {
            log.warn("RM_ADMIN_SENHA tem menos de 12 caracteres; administrador nao criado.");
            return;
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            return;
        }
        usuarios.save(new Usuario("Administrador", email, encoder.encode(senha), PerfilUsuario.ADMIN, null));
        log.info("Administrador inicial criado.");
    }
}
