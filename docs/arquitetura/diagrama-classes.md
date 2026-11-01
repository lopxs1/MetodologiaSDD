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
        -String grauSuporte
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
        -String tipoAgendamento
        -String status
        -Boolean presencaFuncionario
        +verificarConflito(inicio, fim) Boolean
    }

    class AgendaPaciente {
        -Long idAgendaPaciente
        -Boolean presencaPaciente
    }

    class LogAuditoria {
        -Long idLog
        -String acao
        -String recurso
        -String ip
        -LocalDateTime dataHora
    }

    Usuario <|-- Funcionario : Especialização 1:1
    Usuario <|-- Paciente : Especialização 1:1
    Usuario "1" --> "1" NivelAcesso : possui

    Paciente "1" o-- "0..*" RegistroProntuario : possui documentos
    Categoria "1" o-- "0..*" RegistroProntuario : categoriza
    RegistroProntuario "0..1" --> "0..1" RegistroProntuario : idRegistroSubstituido

    Funcionario "1" o-- "0..*" Agendamento : conduz sessões
    Agendamento "1" *-- "1..*" AgendaPaciente : agendado para
    Paciente "1" o-- "0..*" AgendaPaciente : participa de

    Usuario "1" --> "0..*" LogAuditoria : gera registro
```

<div align="center">
<sub><b>Fonte:</b> Autores (Monografia AMDE Digital / Fatec Sorocaba).</sub>
</div>

