# Constituição do Projeto: AMDE Digital — Controle de Prontuário

> **Projeto:** AMDE Digital — Controle de Prontuário  
> **Instituição Parceira:** Associação Amigos dos Deficientes (AMDE) — Sorocaba/SP  
> **Equipe de Desenvolvimento:**  
> - Gustavo Ferreira Lopes  
> - Helena Melchiades de Faria  
> - Kaiky Saquette Pereira  
> - Vinicius Firmino Oliveira de Souza  
> **Orientador:** Prof. Antonio Cesar de Barros Munari  
> **Versão da Constituição:** 1.3.0  
> **Última alteração:** 2026-11-10  
> **Finalidade:** Definir os princípios inegociáveis que valem para todo o projeto. Toda spec, plano e implementação deve respeitá-los. Em caso de conflito entre uma spec e esta constituição, a constituição prevalece, a menos que a spec registre explicitamente a exceção e sua justificativa.

---

## 1. Princípios Gerais

1. **A especificação é a fonte da verdade:** Mudanças de comportamento do sistema começam na spec; o código é derivado dela. Correções e alterações nunca são feitas diretamente no código-fonte sem atualização da respectiva especificação.
2. **Separação estrita "O Quê" vs. "Como":** A spec descreve o **quê** e o **porquê** em linguagem de negócio (domínio clínico e administrativo da AMDE). O plano técnico (`plan.md`) descreve o **como** (decisões técnicas, bibliotecas, endpoints e algoritmos). Specs não mencionam tabelas, DDL nem tipos SQL.
3. **Nada de suposições silenciosas:** Quando a especificação for omissa, ambígua ou incompleta, a IA deve formular perguntas de esclarecimento antes de tomar decisões de projeto. Nenhuma escolha crítica (cardinalidade, exclusão, máscara e restrição) pode passar despercebida. Toda suposição mantida deve ser explicitamente registrada no plano técnico.
4. **Rastreabilidade total:** Cada entidade, atributo, regra de negócio, tela e tarefa de implementação deve ser diretamente rastreável a um requisito funcional (RF), requisito não funcional (RNF) e cenário BDD da spec.
5. **Consistência entre artefatos:** Modelo conceitual (`domain-model.md`), entidades, regras de negócio (RNs), telas e cenários de aceitação descrevem o mesmo domínio e não podem se contradizer. Nenhum artefato prevalece automaticamente sobre outro: divergências são apontadas pela IA como dúvida e resolvidas por decisão humana explícita (ver Seção 5.2).

---

## 2. Stack e Restrições Técnicas

- **Back-end:** Java 17 LTS com Spring Boot 3 (Spring Security 6, Spring Data JPA / Hibernate, MapStruct, Maven).
- **Front-end:** React 18+ (SPA) com TypeScript, Vite, Tailwind CSS, Shadcn/ui (Radix UI) e Axios.
- **Banco de Dados:** PostgreSQL 16 (SGBD Relacional ACID).
- **Controle de Evolução de Banco:** Liquibase com versionamento formal via changelogs em formato SQL (`-- Liquibase formatted SQL`) e suporte obrigatório a rollback.
- **Autenticação & Autorização:** Stateless via tokens JWT (`io.jsonwebtoken`), senhas protegidas via algoritmo BCrypt (RN01) e controle de acesso baseado em perfis (`ADMINISTRADOR`, `MEDICO`, `PACIENTE`).
- **Armazenamento de Arquivos e Infraestrutura:** Docker e Docker Compose para ambiente local; CI/CD via GitHub Actions; armazenamento de PDFs de prontuário em sistema de arquivos local (desenvolvimento) e AWS S3 (produção) sob o caminho padronizado `uploads/prontuarios/paciente-{id}/{uuid}.pdf`, com distribuição do front-end via AWS CloudFront.
- **Testes Automatizados:** JUnit 5, Mockito e Spring Boot Test no back-end; Vitest e Testing Library no front-end; todo requisito funcional deve possuir ao menos um cenário de teste de aceitação automatizado correspondente.

