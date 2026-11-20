# Diagrama de Entidade-Relacionamento (DER) — AMDE Digital

> **Referência Acadêmica:** Capítulo 4.3 da Monografia  
> **Modelo Conceitual Global SDD:** [`memory/domain-model.md`](../../memory/domain-model.md)  
> **SGBD:** PostgreSQL 18 (Relacional ACID — Schema `amde_digital`)

---

### Figura – Modelo de Entidade-Relacionamento do AMDE Digital

```mermaid
erDiagram
    usuario ||--o| funcionario : "1 para 0..1 especializacao"
    usuario ||--o| paciente : "1 para 0..1 especializacao"
    usuario ||--o{ log_auditoria : "dispara acoes"

    paciente ||--o{ registro_prontuario : "possui"
    paciente ||--o{ agendamento_paciente : "vincula-se"

    categoria ||--o{ registro_prontuario : "classifica"
    registro_prontuario |o--o| registro_prontuario : "substitui"

    funcionario ||--o{ agendamento : "responsavel por"
    agendamento ||--|{ agendamento_paciente : "composto por"

    usuario {
        bigint id PK
        text nome
        text email
        text senha
        date data_nascimento
        text nivel_acesso
        telefone num_celular
        text pais
        text estado
        text cidade
        text rua
        timestamptz criado_em
        timestamptz atualizado_em
        timestamptz excluido_em
    }

    funcionario {
        bigint id PK
        bigint usuario_id FK
        text crm
        text cargo
        timestamptz criado_em
        timestamptz atualizado_em
        timestamptz excluido_em
    }

    paciente {
        bigint id PK
        bigint usuario_id FK
        text descricao
        text grau_suporte
        timestamptz criado_em
        timestamptz atualizado_em
        timestamptz excluido_em
    }

    categoria {
        bigint id PK
        text nome UK
        timestamptz criado_em
        timestamptz atualizado_em
    }

    registro_prontuario {
        bigint id PK
        bigint paciente_id FK
        bigint categoria_id FK
        bigint registro_substituido_id FK
        text titulo_documento
        text nome_arquivo_original
        text nome_arquivo_armazenado UK
        timestamptz criado_em
        timestamptz atualizado_em
        timestamptz excluido_em
    }

    agendamento {
        bigint id PK
        bigint funcionario_id FK
        timestamptz data_inicio
        timestamptz data_fim
        text tipo_agendamento
        text status
        boolean possui_presenca_funcionario
        timestamptz criado_em
        timestamptz atualizado_em
    }

    agendamento_paciente {
        bigint agendamento_id PK,FK
        bigint paciente_id PK,FK
        boolean possui_presenca
        timestamptz criado_em
        timestamptz atualizado_em
    }

    log_auditoria {
        bigint id PK
        bigint usuario_id FK
        text acao
        text recurso
        text ip_origem
        timestamptz criado_em
    }
```

<div align="center">
<sub><b>Fonte:</b> Autores (Monografia AMDE Digital / Fatec Sorocaba, adequado ao padrão PostgreSQL 18).</sub>
</div>

---

### Descrição dos Relacionamentos

1. **`usuario` ➔ `funcionario` / `paciente` ($1:0..1$):** Especialização de perfil via chave estrangeira única `usuario_id`. Cada conta de usuário pode ser associada a no máximo um registro de funcionário ou de paciente.
2. **`paciente` ➔ `registro_prontuario` ($1:N$):** Um paciente assistido possui zero ou múltiplos documentos clínicos arquivados no PEP (`paciente_id`).
3. **`categoria` ➔ `registro_prontuario` ($1:N$):** Toda ficha pertence a uma categoria pré-definida (`Exames`, `Laudos`, `Receituario`, `Arquivado`) via `categoria_id`.
4. **`registro_prontuario` ➔ `registro_prontuario` ($0..1:0..1$):** Auto-relacionamento via `registro_substituido_id` que permite versionar a substituição de documentos obsoletos.
5. **`funcionario` ➔ `agendamento` ($1:N$):** Cada compromisso na agenda clínica é conduzido por um profissional responsável (`funcionario_id`).
6. **`agendamento` ➔ `agendamento_paciente` ➔ `paciente` ($N:N$):** Tabela associativa com chave primária composta `(agendamento_id, paciente_id)` que viabiliza o vínculo de pacientes às consultas e registra o comparecimento individual (`possui_presenca`).
7. **`usuario` ➔ `log_auditoria` ($1:N$):** Trilha de auditoria imutável (*append-only*) vinculada ao usuário atuante (`usuario_id`).

