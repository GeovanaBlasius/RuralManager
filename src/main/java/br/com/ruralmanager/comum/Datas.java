package br.com.ruralmanager.comum;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Regras de data que dependem de "hoje", isoladas para poderem ser testadas com um relogio fixo. */
@Component
public class Datas {

    private final Clock relogio;

    public Datas(Clock relogio) {
        this.relogio = relogio;
    }

    public LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    public void naoFutura(LocalDate data, String campo) {
        if (data.isAfter(hoje())) {
            throw new RegraDeNegocioException(campo + " nao pode ser uma data futura.");
        }
        if (data.getYear() < 1900) {
            throw new RegraDeNegocioException(campo + " e anterior a 1900.");
        }
    }
}
