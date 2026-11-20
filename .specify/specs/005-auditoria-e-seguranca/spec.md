# Especificação Funcional: Auditoria e Segurança

> **ID da Spec:** `SPEC-005`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-06 · UC20 · RN16, RN44 a RN48 · Pacote `specs/005-auditoria-e-seguranca/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
Os prontuários de crianças e adolescentes com TEA são dados pessoais sensíveis. A LGPD (Lei nº 13.709/2018) e a Resolução CFM nº 1.821/2007 exigem que a instituição saiba quem fez o quê, quando e de onde nas informações clínicas. No papel, isso era impossível. Sem uma trilha confiável, a AMDE não consegue apurar um acesso indevido nem comprovar conformidade. São impactados a instituição, os profissionais e os pacientes (ou seus titulares).

### 1.2. Objetivo e Valor Entregue
Registrar automaticamente, de forma imutável e sem atrasar o trabalho dos usuários, cada operação sensível do sistema (login, envio, download, exclusão e agendamento), com quem fez, o quê, onde e quando. Esta spec define o catálogo de eventos e as garantias do registro; as demais specs disparam os eventos.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** o catálogo de operações auditáveis (LOGIN, UPLOAD, DOWNLOAD, EXCLUSAO, AGENDAMENTO), o conteúdo mínimo de cada evento, a gravação assíncrona, o tratamento de falhas de gravação, a imutabilidade e a preservação dos eventos, e a minimização de dados pessoais no registro (UC20).
- **Fora de escopo (decisões de spec):** tela, relatório ou filtro para consultar a trilha (a matriz de permissões da RN17 não concede consulta a nenhum perfil nesta versão; a consulta é feita por procedimento técnico fora da aplicação, descrito no plano técnico); registro de tentativas de login falhas e de operações negadas; auditoria de leitura de fichas, listagens, edição de dados clínicos, reclassificação e revelação de celular (a lista de ações é fechada); alertas, detecção de anomalias e notificação de incidentes; política de expurgo ou arquivamento de eventos.
- **Dependências:** SPEC-001 (LOGIN e EXCLUSAO de usuário), SPEC-003 (UPLOAD, DOWNLOAD e EXCLUSAO de documento) e SPEC-004 (AGENDAMENTO) são as origens dos eventos.

---

## 2. Atores e Perfis de Acesso

- **Sistema (`AuditoriaService`):** registra os eventos de forma automática a cada operação sensível bem-sucedida.
- **Administrador, Médico e Paciente:** geram eventos ao usar o sistema, mas nenhum deles consulta, altera ou apaga a trilha nesta versão (RN17, RN45, RN48).
- **Responsável técnico da equipe:** consulta a trilha fora da aplicação, por procedimento técnico controlado descrito no plano técnico, quando a instituição precisar apurar um fato.

---

## 3. Histórias de Usuário (User Stories)

- **US-06.1:** Como **instituição (AMDE)**, quero que **toda operação sensível fique registrada com quem, o quê, onde e quando** para que **eu possa apurar acessos e comprovar conformidade com a LGPD e o CFM**.
- **US-06.2:** Como **usuário do sistema**, quero que **o registro de auditoria não deixe o sistema mais lento** para que **meu atendimento não seja atrasado**.
- **US-06.3:** Como **instituição**, quero que **os eventos nunca possam ser alterados ou apagados pelo sistema** para que **a trilha seja confiável como prova**.
- **US-06.4:** Como **titular de dados (Paciente ou responsável)**, quero que **a trilha guarde só identificadores e nunca o conteúdo clínico** para que **a auditoria não vire mais uma fonte de exposição dos meus dados**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-06.1** | O sistema deve registrar um evento para cada operação do catálogo da RN46: LOGIN, UPLOAD, DOWNLOAD, EXCLUSAO e AGENDAMENTO (UC20). | Crítica |
| **RF-06.2** | O sistema deve gravar em cada evento o usuário responsável, a ação, o recurso (1 a 255 caracteres), o IP de origem (3 a 45 caracteres, obrigatório em requisições HTTP web e anulável apenas em rotinas internas ou testes sem requisição HTTP) e o instante da operação (RN45) (UC20). | Crítica |
| **RF-06.3** | O sistema deve gravar os eventos de forma assíncrona, sem aumentar de forma perceptível o tempo de resposta da operação (RNF-06.1) (UC20). | Crítica |
| **RF-06.4** | O sistema deve tratar falhas de gravação conforme a RN44: seguir com a operação e registrar a falha tecnicamente, exceto na exclusão de documento, que é interrompida (UC20). | Crítica |
| **RF-06.5** | O sistema deve manter os eventos imutáveis e preservados, sem alteração nem remoção por qualquer funcionalidade (RN45, RN48) (UC20). | Crítica |
| **RF-06.6** | O sistema deve registrar no recurso apenas identificadores técnicos, nunca conteúdo clínico ou dado pessoal (RN47) (UC20). | Alta |
| **RF-06.7** | O sistema não deve gerar evento para tentativas falhas, operações negadas ou operações que não foram concluídas com sucesso (RN46) (UC20). | Alta |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-06.1** | Eficiência de Desempenho | O registro assíncrono acrescenta no máximo 50 ms ao tempo de resposta das operações auditadas, no percentil 95. | Teste de carga comparando a operação com e sem a gravação do evento |
| **RNF-06.2** | Confiabilidade | Em condições normais, 100% dos eventos de operações bem-sucedidas ficam gravados em até 5 segundos. | Teste automatizado com operações sintéticas e conferência da trilha |
| **RNF-06.3** | Segurança / Integridade | Nenhuma funcionalidade ou perfil consegue alterar ou remover um evento já gravado. | Teste automatizado de tentativa de alteração e remoção em todas as funcionalidades |
| **RNF-06.4** | Segurança / Confidencialidade | Logs técnicos de falha de gravação e a trilha nunca contêm senhas, conteúdo de prontuário, títulos de documentos, nomes de pacientes ou descrições clínicas. | Varredura dos logs e inspeção dos eventos gravados |
| **RNF-06.5** | Auditabilidade | Em 100% dos eventos, o instante gravado corresponde ao da operação, mesmo quando a gravação ocorre alguns instantes depois. | Teste automatizado comparando o instante da operação e o do evento |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `LogAuditoria` (evento de auditoria) e `Usuario` (responsável pela ação).
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
| `LogAuditoria` | Chave substituta; não há chave de negócio (várias operações idênticas geram vários eventos) | **Obrigatórios sempre:** usuário responsável, ação, recurso (1 a 255 caracteres) e instante da operação. **Obrigatório em requisições HTTP web e opcional (anulável) fora delas:** IP de origem (3 a 45 caracteres para IPv4/IPv6; anulável apenas quando o evento é disparado por rotina interna ou teste automatizado sem requisição HTTP web) | N:1 `Usuario` (obrigatório); sem vínculo direto com os recursos auditados, só pelo identificador no recurso | Ação é `LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO` ou `AGENDAMENTO` (RN46); imutável e somente inclusão (RN45) | Nunca atualizado, inativado ou removido; sobrevive à inativação do usuário (RN48) | `pessoal`: IP de origem, para rastreabilidade e apuração. Recurso guarda só identificador técnico (RN47) |
| `Usuario` | E-mail único entre ativos | Apenas o identificador, para vincular o evento ao responsável | 0..n `LogAuditoria` | Evento exige usuário identificado e ativo no momento da operação | Soft delete (SPEC-001); eventos preservados | `pessoal`, sem uso adicional aqui |

---

## 6. Regras de Negócio (RN)

- **RN16** *(global)*: Toda ação sensível gera registro na trilha de auditoria (LGPD / CFM).
- **RN44:** A gravação do evento é assíncrona e não bloqueia a operação. Se a gravação falhar em LOGIN, UPLOAD, DOWNLOAD, AGENDAMENTO ou EXCLUSAO de usuário, a operação prossegue normalmente e a falha é registrada no log técnico, sem dados pessoais, para tratamento pela equipe. Exceção: na EXCLUSAO de documento de prontuário, o evento deve ser gravado antes da remoção do arquivo, e a falha interrompe a exclusão com `500`, sem remover nada (SPEC-003, RN37). Essa exceção é justificada porque a RN10 exige a trilha antes de qualquer remoção.
- **RN45:** Todo evento guarda, de forma sempre obrigatória: o usuário responsável, a ação, o recurso (1 a 255 caracteres) e o instante da operação. O IP de origem do cliente (3 a 45 caracteres, suportando IPv4 e IPv6) é obrigatório em toda operação originada por requisição HTTP web, sendo anulável apenas quando o evento for disparado por rotina interna ou teste automatizado sem requisição HTTP web. Os eventos são imutáveis (somente inclusão): nenhum perfil nem funcionalidade os altera ou remove.
- **RN46:** Os eventos nascem somente de operações bem-sucedidas, nos casos: LOGIN (login bem-sucedido); UPLOAD (envio e substituição de documento); DOWNLOAD (arquivo efetivamente entregue); EXCLUSAO (exclusão de documento de prontuário e exclusão lógica de usuário); AGENDAMENTO (criar, editar, cancelar, concluir e registrar presença). Login falho, operação negada (`401`, `403`), requisição inválida e operação que falha antes de concluir não geram evento, salvo o caso de tentativa descrito na RN37 da SPEC-003.
- **RN47:** O recurso identifica o objeto afetado apenas por tipo e número identificador (por exemplo, documento 15, agendamento 42, usuário 7), junto com a operação realizada quando houver mais de uma (como cancelar ou concluir), respeitando o limite de 1 a 255 caracteres. Títulos, nomes, descrições, conteúdos e qualquer outro dado pessoal ou clínico nunca entram no recurso.
- **RN48:** Os eventos são preservados sem prazo, mesmo depois da inativação do usuário responsável ou da exclusão do documento ou agendamento a que se referem. Nesta versão, nenhum perfil consulta a trilha pela aplicação (RN17).

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Login gera evento
```gherkin
Dado que existe um usuário ativo
Quando ele faz login com sucesso por requisição web
Então um evento LOGIN é gravado com o usuário, o recurso, o IP de origem e o instante do login
```

### Cenário 2: Envio e substituição geram UPLOAD
```gherkin
Dado que um Médico envia um documento e depois substitui por uma nova versão
Quando as duas operações são concluídas com sucesso
Então dois eventos UPLOAD são gravados, cada um com o recurso identificando o documento correspondente
```

### Cenário 3: Download gera evento somente quando entrega
```gherkin
Dado que um usuário autorizado baixa um documento
Quando o arquivo é entregue com sucesso
Então um evento DOWNLOAD é gravado
E, quando o download falha (404, 403 ou 500), nenhum evento DOWNLOAD é gravado
```

### Cenário 4: Exclusões geram EXCLUSAO
```gherkin
Dado que um documento e um usuário são excluídos, cada um por um usuário autorizado
Quando as exclusões são concluídas
Então dois eventos EXCLUSAO são gravados, um para o documento e outro para o usuário
E o evento do documento é gravado antes da remoção do arquivo físico
```

### Cenário 5: Operações de agendamento geram AGENDAMENTO
```gherkin
Dado que um agendamento é criado, editado, cancelado e outro é concluído com presença registrada
Quando as operações são concluídas com sucesso
Então cada operação gera um evento AGENDAMENTO, com o recurso identificando o agendamento e a operação realizada
```

### Cenário 6: Operações sem sucesso não geram evento
```gherkin
Dado que ocorrem um login com senha errada, uma operação negada com 403 e um envio rejeitado por formato inválido
Quando as três requisições terminam
Então nenhum evento de auditoria é gravado por elas
```

### Cenário 7: Falha de gravação não bloqueia a operação
```gherkin
Dado que a gravação de eventos está indisponível
Quando um usuário faz login, baixa um documento, cria um agendamento ou exclui outro usuário
Então cada operação é concluída normalmente para o usuário
E a falha é registrada no log técnico, sem dados pessoais nem conteúdo clínico
```

### Cenário 8: Falha de gravação interrompe a exclusão de documento
```gherkin
Dado que a gravação de eventos está indisponível
Quando um Médico confirma a exclusão de um documento
Então o sistema responde 500 com mensagem genérica
E o arquivo físico não é removido e o documento permanece ativo
```

### Cenário 9: Gravação assíncrona não atrasa a resposta
```gherkin
Dado que o sistema está sob carga normal
Quando usuários executam operações auditadas
Então o tempo de resposta aumenta no máximo 50 ms no percentil 95 em relação à operação sem auditoria
E o evento correspondente é gravado em até 5 segundos
```

### Cenário 10: Eventos são imutáveis e preservados
```gherkin
Dado que existem eventos gravados e um deles pertence a um usuário que depois foi excluído
Quando qualquer funcionalidade ou perfil tenta alterar ou remover um evento
Então nenhuma alteração ou remoção é possível
E os eventos do usuário excluído continuam preservados
```

### Cenário 11: Recurso sem dados pessoais
```gherkin
Dado que um Médico envia o documento "Laudo neuropsicológico" do paciente "Ana Souza"
Quando o evento UPLOAD é gravado
Então o recurso contém só o tipo e o número identificador do documento
E o título, o nome do paciente e qualquer conteúdo clínico não aparecem no evento
```

**Cobertura RF × Cenários:** RF-06.1 → 1, 2, 3, 4, 5; RF-06.2 → 1, 9; RF-06.3 → 9; RF-06.4 → 7, 8; RF-06.5 → 10; RF-06.6 → 11; RF-06.7 → 6. RN16 → 1, 2, 3, 4, 5; RN44 → 7, 8; RN45 → 1, 10; RN46 → 2, 3, 4, 5, 6; RN47 → 5, 11; RN48 → 10.

---

## 8. Casos de Borda e Tratamento de Exceções

- O IP gravado em requisições HTTP web é o do cliente que originou a requisição (3 a 45 caracteres para IPv4/IPv6), e não o de intermediários (balanceador, CDN ou proxy). O plano técnico deve garantir que só se confie no endereço informado por intermediários da própria infraestrutura.
- Eventos disparados por rotinas internas ou testes automatizados sem requisição HTTP web registram o IP de origem como nulo (`NULL`), conforme previsto no `UC20`; em toda requisição HTTP web, o IP de origem é obrigatório.
- Se a gravação for assíncrona e o sistema for reiniciado antes de concluir, o evento pendente pode ser perdido; essa janela deve ser minimizada pelo plano técnico, e a falha tratada como na RN44, exceto para EXCLUSAO de documento, que só avança com o evento já confirmado.
- Eventos da mesma operação repetida (por exemplo, dois downloads do mesmo documento) geram eventos distintos, um por operação.
- O instante gravado é o da operação, mesmo quando a gravação ocorre depois (RNF-06.5). Horários são guardados com fuso e apresentados em `America/Sao_Paulo` quando consultados.
- Operações disparadas pelo próprio sistema, sem usuário humano identificado, não geram evento nesta versão, pois todo evento exige vínculo obrigatório com um usuário responsável.
- Evento com recurso que deixou de existir (documento excluído ou agendamento cancelado) continua válido e imutável (RN48).
- Alterações de dados clínicos, reclassificações, arquivamentos e revelação de celular não geram evento nesta versão, pois a lista de ações é fechada (RN46).
- Dados pessoais (IP) estão sujeitos à LGPD; não há rotina de expurgo nesta versão, e qualquer política de retenção futura exige nova versão da spec.

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** Quais operações são auditadas? → LOGIN, UPLOAD, DOWNLOAD, EXCLUSAO e AGENDAMENTO. **[Fonte: constitution.md §3.4; domain-model.md, `LogAuditoria`]**
- [x] **Dúvida 2:** A exclusão de usuário é auditada? → Sim, como EXCLUSAO, junto com a exclusão de documento (RN46). **[Fonte: especificacao-requisitos.md RF-06; casos-de-uso.md UC07]**
- [x] **Dúvida 3:** Login falho ou operação negada é auditado? → Não: o evento exige usuário identificado e a lista de ações não prevê falhas (RN46). **[Fonte: domain-model.md, `LogAuditoria`]**
- [x] **Dúvida 4:** A gravação assíncrona conflita com "registro prévio" na exclusão de documento? → Para EXCLUSAO de documento, o evento deve estar gravado antes da remoção, e a falha interrompe a exclusão. Nas demais operações a gravação é assíncrona e não bloqueia (RN44). **[Fonte: constitution.md §3.3 e §3.4, RN10; casos-de-uso.md UC20]**
- [x] **Dúvida 5:** O que fazer quando a gravação falha nas demais operações? → A operação prossegue e a falha vai ao log técnico, sem dados pessoais (RN44). **[Fonte: constitution.md §3.4, logs seguros; Decisão de spec]**
- [x] **Dúvida 6:** Quem consulta a trilha? → Nenhum perfil, pela aplicação, nesta versão; o responsável técnico consulta por procedimento fora da aplicação (RN48). **[Fonte: matriz RN17 da SPEC-001; Decisão de spec quanto ao procedimento técnico]**
- [x] **Dúvida 7:** O que o campo "recurso" guarda? → Apenas tipo e número identificador (1 a 255 caracteres), mais a operação quando aplicável (RN47). **[Fonte: constitution.md §3.4 e dicionario-de-dados.md; Decisão de spec quanto ao formato]**
- [x] **Dúvida 8:** Por quanto tempo os eventos são guardados? → Sem prazo, sem expurgo nesta versão (RN48). **[Fonte: domain-model.md, invariante 8 (somente inclusão); Decisão de spec]**
- [x] **Dúvida 9:** A tentativa de exclusão que falha na remoção do arquivo deixa evento? → Sim, o evento já gravado permanece, pois a trilha é imutável (RN46, SPEC-003 RN37). **[Fonte: domain-model.md, invariante 8; Decisão de spec]**
- [x] **Dúvida 10:** O IP de origem é sempre obrigatório? → É obrigatório (3 a 45 caracteres) em toda operação originada por requisição HTTP web, sendo anulável apenas quando o evento é disparado por rotina interna ou teste automatizado sem requisição HTTP web. **[Fonte: casos-de-uso.md UC20 e dicionario-de-dados.md Seção 8]**
