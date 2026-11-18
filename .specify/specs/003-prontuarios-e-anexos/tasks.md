# Checklist de Tarefas: Prontuários Digitais e Anexos

> **ID da Spec:** `SPEC-003`  
> **Status Geral:** `0 / 26 Concluídas`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos
- [ ] **T01.1:** Revisar requisitos (`RF-04.1` a `RF-04.8`), RNFs (`RNF-04.1` a `RNF-04.6`), regras (`RN05`–`RN10`, `RN16`, `RN34`–`RN38`) e os 14 cenários BDD em [`spec.md`](spec.md).
- [ ] **T01.2:** Validar o contrato OpenAPI 3.0.3 em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) para os 5 endpoints (`/upload`, `/{id}/substituir`, `/{id}/categoria`, `/{id}/download`, `/{id}`).
- [ ] **T01.3:** Submeter o pacote `003-prontuarios-e-anexos` para aprovação da equipe antes da implementação.

---

## 💾 Fase 2: Banco de Dados, Storage e Seeds Sintéticos
- [ ] **T02.1:** Confirmar que `registro_prontuario` e `categoria` em [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) atendem integralmente `UC10`–`UC13` (`uq_registro_prontuario_arquivo_armazenado`, `ck_registro_prontuario_*`).
- [ ] **T02.2:** Configurar o diretório seguro local `${UPLOAD_DIR}` (`uploads/prontuarios`) no ambiente de desenvolvimento sem exposição estática e definir o perfil de armazenamento S3 para produção.
- [ ] **T02.3:** Validar scripts SQL com `sqlfluff lint --dialect postgres` e confirmar **zero linhas** nas 9 verificações de [`data/verificacao.sql`](../../../data/verificacao.sql) (revisando o inventário `[LGPD: sensível]` de `registro_prontuario`).
- [ ] **T02.4:** Criar seed sintético (`data/seeds/003_prontuarios_seed_sintetico.sql`) com documentos sintéticos (vigente, substituído e arquivado) e rotina geradora de PDFs sintéticos para testes locais.

---

## ⚙️ Fase 3: Backend, Storage e Regras de Negócio (Spring Boot 3 / Java 17)
- [ ] **T03.1:** Implementar `ProntuarioStorageService` com implementações `LocalFileSystemStorageService` (dev/teste) e `S3ProntuarioStorageService` (produção) usando o caminho padronizado `paciente-{pacienteId}/{uuid}.pdf`.
- [ ] **T03.2:** Implementar `PdfValidator` validando arquivo não vazio, tamanho `< 10.485.760` bytes (`RN05`), nome original (`5..255` chars terminando em `.pdf`), `Content-Type: application/pdf` e *magic bytes* `%PDF-` (`UC13`, `RN34`).
- [ ] **T03.3:** Configurar limite de multipart no Spring e mapeamento no `GlobalExceptionHandler` para retornar `413` / `400` sem expor caminhos internos nem nomes de documentos (`RNF-04.3`).
- [ ] **T03.4:** Implementar `RegistroProntuarioRepository` incluindo consulta com `@Lock(LockModeType.PESSIMISTIC_WRITE)` para serializar substituições concorrentes (`RN35`).
- [ ] **T03.5:** Implementar `ProntuarioService.enviar` (`UC10`, `RN34`) com validação de paciente ativo, título (`2..150` chars), categoria oficial (`Exames`, `Laudos`, `Receituario`, `Arquivado`), geração de UUID físico único, limpeza compensatória do arquivo em rollback e disparo assíncrono do evento `UPLOAD`.
- [ ] **T03.6:** Implementar `ProntuarioService.substituir` (`UC10`, `RN35`) validando documento vigente (não excluído, não arquivado, ainda não substituído — `409 Conflict`), herdando paciente/categoria e ligando `registro_substituido_id`.
- [ ] **T03.7:** Implementar `ProntuarioService.reclassificarOuArquivar` (`RN36`) alterando a categoria entre `Exames`, `Laudos`, `Receituario` e `Arquivado` sem alterar o arquivo físico.
- [ ] **T03.8:** Implementar `ProntuarioService.prepararDownload` (`UC11`, `RN06`, `RN09`, `RN38`) validando titularidade para `PACIENTE` (`403`), existência do registro (`404`), existência do arquivo físico (`500` sem gravar `DOWNLOAD`), cabeçalho `Content-Disposition` com nome original e evento assíncrono `DOWNLOAD`.
- [ ] **T03.9:** Implementar `ProntuarioService.excluir` (`UC12`, `RN10`, `RN37`) na ordem estrita: (1) auditoria síncrona `REQUIRES_NEW` (`EXCLUSAO`), (2) remoção física no storage e (3) soft delete (`excluido_em = now()`), mais `ProntuarioController` e `ProntuarioMapper` (sem `nomeArquivoArmazenado`).

