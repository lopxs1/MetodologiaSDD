# Modelo de Domínio e Glossário: AMDE Digital

> **Projeto:** AMDE Digital — Controle de Prontuário  
> **Status:** Consolidado (Alinhado ao Padrão PostgreSQL 18)  
> **Versão:** 2.0.0  
> **Data:** 2026-11-14  
> **Finalidade:** Servir como fonte única da verdade para o Modelo Conceitual (DER), Linguagem Ubíqua e Invariantes de Domínio para toda a equipe e ferramentas de IA.

---

## 1. Visão Geral do Domínio

A **Associação Amigos dos Deficientes (AMDE)** é uma Organização da Sociedade Civil (OSC) sem fins lucrativos de Sorocaba/SP, dedicada desde 2010 ao atendimento multidisciplinar e reabilitação de crianças e adolescentes com **Transtorno do Espectro Autista (TEA)** por meio do seu *Centro de Excelência em Autismo*. 

A equipe reúne profissionais de Serviço Social, Fonoaudiologia, Terapia Ocupacional, Educação Física, Pedagogia, Psicologia, Fisioterapia, Psiquiatria e Enfermagem. 

O sistema **AMDE Digital** substitui o controle manual e os registros físicos em papel por uma plataforma integrada de:
1. **Gestão de Usuários e Controle de Acesso:** Baseado em perfis (`ADMINISTRADOR`, `FUNCIONARIO`/`MEDICO` e `PACIENTE`).
2. **Prontuário Eletrônico do Paciente (PEP):** Em conformidade com a Resolução CFM nº 1.821/2007 e LGPD (Lei nº 13.709/2018), com upload e visualização de documentos digitais (laudos, exames, receitas) categorizados e auditados.
3. **Gestão de Agendamentos e Presença:** Organização de horários clínicos dos profissionais, vinculação de pacientes a consultas e controle individual de comparecimento.
4. **Auditoria e Rastreabilidade:** Trilha estrita de logs para leitura e manipulação de informações clínicas confidenciais.

---

## 2. Modelo Conceitual de Dados (DER em Mermaid)

```mermaid
erDiagram
    USUARIO ||--o| FUNCIONARIO : "especializa-se em"
    USUARIO ||--o| PACIENTE : "especializa-se em"
    USUARIO ||--o{ LOG_AUDITORIA : "gera acao de"

    PACIENTE ||--o{ REGISTRO_PRONTUARIO : "possui historico de"
    PACIENTE ||--o{ AGENDAMENTO_PACIENTE : "vincula-se a"

    CATEGORIA ||--o{ REGISTRO_PRONTUARIO : "classifica"
    REGISTRO_PRONTUARIO |o--o| REGISTRO_PRONTUARIO : "substitui/versiona"

    FUNCIONARIO ||--o{ AGENDAMENTO : "conduz consulta de"
    AGENDAMENTO ||--|{ AGENDAMENTO_PACIENTE : "composto por"

    USUARIO {
        bigint id PK "Identificador unico do usuario"
        text nome "Nome completo"
        text email "E-mail unico entre usuarios ativos"
        text senha "Hash criptografado (BCrypt)"
        date data_nascimento "Data de nascimento"
        text nivel_acesso "ADMINISTRADOR | MEDICO | PACIENTE"
        telefone num_celular "Telefone / WhatsApp sem mascara (10 ou 11 digitos)"
        text pais "Pais de residencia"
        text estado "Unidade federativa (UF)"
        text cidade "Municipio"
        text rua "Logradouro e numero"
        timestamptz criado_em "Data e hora de criacao"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
        timestamptz excluido_em "Data e hora da inativacao logica (NULL = ativo)"
    }

    FUNCIONARIO {
        bigint id PK "Identificador do profissional"
        bigint usuario_id FK "Vinculo 1:1 com Usuario"
        text crm "Registro de conselho de classe profissional"
        text cargo "Especialidade ou funcao clinica"
        timestamptz criado_em "Data e hora de criacao"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
        timestamptz excluido_em "Data e hora da inativacao logica (NULL = ativo)"
    }

    PACIENTE {
        bigint id PK "Identificador clinico do paciente"
        bigint usuario_id FK "Vinculo 1:1 com Usuario"
        text descricao "Historico e observacoes clinicas do quadro"
        text grau_suporte "NIVEL_1 | NIVEL_2 | NIVEL_3"
        timestamptz criado_em "Data e hora de criacao"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
        timestamptz excluido_em "Data e hora da inativacao logica (NULL = ativo)"
    }

    CATEGORIA {
        bigint id PK "Identificador da categoria"
        text nome "Exames | Laudos | Receituario | Arquivado"
        timestamptz criado_em "Data e hora de criacao"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
    }

    REGISTRO_PRONTUARIO {
        bigint id PK "Identificador do documento"
        bigint paciente_id FK "Paciente titular do prontuario"
        bigint categoria_id FK "Categoria do documento anexado"
        bigint registro_substituido_id FK "Auto-relacionamento opcional de versao"
        text titulo_documento "Titulo legivel do anexo"
        text nome_arquivo_original "Nome original enviado pelo usuario"
        text nome_arquivo_armazenado "UUID unico no storage seguro"
        timestamptz criado_em "Data e hora do upload"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
        timestamptz excluido_em "Data e hora do arquivamento/exclusao logica (NULL = ativo)"
    }

    AGENDAMENTO {
        bigint id PK "Identificador da sessao clinica"
        bigint funcionario_id FK "Profissional de saude responsavel"
        timestamptz data_inicio "Horario de inicio da sessao"
        timestamptz data_fim "Horario de encerramento da sessao"
        text tipo_agendamento "CONSULTA | EXAME | RETORNO | AVALIACAO | TERAPIA"
        text status "AGENDADO | CONCLUIDO | CANCELADO"
        boolean possui_presenca_funcionario "Confirmacao de presenca do profissional"
        timestamptz criado_em "Data e hora de criacao"
        timestamptz atualizado_em "Data e hora da ultima atualizacao"
    }

    AGENDAMENTO_PACIENTE {
        bigint agendamento_id PK,FK "Sessao clinica vinculada"
        bigint paciente_id PK,FK "Paciente participante"
        boolean possui_presenca "Confirmacao de presenca do paciente"
        timestamptz criado_em "Data e hora de criacao do vinculo"
        timestamptz atualizado_em "Data e hora da atualizacao de presenca"
    }

    LOG_AUDITORIA {
        bigint id PK "Identificador do evento de auditoria"
        bigint usuario_id FK "Usuario responsavel pela acao"
        text acao "LOGIN | UPLOAD | DOWNLOAD | EXCLUSAO | AGENDAMENTO"
        text recurso "Recurso ou identificador acessado"
        text ip_origem "Endereco IP de origem"
        timestamptz criado_em "Data e hora do evento (imutavel)"
    }
```

