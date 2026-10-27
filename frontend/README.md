# Frontend

Use este guia somente para a interface web. Para aplicativo Android/iOS, use `mobile/README.md`. Preencha os passos reais para configurar, executar e validar este componente; não declare comandos que ainda não foram testados pela equipe.

## Responsabilidade e limites

- **Usuários atendidos:** [perfis]
- **Fluxos cobertos:** [casos de uso/requisitos]
- **Estado atual:** [protótipo / em andamento / disponível / não aplicável]

## Pré-requisitos

| Dependência | Versão suportada | Como verificar |
|---|---|---|
| Runtime | [ex.: Node.js] | [comando] |
| Gerenciador de pacotes | [ex.: npm/pnpm] | [comando] |

## Configuração, instalação e execução

Liste variáveis públicas necessárias para a interface. Não coloque segredos no frontend: qualquer variável compilada para o navegador pode ser visualizada pelo usuário.

```text
Instalação: [comando real]
Execução local: [comando real]
Build: [comando real]
Testes: [comando real]


Informe a URL local, como configurar a URL da API e quais serviços precisam estar ativos. Use contas e dados fictícios.

## Validação da interface

Registre os fluxos e estados cobertos: carregando, vazio, sucesso, erro, permissão negada e indisponibilidade do backend. Inclua observações sobre teclado, foco, contraste, responsividade e acessibilidade conforme aplicável.

## Relação com a documentação

- **Documentação acadêmica:** consulte [`docs/README.md`](../docs/README.md).
- **Protótipos web:** [`docs/prototipos/`](../docs/prototipos/README.md), quando houver.
- **Casos de uso:** [`docs/casos-de-uso/`](../docs/casos-de-uso/README.md), quando forem adequados aos fluxos do projeto.
- **Plano e evidências de testes:** [`docs/testes/`](../docs/testes/README.md), quando aplicável.

## Exemplo preenchido (fictício; stack ilustrativa)

> Exemplo para uma interface React/Vite. Use somente se as ferramentas e scripts existirem no projeto da equipe.

- **Usuários atendidos:** funcionários que registram pedidos de materiais.
- **Fluxos cobertos:** consultar catálogo e enviar pedido.
- **Estado atual:** protótipo conectado a uma API local fictícia.
- **Pré-requisitos:** Node.js 22 e npm 10 (versões ilustrativas).

Exemplos de comandos, somente se definidos no `package.json`:

```text
npm ci
npm run dev
npm run build
npm tes


Exemplo de variável pública para endereço local da API: `VITE_API_BASE_URL=http://localhost:8000`. Não coloque tokens ou senhas nesse tipo de variável: o valor pode ser incluído no código entregue ao navegador.

Exemplo de evidência: “No cenário de catálogo vazio, a interface exibe orientação para tentar novamente; conferido manualmente em [commit fictício] com dados sintéticos.” Não use esta frase como teste realizado pela sua equipe.