---

## 🖥️ Fase 4: Frontend / Interface Web (React 18 / TypeScript / Tailwind)
- [ ] **T04.1:** Implementar `prontuarioService.ts` suportando `multipart/form-data` com callback `onUploadProgress` e download binário via `responseType: 'blob'`.
- [ ] **T04.2:** Construir `UploadProntuarioModal` integrado à ficha do paciente (visível apenas para Admin e Médico) com barra de progresso, seleção das 4 categorias, título (`2..150` chars) e validação antecipada de PDF (`< 10 MB`).
- [ ] **T04.3:** Construir `SubstituirProntuarioModal` acionado sobre documentos em situação `Vigente`, atualizando o histórico e preservando a versão anterior como `"Substituído"`.
- [ ] **T04.4:** Construir `ReclassificarCategoriaDialog` e ação **"Arquivar"** para documentos ativos (Admin e Médico).
- [ ] **T04.5:** Integrar a ação **"Baixar PDF"** (Admin, Médico e Paciente titular) salvando o arquivo com `nomeArquivoOriginal` e tratando erros `403`/`404`/`500` sem reload (`RNF-04.6`).
- [ ] **T04.6:** Construir `ConfirmarExclusaoProntuarioDialog` com backdrop escurecido e suporte integral a teclado (`Tab`, `Enter`, `Esc` — `RNF-04.6`).

---

## 🧪 Fase 5: Testes e Validação
- [ ] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `PdfValidator` e `ProntuarioService` cobrindo todas as regras (`RN05`–`RN10`, `RN34`–`RN38`), compensação física em rollback e as falhas parciais da exclusão em 3 passos (`Cenário 14`).
- [ ] **T05.2:** Implementar testes de integração (Spring Boot Test + Testcontainers PostgreSQL 18) validando todos os endpoints, comparação **SHA-256** byte a byte entre PDF enviado e baixado (`RNF-04.4`) e preservação de nomes acentuados (`RN38`).
- [ ] **T05.3:** Implementar testes automatizados de concorrência para uploads simultâneos com mesmo nome original (`RNF-04.5`) e substituições simultâneas do mesmo documento (`1` sucesso + `1` `409 Conflict`).
- [ ] **T05.4:** Implementar testes de front-end (Vitest + Testing Library) para barra de progresso, modais, reclassificação e controle por teclado (`Tab`, `Enter`, `Esc`).
- [ ] **T05.5:** Validar os **14 cenários BDD** de [`spec.md`](spec.md) em testes automatizados e auditar logs e mensagens de erro garantindo zero exposição de título, nome de arquivo ou caminhos físicos (`RNF-04.2`, `RNF-04.3`).
- [ ] **T05.6:** Validar desempenho p95 (`< 5s` upload, `< 2s` início do download) com PDFs sintéticos de até 10 MB (`RNF-04.1`).

---

## 🏁 Fase 6: Conclusão e Revisão
- [ ] **T06.1:** Confirmar alinhamento entre código implementado, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [ ] **T06.2:** Validar `ddl-auto=validate` e Checklist de Banco de Dados ([`AGENTS.md`](../../../AGENTS.md)).
- [ ] **T06.3:** Realizar Code Review final, confirmar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-003` para `Implementado`.
