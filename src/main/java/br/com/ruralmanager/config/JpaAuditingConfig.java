package br.com.ruralmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Habilita o preenchimento de created_at e updated_at nas entidades. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