---

## 3. Princípios de Modelagem e Governança de Dados

### 3.1 Convenções
- **Nomenclatura no Banco:** Nomes de tabelas e colunas em português e em `snake_case`, respeitando os nomes consolidados no DER (`usuario`, `funcionarios`, `pacientes`, `categoria`, `registro_prontuario`, `agendamentos`, `agenda_paciente`, `log_auditoria`). Novas tabelas devem ser criadas no singular.
- **Chave Primária:** Toda tabela possui chave primária artificial sequencial (`SERIAL` / `INTEGER` no PostgreSQL, mapeada como `Long` no JPA) nomeada no formato `id_<entidade>` (ex.: `id_usuario`, `id_funcionario`, `id_paciente`, `id_categoria`, `id_prontuario`, `id_agenda`, `id_agenda_paciente`, `id_log`).
- **Chaves Estrangeiras:** Nomeadas no formato `id_<entidade>` referenciando a chave primária correspondente (ex.: `id_usuario`, `id_paciente`, `id_categoria`, `id_funcionario`, `id_agenda`) ou o papel semântico no auto-relacionamento (`id_registro_substituido`).
- **Auditoria Temporal:** Tabelas cadastrais com ciclo de atualização mantêm as colunas `criado_em` e `atualizado_em` (`TIMESTAMPTZ` / `TIMESTAMP WITH TIME ZONE`, padrão `CURRENT_TIMESTAMP`), enquanto tabelas de eventos e anexos registram o instante exato da ocorrência (`data_registro` em `registro_prontuario` e `data_hora` em `log_auditoria`, ambos `TIMESTAMPTZ`).
- **Anexos e Identificadores Físicos:** Arquivos de prontuário são restritos ao formato PDF com tamanho máximo de 10MB (RN05). O nome original do arquivo é salvo em `nome_arquivo_original` e o arquivo físico é renomeado com `UUID` único em `nome_arquivo_armazenado` (`UNIQUE`) (RN07).
- **Datas e Horários:** Datas puras (ex.: `data_nasc`) utilizam tipo `DATE` (`LocalDate` no Java); eventos com horário (ex.: `data_inicio` e `data_final` de agendamentos e carimbos de auditoria) utilizam `TIMESTAMP WITH TIME ZONE` (`TIMESTAMPTZ`).
- **Campos Booleanos:** Nomeados como afirmação clara com valor padrão definido (ex.: `ativo`, `presenca_funcionario`, `presenca_paciente`).

### 3.2 Integridade e Divisão de Responsabilidades
- **O Banco de Dados garante:** Chaves primárias e estrangeiras, unicidade de identificadores (`UNIQUE` em `usuario.email`, `categoria.nome`, `registro_prontuario.nome_arquivo_armazenado`, `funcionarios.id_usuario`, `pacientes.id_usuario` e chave composta `(id_agenda, id_paciente)` em `agenda_paciente`), obrigatoriedade (`NOT NULL`), integridade referencial e restrições de domínio via `CHECK` (incluindo `CHECK(data_inicio < data_final)` em `agendamentos`).
- **A Aplicação garante:** Regras dependentes de múltiplos estados, contexto temporal e perfil de acesso (ex.: bloqueio de sobreposição de horários da agenda do mesmo profissional com retorno `HTTP 409 Conflict` conforme RN13, validação de PDF < 10MB conforme RN05 e autorização RBAC conforme RN03, RN06 e RN15).
- **Relacionamentos N:N:** Sempre materializados por meio de tabela associativa explícita com atributos próprios do vínculo (ex.: `agenda_paciente` contendo `presenca_paciente`).
- **Listas de Valores Fixos:** Devem utilizar Enums tipados na aplicação (`@Enumerated(EnumType.STRING)`) e colunas `VARCHAR` com constraints `CHECK` no banco de dados (ex.: `nivel_acesso`, `grau_suporte`, `tipo_agendamento`, `status` do agendamento e `acao` de auditoria). Classificações estruturadas em entidade própria no modelo de domínio (como `categoria` de prontuário) são materializadas em tabela dedicada vinculada por chave estrangeira (`id_categoria`).

