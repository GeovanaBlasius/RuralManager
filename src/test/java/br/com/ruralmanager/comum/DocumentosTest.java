package br.com.ruralmanager.comum;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentosTest {

    @Test
    void aceitaCpfComDigitosVerificadoresCorretos() {
        assertThat(Documentos.cpfValido("52998224725")).isTrue();
        assertThat(Documentos.cpfValido("529.982.247-25")).isTrue();
    }

    @Test
    void recusaCpfComDigitoErradoOuRepetido() {
        assertThat(Documentos.cpfValido("12345678901")).isFalse();
        assertThat(Documentos.cpfValido("11111111111")).isFalse();
        assertThat(Documentos.cpfValido("123")).isFalse();
        assertThat(Documentos.cpfValido(null)).isFalse();
    }

    @Test
    void validaCnpjPelosDigitosVerificadores() {
        assertThat(Documentos.cnpjValido("11222333000181")).isTrue();
        assertThat(Documentos.cnpjValido("11.222.333/0001-81")).isTrue();
        assertThat(Documentos.cnpjValido("11222333000182")).isFalse();
        assertThat(Documentos.cnpjValido("00000000000000")).isFalse();
    }

    @Test
    void cpfOuCnpjEscolhePeloTamanho() {
        assertThat(Documentos.cpfOuCnpjValido("52998224725")).isTrue();
        assertThat(Documentos.cpfOuCnpjValido("11222333000181")).isTrue();
        assertThat(Documentos.cpfOuCnpjValido("1234567")).isFalse();
    }
}
