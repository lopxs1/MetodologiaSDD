# Casos de uso (quando aplicáveis)

Use casos de uso para descrever objetivos de atores que interagem com o sistema. Eles podem apoiar projetos web, mobile, desktop ou sistemas integrados. Para pesquisa sem interação com um sistema, pipeline automático ou serviço sem ator humano, outra forma de especificação pode ser mais adequada.

## Visão geral e especificações

- O diagrama conceitual mostra atores, fronteira do sistema e objetivos; não representa telas, tabelas ou passos internos de algoritmo.
- A especificação textual mantém os mesmos identificadores e nomes do diagrama e se relaciona aos requisitos pertinentes.
- Descreva a ação do ator e a resposta do sistema em cada passo. Acrescente alternativas, exceções ou regras somente quando estiverem documentadas no requisito ou confirmadas pela equipe/cliente.
- Se houver diferenças entre o fluxo web e o mobile, registre-as na especificação ou em complemento identificado por plataforma.
- Não é necessário detalhar todos os casos em baixo nível se isso não ajudar a especificar ou validar o projeto.

## Modelos disponíveis

- [`diagrama-casos-de-uso-template.md`](diagrama-casos-de-uso-template.md): visão conceitual e relação com requisitos.
- [`caso-de-uso-detalhado-template.md`](caso-de-uso-detalhado-template.md): tabela textual de fluxo, quando necessária.

Inclua no documento acadêmico somente os diagramas e descrições pertinentes, nos locais e formatos definidos pelo template institucional e pela orientação da disciplina. Os modelos desta pasta não determinam capítulo ou apêndice obrigatório.

**Exemplo fictício:** no sistema escolar ilustrativo, `UC-01 Consultar empréstimos` descreve como um funcionário solicita a consulta e como o sistema apresenta os registros. Se as interfaces web e mobile tiverem respostas ou etapas diferentes, a especificação identifica cada variação. Não se presume filtro por usuário sem requisito que o determine.
