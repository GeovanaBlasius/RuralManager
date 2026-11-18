package br.com.ruralmanager.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param resendApiKey      chave da API do Resend (resend.com); em branco, nenhum e-mail e enviado
 * @param remetente         endereco "De" usado nos e-mails enviados
 * @param exigirVerificacao se true, cadastro novo so entra depois de confirmar o codigo enviado por e-mail
 */
@ConfigurationProperties(prefix = "rm.email")
public record PropriedadesEmail(String resendApiKey, String remetente, boolean exigirVerificacao) {
}
