# Especificação Funcional: Gestão de Pacientes

> **ID da Spec:** `SPEC-002`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-03 · UC08, UC09 · RN04, RN29 a RN33 · Pacote `specs/002-gestao-de-pacientes/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
A equipe multidisciplinar da AMDE atende crianças e adolescentes com TEA e precisa, a cada atendimento, localizar o paciente, saber o seu grau de suporte e ver o histórico de documentos clínicos. Com fichas em papel, esse trabalho é lento, o histórico fica espalhado e não há como limitar quem enxerga a ficha de quem. São impactados Administradores, Médicos (profissionais de saúde) e os próprios Pacientes (ou seus titulares), que precisam consultar o que lhes diz respeito.

### 1.2. Objetivo e Valor Entregue
Permitir que profissionais localizem pacientes por busca, filtro e ordenação, vejam uma ficha clínica única com o histórico de documentos por categoria e mantenham o grau de suporte e a descrição clínica atualizados, enquanto cada Paciente enxerga somente a própria ficha. O resultado é atendimento mais ágil e exposição mínima de dados sensíveis de menores.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** listagem paginada de pacientes ativos, busca por nome ou e-mail, filtro por grau de suporte, ordenação ASC/DESC, ficha do paciente (dados cadastrais, dados clínicos e histórico de documentos por categoria com metadados), acesso do Paciente à própria ficha, atualização do grau de suporte e da descrição clínica por Administrador e Médico, mascaramento do celular.
- **Fora de escopo (decisões de spec):** cadastro e exclusão do paciente (nascem e terminam no cadastro de usuário, SPEC-001); edição de dados cadastrais (nome, e-mail, celular, endereço), que é exclusiva do Administrador na SPEC-001; envio, download e exclusão de documentos (SPEC-003); histórico de alterações da ficha (sem versionamento); exportação ou impressão da ficha; relatórios clínicos.
- **Dependências:** SPEC-001 (usuários, perfis e cadastro do Paciente) e SPEC-003 (documentos que compõem o histórico exibido na ficha).

---

## 2. Atores e Perfis de Acesso

- **Administrador:** lista e busca todos os pacientes ativos, abre qualquer ficha, revela o celular completo e atualiza o grau de suporte e a descrição clínica.
- **Profissional de Saúde / Médico (perfil `MEDICO`):** lista e busca todos os pacientes ativos, abre qualquer ficha, revela o celular completo e atualiza o grau de suporte e a descrição clínica.
- **Paciente:** abre somente a própria ficha (dados e histórico de documentos), com celular sempre mascarado; não lista outros pacientes nem altera dados clínicos.

---

## 3. Histórias de Usuário (User Stories)

- **US-03.1:** Como **Administrador ou Médico**, quero **listar e buscar pacientes por nome ou e-mail, filtrar por grau de suporte e ordenar a lista** para que **eu encontre rapidamente quem vou atender**.
- **US-03.2:** Como **Administrador ou Médico**, quero **abrir a ficha de um paciente com seus dados clínicos e o histórico de documentos por categoria** para que **eu tenha o quadro completo antes do atendimento**.
- **US-03.3:** Como **Paciente (ou seu titular)**, quero **ver a minha própria ficha e os meus documentos** para que **eu acompanhe o meu histórico sem acessar dados de outras pessoas**.
- **US-03.4:** Como **Administrador ou Médico**, quero **atualizar o grau de suporte e a descrição clínica do paciente** para que **a ficha reflita a evolução do atendimento**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-03.1** | O sistema deve permitir a Administrador e Médico listar os pacientes ativos com paginação, exibindo nome, e-mail, grau de suporte e celular mascarado (UC08). | Alta |
| **RF-03.2** | O sistema deve permitir buscar por nome ou e-mail, filtrar por grau de suporte e ordenar ASC/DESC por nome, e-mail ou grau de suporte (UC08). | Alta |
| **RF-03.3** | O sistema deve exibir a ficha do paciente com dados cadastrais, grau de suporte, descrição clínica e histórico de documentos agrupado por categoria (Exames, Laudos, Receituário, Arquivado) com contagem por categoria, exibindo identificador, título, nome original do arquivo, categoria, data de envio e situação/substituição em ordem cronológica, sem entregar o conteúdo binário dos arquivos PDF (UC09). | Alta |
| **RF-03.4** | O sistema deve permitir ao Paciente acessar apenas a própria ficha e negar o acesso à lista e às fichas de outros pacientes (RN29). | Crítica |
| **RF-03.5** | O sistema deve permitir a Administrador e Médico atualizar o grau de suporte e a descrição clínica de um paciente ativo (RN32) (UC08). | Média |
| **RF-03.6** | O sistema deve exibir o celular mascarado em listagens e fichas, revelando-o apenas por ação explícita de Administrador ou Médico na ficha (RN33). | Alta |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-03.1** | Eficiência de Desempenho | A listagem com busca, filtro e ordenação retorna em menos de 500 ms no percentil 95 com 10.000 pacientes. A abertura da ficha responde em menos de 500 ms no percentil 95. | Teste de carga com massa de dados sintética |
| **RNF-03.2** | Segurança / Confidencialidade | Grau de suporte, descrição clínica e dados de contato de pacientes nunca aparecem em logs nem em mensagens de erro enviadas ao cliente. A descrição clínica não é devolvida pela listagem. | Varredura dos logs e inspeção das respostas da listagem |
| **RNF-03.3** | Segurança / Autenticidade | Em 100% das tentativas, um Paciente que solicita a ficha de outro paciente recebe `403 Forbidden`, sem nenhum dado da ficha. | Teste automatizado com perfis e identificadores variados |
| **RNF-03.4** | Usabilidade / Interface | Listagem e ficha apresentam os quatro estados de tela (carregando com skeleton, vazio com ícone e texto, erro com ação de recarregar, sucesso), cabeçalhos de coluna com ordenação visível e paginação na base da tabela. | Teste E2E de interface |
| **RNF-03.5** | Usabilidade / Acessibilidade | Busca, filtro, ordenação e revelação do celular funcionam por teclado, com foco visível e contraste mínimo de 4,5:1 (WCAG 2.1 AA). | Teste E2E e verificação com Axe DevTools |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `Paciente` (grau de suporte, descrição clínica; leitura e atualização), `Usuario` (nome, e-mail, celular e demais dados cadastrais; somente leitura), `RegistroProntuario` e `Categoria` (somente leitura dos metadados para o histórico da ficha).
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
| `Paciente` | Chave substituta; vínculo 1:1 com `Usuario` | **Obrigatório:** grau de suporte. **Opcional (anulável):** descrição clínica (até 5.000 caracteres; opcional porque o cadastro inicial pela recepção pode preceder o registro clínico pelo especialista). Ambos atualizáveis (RN32) | 1:1 `Usuario`; 0..n `RegistroProntuario`; 0..n vínculos de agendamento | Grau de suporte é `NIVEL_1`, `NIVEL_2` ou `NIVEL_3`; só paciente ativo é listado ou consultado | Inativação lógica pela SPEC-001; sem histórico de alterações da ficha | `sensível`: grau de suporte e descrição (dados clínicos de menor atendido), usados para atendimento |
| `Usuario` | E-mail único entre ativos | **Obrigatórios:** nome e e-mail. **Opcionais no cadastro base (SPEC-001):** celular, data de nascimento e endereço (país, estado, cidade, rua), exibidos na ficha em modo somente leitura | 1:1 `Paciente` | Somente leitura nesta spec | Soft delete (SPEC-001) | `pessoal`: nome, e-mail, celular, data de nascimento, país, estado, cidade, rua |
| `RegistroProntuario` | Chave substituta | Identificador, título do documento, nome original do arquivo, categoria, data de envio e vínculo de substituição/situação (vigente, substituído, arquivado) para o histórico; o conteúdo binário do arquivo PDF não é exibido nem entregue na listagem da ficha | N:1 `Paciente`; N:1 `Categoria`; 0..1 documento que substitui | Só documentos não excluídos aparecem | Soft delete (SPEC-003) | `sensível`: título e nome original do arquivo (documentos clínicos de menor) |
| `Categoria` | Nome fixo: Exames, Laudos, Receituário, Arquivado | Nome | 1:N `RegistroProntuario` | Quatro valores oficiais | Dado de referência, sem edição pelo usuário | Sem dado pessoal |

---

## 6. Regras de Negócio (RN)

- **RN04** *(global)*: Todo `Paciente` deriva de exatamente um `Usuario` (vínculo 1:1 com grau de suporte TEA).
- **RN29:** Administrador e Médico enxergam qualquer paciente ativo. O Paciente enxerga somente a própria ficha. Qualquer outro acesso do Paciente (lista ou ficha alheia) é negado com `403 Forbidden`. Paciente inativado não aparece na lista e sua ficha responde `404 Not Found`.
- **RN30:** A busca localiza pacientes cujo nome ou e-mail contém o termo digitado (até 100 caracteres), sem distinguir maiúsculas, minúsculas ou acentos. A ordenação aceita nome, e-mail ou grau de suporte, ASC ou DESC, com padrão nome ASC. A paginação padrão é de 10 itens, com máximo de 50 por página.
- **RN31:** O grau de suporte TEA assume exatamente um dos valores `NIVEL_1`, `NIVEL_2` ou `NIVEL_3`, exibidos como "Nível 1", "Nível 2" e "Nível 3". O filtro aceita um nível por vez; sem filtro, todos os níveis aparecem.
- **RN32:** Administrador e Médico podem atualizar o grau de suporte (obrigatório, um dos três valores) e a descrição clínica (opcional/anulável, até 5.000 caracteres após remover espaços das pontas) de um paciente ativo (UC08). A atualização vale imediatamente, não mantém histórico da versão anterior e não altera dados cadastrais do `Usuario`. Grau de suporte ausente ou inválido ou descrição acima de 5.000 caracteres são rejeitados com `400 Bad Request` e nada é gravado.
- **RN33:** Quando informado, o celular aparece mascarado (ex.: `(15) *****-1234`) na listagem e na ficha. Administrador e Médico podem revelá-lo por ação explícita na ficha. O Paciente vê o próprio celular sempre mascarado, pois a constituição (§4.4) autoriza a revelação apenas aos perfis `ADMINISTRADOR` e `MEDICO`. Quando o paciente não possui celular cadastrado, exibe-se indicação de campo não informado, sem erro.

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Listar pacientes ativos
```gherkin
Dado que existem pacientes ativos e inativados cadastrados
E um Médico ou Administrador está autenticado
Quando ele abre a listagem de pacientes
Então o sistema exibe só pacientes ativos, paginados em 10 itens, ordenados por nome de forma ascendente
E cada linha mostra nome, e-mail, grau de suporte e celular mascarado (ou indicação de não informado)
```

### Cenário 2: Buscar e filtrar
```gherkin
Dado que existem pacientes com nomes e graus de suporte variados
Quando o Médico busca por "jose" e filtra por Nível 2
Então a lista mostra apenas pacientes do Nível 2 cujo nome ou e-mail contém "jose", sem distinguir maiúsculas ou acentos (por exemplo "José")
E, se nenhum paciente corresponder, a tela exibe o estado vazio com orientação
```

### Cenário 3: Ordenar
```gherkin
Dado que a lista de pacientes está exibida
Quando o Administrador ordena pelo cabeçalho "E-mail" de forma descendente
Então a lista é reordenada por e-mail, do maior para o menor, mantendo busca e filtro ativos
```

### Cenário 4: Abrir a ficha com histórico por categoria
```gherkin
Dado que um paciente ativo possui documentos em Exames, Laudos e Receituário
Quando o Médico abre a ficha desse paciente
Então o sistema exibe dados cadastrais, grau de suporte e descrição clínica
E o histórico de documentos agrupado por categoria, com contagem por categoria, exibindo identificador, título, nome original do arquivo, categoria, data de envio e vínculo de substituição/situação em ordem cronológica, do mais recente para o mais antigo
E nenhum conteúdo binário de arquivo PDF é exibido na ficha
```

### Cenário 5: Revelar o celular
```gherkin
Dado que o Administrador ou o Médico está na ficha de um paciente com celular cadastrado
Quando ele aciona "Revelar celular"
Então o celular completo é exibido naquela ficha
E continua mascarado nas listagens e para o Paciente
```

### Cenário 6: Paciente acessa a própria ficha
```gherkin
Dado que um Paciente está autenticado
Quando ele abre "Meu prontuário"
Então o sistema exibe a ficha e o histórico de documentos dele
E o celular aparece mascarado, sem opção de revelar
```

### Cenário 7: Paciente tenta acessar dados de outro paciente
```gherkin
Dado que um Paciente está autenticado
Quando ele tenta abrir a lista de pacientes ou a ficha de outro paciente
Então o sistema responde 403 Forbidden
E nenhum dado de outro paciente é exibido
```

### Cenário 8: Atualizar dados clínicos
```gherkin
Dado que o Médico está na ficha de um paciente ativo com grau Nível 1
Quando ele altera o grau para Nível 2 e atualiza a descrição clínica
Então o sistema grava as alterações e a ficha passa a exibi-las
E os dados cadastrais do usuário permanecem inalterados
```

### Cenário 9: Dados clínicos inválidos
```gherkin
Dado que o Médico está editando os dados clínicos de um paciente
Quando ele envia grau de suporte ausente ou inexistente, ou descrição clínica com mais de 5.000 caracteres
Então o sistema responde 400 com a mensagem do campo inválido
E nenhuma alteração é gravada
```

### Cenário 10: Paciente tenta alterar dados clínicos
```gherkin
Dado que um Paciente está autenticado
Quando ele tenta atualizar o grau de suporte ou a descrição clínica
Então o sistema responde 403 Forbidden
E os dados clínicos permanecem inalterados
```

### Cenário 11: Paciente inativado
```gherkin
Dado que o Administrador excluiu logicamente um paciente (SPEC-001)
Quando um Médico busca por esse paciente ou tenta abrir a sua ficha
Então o paciente não aparece na listagem
E a ficha responde 404 Not Found
```

**Cobertura RF × Cenários:** RF-03.1 → 1; RF-03.2 → 2, 3; RF-03.3 → 4, 6; RF-03.4 → 6, 7; RF-03.5 → 8, 9, 10; RF-03.6 → 5, 6. RN04 → 4, 8; RN29 → 1, 7, 11; RN30 → 2, 3; RN31 → 2, 8, 9; RN32 → 8, 9, 10; RN33 → 1, 5, 6.

---

## 8. Casos de Borda e Tratamento de Exceções

- Paciente sem nenhum documento: o histórico exibe o estado vazio por categoria, sem erro.
- Busca com caracteres especiais ou curingas é tratada como texto literal. Termo acima de 100 caracteres é rejeitado com `400`.
- Página solicitada além do total de páginas retorna lista vazia com os totais corretos. Tamanho de página acima de 50 é limitado a 50.
- Histórico com muitos documentos é exibido paginado por categoria, com 10 itens por página e do mais recente para o mais antigo.
- Documentos substituídos permanecem no histórico, marcados como "Substituído"; documentos excluídos (SPEC-003) não aparecem.
- Dois profissionais editando os dados clínicos ao mesmo tempo: a última gravação prevalece, sem histórico da versão anterior (RN32).
- A atualização de dados clínicos e a revelação do celular não geram evento de auditoria nesta versão, pois a lista de ações auditáveis do domínio é fechada (LOGIN, UPLOAD, DOWNLOAD, EXCLUSAO e AGENDAMENTO).
- Todo paciente possui grau de suporte obrigatório desde o cadastro (SPEC-001); caso a descrição clínica ou dados opcionais de contato e endereço ainda não estejam preenchidos, a ficha exibe indicação de campo não informado, sem erro.
- Falha ao carregar a lista ou a ficha exibe o estado de erro com ação de recarregar, sem expor detalhes técnicos nem dados do paciente.

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** O cadastro de pacientes acontece nesta spec? → Não. O paciente nasce no cadastro de usuário com perfil `PACIENTE` (SPEC-001); esta spec cobre consulta e dados clínicos. **[Fonte: especificacao-requisitos.md RF-02, RF-03, RN04]**
- [x] **Dúvida 2:** Quem pode atualizar o grau de suporte e a descrição clínica? → Administrador e Médico, conforme a matriz de permissões (RN17, SPEC-001) e o Fluxo Alternativo (Passo 4–5) do `UC08` em `casos-de-uso.md`, no qual o Administrador ou Médico edita as anotações clínicas (`descricao`) e `grau_suporte` no modal do paciente. **[Fonte: casos-de-uso.md UC08; matriz RN17]**
- [x] **Dúvida 3:** Quais filtros a listagem oferece? → Busca por nome ou e-mail, filtro por grau de suporte e ordenação ASC/DESC (RN30, RN31). **[Fonte: especificacao-requisitos.md RF-03 e casos-de-uso.md UC08; Decisão de spec quanto às colunas ordenáveis]**
- [x] **Dúvida 4:** O Paciente vê o celular completo na própria ficha? → Não: a revelação é restrita a Administrador e Médico (RN33). **[Fonte: constitution.md §4.4]**
- [x] **Dúvida 5:** O que o histórico mostra? → Somente metadados dos documentos ativos (identificador, título do documento, nome original do arquivo, categoria, data de envio e vínculo de substituição/situação), agrupados por categoria com contagem por aba/acordeão, em ordem cronológica. O conteúdo binário do arquivo PDF é baixado pela SPEC-003. **[Fonte: especificacao-requisitos.md RF-03, RN08 e casos-de-uso.md UC09]**
- [x] **Dúvida 6:** Alterações de dados clínicos geram auditoria ou histórico? → Não nesta versão: a lista de ações auditáveis é fechada e a ficha não é versionada (RN32). **[Fonte: domain-model.md, `LogAuditoria`; Decisão de spec]**
- [x] **Dúvida 7:** Paciente tentando acessar ficha alheia recebe qual resposta? → `403 Forbidden`, coerente com a matriz de permissões (RN29). **[Fonte: casos-de-uso.md UC08]**
- [x] **Dúvida 8:** Qual o tamanho padrão da página? → 10 itens, máximo 50 (RN30). **[Decisão de spec]**
