# Product Backlog do projeto

> **Modelo adaptável:** use este arquivo quando a equipe estiver construindo ou evoluindo um produto e uma lista priorizada ajudar a organizar o trabalho. O backlog não é obrigatório para todo tipo de projeto. Pesquisa, análise de dados, automação, integração ou migração podem usar um plano de trabalho ou outro instrumento mais adequado.
>
> A linha preenchida abaixo é **um exemplo fictício**. Remova-a ou substitua-a antes de registrar o backlog real. Não copie necessidade, regra ou critério como se tivesse sido aprovado pelo cliente.

## Objetivo e escopo do backlog

- **Produto/projeto:** Projeto Exemplo
- **Objetivo do produto:** [resultado que o projeto pretende entregar]
- **Responsável por manter a lista:** [papel ou equipe]
- **Última revisão:** AAAA-MM-DD
- **Critério de priorização:** [explique resumidamente como a equipe considera valor, urgência, risco ou dependências]

**Exemplo fictício:** “Organizar as necessidades da aplicação escolar de consulta a empréstimos, considerando valor para os funcionários, dependências e prazo do semestre.”

## Itens priorizados

Mantenha os itens ordenados da maior para a menor prioridade atual. Reordene quando houver nova informação ou decisão. Use identificadores estáveis e associe RF/UC quando existirem. Um item pode ser funcionalidade, requisito de qualidade, pesquisa, dívida técnica, defeito ou tarefa. Não force todos os itens a gerar tela, endpoint, classe ou tabela.

| ID | Tipo | Item / necessidade | Valor ou motivo | Prioridade | Estado | Critério de aceite / conclusão | Origem e vínculo | Dependências |
|---|---|---|---|---|---|---|---|---|
| PB-EX-01 | Funcionalidade | Exemplo: consultar empréstimos por equipamento | Exemplo: ajudar a equipe a verificar a situação do equipamento | Alta — apenas ilustrativa | A validar | Com dados fictícios preparados, a pessoa consegue localizar um equipamento e visualizar os empréstimos associados; registrar o resultado do cenário de teste | Necessidade hipotética; relacionar a RF/UC reais se aprovados | Fonte de dados de empréstimos |

## Estados sugeridos

Adapte os estados ao processo da equipe. Uma opção simples é: `Proposto` → `A validar` → `Pronto` → `Em andamento` → `Concluído` → `Validado`.

- **Concluído:** o trabalho definido no item foi implementado ou produzido.
- **Validado:** há evidência de que o critério foi verificado, como teste, análise, demonstração ou aceite registrado.
- **Bloqueado:** existe uma dependência ou decisão que impede o avanço; registre qual é.

Não marque como validado algo apenas porque foi planejado, codificado ou demonstrado sem evidência adequada.

## Refinamento e histórico

Divida itens grandes quando isso ajudar a estimar, implementar ou validar o trabalho. Registre dúvidas como pendências, em vez de inventar regras. Decisões que alterem escopo ou prioridade podem ser documentadas em [`docs/decisoes/`](docs/decisoes/README.md).

| Data | Item alterado | Alteração/decisão | Motivo ou origem |
|---|---|---|---|
| AAAA-MM-DD | PB-EX-01 | Exemplo fictício: prioridade ajustada após conversa com o cliente | Registrar referência à decisão ou evidência real |

## Quando o backlog não for adequado

Se o projeto não for desenvolvido como produto iterativo, substitua este arquivo por um instrumento coerente com o trabalho, por exemplo:

- plano de pesquisa e perguntas investigadas;
- catálogo de fontes, etapas e validações de uma análise de dados;
- inventário e plano de migração de um sistema legado;
- plano de integração, experimentos ou entregas de automação.

Explique a escolha no README principal e mantenha os itens rastreáveis às evidências e objetivos do projeto.
