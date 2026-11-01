# Constituição do Projeto: AMDE Digital — Controle de Prontuário

> **Projeto:** AMDE Digital — Controle de Prontuário  
> **Instituição Parceira:** Associação Amigos dos Deficientes (AMDE) — Sorocaba/SP  
> **Equipe de Desenvolvimento:**  
> - Gustavo Ferreira Lopes  
> - Helena Melchiades de Faria  
> - Kaiky Saquette Pereira  
> - Vinicius Firmino Oliveira de Souza  
> **Orientador:** Prof. Antonio Cesar de Barros Munari  
> **Versão da Constituição:** 1.2.0  
> **Última alteração:** 2026-11-01  
> **Finalidade:** Definir os princípios inegociáveis que valem para todo o projeto. Toda spec, plano e implementação deve respeitá-los. Em caso de conflito entre uma spec e esta constituição, a constituição prevalece, a menos que a spec registre explicitamente a exceção e sua justificativa.

---

## 1. Princípios Gerais

1. **A especificação é a fonte da verdade:** Mudanças de comportamento do sistema começam na spec; o código é derivado dela. Correções e alterações nunca são feitas diretamente no código-fonte sem atualização da respectiva especificação.
2. **Separação estrita "O Quê" vs. "Como":** A spec descreve o **quê** e o **porquê** em linguagem de negócio (domínio clínico e administrativo da AMDE). O plano técnico (`plan.md`) descreve o **como** (decisões técnicas, bibliotecas, endpoints e algoritmos). Specs não mencionam tabelas, DDL ou tipos SQL.
3. **Nada de suposições silenciosas:** Quando a especificação for omissa, ambígua ou incompleta, a IA deve formular perguntas/esclarecimentos antes de tomar decisões de projeto. Nenhuma escolha crítica (cardinalidade, exclusão, máscara ou restrição) pode passar despercebida. Toda suposição mantida deve ser explicitamente registrada no plano técnico.
4. **Rastreabilidade total:** Cada entidade, atributo, regra de negócio, tela e tarefa de implementação deve ser diretamente rastreável a um requisito funcional (RF), requisito não funcional (RNF) ou cenário BDD da spec.
5. **Consistência entre artefatos:** Modelo conceitual (`domain-model.md`), entidades, regras de negócio (RNs), telas e cenários de aceitação descrevem o mesmo domínio e não podem se contradizer. Nenhum artefato prevalece automaticamente sobre outro: divergências são apontadas pela IA como dúvida e resolvidas por decisão humana explícita (ver Seção 5.2).

---

## 2. Stack e Restrições Técnicas

- **Back-end:** Java 17 LTS com Spring Boot 3 (Spring Security, Spring Data JPA, MapStruct, Maven).
- **Front-end:** React 18+ com TypeScript, Vite, Tailwind CSS, Shadcn/ui (Radix UI) e Axios.
- **Banco de Dados:** PostgreSQL 16 (SGBD Relacional ACID).
- **Controle de Evolução de Banco:** Liquibase (versionamento formal via changelogs XML/YAML/SQL, com suporte a rollback).
- **Autenticação & Sessão:** Stateless via tokens JWT (JSON Web Tokens), senhas protegidas via algoritmo BCrypt.
- **Ambiente & Infraestrutura:** Docker e Docker Compose para isolamento de serviços; esteiras de CI/CD via GitHub Actions; nuvem AWS (S3 para armazenamento de PDFs e CloudFront para distribuição do frontend).
- **Testes Automatizados:** JUnit 5, Mockito e Spring Boot Test no backend; Vitest/Testing Library no frontend; todo requisito funcional deve possuir ao menos um cenário de teste de aceitação automatizado correspondente.

---

## 3. Princípios de Modelagem e Governança de Dados

### 3.1 Convenções
- **Nomenclatura no Banco:** Nomes de tabelas e colunas em português, `snake_case`, sempre no singular (ex.: `usuario`, `paciente`, `registro_prontuario`, `agendamento`).
- **Chave Primária:** Todo registro possui chave primária artificial `id` (inteiro sequencial ou UUID, conforme o módulo).
- **Chaves Estrangeiras:** Nomeadas no formato `<entidade>_id` ou `id_<entidade>` (ex.: `id_usuario`, `id_paciente`, `id_categoria`).
- **Auditoria Temporal:** Toda tabela deve conter obrigatoriamente as colunas `criado_em` e `atualizado_em` (com data, hora e fuso horário).
- **Valores Monetários:** Sempre tipo `DECIMAL(12,2)`. Jamais usar ponto flutuante (`FLOAT`/`DOUBLE`).
- **Datas e Horários:** Datas puras (ex.: data de nascimento) usam tipo `DATE`; eventos com horário (ex.: agendamentos clínicos) usam `TIMESTAMP WITH TIME ZONE`.
- **Campos Booleanos:** Nomeados como afirmação clara (ex.: `ativo`, `presenca_funcionario`, `presenca_paciente`).