---

## 3. Legenda das Cardinalidades em Português

Para uso nas reuniões com os gestores e profissionais da AMDE:

| Símbolo | Notação Técnica | Leitura em Linguagem Natural | Exemplo Prático no Domínio da AMDE |
|---|---|---|---|
| `\|\|` | `1..1` | **Exatamente um** | Todo `PACIENTE` está vinculado a **exatamente um** `USUARIO`. |
| `\|o` ou `o\|` | `0..1` | **Zero ou um** (opcional) | Um `USUARIO` pode ser especializado em **zero ou um** `FUNCIONARIO`. Um documento pode substituir **zero ou um** prontuário anterior. |
| `\|{` | `1..n` | **Um ou mais** (obrigatório múltiplo) | Todo `AGENDAMENTO` deve possuir **um ou mais** registros na associativa `AGENDAMENTO_PACIENTE`. |
| `o{` | `0..n` | **Zero ou mais** (opcional múltiplo) | Um `PACIENTE` pode ter **zero ou mais** documentos de `REGISTRO_PRONTUARIO`. |

---

## 4. Divisão de Escopo do Modelo

| Elemento / Aspecto | Onde é Definido | Por quê |
|---|---|---|
| **Entidades, Atributos Principais e Vínculos Estruturais** | `domain-model.md` (Diagrama acima) | Garante visão única e compartilhada de toda a arquitetura de dados entre frontend, backend e direção. |
| **Validação de Sobreposição de Horários de Agendamento** | `spec.md` (RN13) e Código do Serviço | Regra dinâmica e contextual dependente de consulta temporal em banco de dados (`data_inicio < data_fim`). |
| **Limites de Tamanho e Extensão de Arquivo (PDF < 10MB)** | `spec.md` (RN05) e Contratos | Validação de requisição HTTP e segurança de infraestrutura. |
| **Formato e Armazenamento Físico de Arquivos em Storage** | `plan.md` e Constituição | Detalhes de infraestrutura de buckets S3 e estrutura `uploads/prontuarios/paciente-{id}/{uuid}.pdf`. |
| **Regras de Mascaramento de CPF / Dados Pessoais** | `constitution.md` e `design-system.md` | Requisito não funcional transversal de conformidade à LGPD. |

