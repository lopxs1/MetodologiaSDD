# Diretório `application/` — Fora de Escopo (AMDE Digital)

Este diretório pertence originalmente à estrutura do template do Projeto Integrador e encontra-se **fora de escopo** no projeto **AMDE Digital**.

No **AMDE Digital**, o código-fonte da plataforma é mantido de forma desacoplada nos diretórios dedicados na raiz do repositório:

| Caminho | Responsabilidade | Stack Principal | Documentação |
|---|---|---|---|
| `backend/` | API REST, segurança JWT/RBAC, persistência JPA e migrations Liquibase | Java 17 LTS, Spring Boot 3, Spring Security 6, PostgreSQL 18 | [`backend/README.md`](../backend/README.md) |
| `frontend/` | Aplicação Web SPA (portal administrativo, clínico e do paciente) | React 18, TypeScript, Vite, Tailwind CSS, Vitest | [`frontend/README.md`](../frontend/README.md) |
| `data/` | Bootstrap de roles/schema (`00_bootstrap.sql`), seeds sintéticos e auditoria (`verificacao.sql`) | PostgreSQL 18 (`sqlfluff` + `psql`) | [`data/README.md`](../data/README.md) |
| `.specify/` | Constituição, modelo conceitual, design system e especificações (`SPEC-001` a `SPEC-006`) | Spec-Driven Development (SDD) | [`.specify/README.md`](../.specify/README.md) |

Para instruções de instalação, execução e testes do sistema completo, consulte o [`README.md`](../README.md) principal.
