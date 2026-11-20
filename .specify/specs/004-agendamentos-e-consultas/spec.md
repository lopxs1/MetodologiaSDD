# Especificação Funcional: Agendamentos e Consultas

> **ID da Spec:** `SPEC-004`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-05 · UC14 a UC19 · RN11 a RN15, RN16, RN39 a RN43 · Pacote `specs/004-agendamentos-e-consultas/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
A agenda da AMDE é organizada em papel e planilhas, o que gera horários duplicados para o mesmo profissional, dificuldade para saber quem compareceu e nenhuma visão clara de quem atende quem. A equipe multidisciplinar (Fonoaudiologia, Terapia Ocupacional, Psicologia e outras) precisa de uma agenda única, sem conflitos, que também mostre ao Paciente os seus próprios atendimentos.

### 1.2. Objetivo e Valor Entregue
Permitir agendar sessões clínicas com um profissional e um ou mais pacientes, impedir conflito de horário do profissional, acompanhar a situação da sessão (agendada, concluída ou cancelada) e registrar a presença de cada participante, com cada paciente e profissional enxergando apenas o que lhe cabe.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** criação de agendamento (`UC14`), consulta detalhada por identificador (`UC15`), atualização de horário, tipo, pacientes, conclusão e registro de presença do profissional e de cada paciente (`UC16`), cancelamento mediante confirmação em modal sem exclusão física (`UC17`), verificação de conflito de horário do profissional (`UC18`), listagem com filtros e visibilidade por perfil (`UC19`), sessões com um ou mais pacientes, tipos de sessão, validação de pré-existência e de datas, e registro de eventos `AGENDAMENTO`.
- **Fora de escopo (decisões de spec):** envio de lembretes ou notificações; agendamento recorrente (série); fila de espera; remarcação automática; validação de conflito de horário do paciente (apenas a do profissional é exigida); calendário externo; exclusão física de agendamentos (cancelar preserva o histórico); tipos de sessão além dos cinco do modelo.
- **Dependências:** SPEC-001 (perfis, Médicos e Pacientes ativos), SPEC-005 (gravação do evento AGENDAMENTO). A SPEC-001 consulta esta spec para bloquear a exclusão de usuários (RN27) e a SPEC-006 consome os agendamentos para o Dashboard.

---

## 2. Atores e Perfis de Acesso

- **Administrador:** cria, consulta, edita, cancela, conclui e registra presença em agendamentos de qualquer profissional, escolhendo o profissional responsável; lista todos os agendamentos.
- **Profissional de Saúde / Médico (perfil `MEDICO`, materializado como Funcionário):** cria, consulta, edita, cancela, conclui e registra presença somente na própria agenda, sendo sempre o responsável; lista somente os seus agendamentos.
- **Paciente:** lista e consulta em detalhe apenas os próprios agendamentos; não cria nem altera nada.

---

## 3. Histórias de Usuário (User Stories)

- **US-05.1:** Como **Administrador ou Médico**, quero **agendar uma sessão com um ou mais pacientes em um horário livre do profissional** para que **a agenda não tenha conflitos**.
- **US-05.2:** Como **Administrador ou Médico**, quero **editar, cancelar ou concluir um agendamento** para que **a agenda reflita o que realmente aconteceu**.
- **US-05.3:** Como **Administrador ou Médico**, quero **registrar a presença do profissional e de cada paciente** para que **a instituição acompanhe o comparecimento**.
- **US-05.4:** Como **Administrador, Médico ou Paciente**, quero **ver a lista e os detalhes dos agendamentos que me dizem respeito, com filtros** para que **eu encontre rapidamente o compromisso desejado**.
- **US-05.5:** Como **instituição**, quero que **as operações de agendamento fiquem registradas** para que **haja rastreabilidade conforme a LGPD**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-05.1** | O sistema deve permitir criar um agendamento com profissional, um ou mais pacientes, início, fim e tipo de sessão, com situação inicial AGENDADO e presenças inicialmente não confirmadas (RN14, RN39, RN40) (UC14). | Crítica |
| **RF-05.2** | O sistema deve exigir que o profissional e todos os pacientes existam e estejam ativos, que o fim seja posterior ao início e que o início seja futuro (RN11, RN12, RN41) (UC14, UC16). | Crítica |
| **RF-05.3** | O sistema deve impedir dois agendamentos não cancelados do mesmo profissional em horários que se sobrepõem, respondendo `409 Conflict` (RN13, RN41) (UC14, UC16, UC18). | Crítica |
| **RF-05.4** | O sistema deve permitir editar horário, tipo e pacientes de um agendamento AGENDADO, revalidando datas e conflito (RN39, RN41) (UC16, UC18). | Alta |
| **RF-05.5** | O sistema deve permitir concluir (`UC16`) e cancelar (`UC17`, mediante confirmação em modal de diálogo com *backdrop*, sem exclusão física) um agendamento AGENDADO, tornando a situação final (RN39, RN42) (UC16, UC17). | Alta |
| **RF-05.6** | O sistema deve permitir registrar e corrigir a presença do profissional e de cada paciente individualmente (RN14, RN42) (UC16). | Alta |
| **RF-05.7** | O sistema deve listar agendamentos com paginação, busca, filtros (período, situação e tipo) e ordenação ASC/DESC (`UC19`), bem como consultar o detalhe de um agendamento específico por identificador (`UC15`), mostrando a cada perfil somente o que lhe cabe (RN15, RN43) (UC15, UC19). | Alta |
| **RF-05.8** | O sistema deve negar a Pacientes qualquer operação de escrita e a Médicos qualquer operação fora da própria agenda (RN43) (UC14, UC15, UC16, UC17, UC19). | Crítica |
| **RF-05.9** | O sistema deve registrar na trilha de auditoria um evento AGENDAMENTO a cada criação, edição, cancelamento, conclusão e registro de presença (RN16, SPEC-005) (UC14, UC16, UC17). | Crítica |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-05.1** | Eficiência de Desempenho | A listagem de agendamentos e a validação de conflito de horário respondem em menos de 500 ms no percentil 95 com 10.000 agendamentos. | Teste de carga com massa de dados sintética |
| **RNF-05.2** | Integridade / Concorrência | Duas requisições simultâneas para o mesmo profissional em horários sobrepostos resultam em exatamente um agendamento criado e um `409 Conflict`. | Teste automatizado de concorrência |
| **RNF-05.3** | Segurança / Confidencialidade | Um Paciente nunca recebe dados de outros pacientes, mesmo em sessões em grupo (RN43). | Teste automatizado comparando respostas de pacientes da mesma sessão |
| **RNF-05.4** | Usabilidade / Interface | Formulários, listagem e modal de confirmação de cancelamento com *backdrop* exibem feedback de sucesso e erro sem recarregar a tela, os quatro estados de tela, horários no fuso `America/Sao_Paulo` e conflito indicado junto ao campo de horário. | Teste E2E de interface |
| **RNF-05.5** | Usabilidade / Acessibilidade | Formulário, filtros, ordenação, modal de confirmação e ações de cancelar, concluir e presença funcionam por teclado (`Tab`, `Enter`, `Esc`), com foco visível e contraste mínimo de 4,5:1 (WCAG 2.1 AA). | Teste E2E e verificação com Axe DevTools |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `Agendamento` (sessão clínica), `AgendamentoPaciente` (vínculo e presença de cada paciente), `Funcionario` (profissional responsável), `Paciente` (participante), `Usuario` (nomes exibidos) e `LogAuditoria` (somente gravação, via SPEC-005).
- **Entidades Novas ou Alteradas:** nenhuma.

### 5.2. Checklist de Suficiência de Entidades (Pré-Planejamento)
*A IA e os desenvolvedores devem checar estas perguntas para cada entidade antes de avançar para o plano técnico:*
- [x] **Identidade:** O que torna a instância única (chave de negócio / natural e chave substituta)?
- [x] **Atributos e Nulabilidade:** Quais informações são obrigatórias e quais são opcionais (com justificativa clara de negócio para cada campo opcional/anulável)?
- [x] **Relacionamentos:** Quais as cardinalidades mínimas e máximas com outras entidades?
- [x] **Regras & Invariantes:** Quais restrições de domínio, limites ou estados válidos aplicam-se?
- [x] **Ciclo de Vida & Histórico:** A entidade sofre inativação lógica (soft delete via data/hora de exclusão) ou é imutável/histórica?
- [x] **Classificação LGPD e Minimização:** Quais atributos contêm dados pessoais (`pessoal`) ou clínicos/saúde (`sensível`) e qual a justificativa de uso de cada um?

| Entidade | Identidade | Atributos usados | Relacionamentos | Regras e invariantes | Ciclo de vida | LGPD |
|---|---|---|---|---|---|---|
| `Agendamento` | Chave substituta; não há chave de negócio (dois agendamentos idênticos são impedidos pelo conflito de horário, RN13) | Profissional, início, fim, tipo, situação e presença do profissional, todos obrigatórios. Nenhum opcional | N:1 `Funcionario`; 1..n `AgendamentoPaciente` | Fim posterior ao início (RN12); sem sobreposição do profissional entre não cancelados (RN13); tipo e situação com valores fechados (RN39) | Nunca é excluído: cancelar muda a situação para CANCELADO e preserva o histórico | Sem dado pessoal próprio; ligado a pessoas por relacionamento |
| `AgendamentoPaciente` | Par (agendamento, paciente), sem repetição | Presença do paciente (padrão: não confirmada) | N:1 `Agendamento`; N:1 `Paciente` | Um agendamento tem ao menos um paciente; o mesmo paciente não se repete no mesmo agendamento (RN40) | Mantido com o agendamento; presença atualizável (RN42) | `sensível`: presença do paciente em atendimento clínico (revela tratamento de menor) |
| `Funcionario` | Chave substituta; vínculo 1:1 com `Usuario` | Identificador e nome (via `Usuario`) do profissional responsável | 0..n `Agendamento` | Só profissional ativo recebe agendamentos novos (RN11) | Soft delete (SPEC-001), bloqueado com agendamentos AGENDADO (RN27 da SPEC-001) | `pessoal`: nome e cargo |
| `Paciente` | Chave substituta; vínculo 1:1 com `Usuario` | Identificador e nome (via `Usuario`) | 0..n `AgendamentoPaciente` | Só paciente ativo é incluído em agendamentos novos (RN11) | Soft delete (SPEC-001), bloqueado com agendamentos AGENDADO | `sensível` (menor atendido); nome exibido só a quem for autorizado (RN43) |
| `LogAuditoria` | Evento único por operação | Usuário, ação `AGENDAMENTO`, recurso (agendamento e operação), IP de origem, instante | N:1 `Usuario` | Imutável (SPEC-005) | Somente inclusão | IP é `pessoal`, usado para rastreabilidade |

---

## 6. Regras de Negócio (RN)

- **RN11** *(global)*: O agendamento exige Paciente e Funcionário pré-existentes.
- **RN12** *(global)*: A data de fim deve ser posterior à data de início.
- **RN13** *(global)*: Um profissional não pode ter agendamentos conflitantes no mesmo horário (`409 Conflict`).
- **RN14** *(global)*: A situação inicial é AGENDADO e a presença é controlada individualmente por paciente.
- **RN15** *(global)*: A visibilidade dos agendamentos é filtrada automaticamente pelo perfil logado.
- **RN16** *(global)*: Toda ação sensível gera registro na trilha de auditoria (LGPD / CFM).
- **RN39:** O tipo de sessão é `CONSULTA`, `EXAME`, `RETORNO`, `AVALIACAO` ou `TERAPIA`. A situação é `AGENDADO`, `CONCLUIDO` ou `CANCELADO`. As únicas transições são AGENDADO para CONCLUIDO e AGENDADO para CANCELADO; CONCLUIDO e CANCELADO são finais e não permitem edição, cancelamento nem conclusão (`409 Conflict`).
- **RN40:** Um agendamento tem um profissional responsável, que é um Funcionário ativo com perfil `MEDICO`, e um ou mais pacientes ativos, sem repetição do mesmo paciente. O profissional responsável não muda na edição; para trocá-lo, cancela-se e cria-se outro agendamento. Pré-existência não atendida (profissional ou paciente inexistente ou inativado) responde `404 Not Found`. Dado obrigatório ausente ou inválido responde `400 Bad Request`.
- **RN41:** Na criação e na edição, o início deve estar no futuro. O conflito da RN13 considera o intervalo do início (inclusive) ao fim (exclusive) e todos os agendamentos do mesmo profissional que não estejam CANCELADOS, inclusive os CONCLUIDOS. Uma sessão que termina exatamente quando outra começa não é conflito. Na edição, o agendamento não conflita consigo mesmo. Horários são apresentados no fuso `America/Sao_Paulo`.
- **RN42:** A conclusão e o registro de presença só podem ocorrer a partir do horário de início da sessão (antes disso, `409 Conflict`). A presença do profissional e a de cada paciente são independentes, começam como não confirmadas e podem ser registradas e corrigidas enquanto o agendamento estiver AGENDADO ou CONCLUIDO; em CANCELADO não se registra presença. Concluir não exige presenças registradas.
- **RN43:** O Administrador opera sobre qualquer agendamento e escolhe o profissional responsável. O Médico cria e opera somente agendamentos em que é o responsável, sendo ele próprio o responsável de tudo que cria; operar ou consultar detalhe em agendamento de outro profissional responde `403 Forbidden`. O Paciente não escreve (`403`) e lista ou consulta em detalhe somente os agendamentos em que participa, vendo data, horário, tipo, situação, profissional e a própria presença, sem nome nem presença dos demais pacientes da sessão (e recebendo `403 Forbidden` ao consultar agendamento de que não participa). O Médico lista somente os seus; o Administrador, todos.

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Médico cria agendamento individual
```gherkin
Dado que um Médico ativo está autenticado e existe um paciente ativo
E o Médico não tem agendamento no horário pretendido, que começa no futuro
Quando ele cria um agendamento do tipo CONSULTA com esse paciente
Então o agendamento é criado com situação AGENDADO e o Médico como responsável
E a presença do profissional e a do paciente ficam como não confirmadas
E um evento AGENDAMENTO é registrado com o usuário e o IP de origem
```

### Cenário 2: Administrador cria sessão em grupo
```gherkin
Dado que um Administrador está autenticado e existem um Médico ativo e dois pacientes ativos
Quando ele cria uma sessão do tipo TERAPIA para o Médico escolhido com os dois pacientes
Então o agendamento é criado com um vínculo para cada paciente, com presença não confirmada
E, se o mesmo paciente for informado duas vezes, o sistema rejeita a criação com 400
```

### Cenário 3: Datas inválidas
```gherkin
Dado que um usuário autorizado está criando um agendamento
Quando o fim é igual ou anterior ao início, ou o início está no passado
Então o sistema responde 400 com a mensagem do campo inválido
E nada é gravado
```

### Cenário 4: Profissional ou paciente inexistente ou inativado
```gherkin
Dado que um usuário autorizado está criando um agendamento
Quando o profissional ou algum paciente não existe ou foi inativado
Então o sistema responde 404 Not Found
E nada é gravado
```

### Cenário 5: Conflito de horário do profissional
```gherkin
Dado que o Médico já tem um agendamento AGENDADO das 10h às 11h
Quando se tenta criar outro para o mesmo Médico das 10h30 às 11h30
Então o sistema responde 409 Conflict com mensagem informativa
E nenhum agendamento é criado nem evento AGENDAMENTO é registrado
```

### Cenário 6: Limites e cancelados não geram conflito
```gherkin
Dado que o Médico tem um agendamento AGENDADO das 10h às 11h e outro CANCELADO das 14h às 15h
Quando se cria um agendamento das 11h às 12h e outro das 14h às 15h para o mesmo Médico
Então ambos são criados, pois o fim de uma sessão não conflita com o início da seguinte e cancelados não contam
```

### Cenário 7: Operar fora da própria agenda ou sem perfil
```gherkin
Dado que existe um agendamento do Médico A
Quando o Médico B tenta consultá-lo em detalhe, editá-lo, cancelá-lo, concluí-lo ou registrar presença, ou tenta criar um agendamento para o Médico A
Então o sistema responde 403 Forbidden
E, quando um Paciente tenta criar ou alterar qualquer agendamento, o sistema responde 403 Forbidden
```

### Cenário 8: Editar agendamento
```gherkin
Dado que existe um agendamento AGENDADO de um Médico
Quando o Médico altera o horário para um intervalo livre, muda o tipo e inclui outro paciente
Então o sistema grava as alterações, mantendo o profissional responsável
E um evento AGENDAMENTO é registrado
E, se o novo horário conflitar com outro agendamento do Médico, o sistema responde 409 sem alterar nada
```

### Cenário 9: Agendamento em situação final
```gherkin
Dado que um agendamento está CONCLUIDO ou CANCELADO
Quando um usuário autorizado tenta editá-lo, cancelá-lo ou concluí-lo
Então o sistema responde 409 Conflict
E a situação do agendamento permanece a mesma
```

### Cenário 10: Cancelar agendamento
```gherkin
Dado que existe um agendamento AGENDADO
Quando o Médico responsável ou o Administrador solicita o cancelamento e confirma no modal de diálogo com backdrop
Então a situação passa a CANCELADO e o histórico é preservado
E o horário fica livre para novos agendamentos do profissional
E um evento AGENDAMENTO é registrado
E, caso ele aborte a confirmação no modal, o agendamento permanece AGENDADO e nenhum evento é registrado
```

### Cenário 11: Concluir e registrar presença
```gherkin
Dado que existe um agendamento AGENDADO cujo horário de início já passou
Quando o Médico marca a presença dele e a de um dos dois pacientes e conclui o agendamento
Então a presença de cada pessoa é registrada de forma independente
E a situação passa a CONCLUIDO
E a presença continua podendo ser corrigida após a conclusão
E um evento AGENDAMENTO é registrado a cada operação
```

### Cenário 12: Conclusão e presença antes do início
```gherkin
Dado que existe um agendamento AGENDADO cujo início ainda não ocorreu, ou um agendamento CANCELADO
Quando o usuário tenta concluí-lo ou registrar presença
Então o sistema responde 409 Conflict
E a situação e as presenças permanecem inalteradas
```

### Cenário 13: Visibilidade por perfil na listagem e na consulta detalhada
```gherkin
Dado que existem agendamentos de vários profissionais e uma sessão em grupo com os pacientes A e B
Quando o Administrador, um Médico e o Paciente A listam os agendamentos ou consultam o detalhe da sessão em grupo
Então o Administrador vê todos, o Médico vê somente os seus e o Paciente A vê somente os dele
E o Paciente A não vê o nome nem a presença do Paciente B nos detalhes da sessão em grupo
E, quando o Paciente A tenta consultar o detalhe de um agendamento em que não participa, o sistema responde 403 Forbidden
```

### Cenário 14: Filtros e ordenação
```gherkin
Dado que existem agendamentos de situações, tipos e datas variados
Quando o usuário filtra por período, situação CONCLUIDO e tipo TERAPIA e ordena por início de forma descendente
Então a lista mostra apenas os agendamentos que atendem aos filtros, paginados e na ordem pedida
E, sem resultados, a tela exibe o estado vazio com orientação
```

### Cenário 15: Requisições simultâneas conflitantes
```gherkin
Dado que um Médico tem horário livre
Quando duas requisições simultâneas tentam agendar horários sobrepostos para ele
Então exatamente uma é aceita e a outra recebe 409 Conflict
```

**Cobertura RF × Cenários:** RF-05.1 → 1, 2; RF-05.2 → 3, 4; RF-05.3 → 5, 6, 15; RF-05.4 → 8; RF-05.5 → 9, 10, 11, 12; RF-05.6 → 11, 12; RF-05.7 → 13, 14; RF-05.8 → 7, 13; RF-05.9 → 1, 8, 10, 11. RN11 → 4; RN12 → 3; RN13 → 5, 6, 15; RN14 → 1, 2; RN15 → 13; RN16 → 1, 10, 11; RN39 → 2, 8, 9; RN40 → 2, 4; RN41 → 3, 5, 6, 8; RN42 → 11, 12; RN43 → 7, 13.

---

## 8. Casos de Borda e Tratamento de Exceções

- Agendamentos que cruzam a meia-noite são aceitos; a RN12 só exige fim posterior ao início.
- O mesmo paciente pode estar em agendamentos sobrepostos de profissionais diferentes: não há validação de conflito do paciente nesta versão.
- Agendamento AGENDADO cujo horário já passou sem conclusão continua AGENDADO até que seja concluído ou cancelado; não há mudança automática de situação.
- Ao editar a lista de pacientes, é preciso manter ao menos um paciente (RN40); remover um paciente preserva a presença dos demais.
- Profissional ou paciente excluído não pode ter agendamentos AGENDADO (RN27 da SPEC-001); agendamentos CONCLUIDO ou CANCELADO permanecem íntegros e visíveis aos autorizados.
- Falha ao gravar o evento AGENDAMENTO não impede a operação, conforme a RN44 (SPEC-005).
- Duas edições simultâneas do mesmo agendamento: a última gravação prevalece, mas sempre com a revalidação de conflito (RN41).
- Paciente ou Médico solicitando o detalhe de um agendamento ao qual não está vinculado recebe `403 Forbidden`; identificador inexistente responde `404 Not Found`.
- Filtro com período invertido (data inicial após a final) é rejeitado com `400`. Tamanho de página acima de 50 é limitado a 50 e o padrão é 10.
- Cancelar ou concluir o mesmo agendamento duas vezes seguidas: a segunda requisição responde `409 Conflict`.

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** O que cada UC cobre? → Conforme `casos-de-uso.md`: `UC14` é Criar Agendamento; `UC15` é Consultar Agendamento (detalhe por identificador); `UC16` é Atualizar Agendamento e Presença (edição de dados, transição para `CONCLUIDO` e registro/correção de presença); `UC17` é Excluir / Cancelar Agendamento (transição para `CANCELADO` após confirmação em modal com *backdrop*, sem exclusão física); `UC18` é Verificar Conflito de Horário (`<<include>>` de `UC14` e `UC16`); e `UC19` é Listar Agendamentos com filtro automático por perfil. **[Fonte: casos-de-uso.md UC14 a UC19]**
- [x] **Dúvida 2:** Uma sessão pode ter mais de um paciente? → Sim, conforme o modelo de domínio (um ou mais pacientes por agendamento). **[Fonte: domain-model.md, `AGENDAMENTO_PACIENTE`]**
- [x] **Dúvida 3:** Quais tipos e situações existem? → Os cinco tipos e as três situações do modelo (RN39). **[Fonte: domain-model.md]**
- [x] **Dúvida 4:** Quem pode ser responsável por um agendamento? → Apenas Funcionário ativo com perfil `MEDICO`; o Administrador não tem registro de Funcionário (RN40). **[Fonte: constitution.md §5.2; Decisão de spec]**
- [x] **Dúvida 5:** Como o conflito é medido? → Início inclusivo e fim exclusivo, ignorando CANCELADOS e considerando CONCLUIDOS (RN41). **[Fonte: especificacao-requisitos.md RN13 e casos-de-uso.md UC18; Decisão de spec quanto aos limites]**
- [x] **Dúvida 6:** Pode-se agendar no passado? → Não: o início deve ser futuro na criação e na edição (RN41). **[Decisão de spec]**
- [x] **Dúvida 7:** Quando é possível concluir ou marcar presença? → A partir do início da sessão, nunca em CANCELADO (RN42). **[Decisão de spec]**
- [x] **Dúvida 8:** O profissional responsável pode ser trocado na edição? → Não; cancela-se e cria-se outro (RN40). **[Decisão de spec]**
- [x] **Dúvida 9:** O que um Paciente vê de uma sessão em grupo? → Apenas os próprios dados, sem nome nem presença dos demais (RN43), por minimização LGPD. **[Fonte: constitution.md §3.4; Decisão de spec]**
- [x] **Dúvida 10:** Quais operações geram AGENDAMENTO? → Criar (`UC14`), editar, concluir, registrar presença (`UC16`) e cancelar (`UC17`). **[Fonte: especificacao-requisitos.md RF-06 e casos-de-uso.md UC14, UC16, UC17]**
- [x] **Dúvida 11:** Há validação de conflito do paciente? → Não nesta versão; a RN13 trata só do profissional. **[Fonte: especificacao-requisitos.md RN13]**