---

## 5. Questões em Aberto e Validações Pendentes com a Instituição

- [ ] **Múltiplos Pacientes no mesmo Agendamento:** A estrutura atual suporta agendamentos coletivos (oficinas ou terapias em grupo) via `AGENDAMENTO_PACIENTE`. Confirmar se o fluxo da interface deve permitir seleção múltipla de pacientes para sessões em grupo de TEA.
- [ ] **Tempo de Retenção de Prontuários (LGPD / CFM):** A Resolução CFM nº 1.821/2007 prevê prazo de guarda de 20 anos para prontuários eletrônicos. Confirmar se a AMDE exige política automática de arquivamento após inativação.
- [ ] **Tipos Adicionais de Agendamento:** Confirmar se além de Consulta, Exame, Retorno, Avaliação e Terapia haverá novos tipos institucionais (ex.: "Visita Domiciliar", "Reunião de Equipe").

---

## 6. Glossário do Projeto (Linguagem Ubíqua)

| Termo do Negócio | Nome no Código | Definição no Contexto AMDE | Termos Desaconselhados |
|---|---|---|---|
| **Usuário** | `Usuario` / `User` | Conta básica de acesso à plataforma com credenciais autenticáveis. | Cliente, Login, Pessoa genérica |
| **Funcionário / Profissional** | `Funcionario` / `Employee` | Profissional de saúde ou colaborador administrativo da AMDE vinculado a um usuário. | Prestador, Operador |
| **Paciente** | `Paciente` / `Patient` | Criança ou adolescente assistido pela AMDE com diagnóstico de TEA. | Aluno, Beneficiário, Cliente |
| **Grau de Suporte** | `grau_suporte` | Nível de suporte para Transtorno do Espectro Autista (`NIVEL_1`, `NIVEL_2`, `NIVEL_3`). | Gravidade, Tipo de autismo |
| **Prontuário Digital / PEP** | `RegistroProntuario` | Registro digital estruturado de documento clínico anexado em PDF (laudo, exame, receituário). | Ficha de papel, Arquivo avulso |
| **Categoria de Prontuário** | `Categoria` | Classificação do arquivo digital (Exames, Laudos, Receituário, Arquivado). | Pasta, Tipo genérico |
| **Agendamento** | `Agendamento` / `Appointment` | Compromisso clínico com data/hora de início e fim conduzido por um profissional. | Consulta simples, Sessão solta |
| **Vínculo de Presença** | `AgendamentoPaciente` (`possui_presenca_funcionario` / `possui_presenca`) | Confirmação de comparecimento registrada individualmente para o profissional e paciente. | Falta, Check-in informal |
| **Log de Auditoria** | `LogAuditoria` / `AuditLog` | Registro rastreável e imutável (*append-only*) de operações sensíveis em conformidade com a LGPD e CFM. | Histórico comum, Log de erro |

---

## 7. Invariantes Globais do Domínio

1. **Unicidade de Identidade Digital Ativa:** Todo usuário ativo (`excluido_em IS NULL`) possui e-mail exclusivo sem distinção de maiúsculas/minúsculas (`uq_usuario_email_ativo` sobre `lower(email) WHERE excluido_em IS NULL`).
2. **Especialização Exclusiva:** Todo paciente e todo funcionário derivam de uma única entidade `usuario` (`usuario_id`).
3. **Soft Delete Temporal Padronizado:** Usuários, funcionários, pacientes e registros de prontuário inativados registram o instante em `excluido_em` (`NULL` = ativo; `NOT NULL` = inativo), preservando integridade histórica e legal.
4. **Formato Obrigatório de Prontuário:** Todo arquivo anexado ao prontuário é estritamente do tipo PDF com tamanho inferior a 10MB (RN05).
5. **Armazenamento Não Colidente:** O nome do arquivo físico no disco ou S3 é sempre um identificador universal único (`UUID`), preservando o nome original apenas como metadado no banco (RN07).
6. **Consistência Temporal de Agendamento:** Nenhum agendamento pode possuir data de término anterior ou igual à data de início (`data_fim > data_inicio`) (RN12).
7. **Não Sobreposição de Agenda do Profissional:** Um funcionário não pode possuir mais de um agendamento não cancelado conflitante no mesmo intervalo de tempo (RN13).
8. **Rastreabilidade Universal Imutável:** Operações em prontuários e agendamentos disparam obrigatoriamente gravação em `log_auditoria` (*append-only*) com vínculo do usuário atuante (`usuario_id`) (RN16).
