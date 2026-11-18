package br.org.amde.digital.domain.entity;

import br.org.amde.digital.domain.enums.AcaoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * Entidade estritamente imutavel (append-only) de trilha de auditoria (SPEC-005).
 */
@Entity
@Immutable
@Table(name = "log_auditoria", schema = "amde_digital")
@Getter
@NoArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", nullable = false, updatable = false, columnDefinition = "text")
    private AcaoAuditoria acao;

    @Column(name = "recurso", nullable = false, updatable = false, columnDefinition = "text")
    private String recurso;

    @Column(name = "ip_origem", updatable = false, columnDefinition = "text")
    private String ipOrigem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public LogAuditoria(Long usuarioId, AcaoAuditoria acao, String recurso, String ipOrigem, OffsetDateTime criadoEm) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.recurso = recurso;
        this.ipOrigem = ipOrigem;
        this.criadoEm = criadoEm != null ? criadoEm : OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "LogAuditoria{id=" + id + ", usuarioId=" + usuarioId + ", acao=" + acao + ", recurso='" + recurso + "'}";
    }
}
