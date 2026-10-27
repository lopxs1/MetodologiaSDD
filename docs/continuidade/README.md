# Continuidade entre semestres

Use esta pasta quando o projeto aproveitar uma aplicação, pesquisa, documentação, protótipo ou conjunto de dados produzido em semestre anterior. O objetivo é tornar explícita a linha de base recebida e as contribuições realizadas agora.

Se o projeto começou neste semestre, registre isso no README principal e não preencha uma continuidade fictícia. Se a versão anterior não estiver acessível, indique a limitação e quais fontes foram usadas para reconstituir o estado inicial.

## Registrar a linha de base

1. Identifique a origem do material recebido: repositório, tag/commit, arquivo, versão, data ou responsável institucional. Não inclua credenciais ou dados pessoais.
2. Descreva as funcionalidades, artefatos e limitações que já existiam no início do semestre. Diferencie implementado de validado.
3. Compare com o estado atual e classifique cada item como herdado e mantido, herdado e alterado, novo neste semestre, pendente/não concluído ou removido por decisão registrada.
4. Para cada alteração, associe evidências verificáveis: caminho de arquivo, tela, teste, documento, decisão, commit/PR ou validação com cliente.
5. Registre a contribuição dos integrantes, incluindo atividades de análise, desenho, programação, teste, documentação e coordenação. Em trabalho conjunto, identifique a colaboração em vez de atribuir autoria exclusiva sem evidência.

Copie [`linha-de-base-e-evolucao-template.md`](linha-de-base-e-evolucao-template.md) para cada etapa ou semestre relevante. Use [`contribuicoes.csv`](contribuicoes.csv) como índice opcional de contribuições individuais.

Os arquivos de exemplo abaixo usam nomes, IDs, commits e resultados inventados. Remova ou substitua as linhas de demonstração antes de registrar o trabalho real.

## Cuidados de interpretação

- Um commit mostra uma alteração associada a uma identidade e data; sozinho, não demonstra todo o trabalho ou autoria individual.
- Não atribua ao semestre atual funcionalidades que já estavam presentes na versão recebida.
- Não trate “existia no código” como “funcionava” ou “foi validado”. Registre a evidência de execução/validação separadamente.
- Preserve o histórico anterior e explique mudanças importantes de arquitetura ou escopo.
- Proteja dados do cliente, credenciais e dados pessoais nas evidências.

## Exemplo resumido (fictício)

Um grupo recebe a versão `v0.1` de um sistema escolar com cadastro de materiais. No início do semestre, o cadastro existe, mas o fluxo de pedidos ainda é um protótipo. Neste semestre, a equipe mantém o cadastro, implementa o registro de pedidos e documenta testes em ambiente local. O relatório deve apontar quais arquivos e testes comprovam cada afirmação, sem atribuir à equipe atual o cadastro herdado.
