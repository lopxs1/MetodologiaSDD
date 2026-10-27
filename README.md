# Repositório do Projeto Integrador / Trabalho de Graduação — ADS

Este repositório é um ponto de partida para projetos de software, aplicativos móveis, integrações, automações, análise de dados e soluções que combinem essas frentes. Adapte a estrutura ao projeto: as pastas são organizacionais e não comprovam que algo foi implementado.

O documento acadêmico deve seguir o [template institucional](docs/README.md) e as orientações da disciplina. Este README apresenta o projeto e explica a organização do código e dos documentos.

## Identificação

- **Projeto:** Nome do projeto
- **Equipe:** identificação conforme orientação da disciplina
- **Orientação:** preencher quando aplicável
- **Cliente ou parceiro:** nome público ou descrição geral, sem dados confidenciais
- **Disciplina/etapa:** PI, TG I, TG II ou outra etapa
- **Versão analisada:** tag, release ou commit, quando houver
- **Atualizado em:** AAAA-MM-DD

## Resumo do projeto

Descreva o contexto, o problema, quem é afetado, o objetivo e o que já foi produzido. Separe resultados observados de resultados esperados.

**Exemplo fictício:** Uma escola registra empréstimos de equipamentos em planilhas separadas, o que dificulta consultar a disponibilidade. A equipe está desenvolvendo uma aplicação web e um aplicativo móvel para registrar solicitações. Nesta versão, o fluxo de solicitação foi implementado em ambiente local e ainda não foi validado pela escola.

## Problema, objetivos e avaliação

- **Problema ou necessidade:** necessidade documentada, sem expor dados confidenciais.
- **Objetivo:** resultado que o projeto pretende produzir.
- **Pergunta ou hipótese:** preencher quando a natureza da pesquisa e a orientação acadêmica pedirem.
- **Como será avaliado:** testes, análise, demonstração, avaliação com participantes ou validação pelo cliente, conforme o método adotado. Registre resultados somente depois de obtê-los.

**Exemplo fictício:** Problema: a equipe escolar não consegue consultar facilmente quais equipamentos estão emprestados. Objetivo: construir um fluxo para registrar e consultar empréstimos. Avaliação prevista: executar cenários com dados sintéticos e, após autorização, discutir o fluxo com representantes da escola.

## Escopo e situação desta versão

### Incluído ou produzido

Descreva funcionalidades, análises, integrações ou artefatos concluídos e em andamento. Identifique o estado de cada item.

### Fora do escopo ou ainda não iniciado

Registre exclusões, itens adiados e dependências conhecidas. Não trate uma ideia como decisão aprovada.

### Limitações, premissas e riscos

Explique a limitação, seu impacto e a ação ou decisão pendente.

**Exemplo fictício:** A interface web e o aplicativo móvel consultam a mesma API em ambiente local. Notificações push e publicação nas lojas não fazem parte desta versão. A avaliação com usuários depende de autorização da escola.

Registre mudanças relevantes em [decisões](docs/decisoes/README.md). Em projetos continuados, compare a entrega recebida com o trabalho desta etapa em [continuidade](docs/continuidade/README.md).

## Organização do código

Escolha a estrutura que representa como o projeto é desenvolvido e executado:

- **Aplicação full stack:** use `application/` quando frontend e backend compõem uma aplicação integrada e são mantidos juntos. Descreva no README dessa pasta a estrutura interna, os pré-requisitos e como iniciar e validar cada parte.
- **Componentes separados:** use `backend/` para API/serviços, `frontend/` para interface web e `mobile/` para aplicativo móvel, conforme existirem. Cada pasta documenta instalação, configuração e execução do seu componente.
- **Aplicativo móvel com backend próprio:** mantenha `mobile/` e `backend/` separados quando forem componentes independentes, mesmo que sejam entregues juntos.
- **Outras soluções:** projetos de dados, automação, pesquisa ou integração podem usar `data/` ou outra estrutura adequada. Explique a organização neste README.

Não mantenha simultaneamente `application/` e pastas separadas para representar o mesmo código sem explicar a relação entre elas. Se o aplicativo móvel tiver código e ciclo de build próprios, mantenha-o em `mobile/`, mesmo quando existir uma aplicação full stack.

