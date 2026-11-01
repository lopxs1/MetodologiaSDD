# Documentação do Projeto: AMDE Digital — Controle de Prontuário

Esta pasta reúne a monografia acadêmica e os artefatos de engenharia de software do projeto **AMDE Digital**, estruturados conforme as orientações institucionais da Fatec Sorocaba e integrados à metodologia **Spec-Driven Development (SDD)** mantida em [`.specify/`](../.specify/README.md).

---

## 📄 Documento Acadêmico

- **Monografia Base:** [`tg.pdf`](tg.pdf) — Documentação de Desenvolvimento de Software do AMDE Digital (Dezembro/2025).
- **Template Institucional:** [`TG-Template-ADS-formato-academico 1-v1.docx`](TG-Template-ADS-formato-academico%201-v1.docx) — Modelo oficial em formato Word para a redação contínua da equipe (`tg.docx`).

---

## 📁 Estrutura de Engenharia de Software da Pasta `docs/`

Os modelos desta pasta consolidam os artefatos concebidos na metodologia SDD para documentação formal da banca e orientador:

| Subpasta | Documentos Principais | Finalidade e Conteúdo |
|---|---|---|
| [`requisitos/`](requisitos/README.md) | [`especificacao-requisitos.md`](requisitos/especificacao-requisitos.md) | Requisitos Funcionais (RF01 a RF07), Não Funcionais (RNF01 a RNF05), Regras de Negócio (RN01 a RN16) e rastreabilidade para as specs em `.specify/specs/`. |
| [`der/`](der/README.md) | [`der.md`](der/der.md)<br>[`dicionario-de-dados.md`](der/dicionario-de-dados.md) | Diagrama de Entidade-Relacionamento conceitual/lógico (Mermaid) e dicionário de dados físico completo do PostgreSQL 16. |
| [`arquitetura/`](arquitetura/README.md) | [`visao-arquitetural.md`](arquitetura/visao-arquitetural.md)<br>[`diagrama-classes.md`](arquitetura/diagrama-classes.md) | Visão de componentes em camadas (React, Spring Boot 3, PostgreSQL, Docker, AWS) e Diagrama de Classes do domínio. |
| [`casos-de-uso/`](casos-de-uso/README.md) | Casos de uso detalhados | Especificação dos casos de uso (UC01 a UC21) documentados na monografia. |
| [`decisoes/`](decisoes/README.md) | Architecture Decision Records (ADRs) | Registro de decisões técnicas de banco (índices, tunning) e arquitetura. |
| [`testes/`](testes/README.md) | Roteiros e planos de teste | Estratégia de testes unitários, integração e cenários de aceitação BDD. |

---

## 🔗 Relação entre `docs/` e `.specify/`

- **`docs/` (Visão Institucional e Acadêmica):** Apresenta os modelos formais consolidados de requisitos, arquitetura, DER e texto acadêmico para avaliação da disciplina e banca.
- **`.specify/` (Motor Operacional SDD com IA):** Conduz o desenvolvimento incremental funcionalidade por funcionalidade (`spec.md` ➔ `plan.md` ➔ `data-model.md` ➔ `contracts/` ➔ `tasks.md`), assegurando que nenhuma linha de código seja produzida sem validação prévia.
