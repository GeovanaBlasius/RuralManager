package br.com.ruralmanager.seguranca;

import br.com.ruralmanager.comum.AcessoProibidoException;
import br.com.ruralmanager.usuario.PerfilUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Unica porta de entrada para saber "quem esta chamando". O produtor vem do
 * token assinado, jamais de parametro enviado pelo cliente; e isso que sustenta
 * o isolamento entre produtores.
 */
@Component
public class ContextoUsuario {

    public UsuarioLogado atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken token)) {
            throw new AcessoProibidoException("Autenticacao necessaria.");
        }
        Jwt jwt = token.getToken();
        Long pid = jwt.getClaim("pid") == null ? null : ((Number) jwt.getClaim("pid")).longValue();
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                pid,
                PerfilUsuario.valueOf(jwt.getClaimAsString("perfil")));
    }

    /** Produtor do usuario logado; falha para quem nao tem produtor (ADMIN). */
    public Long produtorId() {
        Long pid = atual().produtorId();
        if (pid == null) {
            throw new AcessoProibidoException("Esta operacao exige um usuario vinculado a um produtor.");
        }
        return pid;
    }

    public void exigirPerfil(PerfilUsuario... permitidos) {
        PerfilUsuario perfil = atual().perfil();
        for (PerfilUsuario p : permitidos) {
            if (p == perfil) {
                return;
            }
        }
        throw new AcessoProibidoException("Seu perfil nao tem permissao para esta operacao.");
    }
}
