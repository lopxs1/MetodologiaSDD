# Diagrama de Classes — AMDE Digital

> **Referência Acadêmica:** Capítulo 4.3.3 da Monografia (Figura 04)  
> **Camada de Domínio / Backend:** Java 17 com Spring Data JPA

---

### Figura – Diagrama de Classes da Camada de Domínio

```mermaid
classDiagram
    class NivelAcesso {
        <<enumeration>>
        ADMINISTRADOR
        MEDICO
        PACIENTE
    }

    class GrauSuporte {
        <<enumeration>>
        NIVEL_1
        NIVEL_2
        NIVEL_3
    }

    class TipoAgendamento {
        <<enumeration>>
        CONSULTA
        EXAME
        RETORNO
        AVALIACAO
        TERAPIA
    }

    class StatusAgendamento {
        <<enumeration>>
        AGENDADO
        CONCLUIDO
        CANCELADO
    }

    class AcaoAuditoria {
        <<enumeration>>
        LOGIN
        UPLOAD
        DOWNLOAD
        EXCLUSAO
        AGENDAMENTO
    }

    class Usuario {
        -Long id
        -String nome
        -String email
        -String senha
        -LocalDate dataNascimento
        -NivelAcesso nivelAcesso
        -String numCelular
        -String pais
        -String estado
        -String cidade
        -String rua
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
        -OffsetDateTime excluidoEm
        +autenticar(senha) Boolean
    }

    class Funcionario {
        -Long id
        -Long usuarioId
        -String crm
        -String cargo
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
        -OffsetDateTime excluidoEm
    }

    class Paciente {
        -Long id
        -Long usuarioId
        -String descricao
        -GrauSuporte grauSuporte
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
        -OffsetDateTime excluidoEm
    }

    class Categoria {
        -Long id
        -String nome
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
    }

    class RegistroProntuario {
        -Long id
        -Long pacienteId
        -Long categoriaId
        -Long registroSubstituidoId
        -String tituloDocumento
        -String nomeArquivoOriginal
        -String nomeArquivoArmazenado
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
        -OffsetDateTime excluidoEm
        +substituirPor(novoRegistro) void
    }

    class Agendamento {
        -Long id
        -Long funcionarioId
        -OffsetDateTime dataInicio
        -OffsetDateTime dataFim
        -TipoAgendamento tipoAgendamento
        -StatusAgendamento status
        -Boolean possuiPresencaFuncionario
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
        +verificarConflito(inicio, fim) Boolean
    }

    class AgendamentoPacienteId {
        -Long agendamentoId
        -Long pacienteId
    }

    class AgendamentoPaciente {
        -AgendamentoPacienteId id
        -Boolean possuiPresenca
        -OffsetDateTime criadoEm
        -OffsetDateTime atualizadoEm
    }

    class LogAuditoria {
        -Long id
        -Long usuarioId
        -AcaoAuditoria acao
        -String recurso
        -String ipOrigem
        -OffsetDateTime criadoEm
    }

    Usuario "1" -- "0..1" Funcionario : especializa
    Usuario "1" -- "0..1" Paciente : especializa
    Usuario "1" --> "1" NivelAcesso : possui
    Paciente "1" --> "1" GrauSuporte : possui

    Paciente "1" o-- "0..*" RegistroProntuario : possui documentos
    Categoria "1" o-- "0..*" RegistroProntuario : categoriza
    RegistroProntuario "0..1" --> "0..1" RegistroProntuario : registroSubstituidoId

    Funcionario "1" o-- "0..*" Agendamento : conduz sessoes
    Agendamento "1" --> "1" TipoAgendamento : classifica
    Agendamento "1" --> "1" StatusAgendamento : possui
    Agendamento "1" *-- "1..*" AgendamentoPaciente : agendado para
    Paciente "1" o-- "0..*" AgendamentoPaciente : participa de
    AgendamentoPaciente *-- AgendamentoPacienteId : chave composta

    Usuario "1" --> "0..*" LogAuditoria : gera registro
    LogAuditoria "1" --> "1" AcaoAuditoria : registra
```
<div align="center">
    <sub><b>Fonte:</b> Autores (Monografia AMDE Digital / Fatec Sorocaba).</sub>
</div> 
