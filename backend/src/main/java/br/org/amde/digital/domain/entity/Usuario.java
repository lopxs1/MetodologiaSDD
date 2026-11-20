package br.org.amde.digital.domain.entity;

import br.org.amde.digital.domain.enums.NivelAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario", schema = "amde_digital")
@SQLRestriction("excluido_em IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, columnDefinition = "text")
    private String nome;

    @Column(name = "email", nullable = false, columnDefinition = "text")
    private String email;

    @Column(name = "senha", nullable = false, columnDefinition = "text")
    private String senha;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_acesso", nullable = false, columnDefinition = "text")
    private NivelAcesso nivelAcesso;

    @Column(name = "num_celular", columnDefinition = "text")
    private String numCelular;

    @Column(name = "pais", columnDefinition = "text")
    private String pais;

    @Column(name = "estado", columnDefinition = "text")
    private String estado;

    @Column(name = "cidade", columnDefinition = "text")
    private String cidade;

    @Column(name = "rua", columnDefinition = "text")
    private String rua;

    @Generated(event = EventType.INSERT)
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime atualizadoEm;

    @Column(name = "excluido_em")
    private OffsetDateTime excluidoEm;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Funcionario funcionario;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Paciente paciente;

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nivelAcesso=" + nivelAcesso + "}";
    }
}
