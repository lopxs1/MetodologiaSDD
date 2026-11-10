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
        -Long idUsuario
        -String nome
        -String email
        -String senha
        -LocalDate dataNasc
        -NivelAcesso nivelAcesso
        -String numCelular
        -String pais
        -String estado
        -String cidade
        -String rua
        -Boolean ativo
        -LocalDateTime criadoEm
        -LocalDateTime atualizadoEm
        +autenticar(senha) Boolean
    }

    class Funcionario {
        -Long idFuncionario
        -String crm
        -String cargo
    }

    class Paciente {
        -Long idPaciente
        -String descPaciente
        -GrauSuporte grauSuporte
    }

    class Categoria {
        -Long idCategoria
        -String nome
    }

    class RegistroProntuario {
        -Long idProntuario
        -String tituloDocumento
        -String nomeArquivoOriginal
        -String nomeArquivoArmazenado
        -LocalDateTime dataRegistro
        -Boolean ativo
        +substituirPor(novoRegistro) void
    }

    class Agendamento {
        -Long idAgenda
        -LocalDateTime dataInicio
        -LocalDateTime dataFinal
        -TipoAgendamento tipoAgendamento
        -StatusAgendamento status
        -Boolean presencaFuncionario
        +verificarConflito(inicio, fim) Boolean
    }

    class AgendaPaciente {
        -Long idAgendaPaciente
        -Boolean presencaPaciente
    }

    class LogAuditoria {
        -Long idLog
        -AcaoAuditoria acao
        -String recurso
        -String ip
        -LocalDateTime dataHora
    }

    Usuario <|-- Funcionario : Especialização
    Usuario <|-- Paciente : Especialização
    Usuario "1" --> "1" NivelAcesso : possui
    Paciente "1" --> "1" GrauSuporte : possui

    Paciente "1" o-- "0..*" RegistroProntuario : possui documentos
    Categoria "1" o-- "0..*" RegistroProntuario : categoriza
    RegistroProntuario "0..1" --> "0..1" RegistroProntuario : idRegistroSubstituido

    Funcionario "1" o-- "0..*" Agendamento : conduz sessões
    Agendamento "1" --> "1" TipoAgendamento : classifica
    Agendamento "1" --> "1" StatusAgendamento : possui
    Agendamento "1" *-- "1..*" AgendaPaciente : agendado para
    Paciente "1" o-- "0..*" AgendaPaciente : participa de

    Usuario "1" --> "0..*" LogAuditoria : gera registro
    LogAuditoria "1" --> "1" AcaoAuditoria : registra
```
<div align="center">
    <sub><b>Fonte:</b> Autores (Monografia AMDE Digital / Fatec Sorocaba).</sub>
</div> 
