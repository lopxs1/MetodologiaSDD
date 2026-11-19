package br.org.amde.digital.dto.paciente;

import br.org.amde.digital.domain.enums.SituacaoDocumento;

import java.time.OffsetDateTime;

public record DocumentoHistoricoResumoDTO(
        Long id,
        String tituloDocumento,
        String nomeArquivoOriginal,
        String categoria,
        SituacaoDocumento situacao,
        Long registroSubstituidoId,
        OffsetDateTime criadoEm
) {
}

