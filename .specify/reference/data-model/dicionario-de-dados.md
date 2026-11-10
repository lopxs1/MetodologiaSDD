# Dicionário de Dados — AMDE Digital

> **Referência:** Modelo Físico do Banco de Dados PostgreSQL 16  
> **Controle de Versão:** Liquibase Changelogs  
> **Conformidade:** LGPD e Constituição do Projeto (Seção 3)

---

### 1. Tabela: `usuario`
Armazena os dados cadastrais básicos de todas as pessoas que acessam o sistema.

| Coluna | Tipo | Nulo? | Restrições | Descrição / Sensibilidade |
|---|---|---|---|---|
| `id_usuario` | `SERIAL` | Não | `PK` | Identificador único |
| `nome` | `VARCHAR(100)` | Não | - | Nome completo da pessoa |
| `email` | `VARCHAR(100)` | Não | `UNIQUE` | E-mail de login e notificação |
| `senha` | `VARCHAR(255)` | Não | - | Hash criptográfico BCrypt |
| `data_nasc` | `DATE` | Sim | - | Data de nascimento (Sensível) |
| `nivel_acesso` | `VARCHAR(20)` | Não | `CHECK` | `ADMINISTRADOR`, `MEDICO`, `PACIENTE` |
| `num_celular` | `VARCHAR(20)` | Sim | - | Contato telefônico (Sensível) |
| `pais` | `VARCHAR(50)` | Sim | - | País de residência |
| `estado` | `VARCHAR(2)` | Sim | - | UF (ex.: SP) |
| `cidade` | `VARCHAR(100)` | Sim | - | Município |
| `rua` | `VARCHAR(150)` | Sim | - | Logradouro e número |
| `ativo` | `BOOLEAN` | Não | Padrão `TRUE` | Soft delete |
| `criado_em` | `TIMESTAMPTZ` | Não | `CURRENT_TIMESTAMP` | Data de criação |
| `atualizado_em` | `TIMESTAMPTZ` | Não | `CURRENT_TIMESTAMP` | Última modificação |

---

### 2. Tabela: `funcionarios`
Extensão de dados para médicos, psicólogos, fonoaudiólogos e colaboradores.

| Coluna | Tipo | Nulo? | Restrições | Descrição |
|---|---|---|---|---|
| `id_funcionario` | `SERIAL` | Não | `PK` | Identificador do profissional |
| `id_usuario` | `INT` | Não | `FK -> usuario.id_usuario`, `UNIQUE` | Vínculo 1:1 com usuário |
| `crm` | `VARCHAR(30)` | Sim | - | Registro de conselho profissional |
| `cargo` | `VARCHAR(50)` | Não | - | Especialidade ou função clínica |

---

### 3. Tabela: `pacientes`
Extensão de dados clínicos das crianças e adolescentes atendidos pela AMDE.

| Coluna | Tipo | Nulo? | Restrições | Descrição |
|---|---|---|---|---|
| `id_paciente` | `SERIAL` | Não | `PK` | Identificador do paciente |
| `id_usuario` | `INT` | Não | `FK -> usuario.id_usuario`, `UNIQUE` | Vínculo 1:1 com usuário |
| `desc_paciente` | `TEXT` | Sim | - | Histórico e observações do quadro |
| `grau_suporte` | `VARCHAR(20)` | Não | `CHECK` | Grau de suporte TEA (`Nivel 1`, `Nivel 2`, `Nivel 3`) |

---

### 4. Tabela: `registro_prontuario`
Metadados e referências dos arquivos em PDF de prontuários, exames e laudos.

| Coluna | Tipo | Nulo? | Restrições | Descrição |
|---|---|---|---|---|
| `id_prontuario` | `SERIAL` | Não | `PK` | Identificador do documento |
| `id_paciente` | `INT` | Não | `FK -> pacientes.id_paciente` | Paciente associado |
| `id_categoria` | `INT` | Não | `FK -> categoria.id_categoria` | Categoria do documento |
| `id_registro_substituido` | `INT` | Sim | `FK -> registro_prontuario.id_prontuario` | Versionamento de anexo |
| `titulo_documento` | `VARCHAR(150)` | Não | - | Título descritivo |
| `nome_arquivo_original` | `VARCHAR(255)` | Não | - | Nome original enviado |
| `nome_arquivo_armazenado` | `VARCHAR(255)` | Não | `UNIQUE` | UUID seguro salvo no storage |
| `data_registro` | `TIMESTAMPTZ` | Não | `CURRENT_TIMESTAMP` | Data do envio |
| `ativo` | `BOOLEAN` | Não | Padrão `TRUE` | Soft delete do prontuário |

---

### 5. Tabela: `agendamentos` e `agenda_paciente`
Gerenciamento de agenda, disponibilidade de horários e presença.

| Tabela | Colunas Principais | Restrições | Papel |
|---|---|---|---|
| `agendamentos` | `id_agenda` (PK), `id_funcionario` (FK), `data_inicio`, `data_final`, `tipo_agendamento`, `status`, `presenca_funcionario` | `CHECK(data_inicio < data_final)` | Sessão clínica do profissional |
| `agenda_paciente` | `id_agenda_paciente` (PK), `id_agenda` (FK), `id_paciente` (FK), `presenca_paciente` | `UNIQUE(id_agenda, id_paciente)` | Associativa de presença do paciente |

---

### 6. Tabela: `log_auditoria`
Trilha de auditoria das operações de dados sensíveis e acessos clínicos (LGPD / CFM).

| Coluna | Tipo | Nulo? | Restrições | Descrição |
|---|---|---|---|---|
| `id_log` | `SERIAL` | Não | `PK` | Identificador do log |
| `id_usuario` | `INT` | Não | `FK -> usuario.id_usuario` | Usuário responsável |
| `acao` | `VARCHAR(50)` | Não | - | `LOGIN`, `UPLOAD_PRONTUARIO`, `DOWNLOAD`, `EXCLUSAO` |
| `recurso` | `VARCHAR(100)` | Não | - | Identificador do registro acessado |
| `ip` | `VARCHAR(45)` | Sim | - | Endereço IP de origem |
| `data_hora` | `TIMESTAMPTZ` | Não | `CURRENT_TIMESTAMP` | Timestamp do evento |