### 3.2 Integridade e Divisão de Responsabilidades
- **O Banco de Dados garante:** Chaves primárias e estrangeiras, unicidade de identificadores (ex.: unicidade de e-mail em `usuario`), obrigatoriedade (`NOT NULL`), integridade referencial e domínios simples (ex.: valores não nulos ou checagens de formato).
- **A Aplicação garante:** Regras dependentes de múltiplos estados ou contexto temporal (ex.: verificação de conflito de horário entre consultas, limites de agendamento, validação de regras de permissão por perfil).
- **Relacionamentos N:N:** Sempre materializados por meio de tabela associativa explícita (ex.: `agenda_paciente`).
- **Listas de Valores Fixos:** Devem utilizar Enums tipados na aplicação e tipos enumerados ou constraints `CHECK` no banco de dados (ex.: status do agendamento, níveis de acesso, categorias).

### 3.3 Exclusão e Histórico
- **Exclusão Lógica (*Soft Delete*):** Entidades com relevância clínica, histórica ou legal (pacientes, funcionários, agendamentos, registros de auditoria) **nunca** sofrem exclusão física no banco de dados em produção; utilizam flag `ativo = false` ou `deletado_em`.
- **Arquivos Físicos:** Documentos de prontuário possuem ciclo de vida rastreado. Exclusões de arquivos de prontuário devem registrar log em conformidade com a RN10 e manter trilha de auditoria.
- **Versionamento de Documentos:** A substituição de laudos e exames deve preservar o histórico por meio do vínculo `id_registro_substituido`.

### 3.4 Proteção de Dados Sensíveis (LGPD e Resolução CFM nº 1.821/2007)
- **Dados Pessoais e Clínicos:** Nome, CPF, e-mail, telefone, data de nascimento, laudos e prontuários de crianças e adolescentes assistidos pela AMDE são dados sensíveis protegidos pela LGPD.
- **Logs Seguros:** Dados sensíveis, senhas criptografadas e conteúdos de prontuários **nunca** aparecem em logs de console, arquivos de depuração ou mensagens de erro enviadas ao cliente.
- **Trilha de Auditoria Obrigatória:** Todo acesso, leitura, download, criação, alteração ou exclusão de dados clínicos ou agendamentos deve disparar registro em `log_auditoria` (UC20 / RN16).

### 3.5 Evolução do Esquema (Migrations)
- **Imutabilidade em Produção:** Uma vez aplicada em ambiente compartilhado ou com dados reais, uma migration do Liquibase é imutável e vira histórico permanente. Erros são corrigidos exclusivamente por uma nova migration de compensação.
- **Migrations Incrementais:** Toda alteração no modelo de dados gera um novo changelog incremental no Liquibase contendo migração ascendente e, sempre que tecnicamente possível, procedimento de reversão seguro.
- **Veto a Operações Destrutivas Silenciosas:** A IA está proibida de gerar scripts que dropem tabelas, colunas ou convertam tipos com perda de dados sem alerta explícito e validação humana da equipe.

---

## 4. Princípios de Interface e Experiência do Usuário

### 4.1 Design System e Tokens
- **Biblioteca Base:** Componentes construídos com Tailwind CSS e Shadcn/ui (Radix UI), promovendo acessibilidade e personalização.
- **Design Tokens:** Cores institucionais da AMDE, tipografia, espaçamentos e raios de borda são formalizados em [`design/tokens.json`](../../design/tokens.json) e documentados em [`memory/design-system.md`](design-system.md).
- **Proibição de Valores Literais:** O código do front-end não deve declarar cores hexadecimais ou fontes soltas diretamente nos componentes, consumindo exclusivamente os tokens declarados.

