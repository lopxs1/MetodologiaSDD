package br.org.amde.digital.service.auditoria;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Formata e valida o identificador tecnico canonico do recurso auditado (RN47, RNF-06.4),
 * impedindo inclusao de nomes, titulos ou conteudo clinico em {@code log_auditoria.recurso}.
 */
@Component
public class RecursoAuditoriaFormatter {

    private static final Pattern PADRAO_CANONICO =
            Pattern.compile("^(usuario|documento|agendamento):[0-9]+(:[A-Z_]+)?$");
    private static final Set<String> OPERACOES_AGENDAMENTO =
            Set.of("CRIAR", "EDITAR", "CANCELAR", "CONCLUIR", "PRESENCA");

    public String formatarLogin(Long usuarioId) {
        validarId(usuarioId, "usuarioId");
        return "usuario:" + usuarioId + ":LOGIN";
    }

    public String formatarExclusaoUsuario(Long usuarioId) {
        validarId(usuarioId, "usuarioId");
        return "usuario:" + usuarioId + ":EXCLUSAO";
    }

    public String formatarUploadDocumento(Long documentoId) {
        validarId(documentoId, "documentoId");
        return "documento:" + documentoId + ":UPLOAD";
    }

    public String formatarSubstituicaoDocumento(Long documentoId) {
        validarId(documentoId, "documentoId");
        return "documento:" + documentoId + ":SUBSTITUICAO";
    }

    public String formatarDownloadDocumento(Long documentoId) {
        validarId(documentoId, "documentoId");
        return "documento:" + documentoId + ":DOWNLOAD";
    }

    public String formatarExclusaoDocumento(Long documentoId) {
        validarId(documentoId, "documentoId");
        return "documento:" + documentoId + ":EXCLUSAO";
    }

    public String formatarAgendamento(Long agendamentoId, String operacao) {
        validarId(agendamentoId, "agendamentoId");
        if (operacao == null || !OPERACOES_AGENDAMENTO.contains(operacao)) {
            throw new IllegalArgumentException("Operacao de agendamento invalida para auditoria");
        }
        return "agendamento:" + agendamentoId + ":" + operacao;
    }

    public String validarRecurso(String recurso) {
        if (recurso == null || recurso.isBlank() || recurso.length() > 255) {
            throw new IllegalArgumentException("Recurso de auditoria fora do limite permitido");
        }
        if (!PADRAO_CANONICO.matcher(recurso).matches()) {
            throw new IllegalArgumentException("Formato de recurso de auditoria invalido pela RN47");
        }
        return recurso;
    }

    private void validarId(Long id, String nomeCampo) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(nomeCampo + " deve ser positivo");
        }
    }
}
