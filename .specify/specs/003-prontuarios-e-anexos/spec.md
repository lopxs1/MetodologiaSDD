# Especificação Funcional: Prontuários Digitais e Anexos

> **ID da Spec:** `SPEC-003`  
> **Status:** Rascunho  
> **Autores / Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Data:** 2026-11-18  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Rastreio:** RF-04 · UC10, UC11, UC12, UC13 · RN05 a RN10, RN16, RN34 a RN38 · Pacote `specs/003-prontuarios-e-anexos/`  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
Laudos, exames e receituários dos pacientes da AMDE ficam em papel, o que dificulta localizar documentos, compartilhar com a equipe e comprovar quem acessou o quê. Documentos clínicos de crianças e adolescentes são dados sensíveis e, pela Resolução CFM nº 1.821/2007 e pela LGPD, exigem guarda segura, preservação do histórico e rastreabilidade de envio, acesso e exclusão. São impactados Médicos, Administradores e Pacientes (ou seus titulares).

### 1.2. Objetivo e Valor Entregue
Permitir que a equipe anexe documentos em PDF ao prontuário do paciente, classifique-os, substitua versões sem perder o histórico, e que o paciente ou o profissional baixe o documento com segurança, com cada envio, download e exclusão registrado na auditoria.

### 1.3. Escopo, Fora de Escopo e Dependências
- **Dentro do escopo:** envio de documento PDF a um paciente com título e categoria e substituição com preservação da versão anterior (UC10), validação de arquivo PDF e limite de 10 MB (UC13), download (UC11), exclusão (UC12), reclassificação e arquivamento, nome físico único, registro de UPLOAD, DOWNLOAD e EXCLUSAO.
- **Fora de escopo (decisões de spec):** pré-visualização do PDF no navegador; edição do conteúdo do arquivo; formatos além de PDF; envio em lote; criação, edição ou exclusão de categorias (as quatro são fixas); política automática de retenção e arquivamento por prazo (sem rotina automática nesta versão); listagem do histórico, que é exibida na ficha do paciente (SPEC-002); detalhes de armazenamento físico, descritos no plano técnico.
- **Dependências:** SPEC-001 (perfis e permissões), SPEC-002 (ficha que lista os documentos) e SPEC-005 (gravação dos eventos UPLOAD, DOWNLOAD e EXCLUSAO).

---

## 2. Atores e Perfis de Acesso

- **Administrador:** envia, substitui, reclassifica, arquiva, exclui e baixa documentos de qualquer paciente.
- **Profissional de Saúde / Médico (perfil `MEDICO`):** envia, substitui, reclassifica, arquiva, exclui e baixa documentos de qualquer paciente.
- **Paciente:** baixa somente os próprios documentos; não envia, substitui, reclassifica, arquiva nem exclui.

---

## 3. Histórias de Usuário (User Stories)

