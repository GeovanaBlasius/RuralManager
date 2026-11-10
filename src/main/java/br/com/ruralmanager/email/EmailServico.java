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
 * Sem chave configurada, so registra no log em vez de falhar: isso deixa o
 * resto do sistema (cadastro, recuperacao de senha) funcionando mesmo antes
 * de configurar o envio de e-mail. Falha de envio tambem so vai pro log --
 * nunca pro cliente, para nao revelar se o e-mail existe nem detalhe interno.
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
    }

    public void enviar(String destinatario, String assunto, String corpoHtml) {
        if (!habilitado) {
            log.warn("RM_RESEND_API_KEY nao configurada; e-mail para {} nao foi enviado.", destinatario);
            return;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .body(Map.of("from", remetente, "to", destinatario, "subject", assunto, "html", corpoHtml))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail via Resend para {}", destinatario, e);
        }
    }
}
