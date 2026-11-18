package br.org.amde.digital.service.auditoria;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecursoAuditoriaFormatterTest {

    private final RecursoAuditoriaFormatter formatter = new RecursoAuditoriaFormatter();

    @Test
    void deveFormatarRecursosCanonicosDeUsuarioDocumentoEAgendamento() {
        assertThat(formatter.formatarLogin(1L)).isEqualTo("usuario:1:LOGIN");
        assertThat(formatter.formatarExclusaoUsuario(2L)).isEqualTo("usuario:2:EXCLUSAO");
        assertThat(formatter.formatarUploadDocumento(10L)).isEqualTo("documento:10:UPLOAD");
        assertThat(formatter.formatarSubstituicaoDocumento(11L)).isEqualTo("documento:11:SUBSTITUICAO");
        assertThat(formatter.formatarDownloadDocumento(12L)).isEqualTo("documento:12:DOWNLOAD");
        assertThat(formatter.formatarExclusaoDocumento(13L)).isEqualTo("documento:13:EXCLUSAO");
        assertThat(formatter.formatarAgendamento(5L, "CRIAR")).isEqualTo("agendamento:5:CRIAR");
    }

    @Test
    void deveRejeitarIdentificadoresNulosOuNaoPositivos() {
        assertThatThrownBy(() -> formatter.formatarLogin(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.formatarExclusaoUsuario(0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.formatarUploadDocumento(-3L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarOperacaoDeAgendamentoInvalida() {
        assertThatThrownBy(() -> formatter.formatarAgendamento(5L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.formatarAgendamento(5L, "REMOVER"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarTextoLivreOuPiiEmValidarRecurso() {
        assertThat(formatter.validarRecurso("usuario:15:LOGIN")).isEqualTo("usuario:15:LOGIN");

        assertThatThrownBy(() -> formatter.validarRecurso(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.validarRecurso("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.validarRecurso("paciente:Joao Silva"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> formatter.validarRecurso("usuario:1:LOGIN:" + "A".repeat(250)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