- **US-04.1:** Como **Administrador ou Médico**, quero **anexar um laudo, exame, receituário ou registro arquivado em PDF ao prontuário de um paciente** para que **o documento fique disponível de forma segura e organizada**.
- **US-04.2:** Como **Administrador, Médico ou Paciente titular**, quero **baixar um documento do prontuário** para que **eu consulte o original quando precisar**.
- **US-04.3:** Como **Administrador ou Médico**, quero **substituir um documento por uma versão corrigida mantendo a anterior no histórico** para que **o histórico clínico nunca seja perdido**.
- **US-04.4:** Como **Administrador ou Médico**, quero **reclassificar ou arquivar documentos** para que **o prontuário continue organizado**.
- **US-04.5:** Como **Administrador ou Médico**, quero **excluir um documento enviado por engano** para que **ele deixe de existir no prontuário, com a exclusão rastreada**.
- **US-04.6:** Como **instituição**, quero que **envio, download e exclusão fiquem registrados** para que **a AMDE comprove conformidade com a LGPD e o CFM**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-04.1** | O sistema deve permitir a Administrador e Médico enviar um documento a um paciente ativo informando título, categoria (`Exames`, `Laudos`, `Receituário` ou `Arquivado`) e o arquivo, preservando o nome original e guardando o arquivo com nome interno único (UC10). | Crítica |
| **RF-04.2** | O sistema deve aceitar somente arquivos PDF válidos com tamanho menor que 10 MB e rejeitar os demais sem gravar nada (RN05, RN34) (UC10, UC13). | Crítica |
| **RF-04.3** | O sistema deve permitir baixar o documento, entregando o arquivo original com o nome original, a Administrador e Médico (qualquer paciente) e ao Paciente (somente os próprios), respondendo `404` para documento inexistente e `500` para arquivo físico ausente (RN06, RN09, RN38) (UC11). | Crítica |
| **RF-04.4** | O sistema deve permitir substituir um documento vigente por um novo arquivo, mantendo o documento anterior no histórico ligado à nova versão (RN08, RN35) (UC10). | Alta |
| **RF-04.5** | O sistema deve permitir reclassificar um documento entre as categorias e arquivá-lo na categoria Arquivado (RN08, RN36). | Média |
| **RF-04.6** | O sistema deve permitir excluir um documento, após confirmação em diálogo, na sequência: registrar EXCLUSAO, remover o arquivo físico e inativar o registro (RN10, RN37) (UC12). | Alta |
| **RF-04.7** | O sistema deve registrar na trilha de auditoria os eventos UPLOAD (envio e substituição), DOWNLOAD (entrega bem-sucedida) e EXCLUSAO (RN16, SPEC-005). | Crítica |
| **RF-04.8** | O sistema deve restringir envio, substituição, reclassificação, arquivamento e exclusão a Administrador e Médico, negando-os ao Paciente (RN06). | Crítica |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-04.1** | Eficiência de Desempenho | O envio de um PDF de até 10 MB conclui em menos de 5 s no percentil 95, e o início do download ocorre em menos de 2 s, em rede local do ambiente de teste. | Teste de carga com PDFs sintéticos de tamanhos variados |
| **RNF-04.2** | Segurança / Confidencialidade | O arquivo só é entregue por requisição autenticada e autorizada. Nenhum endereço público, previsível ou derivado do nome original dá acesso ao arquivo. | Teste de segurança tentando acessar o arquivo sem sessão, com outro perfil e por endereço adivinhado |
| **RNF-04.3** | Segurança / Confidencialidade | Conteúdo, título e nome original dos documentos nunca aparecem em logs nem em mensagens de erro enviadas ao cliente. | Varredura dos logs e das respostas de erro após os cenários |
| **RNF-04.4** | Integridade | O arquivo baixado é idêntico, byte a byte, ao arquivo enviado. | Teste automatizado comparando resumo criptográfico do enviado e do baixado |
| **RNF-04.5** | Integridade | Dois envios com o mesmo nome original nunca colidem nem sobrescrevem um ao outro. | Teste automatizado com envios simultâneos do mesmo nome |
| **RNF-04.6** | Usabilidade / Interface | O envio exibe progresso e feedback de sucesso ou erro sem recarregar a tela; a exclusão exige modal de confirmação informando o impacto; todas as ações funcionam por teclado (`Tab`, `Enter`, `Esc`). | Teste E2E de interface |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** `RegistroProntuario` (documento anexado), `Categoria` (Exames, Laudos, Receituário, Arquivado), `Paciente` (titular do prontuário), `Usuario` (quem executa a ação) e `LogAuditoria` (somente gravação, via SPEC-005).
- **Entidades Novas ou Alteradas:** nenhuma.

### 5.2. Checklist de Suficiência de Entidades (Pré-Planejamento)
*A IA e os desenvolvedores devem checar estas perguntas para cada entidade antes de avançar para o plano técnico:*
- [x] **Identidade:** O que torna a instância única (chave de negócio / natural e chave substituta)?
- [x] **Atributos e Nulabilidade:** Quais informações são obrigatórias e quais são opcionais (com justificativa clara de negócio para cada campo opcional/anulável)?
- [x] **Relacionamentos:** Quais as cardinalidades mínimas e máximas com outras entidades?
- [x] **Regras & Invariantes:** Quais restrições de domínio, limites ou estados válidos aplicam-se?
- [x] **Ciclo de Vida & Histórico:** A entidade sofre inativação lógica (soft delete via data/hora de exclusão) ou é imutável/histórica?
- [x] **Classificação LGPD e Minimização:** Quais atributos contêm dados pessoais (`pessoal`) ou clínicos/saúde (`sensível`) e qual a justificativa de uso de cada um?

