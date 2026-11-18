package br.com.ruralmanager.email;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Envio de e-mail transacional via Resend (resend.com).
 *
 * Quem chama precisa saber se o e-mail saiu de verdade: {@link #disponivel()} diz se ha chave
 * configurada e {@link #enviar} devolve true so quando o Resend aceitou a mensagem. Os detalhes
 * de uma falha ficam no log, nunca na resposta ao cliente.
 *
 * Atencao: com o remetente de teste (onboarding@resend.dev) o Resend so entrega para o e-mail
 * dono da conta. Para enviar a qualquer pessoa e preciso verificar um dominio no painel do Resend
 * e usar um remetente desse dominio em RM_EMAIL_REMETENTE.
 */
@Service
@EnableConfigurationProperties(PropriedadesEmail.class)
public class EmailServico {

    private static final Logger log = LoggerFactory.getLogger(EmailServico.class);

    private final RestClient restClient;
    private final String remetente;
    private final boolean habilitado;

    public EmailServico(PropriedadesEmail props) {
        this.remetente = props.remetente() == null || props.remetente().isBlank()
                ? "RuralManager <onboarding@resend.dev>" : props.remetente();
        this.habilitado = props.resendApiKey() != null && !props.resendApiKey().isBlank();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + (habilitado ? props.resendApiKey() : ""))
                .build();
        if (!habilitado) {
            log.warn("RM_RESEND_API_KEY nao configurada: nenhum e-mail sera enviado "
                    + "(cadastro com verificacao e recuperacao de senha ficam indisponiveis).");
        } else if (this.remetente.contains("onboarding@resend.dev")) {
            log.warn("Remetente de teste do Resend em uso: so e entregue ao e-mail dono da conta Resend. "
                    + "Verifique um dominio e ajuste RM_EMAIL_REMETENTE.");
        }
    }

    /** Ha chave do Resend configurada? Sem ela, nenhum e-mail sai. */
    public boolean disponivel() {
        return habilitado;
    }

    /** @return true se o Resend aceitou o envio; false se nao ha chave ou o envio falhou. */
    public boolean enviar(String destinatario, String assunto, String corpoHtml) {
        if (!habilitado) {
            log.warn("RM_RESEND_API_KEY nao configurada; e-mail '{}' para {} NAO foi enviado.", assunto, destinatario);
            return false;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .body(Map.of("from", remetente, "to", destinatario, "subject", assunto, "html", corpoHtml))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail via Resend para {}", destinatario, e);
            return false;
        }
    }
}
