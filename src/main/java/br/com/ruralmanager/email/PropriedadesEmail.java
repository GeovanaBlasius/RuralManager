package br.com.ruralmanager.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param resendApiKey chave da API do Resend (resend.com); em branco desliga o envio de e-mail
 * @param remetente    endereco "De" usado nos e-mails enviados
 */
@ConfigurationProperties(prefix = "rm.email")
public record PropriedadesEmail(String resendApiKey, String remetente) {
}