### 4.2 Padrões de Interação e Usabilidade
- **Formulários:** Rótulos visíveis acima do campo, identificação clara de campos obrigatórios (`*`), validação no envio e ao desfocar o campo (*onBlur*), com mensagens de erro humanizadas junto ao componente.
- **Listagens e Tabelas:** Paginação ou carregamento sob demanda para listagens de pacientes e agendamentos; filtros de busca pelo nome/e-mail no topo; ordenação ascendente/descendente visível nas colunas.
- **Ações Destrutivas:** Exclusão de usuários ou arquivos clínicos requer confirmação explícita em modal de diálogo informando o impacto da ação.
- **Estados de Tela Obrigatórios:** Toda tela que consome APIs deve prever 4 estados visuais bem definidos: *Carregando (Skeleton/Spinner)*, *Vazio (Empty State orientativo)*, *Erro (Feedback amigável com ação de retry)* e *Sucesso (Dados renderizados)*.

### 4.3 Acessibilidade e Responsividade
- **Conformidade WCAG:** Aderência às diretrizes WCAG 2.1 nível AA.
- **Navegação por Teclado:** Todas as ações devem ser acessíveis via teclado (`Tab`, `Enter`, `Esc`), com indicadores visuais de foco nítidos.
- **Responsividade:** Layout adaptável com breakpoints Tailwind para visualização em desktops da clínica e dispositivos móveis dos profissionais.

### 4.4 Exibição de Dados Sensíveis
- Dados sensíveis como CPF e telefones devem ser apresentados na interface com máscara protetora por padrão (ex.: `***.456.789-**`), revelando-se apenas sob permissão explícita ou ação do usuário autorizado.

---

## 5. Comportamento Esperado da IA

### 5.1 Ao ler uma Especificação (`spec.md`)
1. Mapear todas as entidades, atributos e fluxos de negócio mencionados, inclusive os presentes em wireframes ou critérios BDD.
2. Checar a completude dos dados (obrigatoriedade, unicidade, cardinalidade, ciclo de vida e histórico).
3. Caso identifique omissões ou contradições, a IA **deve formular perguntas de esclarecimento**, sem tomar decisões arquiteturais por conta própria.

### 5.2 Verificação de Consistência entre Artefatos
Antes de gerar o plano técnico e sempre que uma especificação mudar, a IA deve checar:
- **Modelo Conceitual × Entidades:** Toda entidade do DER (`domain-model.md`) deve possuir correspondência nas especificações funcionais e vice-versa.
- **Entidades × Regras de Negócio:** Toda entidade citada em uma RN (ex.: regras RN01 a RN16) deve existir formalmente no modelo.
- **Telas × Entidades:** Todo campo exibido ou coletado na interface deve ter uma entidade de destino ou cálculo de origem declarado.
- **Cenários BDD × Requisitos:** Todo cenário de aceite deve testar ao menos um RF ou RN, e todo RF deve possuir ao menos um cenário de teste.
- **Linguagem Ubíqua:** Termos como "Paciente", "Profissional", "Prontuário", "Agendamento" e "Presença" devem ter rigorosamente o mesmo significado em todos os documentos.

### 5.3 Ao gerar o Delta do Modelo de Dados (`data-model.md`)
- Derivar o modelo lógico estritamente a partir do modelo conceitual em `domain-model.md`.
- Apresentar a tabela de **Mapeamento Conceitual ➔ Materialização Lógica**.
- Distinguir o que é *Definido na Spec*, o que é *Derivado da Constituição* e o que é *Suposição Técnica com Justificativa*.
- Gerar scripts Liquibase com validação de tipos e rollback.

### 5.4 Ao receber uma alteração de Requisito
- Atualizar o modelo conceitual em `domain-model.md` caso a regra envolva cardinalidades ou novas entidades.
- Executar a checagem cruzada de consistência.
- Gerar nova migration Liquibase incremental (sem reescrever histórico).
- Identificar e regenerar apenas as telas e endpoints diretamente impactados pela mudança.

---

## 6. Governança e Versionamento

- As alterações nesta constituição seguem versionamento semântico:
  - **MAJOR (ex.: 2.0.0):** Alteração de princípios fundamentais, troca de banco de dados ou inversão de regras de governança. Exige revisão completa de todas as specs ativas.
  - **MINOR (ex.: 1.3.0):** Adição de novos princípios, regras de auditoria ou novas restrições técnicas.
  - **PATCH (ex.: 1.2.1):** Correção ortográfica, refinamento de redação ou clarificação de diretriz sem alteração de escopo.
- A aprovação de alterações na Constituição cabe à equipe técnica do projeto e ao orientador acadêmico.
