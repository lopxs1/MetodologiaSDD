# Especificação Funcional: Autenticação, Controle de Acesso e Gestão de Usuários

> **ID da Spec:** `SPEC-001`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-01, RF-02 · UC01 a UC07 · RN01 a RN04, RN16, RN17 a RN28 · Pacote `specs/001-autenticacao-e-usuarios/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
Na AMDE, o acesso às informações de crianças e adolescentes com TEA sempre dependeu de fichas em papel e planilhas descentralizadas, sem como saber quem consultou o quê e sem um cadastro único de quem trabalha na instituição ou é atendido por ela. Com o AMDE Digital, dados clínicos sensíveis passam a ficar em um sistema único. Sem identificar cada pessoa, limitar o que cada perfil pode fazer e manter um cadastro confiável de usuários, a instituição fica exposta a acesso indevido e a descumprimento da LGPD e da Resolução CFM nº 1.821/2007. São impactados administradores, profissionais de saúde e pacientes (ou seus titulares).

### 1.2. Objetivo e Valor Entregue
Permitir que o Administrador cadastre, consulte, atualize e exclua usuários, e que cada pessoa cadastrada entre no sistema com e-mail e senha e acesse apenas o que o seu perfil permite. Esta spec é a base das demais: define os perfis, a matriz de permissões (RN17), o cadastro que origina Funcionários (Médicos) e Pacientes e o registro de cada login e de cada exclusão para a trilha de auditoria.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** login por e-mail e senha, rejeição de credenciais inválidas, sessão autenticada com validade, autorização por perfil, encerramento de sessão ("Sair"), direcionamento inicial por perfil, registro de login; cadastro, consulta (lista paginada com busca, filtro opcional por perfil de acesso, ordenação, totalizadores agregados por perfil e visualização detalhada), atualização e exclusão lógica de usuários pelo Administrador, incluindo a especialização em Médico (Funcionário) ou Paciente no momento do cadastro.
- **Fora de escopo (decisões de spec):** recuperação ou troca de senha pelo próprio usuário (não existe em RF/UC); autenticação em dois fatores; bloqueio por tentativas falhas (exigiria novo estado no modelo de domínio); perfis "Visitante" e "Colaborador de Apoio" citados na monografia (o modelo oficial tem só três perfis); auto-cadastro e importação em massa de usuários; alteração do perfil de um usuário já cadastrado (RN24); exclusão definitiva de usuários; criação do primeiro administrador (procedimento de implantação descrito no plano técnico, com dados sintéticos em desenvolvimento, sem dados reais de menores).
- **Dependências:** SPEC-004 (consulta de agendamentos em situação AGENDADO para bloquear exclusão, RN27) e SPEC-005 (gravação dos eventos LOGIN e EXCLUSAO). A SPEC-002 consome os Pacientes cadastrados aqui.

---

## 2. Atores e Perfis de Acesso

- **Administrador:** entra no sistema, acessa todas as funcionalidades administrativas e é o único que gere usuários (cadastrar, consultar, atualizar e excluir); ao entrar, é direcionado ao Dashboard.
- **Profissional de Saúde / Médico (perfil `MEDICO`, materializado como Funcionário):** entra no sistema e acessa pacientes, prontuários e a própria agenda; ao entrar, é direcionado a Agendamentos. Não gere usuários.
- **Paciente:** entra no sistema e acessa apenas o próprio cadastro, os próprios documentos e os próprios agendamentos; ao entrar, é direcionado a Agendamentos. Não gere usuários.
- **Visitante (não autenticado):** só enxerga a tela de login.

---

## 3. Histórias de Usuário (User Stories)

- **US-01.1:** Como **usuário cadastrado (qualquer perfil)**, quero **entrar com meu e-mail e senha** para que **eu acesse as funções do meu perfil**.
- **US-01.2:** Como **Administrador**, quero que **cada perfil acesse somente o que lhe cabe** para que **os dados clínicos sensíveis fiquem protegidos conforme a LGPD**.
- **US-01.3:** Como **usuário autenticado**, quero **encerrar minha sessão pelo botão "Sair"** para que **ninguém use minha conta em um computador compartilhado da clínica**.
- **US-01.4:** Como **Administrador**, quero que **todo login bem-sucedido fique registrado** para que **a instituição saiba quem acessou o sistema e quando**.
- **US-02.1:** Como **Administrador**, quero **cadastrar um novo usuário com seu perfil e dados profissionais ou clínicos** para que **ele possa acessar o sistema e ser atendido ou atender na AMDE**.
- **US-02.2:** Como **Administrador**, quero **buscar, filtrar por perfil, ordenar e consultar os usuários cadastrados com seus totalizadores** para que **eu encontre rapidamente qualquer pessoa e acompanhe a composição da base**.
- **US-02.3:** Como **Administrador**, quero **atualizar os dados de um usuário** para que **o cadastro reflita mudanças de contato, endereço, senha ou dados profissionais**.
- **US-02.4:** Como **Administrador**, quero **excluir usuários que não fazem mais parte da AMDE** para que **percam o acesso sem que o histórico clínico seja perdido**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-01.1** | O sistema deve permitir login com e-mail e senha de usuário ativo, concedendo sessão autenticada que identifica o usuário e o seu perfil (UC01, UC02, UC03). | Crítica |
| **RF-01.2** | O sistema deve rejeitar credenciais inválidas (e-mail inexistente, senha incorreta ou usuário inativado) com uma única mensagem genérica, sem revelar qual dado falhou. | Crítica |
| **RF-01.3** | O sistema deve exigir sessão válida em toda funcionalidade, exceto o login, e rejeitar requisições sem sessão, com sessão expirada ou adulterada. | Crítica |
| **RF-01.4** | O sistema deve negar qualquer operação fora do perfil do usuário autenticado, conforme a matriz de permissões da RN17. | Crítica |
| **RF-01.5** | O sistema deve permitir que o usuário encerre a própria sessão pelo item "Sair" do menu lateral. | Alta |
| **RF-01.6** | O sistema deve direcionar o usuário após o login conforme o perfil (Administrador: Dashboard; Médico e Paciente: Agendamentos) e exibir no menu lateral apenas os itens permitidos ao perfil. | Média |
| **RF-01.7** | O sistema deve registrar na trilha de auditoria todo login bem-sucedido (RN16, SPEC-005). | Crítica |
| **RF-02.1** | O sistema deve permitir ao Administrador cadastrar um usuário informando os dados obrigatórios e eventuais campos opcionais da RN22, a senha inicial (RN21) e o perfil, criando junto a especialização exigida pelo perfil (RN23) e rejeitando e-mail já usado por usuário ativo com `409 Conflict` (RN02) (UC04). | Alta |
| **RF-02.2** | O sistema deve permitir ao Administrador listar usuários ativos com paginação, busca textual por nome ou e-mail, filtro opcional por perfil de acesso (`ADMINISTRADOR`, `MEDICO`, `PACIENTE`), ordenação ASC/DESC e totalizadores agregados por perfil (`Total`, `Administradores`, `Médicos`, `Pacientes`), e abrir o detalhe de um usuário, exibindo o celular mascarado até ação explícita de revelar (RN28) (UC05). | Alta |
| **RF-02.3** | O sistema deve permitir ao Administrador atualizar os dados de um usuário ativo, inclusive os de sua especialização, sem alterar o perfil (RN24), revalidando e-mail único (RN02) e, se informada, a nova senha (RN21) (UC06). | Alta |
| **RF-02.4** | O sistema deve permitir ao Administrador excluir logicamente um usuário, após confirmação em diálogo, inativando também sua especialização, respeitando os bloqueios das RN26 e RN27 e registrando o evento EXCLUSAO na auditoria (RN16, RN25) (UC07). | Alta |
| **RF-02.5** | O sistema deve restringir as operações de gestão de usuários exclusivamente ao Administrador, negando-as a Médicos e Pacientes (RN03). | Crítica |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-01.1** | Segurança / Confidencialidade | A senha nunca é armazenada em texto puro, nem devolvida em resposta, nem escrita em logs (RN01). | Teste de integração inspecionando o dado persistido e varredura dos logs após cenários de login e cadastro |
| **RNF-01.2** | Segurança / Autenticidade | A sessão vale 8 horas, sem renovação automática. Sessão adulterada ou vencida é rejeitada em 100% dos casos. | Testes automatizados com sessão vencida e adulterada |
| **RNF-01.3** | Segurança / Confidencialidade | E-mail inexistente, senha incorreta e usuário inativado produzem respostas com o mesmo conteúdo e o mesmo código de erro. | Teste automatizado comparando as três respostas |
| **RNF-01.4** | Segurança / Confidencialidade | Toda comunicação com o sistema ocorre por canal seguro (HTTPS). | Inspeção de cabeçalhos e configuração do ambiente |
| **RNF-01.5** | Eficiência de Desempenho | O login responde em menos de 500 ms no percentil 95 com 20 logins simultâneos. | Teste de carga com massa de dados sintética |
| **RNF-01.6** | Usabilidade / Acessibilidade | A tela de login atende WCAG 2.1 AA: envio por `Enter`, foco visível, erro associado ao campo e contraste mínimo de 4,5:1. | Teste E2E de interface e verificação com Axe DevTools |
| **RNF-02.1** | Eficiência de Desempenho | A listagem de usuários com busca, filtro e ordenação retorna em menos de 500 ms no percentil 95 com 10.000 usuários cadastrados. | Teste de carga com massa de dados sintética |
| **RNF-02.2** | Segurança / Confidencialidade | Dados pessoais (celular completo, endereço, data de nascimento) e senhas nunca aparecem em logs ou em mensagens de erro enviadas ao cliente. | Varredura automatizada dos logs e das respostas de erro após os cenários de cadastro, edição e exclusão |
| **RNF-02.3** | Usabilidade / Interface | Formulários e listagem seguem a constituição (§4.2): rótulos visíveis, campos obrigatórios com `*`, validação ao sair do campo e no envio, botão bloqueado com erros, modal de confirmação na exclusão e os quatro estados de tela (carregando, vazio, erro, sucesso). | Teste E2E de interface |
| **RNF-02.4** | Integridade | Dois cadastros simultâneos com o mesmo e-mail resultam em exatamente um sucesso e um `409 Conflict`. | Teste automatizado de concorrência |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `Usuario` (dados cadastrais, senha protegida, perfil de acesso, situação ativo/inativo), `Funcionario` (registro profissional e cargo do Médico), `Paciente` (grau de suporte e descrição clínica), `LogAuditoria` (somente gravação dos eventos LOGIN e EXCLUSAO, via SPEC-005).
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
| `Usuario` | Chave substituta; chave de negócio: e-mail único entre usuários ativos, sem distinguir maiúsculas de minúsculas | **Obrigatórios:** nome, e-mail, senha e perfil. **Opcionais (anuláveis):** data de nascimento (contas administrativas iniciais podem ser criadas antes da complementação cadastral), celular (nem todo usuário possui telefone próprio no ato do pré-cadastro) e endereço — país (padrão `"Brasil"`), estado, cidade e rua (dados complementares de endereço) | 0..1 `Funcionario` ou 0..1 `Paciente` (exatamente o exigido pelo perfil, RN23) | Exatamente um perfil, imutável (RN24); só usuário ativo autentica (RN18) | Inativação lógica bloqueia o login imediatamente e libera o e-mail para novo cadastro (RN25) | `pessoal`: nome, e-mail (credencial), data de nascimento, celular, país, estado, cidade, rua. Senha é só um resumo criptográfico, nunca exibido |
| `Funcionario` | Chave substituta; vínculo 1:1 com `Usuario` | **Obrigatório:** cargo (especialidade ou função clínica que alimenta o Dashboard). **Opcional (anulável):** registro profissional (colaboradores administrativos ou funções pedagógicas de apoio podem não possuir registro em conselho médico) | 1:1 com `Usuario`; 0..n `Agendamento` (SPEC-004) | Existe somente para usuário `MEDICO` (RN23). Sem unicidade nem validação externa do registro profissional nesta versão | Inativado junto com o `Usuario` (RN25) | `pessoal`: registro profissional e cargo |
| `Paciente` | Chave substituta; vínculo 1:1 com `Usuario` | **Obrigatório:** grau de suporte. **Opcional (anulável):** descrição clínica (o cadastro inicial pela recepção pode preceder a avaliação clínica pelo especialista) | 1:1 com `Usuario`; 0..n documentos (SPEC-003); 0..n vínculos de agendamento (SPEC-004) | Existe somente para usuário `PACIENTE` (RN23); grau de suporte é `NIVEL_1`, `NIVEL_2` ou `NIVEL_3` | Inativado junto com o `Usuario` (RN25); histórico preservado | `sensível`: grau de suporte e descrição (dados clínicos de menor atendido) |
| `LogAuditoria` | Evento único por operação auditada | Usuário, ação (`LOGIN` ou `EXCLUSAO`), recurso, IP de origem, instante | N:1 `Usuario` | Imutável (SPEC-005) | Somente inclusão | IP é `pessoal`, usado para rastreabilidade |

---

## 6. Regras de Negócio (RN)

- **RN01** *(global)*: Senhas são protegidas exclusivamente com BCrypt, nunca em texto puro.
- **RN02** *(global)*: O e-mail é único entre usuários ativos. Cadastro ou edição com e-mail duplicado é rejeitado com `409 Conflict`.
- **RN03** *(global)*: A criação e a gestão de usuários são exclusivas do Administrador.
- **RN04** *(global)*: Todo `Usuario` pode se especializar em exatamente um `Paciente` (grau de suporte TEA) ou `Funcionario` (registro profissional e cargo), em vínculo 1:1.
- **RN16** *(global)*: Toda ação sensível gera registro na trilha de auditoria (LGPD / CFM).
- **RN17:** Existem três perfis oficiais: `ADMINISTRADOR`, `MEDICO` e `PACIENTE`. Todo usuário tem exatamente um perfil, e as permissões seguem a matriz abaixo, que vale para todas as specs.
- **RN18:** Apenas usuários ativos autenticam. Se um usuário for inativado, perde o acesso na requisição seguinte, mesmo com sessão ainda dentro da validade.
- **RN19:** A falha de autenticação tem resposta única e genérica ("E-mail ou senha inválidos"), com erro `401 Unauthorized`.
- **RN20:** A sessão vale 8 horas e não é renovada automaticamente. Depois de expirar, é preciso fazer login de novo. "Sair" encerra a sessão no navegador; como a autenticação é stateless, não há revogação no servidor.
- **RN21:** A senha definida pelo Administrador no cadastro (e na edição, quando informada) deve ter no mínimo 8 caracteres, não pode ser formada só por espaços e deve ter no máximo 72 bytes na codificação UTF-8 (limite do algoritmo da RN01). Senhas acima do limite são rejeitadas, nunca cortadas em silêncio. Espaços, acentos e símbolos são aceitos.
- **RN22:** Todo cadastro de usuário exige obrigatoriamente: nome (2 a 100 caracteres); e-mail válido (5 a 254 caracteres, com `@` após o primeiro caractere, normalizado sem espaços nas pontas e em minúsculas); senha (RN21); e perfil. São opcionais (anuláveis) no cadastro: data de nascimento (quando informada, válida entre `1900-01-01` e a data atual, não futura); celular (quando informado, 10 ou 11 dígitos apenas numéricos e com DDD — a máscara é só da interface); país (quando informado, 2 a 60 caracteres, com padrão `"Brasil"`); estado (quando informado, sigla de UF válida com 2 letras maiúsculas); cidade (quando informada, 2 a 100 caracteres); e rua com logradouro e número (quando informada, 2 a 150 caracteres). Violação de qualquer item informado ou ausência de campo obrigatório é rejeitada com `400 Bad Request` e mensagem por campo. Não há restrição de idade por perfil além da faixa válida.
- **RN23:** O perfil define a especialização criada no mesmo cadastro (detalha a RN04): `PACIENTE` exige grau de suporte obrigatório (`NIVEL_1`, `NIVEL_2` ou `NIVEL_3`) e aceita descrição clínica opcional (quando informada, até 5.000 caracteres); `MEDICO` exige cargo obrigatório (2 a 80 caracteres) e aceita registro profissional opcional (quando informado, 3 a 30 caracteres, ex.: "CRM/SP 123456"); `ADMINISTRADOR` não tem especialização. Dados de especialização incompatíveis com o perfil ou fora dos limites são rejeitados com `400 Bad Request`.
- **RN24:** O perfil de um usuário não pode ser alterado depois do cadastro. Para mudar de perfil, o usuário antigo é excluído e um novo é cadastrado.
- **RN25:** A exclusão de usuário é lógica: inativa o usuário e a sua especialização, preserva todo o histórico (documentos, agendamentos e eventos de auditoria) e libera o e-mail para um novo cadastro ativo. Usuário já inativado não pode ser editado nem excluído de novo (`404 Not Found`).
- **RN26:** O Administrador não pode excluir a própria conta (`409 Conflict`), para que a instituição nunca fique sem administrador.
- **RN27:** Um usuário `MEDICO` responsável por agendamentos em situação AGENDADO, ou um `PACIENTE` participante deles, não pode ser excluído (`409 Conflict`). Os agendamentos devem antes ser cancelados ou concluídos (SPEC-004).
- **RN28:** Quando informado, o celular é exibido mascarado (ex.: `(15) *****-1234`) na listagem e também no detalhe, e só é revelado por ação explícita do Administrador na visualização detalhada, conforme a constituição (§4.4). Quando não informado no cadastro, exibe indicação de não informado sem erro.

**Matriz de permissões (RN17)**

| Operação | Administrador | Médico | Paciente |
|---|---|---|---|
| Gerir usuários (cadastrar, consultar, atualizar, excluir) — SPEC-001 | Sim | Não | Não |
| Listar pacientes e consultar ficha — SPEC-002 | Sim | Sim | Apenas a própria ficha |
| Editar dados clínicos do paciente — SPEC-002 | Sim | Sim | Não |
| Enviar, substituir, reclassificar, arquivar e excluir documento — SPEC-003 | Sim | Sim | Não |
| Baixar documento de prontuário — SPEC-003 | Qualquer paciente | Qualquer paciente | Apenas os próprios |
| Criar, editar, cancelar, concluir e registrar presença em agendamento — SPEC-004 | Qualquer profissional | Somente a própria agenda | Não |
| Listar agendamentos — SPEC-004 | Todos | Somente os seus | Somente os seus |
| Dashboard gerencial — SPEC-006 | Sim | Não | Não |
| Consultar a trilha de auditoria — SPEC-005 | Não (fora do escopo desta versão) | Não | Não |

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Login com sucesso (Administrador)
```gherkin
Dado que existe um usuário ativo com perfil ADMINISTRADOR e senha cadastrada
Quando ele informa o e-mail e a senha corretos
Então o sistema concede uma sessão válida por 8 horas
E o direciona ao Dashboard, com o menu completo do perfil
E um evento LOGIN é registrado na trilha de auditoria com o usuário e o IP de origem
```

### Cenário 2: Senha incorreta
```gherkin
Dado que existe um usuário ativo
Quando ele informa o e-mail correto e uma senha incorreta
Então o sistema responde 401 com a mensagem genérica "E-mail ou senha inválidos"
E nenhuma sessão é concedida
E nenhum evento LOGIN é registrado
```

### Cenário 3: E-mail inexistente ou usuário inativado
```gherkin
Dado que um e-mail não está cadastrado, ou pertence a um usuário inativado
Quando alguém tenta entrar com esse e-mail e qualquer senha
Então o sistema responde exatamente igual ao Cenário 2, com o mesmo código e a mesma mensagem
```

### Cenário 4: Campos obrigatórios vazios
```gherkin
Dado que a tela de login está aberta
Quando o usuário envia o formulário sem e-mail ou sem senha
Então o sistema indica o campo obrigatório junto ao campo, sem tentar autenticar
```

### Cenário 5: Redirecionamento por perfil
```gherkin
Dado que existem usuários ativos com perfis MEDICO e PACIENTE
Quando cada um faz login com sucesso
Então ambos são direcionados a Agendamentos
E o menu lateral do Médico mostra Pacientes e Agendamentos, sem Usuários nem Dashboard
E o menu do Paciente mostra o próprio prontuário e Agendamentos
```

### Cenário 6: Acesso fora do perfil
```gherkin
Dado que um Paciente ou um Médico está autenticado
Quando ele tenta acessar a gestão de usuários (listar, cadastrar, editar ou excluir)
Então o sistema responde 403 Forbidden
E nenhum dado de usuário é exibido ou alterado
```

### Cenário 7: Sessão expirada ou adulterada
```gherkin
Dado que a sessão de um usuário passou de 8 horas, ou foi alterada manualmente
Quando ele faz qualquer requisição
Então o sistema responde 401
E a interface o leva à tela de login
```

### Cenário 8: Encerrar sessão
```gherkin
Dado que um usuário está autenticado
Quando ele aciona "Sair"
Então a interface descarta a sessão e exibe a tela de login
E qualquer tentativa de voltar às telas internas exige novo login
```

### Cenário 9: Usuário inativado durante a sessão
```gherkin
Dado que um usuário tem sessão válida
E o Administrador o exclui (inativação lógica)
Quando ele faz a próxima requisição
Então o sistema responde 401 e nega o acesso
```

### Cenário 10: Cadastro de Médico com sucesso
```gherkin
Dado que o Administrador está autenticado
Quando ele cadastra um usuário com perfil MEDICO, nome, e-mail, senha válida e cargo, com ou sem registro profissional e campos opcionais de contato e endereço
Então o usuário e o seu Funcionário são criados na mesma operação
E a senha é guardada protegida e nunca é devolvida na resposta
E o novo Médico consegue fazer login em seguida
```

### Cenário 11: Cadastro de Paciente com sucesso
```gherkin
Dado que o Administrador está autenticado
Quando ele cadastra um usuário com perfil PACIENTE, nome, e-mail, senha válida, grau de suporte NIVEL_2 e descrição clínica opcional
Então o usuário e o seu Paciente são criados na mesma operação
E o Paciente passa a constar na listagem de pacientes (SPEC-002)
```

### Cenário 12: E-mail já cadastrado
```gherkin
Dado que já existe um usuário ativo com o e-mail "Ana@amde.org"
Quando o Administrador cadastra outro usuário com o e-mail "  ana@AMDE.org "
Então o sistema responde 409 Conflict
E nenhum usuário nem especialização é criado
```

### Cenário 13: Dados inválidos no cadastro
```gherkin
Dado que o Administrador está autenticado
Quando ele envia o cadastro com senha de 6 caracteres, nome acima de 100 caracteres, celular com 9 dígitos, estado inexistente, cargo acima de 80 caracteres ou especialização incompatível com o perfil
Então o sistema responde 400 com a mensagem do campo inválido
E nenhum dado é persistido
```

### Cenário 14: Consultar usuários com busca, filtro por perfil, ordenação e totalizadores
```gherkin
Dado que existem usuários ativos de diferentes perfis e usuários inativados cadastrados
Quando o Administrador abre a listagem, busca por parte do nome ou do e-mail, filtra opcionalmente por perfil e ordena por nome de forma descendente
Então o sistema exibe só usuários ativos que correspondem à busca e ao filtro, paginados e na ordem pedida
E exibe os totalizadores agregados de usuários ativos por perfil (Total, Administradores, Médicos e Pacientes)
E o celular informado de cada usuário aparece mascarado
E, sem resultados, a tela exibe o estado vazio com orientação
```

### Cenário 15: Detalhe e revelação do celular
```gherkin
Dado que o Administrador abriu o detalhe de um usuário com celular cadastrado
Quando ele aciona "Revelar celular"
Então o celular completo é exibido naquela visualização
E o celular continua mascarado nas listagens
```

### Cenário 16: Atualizar usuário
```gherkin
Dado que existem dois usuários ativos
Quando o Administrador altera o celular e a cidade do primeiro, sem informar senha
Então os dados são atualizados e a senha anterior continua válida
E, se ele informar o e-mail do segundo usuário, o sistema responde 409 Conflict
E, se ele tentar trocar o perfil, o sistema rejeita a alteração com 400
```

### Cenário 17: Excluir usuário
```gherkin
Dado que um usuário ativo não tem agendamentos em situação AGENDADO
Quando o Administrador confirma a exclusão no diálogo de confirmação
Então o usuário e a sua especialização são inativados, sem apagar o histórico
E um evento EXCLUSAO é registrado na trilha de auditoria
E o e-mail do usuário fica disponível para novo cadastro
E o usuário não consegue mais fazer login
```

### Cenário 18: Exclusão bloqueada
```gherkin
Dado que o Administrador está autenticado
Quando ele tenta excluir a própria conta, ou um Médico ou Paciente com agendamentos em situação AGENDADO
Então o sistema responde 409 Conflict com mensagem informativa
E o usuário permanece ativo
E nenhum evento EXCLUSAO é registrado
```

**Cobertura RF × Cenários:** RF-01.1 → 1; RF-01.2 → 2, 3, 4; RF-01.3 → 7; RF-01.4 → 6; RF-01.5 → 8; RF-01.6 → 1, 5; RF-01.7 → 1; RF-02.1 → 10, 11, 12, 13; RF-02.2 → 14, 15; RF-02.3 → 16; RF-02.4 → 17, 18; RF-02.5 → 6. RN01 → 1, 10; RN02 → 12, 16; RN03 → 6; RN04 → 10, 11; RN16 → 1, 17; RN18 → 3, 9; RN19 → 2, 3; RN20 → 7, 8; RN21 → 13; RN22 → 10, 11, 13; RN23 → 10, 11, 13; RN24 → 16; RN25 → 17; RN26 → 18; RN27 → 18; RN28 → 14, 15.

---

## 8. Casos de Borda e Tratamento de Exceções

- E-mail digitado com espaços nas pontas ou com maiúsculas é normalizado antes de comparar, tanto no login quanto no cadastro e na edição, em coerência com a unicidade sem distinção de caixa.
- Falta de e-mail ou senha no login é erro de validação do formulário, diferente da falha de autenticação, e não confirma a existência de nenhuma conta.
- O mesmo usuário pode ter várias sessões simultâneas (por exemplo, recepção e sala de atendimento).
- Sessão adulterada ou com assinatura inválida é tratada como não autenticada (401), sem detalhar o motivo.
- Falha ao gravar o evento LOGIN não impede o acesso do usuário. A falha é tratada conforme a RN44 (SPEC-005). Já o evento EXCLUSAO de usuário segue a mesma regra: a exclusão prossegue e a falha é tratada pela RN44.
- Sem bloqueio por tentativas falhas nesta versão. O risco de força bruta fica registrado para evolução futura.
- Senha com espaços, acentos ou caracteres especiais é aceita no login. Os limites de tamanho valem só no cadastro e na edição (RN21).
- Alterar a senha ou o e-mail de um usuário não derruba sessões já emitidas, que seguem válidas até expirar (RN20). Apenas a exclusão derruba o acesso, pela RN18.
- Dois cadastros simultâneos com o mesmo e-mail: apenas um é aceito e o outro recebe `409 Conflict` (RNF-02.4). Duas edições simultâneas do mesmo usuário: a última gravação prevalece.
- Celular colado com máscara, parênteses ou traços: a interface remove os símbolos antes de enviar. Se o sistema receber caracteres não numéricos, rejeita com `400`. Se o usuário não possuir celular cadastrado, a listagem e o detalhe exibem indicação de não informado sem opção de revelação e sem erro.
- Campos opcionais omitidos no cadastro (`data de nascimento`, `celular`, `estado`, `cidade`, `rua`, `registro profissional` do Médico e `descrição clínica` do Paciente) são preservados como nulos até complementação posterior; `país` assume `"Brasil"` por padrão quando não especificado.
- Editar ou excluir usuário inexistente ou já inativado responde `404 Not Found`.
- Busca com caracteres especiais é tratada como texto literal. Termo de busca acima de 100 caracteres é rejeitado com `400`.
- Médico ou Paciente excluído mantém seus documentos e agendamentos históricos, sem política automática de arquivamento nesta versão.

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** Quais perfis existem, já que a monografia fala em "Visitante" e "Colaborador de Apoio"? → Apenas `ADMINISTRADOR`, `MEDICO` e `PACIENTE`. **[Fonte: constitution.md §2; domain-model.md]**
- [x] **Dúvida 2:** Os IDs de RF da monografia (RF01–RF05) divergem dos de `especificacao-requisitos.md` (RF-01–RF-07). → Vale `especificacao-requisitos.md` v1.0.0, usada pela constituição e pelo roadmap. **[Fonte]**
- [x] **Dúvida 3:** Qual a validade da sessão? → 8 horas, sem renovação (RN20). **[Decisão de spec]**
- [x] **Dúvida 4:** Login falho deve ir para a auditoria? → Não. O evento exige usuário identificado e a lista de ações auditáveis não tem "login falho". **[Fonte: domain-model.md, `LogAuditoria`]**
- [x] **Dúvida 5:** O que "Sair" faz sem estado no servidor? → Descarta a sessão no navegador, sem revogação no servidor (RN20). **[Fonte: constitution.md §2, autenticação stateless]**
- [x] **Dúvida 6:** O usuário inativado perde acesso na hora? → Sim (RN18). **[Decisão de spec]**
- [x] **Dúvida 7:** Autenticação e gestão de usuários ficam na mesma spec? → Sim: o roadmap e o `especificacao-requisitos.md` associam RF-01 e RF-02 ao mesmo pacote `001-autenticacao-e-usuarios`. **[Fonte]**
- [x] **Dúvida 8:** Quando a especialização (Médico ou Paciente) é criada? → No mesmo cadastro do usuário, conforme o perfil (RN04, RN23). **[Fonte: especificacao-requisitos.md RN04; casos-de-uso.md UC04]**
- [x] **Dúvida 9:** Quais campos do usuário e de suas especializações são obrigatórios e quais são opcionais? → Em `Usuario`, são obrigatórios nome, e-mail, senha e perfil; data de nascimento, celular e endereço (país com padrão `"Brasil"`, estado, cidade e rua) são opcionais (anuláveis) para permitir pré-cadastros e contas administrativas iniciais (RN22). Em `Funcionario`, cargo é obrigatório e registro profissional é opcional (para funções pedagógicas ou administrativas de apoio sem registro em conselho). Em `Paciente`, grau de suporte é obrigatório e descrição clínica é opcional (o cadastro inicial pela recepção pode preceder a avaliação clínica pelo especialista) (RN23). **[Fonte: dicionario-de-dados.md, 001_esquema_inicial.sql e casos-de-uso.md UC04]**
- [x] **Dúvida 10:** Quem define a senha e como ela é trocada? → O Administrador define a senha no cadastro e pode redefini-la na edição. O usuário não troca a própria senha (fora de escopo). Limites na RN21. **[Decisão de spec]**
- [x] **Dúvida 11:** O perfil pode ser alterado depois? → Não (RN24). **[Decisão de spec]**
- [x] **Dúvida 12:** O que impede exclusões indevidas? → O Administrador não exclui a própria conta (RN26) e ninguém com agendamentos em situação AGENDADO é excluído (RN27). **[Decisão de spec]**
- [x] **Dúvida 13:** A exclusão de usuário é auditada? → Sim, como EXCLUSAO, pois a RF-06 cobre "exclusão" sem restringi-la a documentos. **[Fonte: especificacao-requisitos.md RF-06; casos-de-uso.md UC07]**
- [x] **Dúvida 14:** Como o celular é exibido? → Mascarado por padrão e revelado por ação explícita do Administrador (RN28). **[Fonte: constitution.md §4.4]**
- [x] **Dúvida 15:** O registro profissional do Médico precisa ser único ou validado em conselho? → Não nesta versão: o modelo de domínio não prevê unicidade nem consulta externa. **[Fonte: domain-model.md]**
- [x] **Dúvida 16:** Quem edita os dados clínicos do Paciente, além do Administrador? → O Médico, pela SPEC-002, conforme a matriz de permissões (RN17) e o Fluxo Alternativo do `UC08`. **[Fonte: casos-de-uso.md UC08; Decisão de spec]**
