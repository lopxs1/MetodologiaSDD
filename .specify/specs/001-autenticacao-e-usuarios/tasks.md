# Checklist de Tarefas: Autenticação, Controle de Acesso e Gestão de Usuários

> **ID da Spec:** `SPEC-001`  
> **Status Geral:** `0 / 26 Concluídas`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos
- [ ] **T01.1:** Revisar e aprovar requisitos (`RF-01`, `RF-02`), RNFs, regras (`RN01`–`RN04`, `RN16`, `RN17`–`RN28`) e os 18 cenários BDD em [`spec.md`](spec.md).
- [ ] **T01.2:** Validar o contrato OpenAPI 3.0.3 em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) para os 6 endpoints (`POST /api/auth/login`, `POST /api/usuarios/registro`, `GET /api/usuarios`, `GET /api/usuarios/{id}`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}`).
- [ ] **T01.3:** Submeter o pacote `001-autenticacao-e-usuarios` para validação da equipe e do orientador antes de iniciar o código.

---

## 💾 Fase 2: Banco de Dados, Verificação e Seeds Sintéticos
- [ ] **T02.1:** Confirmar o bootstrap local (`docker compose up -d postgres`) e a aplicação da migration inicial [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) pela role `amde_digital_owner`.
- [ ] **T02.2:** Validar os scripts SQL executando `sqlfluff lint --dialect postgres backend/src/main/resources/db/changelog/migrations/ data/verificacao.sql`.
- [ ] **T02.3:** Executar [`data/verificacao.sql`](../../../data/verificacao.sql) garantindo **zero linhas** nas 9 verificações estruturais e revisar o inventário `[LGPD: pessoal]` / `[LGPD: sensível]` das tabelas `usuario`, `funcionario` e `paciente`.
- [ ] **T02.4:** Criar arquivo de seed sintético local (`data/seeds/001_usuarios_seed_sintetico.sql`) com usuários fictícios (`ADMINISTRADOR`, `MEDICO` e `PACIENTE`) e procedimento seguro de provisionamento do primeiro administrador sem versionar senhas ou dados reais.

---

## ⚙️ Fase 3: Backend e Regras de Negócio (Spring Boot 3 / Java 17)
- [ ] **T03.1:** Estruturar o projeto Maven em `backend/` com Spring Boot 3, Spring Security 6, Spring Data JPA, MapStruct, Liquibase e JJWT (`io.jsonwebtoken`), configurado com `amde_digital_app` (runtime) e `amde_digital_owner` (Liquibase) e `ddl-auto=validate`.
- [ ] **T03.2:** Implementar os Enums (`NivelAcesso`, `GrauSuporte`) e as `@Entity` (`Usuario`, `Funcionario`, `Paciente`) aderentes a [`AGENTS.md`](../../../AGENTS.md) (`schema = "amde_digital"`, `columnDefinition = "text"`, `OffsetDateTime`, `LocalDate`, `@SQLRestriction("excluido_em IS NULL")`).
- [ ] **T03.3:** Implementar os repositórios Spring Data JPA (`UsuarioRepository`, `FuncionarioRepository`, `PacienteRepository`) com consulta case-insensitive de e-mail ativo (`uq_usuario_email_ativo`), busca paginada com `unaccent` sobre nome/e-mail, filtro por `nivel_acesso` e consulta de totalizadores agregados por perfil.
- [ ] **T03.4:** Implementar `JwtService` (assinatura HS256, claims mínimas sem dados sensíveis, expiração exata de 8h sem renovação — `RN20`) e `JwtAuthenticationFilter` (validação do token + verificação em banco de usuário ativo `excluido_em IS NULL` a cada requisição — `RN18`).
- [ ] **T03.5:** Configurar `SecurityConfig` com RBAC por perfil (`RN17`), `BCryptPasswordEncoder` (`RN01`), `AuthenticationEntryPoint` (`401` uniforme — `RN19`, `RNF-01.3`) e `AccessDeniedHandler` (`403`).
- [ ] **T03.6:** Implementar `AuthController` e `AuthService` (`POST /api/auth/login`) com normalização de e-mail, rejeição genérica de falhas e disparo assíncrono do evento `LOGIN` em `AuditoriaService` sem bloquear acesso em falha de gravação (`RN16`, `RN44`).
- [ ] **T03.7:** Implementar DTOs de entrada/saída, `UsuarioMapper` (MapStruct) sem expor `senha` e `CelularMasker` com mascaramento padrão e revelação condicional no detalhe (`RN28`).
- [ ] **T03.8:** Implementar `UsuarioService.cadastrar` (`UC04`) e `UsuarioService.atualizar` (`UC06`) com validação de senha (`>= 8` chars, não só espaços, `<= 72` bytes UTF-8 — `RN21`), limites e opcionalidade de campos (`RN22`), criação/edição transacional da especialização (`RN04`, `RN23`), bloqueio de troca de perfil (`RN24`) e tratamento de duplicidade de e-mail para `409 Conflict` (`RN02`).
- [ ] **T03.9:** Implementar `UsuarioService.listar` e `UsuarioService.consultarPorId` (`UC05`, paginação padrão 10 / máximo 50, termo até 100 chars, totalizadores, parâmetro `revelarCelular`) e `UsuarioService.excluirLogicamente` (`UC07`, bloqueio de autoexclusão — `RN26`, bloqueio por agendamentos `AGENDADO` — `RN27`, soft delete em cascata lógica `RN25` e evento `EXCLUSAO`).
- [ ] **T03.10:** Implementar `UsuarioController` e `GlobalExceptionHandler` garantindo respostas padronizadas (`400`, `401`, `403`, `404`, `409`) e logs seguros sem PII ou senhas (`RNF-01.1`, `RNF-02.2`).

---

## 🖥️ Fase 4: Frontend / Interface Web (React 18 / TypeScript / Tailwind)
- [ ] **T04.1:** Estruturar o projeto em `frontend/` com Vite, TypeScript, Tailwind CSS e Shadcn/ui configurados exclusivamente a partir de [`design/tokens.json`](../../../design/tokens.json).
- [ ] **T04.2:** Implementar `AuthContext`, interceptor Axios com envio de Bearer token e redirecionamento automático para `/login` ao receber `401`, além de `ProtectedRoute` com bloqueio por `NivelAcesso` (`RN17`).
- [ ] **T04.3:** Construir o layout principal com `Sidebar` condicional por perfil (`ADMINISTRADOR`, `MEDICO`, `PACIENTE`), `Header` institucional e ação **"Sair"** descartando a sessão no navegador (`RF-01.5`, `RN20`).
- [ ] **T04.4:** Construir `LoginPage` (`/login`) com validação `onBlur`/envio, envio via `Enter`, exibição de erro genérico `401`, redirecionamento por perfil (`Dashboard` para Admin; `Agendamentos` para Médico e Paciente — `RF-01.6`) e acessibilidade WCAG 2.1 AA (`RNF-01.6`).
- [ ] **T04.5:** Construir `UsuariosPage` (`/usuarios`) com os 4 cards totalizadores, busca textual por nome/e-mail, filtro por perfil, cabeçalhos ordenáveis (`ASC`/`DESC`), paginação na base, badges semânticos, máscara de celular e os 4 estados obrigatórios (`Skeleton`, `Vazio`, `Erro`, `Sucesso` — `RNF-02.3`).
- [ ] **T04.6:** Construir `UsuarioFormModal` (criação e edição com exibição condicional dos campos de `Funcionario` e `Paciente`, validação inline de senha/campos e perfil bloqueado na edição) e `UsuarioDetalheModal` com ação **"Revelar celular"** (`RN28`).
- [ ] **T04.7:** Construir `ConfirmarExclusaoDialog` com backdrop escurecido e suporte a teclado (`Tab`, `Enter`, `Esc`) conectado ao endpoint `DELETE /api/usuarios/{id}` com feedback visual de erros (`409` para `RN26`/`RN27`).

---

## 🧪 Fase 5: Testes e Validação
- [ ] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `AuthService`, `JwtService`, `UsuarioService` e `CelularMasker` cobrindo todas as RNs (`RN01`–`RN04`, `RN17`–`RN28`).
- [ ] **T05.2:** Implementar testes de integração (Spring Boot Test + Testcontainers PostgreSQL 18) para todos os endpoints de `/api/auth/login` e `/api/usuarios/**`, incluindo revogação imediata pós-exclusão (`RN18`) e recadastro de e-mail liberado (`RN25`).
- [ ] **T05.3:** Implementar teste automatizado de concorrência validando dois cadastros simultâneos com o mesmo e-mail (`1` sucesso `201` + `1` `409 Conflict` — `RNF-02.4`) e verificação de resposta uniforme `401` (`RNF-01.3`).
- [ ] **T05.4:** Implementar testes de front-end (Vitest + Testing Library) para formulários, redirecionamentos, estados de tela e mascaramento/revelação de celular.
- [ ] **T05.5:** Validar os **18 cenários BDD** de [`spec.md`](spec.md) em testes automatizados E2E/aceitação e auditar logs e payloads contra vazamento de senhas ou dados pessoais (`RNF-01.1`, `RNF-02.2`).
- [ ] **T05.6:** Validar acessibilidade WCAG 2.1 AA com Axe DevTools (`RNF-01.6`) e desempenho p95 < 500 ms com massa sintética (`RNF-01.5`, `RNF-02.1`).

---

## 🏁 Fase 6: Conclusão e Revisão
- [ ] **T06.1:** Confirmar alinhamento entre código implementado, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [ ] **T06.2:** Preencher o Checklist de Revisão de Banco de Dados ([`AGENTS.md`](../../../AGENTS.md)) e validar o `ddl-auto=validate` com `amde_digital_app`.
- [ ] **T06.3:** Realizar Code Review final, validar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-001` para `Implementado`.
