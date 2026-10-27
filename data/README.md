# Dados, pesquisa e pipelines (opcional)

Use esta pasta quando o projeto manipular conjuntos de dados, consultas, ETL, análises, modelos ou integrações orientadas a dados. Ela é opcional; não copie dados reais ou arquivos de origem protegidos para o repositório.

## Fontes e proveniência

| Conjunto/fonte | Responsável ou URL pública | Período/cobertura | Licença ou condição de uso | Estado da cópia |
|---|---|---|---|---|
| [nome da fonte] | [origem] | [período e cobertura] | [licença/condição] | [não obtida / amostra / cópia local] |

Registre data de acesso, versão, hash ou outro identificador quando útil. Se a fonte exigir permissão, descreva o processo sem guardar credenciais nem dados pessoais.

## Organização e reprodução

- **Dados fictícios ou amostras:** descreva sua origem e como gerá-los.
- **Dados excluídos do Git:** indique o caminho ignorado e como obtê-los com autorização.
- **Transformações:** documente entrada, saída, filtros, unidades, chaves e perdas conhecidas.
- **Execução:** informe pré-requisitos e comandos realmente testados; marque como pendente o que ainda não foi executado.
- **Resultados:** identifique período, versão do código/dados e método usado para produzir tabelas, gráficos ou métricas.

Não versionar bases completas de clientes, dados pessoais, prontuários, credenciais ou conteúdo sob licença incompatível. Prefira amostras sintéticas e explique como reproduzir a análise sem expor dados protegidos.

## Relação com o restante do projeto

Relacione fontes e transformações aos requisitos, perguntas de pesquisa, serviços, interfaces e testes apenas quando a relação existir. Consulte [`docs/der/`](../docs/der/README.md), [`docs/requisitos/`](../docs/requisitos/README.md) e [`docs/testes/`](../docs/testes/).

## Exemplo preenchido (fictício)

Uma equipe analisa dados públicos de equipamentos escolares. A fonte fictícia `inventario-escolar.csv` cobre três unidades em 2025 e é publicada com licença aberta. Um script remove linhas sem identificador e grava uma tabela agregada por unidade; a amostra de exemplo contém dados inventados. A reprodução ainda está pendente até a equipe registrar e executar o comando real. Nenhum dado de aluno ou funcionário é incluído.