### 3.3 Exclusão, Ciclo de Vida e Histórico
- **Exclusão Lógica (*Soft Delete*):** Contas de usuários (`usuario`), pacientes e funcionários vinculados nunca sofrem exclusão física em produção; a inativação ocorre exclusivamente pela flag booleana `ativo = false`. Agendamentos cancelados preservam seu histórico alterando a coluna `status` para `Cancelado`, e registros em `log_auditoria` são estritamente imutáveis (*append-only*).
- **Ciclo de Vida de Arquivos de Prontuário:** O arquivamento lógico de documentos utiliza a flag `ativo = false` em `registro_prontuario` (e classificação na categoria `Arquivado`). Já a operação de exclusão de prontuário (UC12 / RN10), restrita a `ADMINISTRADOR` e `MEDICO`, exige o registro prévio em `log_auditoria` e a remoção do arquivo físico do storage antes da exclusão do metadado no banco.
- **Versionamento de Documentos:** A substituição de laudos, exames e receituários deve preservar o histórico clínico por meio do auto-relacionamento `id_registro_substituido` em `registro_prontuario` (RN08).

### 3.4 Proteção de Dados Sensíveis (LGPD e Resolução CFM nº 1.821/2007)
- **Dados Pessoais e Clínicos:** Nome, e-mail, telefone (`num_celular`), data de nascimento (`data_nasc`), endereço residencial, grau de suporte TEA (`grau_suporte`), histórico clínico (`desc_paciente`) e arquivos PDF de prontuários de crianças e adolescentes assistidos pela AMDE são dados pessoais e sensíveis protegidos pela LGPD.
- **Logs Seguros:** Dados pessoais sensíveis, senhas (mesmo em hash) e conteúdos de prontuários **nunca** aparecem em logs de console, arquivos de depuração ou mensagens técnicas de erro enviadas ao cliente.
- **Trilha de Auditoria Obrigatória:** Toda operação sensível (`LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO` e `AGENDAMENTO`) deve disparar gravação assíncrona obrigatória em `log_auditoria` via `AuditoriaService` (UC20 / RN16).

### 3.5 Evolução do Esquema (Migrations)
- **Imutabilidade em Produção:** Uma vez aplicada em ambiente compartilhado ou de produção, uma migration do Liquibase é imutável e torna-se histórico permanente. Correções são feitas exclusivamente por nova migration de compensação.
- **Migrations Incrementais:** Toda alteração no modelo de dados gera um novo changelog incremental no Liquibase contendo migração ascendente (`UP`) e procedimento de reversão seguro (`-- rollback`).
- **Veto a Operações Destrutivas Silenciosas:** A IA está proibida de gerar scripts que descartem tabelas (`DROP TABLE`), removam colunas (`DROP COLUMN`) ou convertam tipos com perda de dados sem alerta explícito e aprovação humana da equipe.

---

## 4. Princípios de Interface e Experiência do Usuário

### 4.1 Design System e Tokens
- **Biblioteca Base:** Componentes construídos com Tailwind CSS e Shadcn/ui (Radix UI), promovendo acessibilidade e padronização.
- **Design Tokens:** Cores institucionais da AMDE, cores semânticas de status/perfil, tipografia (`Inter` e `JetBrains Mono`), espaçamentos e raios de borda são formalizados em [`design/tokens.json`](../../design/tokens.json) e documentados em [`memory/design-system.md`](design-system.md).
- **Proibição de Valores Literais:** O código do front-end não deve declarar cores hexadecimais nem fontes soltas diretamente nos componentes, consumindo exclusivamente os tokens declarados.

