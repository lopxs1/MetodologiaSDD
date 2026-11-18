# Especificação Funcional: Dashboard Gerencial

> **ID da Spec:** `SPEC-006`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-07 · UC21 · RN49 a RN52 · Pacote `specs/006-dashboard-gerencial/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
A gestão da AMDE precisa prestar contas a financiadores e planejar a equipe, mas hoje não sabe, sem contar fichas e planilhas, quantas consultas acontecem, quantos pacientes estão em atendimento, quantos profissionais atuam e como o trabalho se distribui ao longo do tempo e entre as especialidades. A direção decide com base em estimativas. São impactados os Administradores, que respondem pela instituição.

### 1.2. Objetivo e Valor Entregue
Dar ao Administrador, logo após o login, uma visão única e atual do volume de atendimento da AMDE: os 4 contadores principais (*Total de Consultas*, *Consultas Hoje*, *Total de Pacientes* e *Profissionais Ativos*) e 2 gráficos analíticos (*Consultas ao Longo do Tempo* e *Consultas por Especialidade*), com filtros de **Período** e **Profissional**, para apoiar decisões de gestão sem contagem manual.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** 4 contadores (*Total de Consultas*, *Consultas Hoje*, *Total de Pacientes / Pacientes Ativos* e *Profissionais Ativos*); 2 gráficos analíticos (*Consultas ao Longo do Tempo* com evolução diária e *Consultas por Especialidade* com distribuição percentual por área de atuação); filtros por **Período** (ex.: últimos 30 dias ou intervalo selecionado) e por **Profissional** (todos ou um funcionário específico); acesso exclusivo do Administrador; estados de tela; alternativa acessível aos gráficos (UC21).
- **Fora de escopo (decisões de spec):** exportação ou impressão; detalhamento (clicar no gráfico para listar consultas); comparação automática entre períodos distintos; indicadores financeiros ou de presença; atualização automática em tempo real; painéis para Médico ou Paciente; gráficos além de *Consultas ao Longo do Tempo* e *Consultas por Especialidade*.
- **Dependências:** SPEC-001 (perfil Administrador, pacientes ativos e profissionais ativos com seus cargos) e SPEC-004 (agendamentos que alimentam os contadores e os gráficos).

---

## 2. Atores e Perfis de Acesso

- **Administrador:** acessa o Dashboard, que é a sua tela inicial após o login, aplica filtros de período e profissional e vê todos os 4 indicadores e 2 gráficos.
- **Profissional de Saúde / Médico:** sem acesso ao Dashboard.
- **Paciente:** sem acesso ao Dashboard.

---

## 3. Histórias de Usuário (User Stories)

- **US-07.1:** Como **Administrador**, quero **ver o total de consultas, as consultas de hoje, o total de pacientes ativos e o total de profissionais ativos, filtráveis por período e profissional** para que **eu acompanhe o volume de atendimento e a capacidade da equipe da AMDE de relance**.
- **US-07.2:** Como **Administrador**, quero **ver a evolução diária das consultas ao longo do tempo e a distribuição percentual das consultas por especialidade** para que **eu identifique tendências, picos e áreas com maior demanda e dimensione a equipe**.
- **US-07.3:** Como **Administrador**, quero **que somente eu acesse esses indicadores** para que **a visão gerencial da instituição não fique exposta a outros perfis**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-07.1** | O sistema deve exibir ao Administrador os 4 contadores de métricas agregadas sobre registros válidos: **Total de Consultas**, **Consultas Hoje**, **Total de Pacientes (Pacientes Ativos)** e **Profissionais Ativos**, calculados conforme a RN50 (UC21). | Média |
| **RF-07.2** | O sistema deve exibir os 2 gráficos analíticos — **Consultas ao Longo do Tempo** (evolução diária no período) e **Consultas por Especialidade** (distribuição por quantidade e percentual) — conforme a RN51, com alternativa em tabela acessível para ambos (UC21). | Média |
| **RF-07.3** | O sistema deve restringir o Dashboard ao Administrador, negando-o a Médicos e Pacientes e ocultando o item do menu desses perfis (RN49) (UC21). | Crítica |
| **RF-07.4** | O sistema deve permitir aplicar filtros de **Período** (ex.: últimos 30 dias ou intervalo selecionado) e **Profissional** (todos ou um funcionário específico) e calcular os indicadores no momento do acesso, de modo que os contadores e os gráficos sejam coerentes entre si (RN50, RN51, RN52) (UC21). | Média |
| **RF-07.5** | O sistema deve exibir os quatro estados de tela do Dashboard (carregando com skeleton, vazio com orientação, erro com ação de recarregar e sucesso), mostrando contadores zerados e séries vazias quando não houver consultas no período ou filtro selecionado (UC21). | Média |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-07.1** | Eficiência de Desempenho | O Dashboard carrega os 4 contadores e os 2 gráficos em menos de 500 ms no percentil 95 com 10.000 agendamentos e 1.000 pacientes. | Teste de carga com massa de dados sintética |
| **RNF-07.2** | Usabilidade / Acessibilidade | Os gráficos têm alternativa em tabela, não dependem só de cor para distinguir categorias ou séries, e atendem contraste mínimo de 4,5:1 e navegação por teclado (WCAG 2.1 AA). | Verificação com Axe DevTools e Lighthouse, e teste E2E por teclado |
| **RNF-07.3** | Usabilidade / Interface | O Dashboard exibe skeleton enquanto carrega, estado vazio com texto orientativo, estado de erro com ação de recarregar e é legível em desktop e dispositivo móvel (breakpoints do Tailwind). | Teste E2E de interface em diferentes larguras de tela |
| **RNF-07.4** | Segurança / Confidencialidade | O Dashboard exibe somente números agregados e rótulos de especialidade ou datas, sem expor dados clínicos ou identificadores de pacientes (e usando o filtro de profissional apenas por seleção administrativa autorizada). | Inspeção da resposta e da tela em todos os cenários |
| **RNF-07.5** | Precisão | A soma das quantidades da distribuição por especialidade e da série diária ao longo do tempo é igual ao contador de *Total de Consultas* para o período e profissional selecionados em 100% das consultas ao Dashboard. | Teste automatizado comparando os valores retornados |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `Agendamento` (situação, tipo, data de início e profissional responsável), `Funcionario` (identificador para filtro por profissional, situação ativo/inativo para contagem de *Profissionais Ativos* e cargo que define a especialidade) e `Paciente` (situação ativo/inativo para contagem de *Pacientes Ativos*).
- **Entidades Novas ou Alteradas:** nenhuma. O Dashboard apenas lê e agrega; não cria entidade nem guarda indicadores.

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
| `Agendamento` | Chave substituta | Situação (para excluir CANCELADOS), início (para filtro de período, "hoje" e série diária) e profissional responsável (para filtro por profissional e especialidade). Todos obrigatórios no modelo | N:1 `Funcionario` | Tipo, situação e período válidos (SPEC-004); todos os tipos contam como consulta (RN50) | Nunca é excluído: cancelar muda a situação | Sem dado pessoal; só contagem agregada é exibida |
| `Funcionario` | Chave substituta | Identificador (para filtro por profissional), situação ativo/inativo (para contagem de **Profissionais Ativos**) e cargo (usado como especialidade no gráfico *Consultas por Especialidade*) | 0..n `Agendamento` | Profissional inativado não conta em *Profissionais Ativos*, mas suas consultas históricas continuam contando nas consultas e séries (RN50, RN51) | Soft delete (SPEC-001) | `pessoal`: cargo (exibido só como rótulo agregado) e nome na lista de seleção do filtro exclusivo do Administrador |
| `Paciente` | Chave substituta | Apenas a situação ativo/inativo, para a contagem de **Total de Pacientes (Pacientes Ativos)** | 0..n vínculos de agendamento | Ativo é o paciente não excluído logicamente | Soft delete (SPEC-001) | `sensível` (menor atendido); só o total agregado é exibido |

---

## 6. Regras de Negócio (RN)

- **RN49:** O Dashboard é exclusivo do Administrador. Médico e Paciente que tentarem acessá-lo recebem `403 Forbidden`, e o item não aparece no menu desses perfis.
- **RN50:** Os 4 contadores do Dashboard são definidos como:
  - **"Total de Consultas"**: quantidade de agendamentos `AGENDADO` ou `CONCLUIDO`, de qualquer tipo de sessão, dentro do **Período** e **Profissional** filtrados (ou em todo o histórico/todos os profissionais, quando sem filtro específico); agendamentos `CANCELADO` não contam.
  - **"Consultas Hoje"**: mesma contagem restrita aos agendamentos cujo início cai no dia corrente no fuso `America/Sao_Paulo`, respeitando o filtro de **Profissional** quando aplicado.
  - **"Total de Pacientes (Pacientes Ativos)"**: quantidade de pacientes não excluídos logicamente (`excluido_em IS NULL`).
  - **"Profissionais Ativos"**: quantidade de funcionários não excluídos logicamente (`excluido_em IS NULL`).
- **RN51:** Os 2 gráficos analíticos utilizam exatamente o mesmo conjunto de consultas válidas contado em *"Total de Consultas"* (agendamentos `AGENDADO` ou `CONCLUIDO` dentro do período e profissional selecionados):
  - **Consultas ao Longo do Tempo**: agrupa essas consultas por data de início no fuso `America/Sao_Paulo`, exibindo a evolução diária da quantidade no período selecionado em ordem cronológica crescente.
  - **Consultas por Especialidade**: agrupa essas consultas pelo cargo do profissional responsável, calculando quantidade e percentual sobre o total do recorte, sem distinguir maiúsculas e minúsculas nem espaços nas pontas. O rótulo exibido é a grafia usada pelo primeiro profissional cadastrado com aquele cargo. Consultas de profissionais inativados continuam contando quando incluídas no recorte. A lista é ordenada da maior quantidade para a menor e, em empate, em ordem alfabética do rótulo.
- **RN52:** O Dashboard aceita filtros opcionais de **Período** (ex.: últimos 30 dias ou intervalo com data inicial e final) e **Profissional** (todos ou um funcionário específico). Os indicadores são calculados no momento do acesso, sem armazenamento de resultados, de modo que a soma dos pontos da evolução diária e a soma da distribuição por especialidade sejam ambas iguais ao *Total de Consultas* do recorte selecionado e reflitam o estado atual dos agendamentos, pacientes e profissionais.

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Contadores corretos
```gherkin
Dado que existem 10 agendamentos AGENDADO ou CONCLUIDO no período padrão, 2 CANCELADOS, 3 deles com início hoje, 5 pacientes ativos e 4 profissionais ativos
Quando o Administrador abre o Dashboard sem filtro restritivo de profissional
Então "Total de Consultas" mostra 10
E "Consultas Hoje" mostra 3
E "Total de Pacientes" mostra 5
E "Profissionais Ativos" mostra 4
```

### Cenário 2: Cancelados e "hoje" no fuso correto
```gherkin
Dado que um agendamento CANCELADO tem início hoje e outro AGENDADO começa às 23h30 de hoje em America/Sao_Paulo
Quando o Administrador abre o Dashboard
Então o cancelado não entra em nenhum contador nem gráfico
E o agendamento das 23h30 conta em "Consultas Hoje" e na série diária do dia corrente
```

### Cenário 3: Pacientes e profissionais inativados
```gherkin
Dado que existem 6 pacientes cadastrados (1 excluído logicamente) e 4 profissionais cadastrados (1 excluído logicamente com 2 consultas válidas no período)
Quando o Administrador abre o Dashboard
Então "Total de Pacientes" mostra 5
E "Profissionais Ativos" mostra 3
E as 2 consultas históricas do profissional inativado continuam contando em "Total de Consultas" e nos gráficos
```

### Cenário 4: Gráficos de evolução diária e distribuição por especialidade
```gherkin
Dado que existem 10 consultas válidas distribuídas em 3 dias do período, sendo de profissionais com cargos "Psicologia" (4), "Fonoaudiologia" (3) e "Terapia Ocupacional" (3)
Quando o Administrador abre o Dashboard
Então o gráfico "Consultas ao Longo do Tempo" exibe a evolução diária dos 3 dias cuja soma é 10
E o gráfico "Consultas por Especialidade" mostra as especialidades da maior para a menor quantidade (com empate em ordem alfabética), com quantidades e percentuais cuja soma também é 10
E tabelas alternativas acessíveis exibem os mesmos números de ambos os gráficos
```

### Cenário 5: Cargos equivalentes no agrupamento por especialidade
```gherkin
Dado que dois profissionais têm os cargos "Psicologia" e " psicologia "
Quando o Administrador abre o Dashboard
Então as consultas dos dois aparecem somadas em uma única especialidade no gráfico "Consultas por Especialidade"
```

### Cenário 6: Sem consultas no período
```gherkin
Dado que não existem consultas válidas no período ou para o profissional selecionado
Quando o Administrador abre ou filtra o Dashboard
Então "Total de Consultas" e "Consultas Hoje" mostram 0
E ambos os gráficos exibem séries vazias e estado vazio com texto orientativo, sem falhas de renderização
```

### Cenário 7: Acesso fora do perfil
```gherkin
Dado que um Médico ou um Paciente está autenticado
Quando ele tenta acessar o Dashboard
Então o sistema responde 403 Forbidden
E nenhum indicador é exibido
E o item "Dashboard" não aparece no menu lateral desses perfis
```

### Cenário 8: Estados de carregamento e erro
```gherkin
Dado que o Administrador abre o Dashboard
Quando os dados ainda estão carregando, ou o carregamento falha
Então a tela exibe primeiro o skeleton
E, na falha, exibe um alerta amigável com a ação de recarregar, sem detalhes técnicos
```

### Cenário 9: Dados atualizados no acesso
```gherkin
Dado que o Dashboard mostra 10 consultas em "Total de Consultas"
Quando um novo agendamento é criado no período exibido e o Administrador recarrega o Dashboard
Então "Total de Consultas" mostra 11 e os gráficos de evolução diária e especialidade refletem a nova consulta
```

### Cenário 10: Filtros de período e profissional
```gherkin
Dado que existem consultas de diferentes profissionais e datas no sistema
Quando o Administrador seleciona um período específico e um profissional específico no Dashboard
Então "Total de Consultas", "Consultas Hoje", "Consultas ao Longo do Tempo" e "Consultas por Especialidade" passam a refletir apenas os agendamentos válidos desse profissional no período escolhido
E a soma de cada gráfico continua igual a "Total de Consultas"
```

**Cobertura RF × Cenários:** RF-07.1 → 1, 2, 3; RF-07.2 → 4, 5; RF-07.3 → 7; RF-07.4 → 9, 10; RF-07.5 → 6, 8. RN49 → 7; RN50 → 1, 2, 3, 10; RN51 → 4, 5, 10; RN52 → 4, 9, 10.

---

## 8. Casos de Borda e Tratamento de Exceções

- Agendamento que começa exatamente à meia-noite conta no dia que se inicia, no fuso `America/Sao_Paulo`.
- Agendamento AGENDADO com horário já passado e ainda não concluído continua contando normalmente; "Consultas Hoje" considera apenas o início no dia corrente.
- Filtro de período com data inicial posterior à data final é rejeitado com `400 Bad Request`.
- Filtro com identificador de profissional inexistente responde `404 Not Found` (ou lista vazia se apenas não houver consultas para um profissional existente no período).
- Dias dentro do período selecionado sem nenhum agendamento válido são apresentados com valor zero na série contínua (ou omitidos conforme definição visual no plano técnico), preservando a soma igual a *Total de Consultas*.
- Profissional com cargo muito longo ou com caracteres especiais é exibido com rótulo truncado visualmente, com o texto completo disponível na tabela alternativa.
- Muitas especialidades diferentes: o gráfico rola ou agrupa visualmente, mantendo a tabela alternativa com todas.
- Falha no cálculo exibe o estado de erro com ação de recarregar, sem dados parciais que contradigam a RNF-07.5.
- Administrador sem consultas, sem pacientes e sem profissionais vê zeros, sem erro.
- Sessão expirada ao abrir ou recarregar o Dashboard responde `401` e leva à tela de login (SPEC-001).
- O Dashboard e o acesso a ele não geram evento de auditoria, pois a lista de ações auditáveis é fechada (SPEC-005).

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** Quem acessa o Dashboard? → Somente o Administrador (RN49). **[Fonte: especificacao-requisitos.md RF-07; casos-de-uso.md UC21; matriz RN17 da SPEC-001]**
- [x] **Dúvida 2:** Quais são os contadores e o que conta como "consulta"? → São 4 contadores: *Total de Consultas*, *Consultas Hoje*, *Total de Pacientes (Pacientes Ativos)* e *Profissionais Ativos*. Conta como consulta todo agendamento `AGENDADO` ou `CONCLUIDO`, de qualquer tipo de sessão; cancelados não contam (RN50). **[Fonte: casos-de-uso.md UC21; Decisão de spec quanto aos status considerados]**
- [x] **Dúvida 3:** O que significa "pacientes ativos" e "profissionais ativos"? → Pacientes e funcionários não excluídos logicamente (`excluido_em IS NULL`), pela definição de ativo do modelo de domínio (RN50). **[Fonte: domain-model.md, invariante 3; casos-de-uso.md UC21]**
- [x] **Dúvida 4:** O que é "especialidade"? → O cargo do profissional responsável pelo agendamento (RN51). **[Fonte: domain-model.md, `FUNCIONARIO` ("Especialidade ou função clínica"); casos-de-uso.md UC21]**
- [x] **Dúvida 5:** Quantos gráficos existem? → Dois: *Consultas ao Longo do Tempo* (evolução diária de agendamentos no período) e *Consultas por Especialidade* (distribuição percentual por cargo) (RN51). **[Fonte: casos-de-uso.md UC21]**
- [x] **Dúvida 6:** Há filtros no Dashboard? → Sim: filtros de **Período** (ex.: últimos 30 dias ou intervalo selecionado) e **Profissional** (todos ou um funcionário específico) (RN52). **[Fonte: casos-de-uso.md UC21]**
- [x] **Dúvida 7:** Qual o fuso de "hoje" e da série diária? → `America/Sao_Paulo`, o mesmo configurado para o banco. **[Fonte: constitution.md §3.5]**
- [x] **Dúvida 8:** Os dados são em tempo real ou guardados? → Calculados no acesso, sem atualização automática (RN52). **[Decisão de spec]**