| Entidade | Identidade | Atributos usados | Relacionamentos | Regras e invariantes | Ciclo de vida | LGPD |
|---|---|---|---|---|---|---|
| `RegistroProntuario` | Chave substituta; o nome interno do arquivo é único (nunca se repete entre documentos) | Paciente, categoria, título, nome original e nome interno são obrigatórios. Único opcional: o documento que ele substitui, preenchido só em versões criadas por substituição (RN35) | N:1 `Paciente`; N:1 `Categoria`; 0..1 documento anterior substituído; 0..n eventos de auditoria | PDF menor que 10 MB (RN05); nome interno único (RN07); só uma versão vigente por cadeia (RN35) | Inativação lógica na exclusão (RN10); arquivamento é reclassificação, não exclusão; versões antigas permanecem | `sensível`: título, nome original e nome interno do arquivo (documentos clínicos de menor) |
| `Categoria` | Nome oficial único: Exames, Laudos, Receituário, Arquivado | Nome | 1:N `RegistroProntuario` | Quatro valores fixos, sem criação pelo usuário | Dado de referência, sem exclusão | Sem dado pessoal |
| `Paciente` | Chave substituta; vínculo 1:1 com `Usuario` | Apenas o identificador, para validar que o paciente existe e está ativo | 0..n `RegistroProntuario` | Só paciente ativo recebe documento (RN34) | Soft delete (SPEC-001); documentos preservados | `sensível` (dados clínicos de menor), sem uso adicional aqui |
| `LogAuditoria` | Evento único por operação | Usuário, ação (`UPLOAD`, `DOWNLOAD` ou `EXCLUSAO`), recurso, IP de origem, instante | N:1 `Usuario` | Imutável (SPEC-005) | Somente inclusão | IP é `pessoal`, usado para rastreabilidade |

---

## 6. Regras de Negócio (RN)

- **RN05** *(global)*: O upload de prontuário valida formato (PDF) e tamanho máximo (menor que 10 MB).
- **RN06** *(global)*: Envio e exclusão são restritos a Funcionário (Médico) e Administrador; o download é aberto ao titular.
- **RN07** *(global)*: O nome original é preservado no banco e o arquivo físico usa um UUID.
- **RN08** *(global)*: A categorização é obrigatória (Exames, Laudos, Receituário, Arquivado) e o versionamento é feito por substituição.
- **RN09** *(global)*: O download retorna `404 Not Found` para identificador inexistente e `500` quando o arquivo físico está ausente.
- **RN10** *(global)*: A exclusão remove o arquivo físico e registra o log antes de inativar o registro.
- **RN16** *(global)*: Toda ação sensível gera registro na trilha de auditoria (LGPD / CFM).
- **RN34:** Todo envio exige paciente ativo, título legível (2 a 150 caracteres após remover espaços das pontas), uma das quatro categorias oficiais (`Exames`, `Laudos`, `Receituário` ou `Arquivado`) e um arquivo PDF válido com nome original terminado em `.pdf` (5 a 255 caracteres). Paciente inexistente ou inativado responde `404 Not Found`. Dado obrigatório ausente ou inválido responde `400 Bad Request`. Arquivo vazio, corrompido ou que não seja PDF (extensão `.pdf`, `Content-Type: application/pdf` e *magic bytes* `%PDF-`) é rejeitado com `400 Bad Request` (ou `415 Unsupported Media Type`), e arquivo com tamanho igual ou superior a 10 MB (`>= 10.485.760` bytes) é rejeitado com `400 Bad Request` (ou `413 Payload Too Large`); em qualquer rejeição, nada é gravado no banco nem no armazenamento físico.
- **RN35:** A substituição só vale para documento vigente (não excluído, não arquivado e ainda não substituído). O novo documento herda paciente e categoria, aceita novo título (2 a 150 caracteres; padrão: o anterior), segue as validações da RN05 e da RN34 e fica ligado ao anterior. O anterior permanece no histórico, marcado como "Substituído" e ainda pode ser baixado. Tentar substituir documento já substituído, arquivado ou excluído responde `409 Conflict`.
- **RN36:** Administrador e Médico podem reclassificar um documento não excluído entre `Exames`, `Laudos` e `Receituário`, ou arquivá-lo na categoria `Arquivado`. Documento arquivado continua visível no histórico e disponível para download, mas não pode ser substituído. Reclassificar não altera o arquivo.
- **RN37:** A exclusão segue a ordem: (1) registrar o evento EXCLUSAO, (2) remover o arquivo físico, (3) inativar o registro. Se o passo 1 falhar, nada é removido. Se o passo 2 falhar, o registro permanece ativo, a operação responde `500` e o evento já gravado permanece como registro da tentativa (a trilha é imutável); o usuário pode tentar de novo. Documento excluído deixa de aparecer no histórico e não pode ser baixado (`404`).
- **RN38:** O download entrega o arquivo com o nome original enviado. O nome interno do arquivo nunca é exibido ao usuário.

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: Envio com sucesso
```gherkin
Dado que um Médico está autenticado e existe um paciente ativo
Quando ele envia o PDF "laudo-2026.pdf" de 2 MB com título "Laudo neuropsicológico" e categoria Laudos
Então o documento é salvo no prontuário do paciente, com o nome original preservado e um nome interno único
E o documento aparece no histórico da ficha na categoria Laudos
E um evento UPLOAD é registrado com o usuário, o recurso e o IP de origem
```

