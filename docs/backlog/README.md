# Backlog do projeto (opcional e adaptável)

## Quando usar

Um **Product Backlog** é adequado quando a equipe está construindo ou evoluindo um produto de forma iterativa e precisa manter uma lista ordenada de necessidades, melhorias e trabalho técnico. É uma opção útil para projetos de software, mas não é obrigatório e só deve ser chamado de Product Backlog se a equipe realmente usar esse conceito/processo.

Adapte ou escolha outro instrumento quando o projeto for principalmente pesquisa, análise de dados, integração, automação, migração ou estudo de sistema legado. Exemplos: plano de pesquisa e perguntas; catálogo de fontes e pipeline; contratos de integração; inventário de componentes e plano de migração. A escolha deve ajudar a controlar o trabalho, não criar burocracia.

## Como registrar itens

O arquivo [`PRODUCT-BACKLOG.md`](../../PRODUCT-BACKLOG.md) na raiz é o modelo principal em Markdown. Se a equipe preferir uma planilha, pode usar `product-backlog.csv` como alternativa; mantenha uma única fonte principal para evitar divergências. Cada item deve ter identificador estável, descrição curta, prioridade, estado, critério de aceite e origem/justificativa. Reordene a lista conforme as decisões da equipe e do cliente.

Um item pode representar requisito, história, defeito, pesquisa técnica ou tarefa. Nem todo item gera endpoint, tabela, classe ou tela. Quando um item não se aplica ao produto atual, registre a decisão e o motivo.

## Estados sugeridos

`Proposto` → `A validar` → `Pronto para desenvolvimento` → `Em andamento` → `Concluído` → `Validado`.

Adapte os estados à forma de trabalho da equipe. `Concluído` significa que o trabalho foi implementado conforme a equipe definiu; `Validado` indica que existe evidência de teste, demonstração ou aceite. Não use `Validado` sem evidência.

## Critério de aceite

Prefira condições observáveis e verificáveis. Evite descrições vagas como “funciona bem” ou “tela intuitiva”. Quando uma decisão depende do cliente, use `[a confirmar]` até haver registro da validação.

## Exemplo de item (fictício)

- **ID:** PB-EX-01
- **Tipo:** funcionalidade
- **Título:** Registrar pedido de material
- **Descrição/necessidade:** funcionário autorizado precisa solicitar itens para seu setor.
- **Prioridade/estado:** Alta / A validar (rótulos apenas ilustrativos; a equipe define os critérios).
- **Critério de aceite:** dado um usuário autorizado e um material ativo, quando ele informa quantidade positiva e confirma, o pedido fica registrado com identificador e estado inicial visíveis.
- **Origem:** necessidade hipotética da Escola Exemplo.
- **Dependência:** cadastro de materiais.

O item é um exemplo para demonstrar o nível de detalhe. Remova-o ou substitua-o ao iniciar o backlog real.
