package br.com.ruralmanager.seguranca;

import br.com.ruralmanager.usuario.PerfilUsuario;

/** Identidade extraida do token da requisicao atual. Nada aqui vem do corpo da requisicao. */
public record UsuarioLogado(Long id, Long produtorId, PerfilUsuario perfil) {
}
