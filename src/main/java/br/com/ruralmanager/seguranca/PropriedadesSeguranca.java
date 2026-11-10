package br.com.ruralmanager.seguranca;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao de seguranca, toda vinda do ambiente.
 *
 * @param jwtSecret           segredo HMAC do token, no minimo 32 caracteres; nunca no repositorio
 * @param jwtExpiracaoMinutos validade do token de acesso
 * @param corsOrigens         origens do front-end autorizadas a chamar a API
 */
@ConfigurationProperties(prefix = "rm.seguranca")
public record PropriedadesSeguranca(String jwtSecret, long jwtExpiracaoMinutos, List<String> corsOrigens) {
}
