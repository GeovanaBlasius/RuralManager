package br.com.ruralmanager.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relogio injetavel. As regras de "data nao futura" dependem do dia de hoje, e
 * com o Clock elas podem ser testadas sem depender da data em que o teste roda.
 * Fixa o fuso do Brasil: um servidor em UTC viraria o dia tres horas antes.
 */
@Configuration
public class RelogioConfig {

    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
