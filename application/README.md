# Aplicação full stack

Use esta pasta quando a aplicação web e o backend forem desenvolvidos e mantidos como uma unidade integrada. Ela pode conter subpastas para interface e servidor, por exemplo. Explique a estrutura real abaixo. Se backend e frontend forem componentes separados, documente-os em `backend/` e `frontend/` na raiz. Um app móvel com código e build próprios fica em `mobile/`.

## Estrutura e responsabilidades

| Caminho | Responsabilidade | Tecnologia/versão |
|---|---|---|
| `frontend/` ou caminho real | Exemplo: interface web da aplicação | A preencher |
| `backend/` ou caminho real | Exemplo: regras do servidor e acesso a dados | A preencher |
| `mobile/` na raiz, se existir | Aplicativo móvel separado, que pode consumir os serviços desta aplicação | A preencher |

**Exemplo fictício:** `application/web/` contém a interface React e `application/api/` contém o serviço Node.js. O app Android fica em `mobile/` porque possui ciclo de build separado. Substitua essa organização pelo código efetivo do projeto.

## Pré-requisitos

Liste linguagens, runtimes, ferramentas, serviços, versões suportadas e como verificar cada uma. Diferencie o que é necessário para desenvolver do que é necessário para executar a aplicação.

| Ferramenta ou serviço | Versão utilizada | Como verificar |
|---|---|---|
| Exemplo: runtime da aplicação | A preencher pela equipe | Comando real do projeto |
| Exemplo: banco/serviço externo | A preencher ou não aplicável | Procedimento real |

## Configuração segura

Documente variáveis necessárias, seus propósitos e valores fictícios em arquivo `.env.example`. Mantenha `.env` local fora do Git. Não inclua credenciais, chaves, tokens ou dados reais do cliente.

## Instalação, execução e validação

Registre comandos testados para instalar dependências, preparar dados, iniciar a aplicação, executar verificações e encerrar processos. Informe se a execução exige iniciar mais de um serviço.

Exemplo fictício de comandos; use somente quando existirem scripts equivalentes no projeto:

    npm install
    npm run dev
    npm test

## Configuração integrada (se aplicável)

Se houver configuração para iniciar múltiplos componentes juntos, como Docker Compose, documente-a aqui. Não é necessário criar uma camada de orquestração para uma aplicação simples.

**Exemplo fictício:** quando houver um `compose.yaml` configurado e testado:

    docker compose up --build
    docker compose down

## Relação com outras pastas

- **App móvel independente:** [`mobile/README.md`](../mobile/README.md), se existir.
- **Dados, análises ou pipelines:** [`data/README.md`](../data/README.md), se aplicável.
- **Documentação acadêmica e artefatos:** [`docs/README.md`](../docs/README.md).
- **Backlog do produto:** [`PRODUCT-BACKLOG.md`](../PRODUCT-BACKLOG.md), se adotado.

## Exemplo de estado (fictício)

“A aplicação full stack é executada localmente com dados sintéticos. Os testes do fluxo de consulta foram planejados, mas ainda não executados. A equipe atualizará este trecho quando houver evidência reproduzível.” Não use o exemplo como comprovação do estado real.