### Cenário 2: Arquivo que não é PDF
```gherkin
Dado que um Médico está enviando um documento
Quando ele seleciona um arquivo vazio ou que não é PDF (por exemplo uma imagem, ou um arquivo com extensão ".pdf" e conteúdo sem assinatura "%PDF-")
Então o sistema responde 400 (ou 415) com mensagem informativa
E nenhum documento nem evento UPLOAD é registrado
```

### Cenário 3: Arquivo grande demais
```gherkin
Dado que um Médico está enviando um PDF
Quando o arquivo tem 10 MB (10.485.760 bytes) ou mais
Então o sistema responde 400 (ou 413) com mensagem informativa
E nenhum documento nem evento UPLOAD é registrado
```

### Cenário 4: Dados do envio inválidos
```gherkin
Dado que um Administrador está enviando um documento
Quando ele omite o título, informa um título com 1 caractere ou acima de 150 caracteres, escolhe uma categoria inexistente ou indica um paciente inexistente ou inativado
Então o sistema responde 400 (dados inválidos) ou 404 (paciente/categoria não encontrado)
E nada é gravado
```

### Cenário 5: Paciente tenta enviar ou alterar documentos
```gherkin
Dado que um Paciente está autenticado
Quando ele tenta enviar, substituir, reclassificar, arquivar ou excluir um documento
Então o sistema responde 403 Forbidden
E nenhum documento é alterado
```

### Cenário 6: Download por Médico ou Administrador
```gherkin
Dado que existe um documento ativo de qualquer paciente
Quando um Médico ou Administrador solicita o download
Então o sistema entrega o arquivo idêntico ao enviado, com o nome original
E um evento DOWNLOAD é registrado
```

### Cenário 7: Download pelo Paciente titular e por outro Paciente
```gherkin
Dado que existe um documento ativo do Paciente A
Quando o Paciente A solicita o download
Então o sistema entrega o arquivo e registra o evento DOWNLOAD
E, quando o Paciente B solicita o mesmo documento, o sistema responde 403 Forbidden, sem entregar o arquivo nem registrar DOWNLOAD
```

### Cenário 8: Documento inexistente ou arquivo físico ausente
```gherkin
Dado que um usuário autorizado solicita o download
Quando o identificador do documento não existe ou o documento foi excluído
Então o sistema responde 404 Not Found
E, quando o registro existe mas o arquivo físico não é encontrado, o sistema responde 500 com mensagem genérica, sem expor detalhes técnicos nem registrar DOWNLOAD
```

