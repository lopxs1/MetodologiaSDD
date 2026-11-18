package br.org.amde.digital.service.usuario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CelularMaskerTest {

    private final CelularMasker masker = new CelularMasker();

    @Test
    void deveMascararCelularComOnzeDigitos() {
        assertThat(masker.mascarar("15999991234")).isEqualTo("(15) *****-1234");
    }

    @Test
    void deveMascararTelefoneComDezDigitos() {
        assertThat(masker.mascarar("1532221234")).isEqualTo("(15) ****-1234");
    }

    @Test
    void deveRetornarNuloOuAsteriscosParaEntradasForaDoPadrao() {
        assertThat(masker.mascarar(null)).isNull();
        assertThat(masker.mascarar("   ")).isNull();
        assertThat(masker.mascarar("12345")).isEqualTo("****");
    }

    @Test
    void deveFormatarCompletoComDezEOnzeDigitos() {
        assertThat(masker.formatarCompleto("15999991234")).isEqualTo("(15) 99999-1234");
        assertThat(masker.formatarCompleto("1532221234")).isEqualTo("(15) 3222-1234");
        assertThat(masker.formatarCompleto(null)).isNull();
        assertThat(masker.formatarCompleto("")).isNull();
    }
}