| Pasta/arquivo | Propósito resumido | Quando usar |
|---|---|---|
| `application/` | Código e instruções de uma aplicação full stack mantida como unidade integrada | Quando frontend e backend fazem parte da mesma aplicação e organização de código |
| `backend/` | API, serviços e lógica de servidor | Quando o backend é mantido como componente separado |
| `frontend/` | Interface web | Quando existe uma aplicação web separada |
| `mobile/` | Aplicativo Android, iOS ou multiplataforma | Quando o projeto inclui app móvel com código próprio |
| `data/` | Materiais de dados do projeto: amostras sintéticas ou públicas, consultas, scripts e pipelines | Quando análise ou processamento de dados fizer parte do projeto. Registre origem e preparo; não versione dados pessoais, confidenciais ou sem licença adequada. |
| [`.specify/`](.specify/README.md) | Estrutura da metodologia SDD (Spec-Driven Development) | Mantém a memória global (`memory/`) e pacotes de especificações incrementais (`specs/`) com requisitos, planos técnicos, delta de dados, contratos e tarefas. |
| `docs/` | Documento acadêmico e documentação de apoio | Consulte [`docs/README.md`](docs/README.md) para a função de cada subpasta: requisitos, casos de uso, DER, arquitetura, protótipos, testes, decisões, continuidade e planejamento. |
| [`PRODUCT-BACKLOG.md`](PRODUCT-BACKLOG.md) | Lista priorizada de necessidades e trabalho | Opcional; use se o projeto mantiver um backlog de produto. Para outros métodos, adapte ou remova. |

### Exemplos de estruturas

Aplicação full stack integrada:

    application/
      README.md
      frontend/
      backend/
    mobile/             # somente se houver app móvel com código próprio
    docs/

Componentes separados:

    backend/
    frontend/
    mobile/             # somente se houver app móvel
    data/               # somente se aplicável
    docs/

Esses exemplos são ilustrativos. Não crie pastas vazias para preencher o modelo.

## Componentes e evidências

Registre o estado atual e a localização real da evidência. Remova as linhas que não se aplicam.

| Componente ou frente | Estado | Evidência e localização |
|---|---|---|
| Aplicação full stack ou componentes separados | Não iniciado / em andamento / concluído / N/A | `application/` ou `backend/` e `frontend/` |
| Aplicativo móvel | Não iniciado / em andamento / concluído / N/A | `mobile/` ou caminho real |
| Dados, integração ou automação | Não iniciado / em andamento / concluído / N/A | `data/` ou caminho real |
| Requisitos, backlog e modelos | Não iniciado / em andamento / concluído / N/A | `docs/` e `PRODUCT-BACKLOG.md` |
| Testes e validação | Não iniciado / em andamento / concluído / N/A | `docs/testes/` ou caminho real |
| Documento acadêmico | Não iniciado / em andamento / concluído / N/A | `docs/` e versão/data |

## Rastreabilidade do projeto

Relacione necessidades e objetivos a requisitos ou itens de trabalho, modelos, implementação e validação quando essas relações existirem. Não exija correspondência um-para-um.

| Necessidade/objetivo | Requisito ou item de trabalho | Caso de uso/modelo/protótipo | Implementação ou análise | Validação/evidência | Estado |
|---|---|---|---|---|---|
| Exemplo fictício: consultar equipamentos | RF-01, se aprovado | UC-01 e protótipo da consulta | Aplicação web e app móvel | Cenário com dados sintéticos; resultado registrado no roteiro | Em andamento |

## Continuidade entre semestres

Se o projeto recebeu código, pesquisa, protótipos, documentos ou dados de uma etapa anterior, registre a linha de base e diferencie o que foi herdado, alterado, criado, validado ou descontinuado. Se começou nesta etapa, registre que não há linha de base anterior. Consulte [continuidade](docs/continuidade/README.md).

## Como começar

1. Leia [`docs/README.md`](docs/README.md) e mantenha somente os artefatos pertinentes.
2. Escolha a organização do código: `application/` para full stack integrado ou `backend/`, `frontend/` e `mobile/` para componentes separados, conforme aplicável.
3. Consulte o README da pasta escolhida e registre somente comandos e pré-requisitos reais.
4. Use dados fictícios em desenvolvimento e testes. Nunca versione senhas, tokens, chaves privadas ou dados pessoais/reais do cliente.
5. Use e atualize [`PRODUCT-BACKLOG.md`](PRODUCT-BACKLOG.md) se o backlog de produto for adequado ao projeto.

## Exemplo preenchido (fictício)

O exemplo da escola nesta página é inventado e serve apenas para demonstrar como descrever problema, escopo, componentes e evidências. Substitua-o pelos fatos confirmados do projeto; não o apresente como requisito ou validação real.