### Cenário 9: Nomes originais repetidos
```gherkin
Dado que dois documentos diferentes são enviados com o mesmo nome original "exame.pdf"
Quando ambos são salvos, inclusive simultaneamente
Então os dois existem com nomes internos distintos, sem sobrescrita
E cada download entrega o conteúdo do respectivo documento
```

### Cenário 10: Substituição de documento
```gherkin
Dado que existe um laudo vigente de um paciente
Quando o Médico envia um novo PDF válido para substituí-lo
Então o novo documento passa a ser a versão vigente, herdando paciente e categoria
E o laudo anterior permanece no histórico como "Substituído" e continua disponível para download
E um evento UPLOAD é registrado para o novo documento
```

### Cenário 11: Substituição inválida
```gherkin
Dado que um documento já foi substituído, está arquivado ou foi excluído
Quando o Médico tenta substituí-lo
Então o sistema responde 409 Conflict
E nenhum documento novo é criado
```

### Cenário 12: Reclassificar e arquivar
```gherkin
Dado que existe um documento não excluído na categoria Exames
Quando o Médico o reclassifica para Laudos e depois o arquiva
Então o documento aparece primeiro em Laudos e depois em Arquivado, com o mesmo arquivo
E o documento arquivado continua disponível para download e não pode ser substituído
```

### Cenário 13: Exclusão com sucesso
```gherkin
Dado que existe um documento ativo
Quando o Administrador ou Médico confirma a exclusão no diálogo de confirmação
Então um evento EXCLUSAO é registrado antes de qualquer remoção
E o arquivo físico é removido e o registro é inativado
E o documento deixa de aparecer no histórico e o download passa a responder 404
```

### Cenário 14: Exclusão cancelada ou com falha
```gherkin
Dado que o usuário abriu o diálogo de exclusão de um documento
Quando ele cancela a exclusão
Então nada é alterado e nenhum evento EXCLUSAO é registrado
E, quando a gravação do evento EXCLUSAO falha, o sistema responde 500 com mensagem genérica, o arquivo físico não é removido e o documento permanece ativo
E, quando a remoção do arquivo físico falha, o sistema responde 500, o documento permanece ativo e o evento já gravado fica como registro da tentativa
```

**Cobertura RF × Cenários:** RF-04.1 → 1, 4; RF-04.2 → 2, 3; RF-04.3 → 6, 7, 8; RF-04.4 → 10, 11; RF-04.5 → 12; RF-04.6 → 13, 14; RF-04.7 → 1, 6, 7, 10, 13; RF-04.8 → 5. RN05 → 2, 3; RN06 → 5, 7; RN07 → 1, 9; RN08 → 10, 12; RN09 → 8; RN10 → 13; RN16 → 1, 6, 13; RN34 → 1, 2, 3, 4; RN35 → 10, 11; RN36 → 12; RN37 → 13, 14; RN38 → 6.

---

## 8. Casos de Borda e Tratamento de Exceções

- Arquivo de 0 byte, corrompido, sem extensão `.pdf`, sem `Content-Type: application/pdf` ou com conteúdo sem *magic bytes* `%PDF-` é tratado como formato inválido (`400` ou `415`). O limite de 10 MB é exclusivo: 10 MB (`10.485.760` bytes) ou mais é rejeitado (`400` ou `413`).
- Nome original com acentos, espaços ou símbolos é preservado como informação e entregue no download; caracteres que ameacem o sistema de arquivos não afetam o nome interno, que é sempre gerado pelo sistema. O nome original deve terminar em `.pdf` (sem distinção de maiúsculas/minúsculas) e ter entre 5 e 255 caracteres; nomes fora desse intervalo ou sem extensão `.pdf` são rejeitados com `400`.
- Queda de conexão no meio do envio: nenhum documento parcial é registrado nem evento UPLOAD é gravado.
- Envios simultâneos do mesmo nome original: ambos aceitos, sem colisão (RNF-04.5).
- Duas substituições simultâneas do mesmo documento: uma é aceita e a outra recebe `409 Conflict` (RN35).
- Exclusão duplicada do mesmo documento: a segunda responde `404 Not Found`.
- Documento de paciente que foi inativado permanece preservado, sem política automática de arquivamento por prazo nesta versão.
- O evento DOWNLOAD só é gravado quando o arquivo é entregue. Falha ao gravar UPLOAD ou DOWNLOAD não impede a operação (RN44, SPEC-005); falha ao gravar EXCLUSAO impede a exclusão (RN37).
- Reclassificação e arquivamento não geram evento de auditoria nesta versão, pois a lista de ações auditáveis é fechada.
- Paciente que tenta baixar documento de outro paciente recebe `403` mesmo que o identificador exista; identificador inexistente responde `404`.

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Todas as dúvidas levantadas na elaboração foram resolvidas. **[Fonte]** indica decisão tirada de documento do repositório; **[Decisão de spec]** indica escolha feita aqui e que a equipe deve validar na aprovação.*

