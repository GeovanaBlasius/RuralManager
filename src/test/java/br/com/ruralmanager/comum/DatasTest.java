package br.com.ruralmanager.comum;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class DatasTest {

    // Relogio fixo: o teste nao depende do dia em que roda.
    private final Datas datas = new Datas(
            Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo")));

    @Test
    void aceitaHojeEDiasPassados() {
        assertThatCode(() -> datas.naoFutura(LocalDate.of(2026, 11, 10), "A data")).doesNotThrowAnyException();
        assertThatCode(() -> datas.naoFutura(LocalDate.of(2025, 3, 1), "A data")).doesNotThrowAnyException();
    }

    @Test
    void recusaDataFutura() {
        assertThatThrownBy(() -> datas.naoFutura(LocalDate.of(2026, 11, 11), "A data da colheita"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("A data da colheita")
                .hasMessageContaining("futura");
    }

    @Test
    void recusaDataAnteriorA1900() {
        assertThatThrownBy(() -> datas.naoFutura(LocalDate.of(1899, 12, 31), "A data"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
