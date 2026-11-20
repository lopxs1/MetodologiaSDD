package br.org.amde.digital.dto.prontuario;

import br.org.amde.digital.domain.enums.SituacaoDocumento;

import java.time.OffsetDateTime;

public record ProntuarioResponse(
        Long id,
        Long pacienteId,
        String categoria,
        Long registroSubstituidoId,
        String tituloDocumento,
        String nomeArquivoOriginal,
        SituacaoDocumento situacao,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}