- [x] **Dúvida 1:** O que cada UC cobre? → Em `casos-de-uso.md`, `UC10` cobre o upload de prontuário (envio de novo documento e substituição/versionamento via `registro_substituido_id`), `UC11` cobre o download, `UC12` cobre a exclusão e `UC13` cobre a validação de arquivo PDF (`<<include>>` de `UC10`). **[Fonte: casos-de-uso.md UC10 a UC13]**
- [x] **Dúvida 2:** "Arquivar" é o mesmo que "excluir"? → Não. Arquivar move o documento para a categoria Arquivado, mantendo-o visível e baixável. Excluir remove o arquivo físico e inativa o registro. **[Fonte: constitution.md §3.3 ("e/ou") e casos-de-uso.md UC12; Decisão de spec quanto à distinção]**
- [x] **Dúvida 3:** O que acontece com a versão antiga ao substituir? → Permanece no histórico, ligada à nova, marcada "Substituído", ainda baixável (RN35). **[Fonte: constitution.md §3.3, versionamento; Decisão de spec quanto à marcação]**
- [x] **Dúvida 4:** Quais códigos de erro para formato e tamanho? → Em `casos-de-uso.md` (`UC13`), o arquivo fora do padrão PDF (extensão, `Content-Type` ou *magic bytes* `%PDF-`) é rejeitado com `400 Bad Request` (aceitando-se `415 Unsupported Media Type` como equivalente semântico), e o tamanho `>= 10 MB` (`>= 10.485.760` bytes) é rejeitado com `400 Bad Request` (ou `413 Payload Too Large`). **[Fonte: casos-de-uso.md UC13]**
- [x] **Dúvida 5:** "Menor que 10 MB" significa quantos bytes? → Menos de 10.485.760 bytes (10 MB binários). **[Fonte: casos-de-uso.md UC13]**
- [x] **Dúvida 6:** É possível enviar já como "Arquivado"? → Sim: conforme `UC10` (Passo 1) de `casos-de-uso.md`, o envio aceita qualquer uma das quatro categorias oficiais (`Exames`, `Laudos`, `Receituário` ou `Arquivado`), além da operação posterior de arquivamento por reclassificação (`RN36`). **[Fonte: casos-de-uso.md UC10]**
- [x] **Dúvida 7:** Se a remoção do arquivo falhar na exclusão, o evento já gravado some? → Não: a trilha é imutável; o evento fica como registro da tentativa e o documento continua ativo (RN37). **[Fonte: domain-model.md, invariante 8; Decisão de spec]**
- [x] **Dúvida 8:** Reclassificação e arquivamento são auditados? → Não nesta versão: a lista de ações é fechada. **[Fonte: domain-model.md, `LogAuditoria`]**
- [x] **Dúvida 9:** Quem pode baixar? → Administrador e Médico, qualquer paciente; Paciente, somente os próprios (RN06). **[Fonte: especificacao-requisitos.md RN06; matriz RN17 da SPEC-001]**
- [x] **Dúvida 10:** Onde o histórico é listado? → Na ficha do paciente (SPEC-002), que só mostra metadados; o conteúdo binário é baixado aqui. **[Fonte: especificacao-requisitos.md RF-03 e casos-de-uso.md UC09]**
