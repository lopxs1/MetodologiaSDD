package br.org.amde.digital.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "registro_prontuario", schema = "amde_digital")
@SQLRestriction("excluido_em IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class RegistroProntuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_substituido_id")
    private RegistroProntuario registroSubstituido;

    @Column(name = "titulo_documento", nullable = false, columnDefinition = "text")
    private String tituloDocumento;

    @Column(name = "nome_arquivo_original", nullable = false, columnDefinition = "text")
    private String nomeArquivoOriginal;

    @Column(name = "nome_arquivo_armazenado", nullable = false, unique = true, columnDefinition = "text")
    private String nomeArquivoArmazenado;

    @Generated(event = EventType.INSERT)
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime atualizadoEm;

    @Column(name = "excluido_em")
    private OffsetDateTime excluidoEm;

    @Override
    public String toString() {
        return "RegistroProntuario{id=" + id + "}";
    }
}

