package br.com.ruralmanager.seguranca;

import br.com.ruralmanager.comum.RegraDeNegocioException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Freio simples contra tentativa de adivinhar senha: apos N falhas seguidas
 * para o mesmo e-mail, bloqueia por uma janela de tempo.
 *
 * Vale por instancia. Com varias instancias o limite efetivo e N vezes o numero
 * delas; para um limite global, aplicar rate limit no balanceador ou API gateway.
 */
@Component
public class LimitadorDeTentativas {

    private static final int MAXIMO = 10;
    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final int TETO_DO_MAPA = 10_000;

    private record Registro(int falhas, Instant desde) {
    }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    public void exigirLiberado(String chave) {
        Registro r = registros.get(chave);
        if (r == null) {
            return;
        }
        if (r.desde().plus(JANELA).isBefore(Instant.now())) {
            registros.remove(chave);
            return;
        }
        if (r.falhas() >= MAXIMO) {
            throw new RegraDeNegocioException("Muitas tentativas. Aguarde alguns minutos e tente novamente.");
        }
    }

    public void registrarFalha(String chave) {
        if (registros.size() > TETO_DO_MAPA) {
            Instant limite = Instant.now().minus(JANELA);
            registros.entrySet().removeIf(e -> e.getValue().desde().isBefore(limite));
        }
        registros.merge(chave, new Registro(1, Instant.now()),
                (antigo, novo) -> new Registro(antigo.falhas() + 1, antigo.desde()));
    }

    public void registrarSucesso(String chave) {
        registros.remove(chave);
    }
}