### 4.2 Padrões de Interação e Usabilidade
- **Formulários:** Rótulos visíveis acima do campo, identificação clara de campos obrigatórios (`*`), validação ao desfocar o campo (*onBlur*) e no envio, bloqueando o botão de submissão enquanto houver erros e exibindo mensagens humanizadas junto ao componente.
- **Listagens e Tabelas:** Paginação na base da tabela (compatível com consultas paginadas da API), linha de busca textual no topo com filtro por nome e e-mail, e ordenação ascendente/descendente (`ASC`/`DESC`) visível nos cabeçalhos das colunas.
- **Ações Destrutivas:** Exclusão de usuários ou documentos clínicos requer confirmação explícita em modal de diálogo com fundo escurecido (*backdrop*) informando o impacto da ação.
- **Estados de Tela Obrigatórios:** Toda tela que consome APIs deve implementar 4 estados visuais bem definidos: *Carregando (Skeleton simulando a estrutura da tabela/card)*, *Vazio (Ícone ilustrativo e texto orientativo)*, *Erro (Alerta amigável com ação de recarregar)* e *Sucesso (Dados renderizados)*.

### 4.3 Acessibilidade e Responsividade
- **Conformidade WCAG:** Aderência às diretrizes WCAG 2.1 nível AA, com relação mínima de contraste de 4.5:1 para textos normais.
- **Navegação por Teclado:** Todas as ações devem ser acessíveis via teclado (`Tab`, `Enter`, `Esc` para fechar modais), com indicadores visuais de foco nítidos.
- **Responsividade:** Layout estruturado em Painel Administrativo com Menu Lateral Fixo (*Sidebar*) e Cabeçalho Superior (*Header*), adaptável com breakpoints Tailwind para desktops da clínica e dispositivos móveis dos profissionais.

### 4.4 Exibição de Dados Sensíveis
- Dados pessoais de contato e identificação (como `num_celular` e documentos) devem ser renderizados nas listagens com máscara protetora por padrão (ex.: `(15) *****-1234`), sendo revelados integralmente apenas na visualização detalhada mediante ação explícita de usuário com perfil autorizado (`ADMINISTRADOR` ou `MEDICO`).

---

## 5. Comportamento Esperado da IA

### 5.1 Ao ler uma Especificação (`spec.md`)
1. Mapear todas as entidades, atributos e fluxos de negócio mencionados, inclusive os presentes em wireframes e critérios BDD.
2. Checar a completude dos dados (obrigatoriedade, unicidade, cardinalidade, ciclo de vida e histórico).
3. Caso identifique omissões ou contradições, a IA **deve formular perguntas de esclarecimento**, sem tomar decisões arquiteturais por conta própria.

### 5.2 Verificação de Consistência entre Artefatos
Antes de gerar o plano técnico e sempre que uma especificação mudar, a IA deve checar:
- **Modelo Conceitual × Entidades:** Toda entidade do DER (`domain-model.md`) deve possuir correspondência nas especificações funcionais e vice-versa.
- **Entidades × Regras de Negócio:** Toda entidade citada em uma RN (regras RN01 a RN16) deve existir formalmente no modelo.
- **Telas × Entidades:** Todo campo exibido ou coletado na interface deve ter uma entidade de destino ou cálculo de origem declarado.
- **Cenários BDD × Requisitos:** Todo cenário de aceite deve testar ao menos um RF ou RN, e todo RF deve possuir ao menos um cenário de teste.
- **Linguagem Ubíqua:** Termos como "Usuário", "Funcionário / Profissional" (materializado na tabela `funcionarios` e autorizado sob o perfil `MEDICO`), "Paciente", "Grau de Suporte", "Prontuário Digital / PEP", "Categoria de Prontuário", "Agendamento", "Presença" e "Log de Auditoria" devem manter rigorosamente o mesmo significado em todos os documentos.

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
