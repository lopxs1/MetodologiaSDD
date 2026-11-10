# Diagrama de Entidade-Relacionamento (DER) — AMDE Digital

> **Referência Acadêmica:** Capítulo 4.3 da Monografia  
> **Modelo Conceitual Global SDD:** [`.specify/memory/domain-model.md`](../../.specify/memory/domain-model.md)  
> **SGBD:** PostgreSQL 16 (Relacional ACID)

---

### Figura – Modelo de Entidade-Relacionamento do AMDE Digital

```mermaid
erDiagram
    usuario ||--o| funcionarios : "1:1 especializacao"
    usuario ||--o| pacientes : "1:1 especializacao"
    usuario ||--o{ log_auditoria : "dispara acoes"

    pacientes ||--o{ registro_prontuario : "possui"
    pacientes ||--o{ agenda_paciente : "vincula-se"

    categoria ||--o{ registro_prontuario : "classifica"
    registro_prontuario |o--o| registro_prontuario : "substitui"

    funcionarios ||--o{ agendamentos : "responsavel por"
    agendamentos ||--|{ agenda_paciente : "composto por"

    usuario {
        serial id_usuario PK
        varchar nome
        varchar email UK
        varchar senha
        date data_nasc
        varchar nivel_acesso
        varchar num_celular
        varchar pais
        varchar estado
        varchar cidade
        varchar rua
        boolean ativo
        timestamptz criado_em
        timestamptz atualizado_em
    }

    funcionarios {
        serial id_funcionario PK
        int id_usuario FK
        varchar crm
        varchar cargo
    }

    pacientes {
        serial id_paciente PK
        int id_usuario FK
        varchar desc_paciente
        varchar grau_suporte
    }

    categoria {
        serial id_categoria PK
        varchar nome UK
    }

    registro_prontuario {
        serial id_prontuario PK
        int id_paciente FK
        int id_categoria FK
        int id_registro_substituido FK
        varchar titulo_documento
        varchar nome_arquivo_original
        varchar nome_arquivo_armazenado
        timestamptz data_registro
        boolean ativo
    }

    agendamentos {
        serial id_agenda PK
        int id_funcionario FK
        timestamptz data_inicio
        timestamptz data_final
        varchar tipo_agendamento
        varchar status
        boolean presenca_funcionario
    }

    agenda_paciente {
        serial id_agenda_paciente PK
        int id_agenda FK
        int id_paciente FK
        boolean presenca_paciente
    }

    log_auditoria {
        serial id_log PK
        int id_usuario FK
        varchar acao
        varchar recurso
        varchar ip
        timestamptz data_hora
    }
```

<div align="center">
<sub><b>Fonte:</b> Autores (Monografia AMDE Digital / Fatec Sorocaba).</sub>
</div>

---

### Descrição dos Relacionamentos

1. **`usuario` ➔ `funcionarios` / `pacientes` ($1:0..1$):** Especialização de perfil. Cada conta de usuário pode ser associada a no máximo um registro de funcionário ou de paciente.
2. **`pacientes` ➔ `registro_prontuario` ($1:N$):** Um paciente assistido possui zero ou múltiplos documentos clínicos arquivados no PEP.
3. **`categoria` ➔ `registro_prontuario` ($1:N$):** Toda ficha pertence a uma categoria pré-definida (Exames, Laudos, Receituário, Arquivado).
4. **`registro_prontuario` ➔ `registro_prontuario` ($0..1:0..1$):** Auto-relacionamento que permite versionar a substituição de documentos obsoletos.
5. **`funcionarios` ➔ `agendamentos` ($1:N$):** Cada compromisso na agenda clínica é conduzido por um profissional responsável.
6. **`agendamentos` ➔ `agenda_paciente` ➔ `pacientes` ($N:N$):** Tabela associativa que viabiliza o vínculo de pacientes às consultas e registra o comparecimento individual.

