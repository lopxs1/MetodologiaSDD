# Aplicativo móvel

Use esta pasta para o código-fonte do aplicativo Android, iOS ou multiplataforma. Ela é opcional: mantenha-a quando o projeto tiver um cliente móvel. Um aplicativo pode consumir uma API em `backend/`, acessar serviços externos documentados ou operar localmente, conforme a arquitetura definida pela equipe.

## Responsabilidade e limites

- **Plataformas:** [Android / iOS / multiplataforma]
- **Tecnologia e versão:** [tecnologia e versão suportada]
- **Fluxos atendidos:** [requisitos ou casos de uso relacionados]
- **Comunicação com serviços:** [API/serviço e ambiente, sem segredos; ou não se aplica]
- **Estado:** [protótipo / em desenvolvimento / disponível / não aplicável]

## Pré-requisitos

| Ferramenta ou serviço | Versão suportada pela equipe | Como verificar |
|---|---|---|
| SDK/runtime | [versão] | [comando] |
| IDE ou ferramentas de build | [versão, se necessária] | [comando ou local] |
| Emulador ou dispositivo | [modelo/versão do SO usados na validação] | [como identificar] |
| Backend ou serviço externo | [versão/URL de desenvolvimento ou N/A] | [verificação] |

Registre diferenças relevantes entre execução em emulador e dispositivo. Liste os requisitos de assinatura/build apenas quando forem usados; não inclua chaves ou certificados privados.

## Configuração, instalação e execução

Descreva as variáveis de configuração e mantenha exemplos sem credenciais reais. Não embuta tokens, chaves privadas ou senhas no aplicativo: valores distribuídos no pacote podem ser extraídos.

```text
Instalação de dependências: [comando realmente usado]
Execução em emulador/dispositivo: [comando realmente usado]
Build local: [comando realmente usado]
Testes: [comando realmente usado ou N/A com justificativa]


Informe como iniciar os serviços necessários, a URL de desenvolvimento, como carregar dados fictícios e como encerrar a execução. Registre a última execução verificada com data, commit, plataforma/dispositivo e resultado. Se os comandos ainda não foram executados, identifique-os como instruções pendentes.

## Comportamentos específicos do dispositivo

Documente somente os itens que o aplicativo realmente usa:

- permissões solicitadas e motivo;
- notificações, câmera, localização, arquivos, sensores ou outros recursos;
- armazenamento local, sincronização e comportamento offline;
- links profundos, abertura de arquivos e integração com outros aplicativos;
- diferenças de navegação, acessibilidade e layout entre plataformas.

Explique como o usuário pode revogar permissões e o que acontece quando um recurso ou serviço não está disponível. Não solicite permissões sem uma necessidade do produto documentada.

## Validação

Liste cenários executados separadamente por plataforma, emulador ou dispositivo. Registre versão do app, pré-condições, resultado esperado, resultado observado e evidência. Não descreva um teste planejado como se já tivesse sido executado.

## Relação com outros componentes

- **Backend/API:** [`backend/README.md`](../backend/README.md), se existir.
- **Interface web:** [`frontend/README.md`](../frontend/README.md), se existir.
- **Execução integrada/implantação:** [`application/README.md`](../application/README.md), se existir.
- **Requisitos, protótipos e testes:** [`docs/README.md`](../docs/README.md).

## Exemplo preenchido (fictício; stack ilustrativa)

> Exemplo de app Flutter de uma escola para consultar equipamentos emprestados. Substitua tecnologia, comandos e resultados pelos que a equipe realmente usa. O exemplo não representa execução validada.

- **Plataformas:** Android; iOS ainda não incluído nesta versão fictícia.
- **Tecnologia:** Flutter, versão definida no ambiente da equipe.
- **Fluxos atendidos:** consultar equipamentos e visualizar o estado de uma solicitação.
- **Comunicação:** API local de desenvolvimento; dados sintéticos.
- **Estado:** em desenvolvimento; sem publicação em loja.

```text
flutter pub ge
flutter run
flutter tes


Antes de registrar esses comandos como evidência real, confirme que existem no projeto, execute-os no ambiente descrito e informe a plataforma e o resultado observado.
