package br.com.ruralmanager.producao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Espelha as CHECKs ck_safra_ano_inicio e ck_safra_ano_fim do banco. */
class SafraTest {

    @Test
    void safraQueAtravessaDoisAnos() {
        Safra s = new Safra(null, 2025, 2026, null);
        assertThat(s.getRotulo()).isEqualTo("2025/2026");
    }

    @Test
    void safraDeUmUnicoAnoCivil() {
        assertThat(new Safra(null, 2025, 2025, null).getRotulo()).isEqualTo("2025/2025");
    }

    @Test
    void recusaFimAnteriorAoInicio() {
        assertThatThrownBy(() -> new Safra(null, 2026, 2025, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recusaFimMaisDeUmAnoDepoisDoInicio() {
        assertThatThrownBy(() -> new Safra(null, 2025, 2027, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recusaInicioAntesDe1900() {
        assertThatThrownBy(() -> new Safra(null, 1899, 1900, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
