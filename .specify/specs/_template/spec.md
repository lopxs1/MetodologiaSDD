# Especificação Funcional: [Nome da Funcionalidade]

> **ID da Spec:** `SPEC-XXX`  
> **Status:** [Rascunho | Em Esclarecimento | Aprovado | Implementado]  
> **Autores / Responsáveis:** [Nomes dos Integrantes da Equipe]  
> **Data:** AAAA-MM-DD  
> **Foco:** **O Quê e Por Quê** (Linguagem de Negócio, Requisitos, Regras e Critérios de Aceite)  
> **Memória Global:** [constitution.md](../../memory/constitution.md) | [domain-model.md](../../memory/domain-model.md)

---

## 1. Visão Geral e Contexto de Negócio

### 1.1. Contexto e Problema
[Descreva a dor ou necessidade real no fluxo da AMDE. Quem é impactado e por quê?]

### 1.2. Objetivo e Valor Entregue
[Qual o benefício concreto alcançado para a instituição, profissionais ou pacientes?]

---

## 2. Atores e Perfis de Acesso

- **Administrador:** [Permissões e objetivos na funcionalidade]
- **Profissional de Saúde / Médico:** [Permissões e objetivos na funcionalidade]
- **Paciente:** [Permissões e objetivos na funcionalidade]

---

## 3. Histórias de Usuário (User Stories)

- **US-01:** Como **[perfil do usuário]**, quero **[ação ou objetivo no sistema]** para que **[valor de negócio obtido]**.
- **US-02:** Como **[perfil do usuário]**, quero **[ação ou objetivo no sistema]** para que **[valor de negócio obtido]**.

---

## 4. Requisitos

### 4.1. Requisitos Funcionais (RF)

| ID | Descrição do Comportamento Esperado | Prioridade [Crítica / Alta / Média] |
|---|---|---|
| **RF-01** | [O sistema deve permitir...] | [Prioridade] |
| **RF-02** | [O sistema deve validar...] | [Prioridade] |

### 4.2. Requisitos Não Funcionais Específicos (RNF)

> *Nota: Os requisitos não funcionais transversais (LGPD, WCAG 2.1 AA, arquitetura base e segurança) são herdados da [constitution.md](../../memory/constitution.md). Registre aqui apenas exigências específicas desta funcionalidade.*

| ID | Categoria (ISO/IEC 25010) | Requisito Mensurável | Como Verificar |
|---|---|---|---|
| **RNF-01** | Eficiência de Desempenho | [Ex.: A consulta de agendamentos retorna em menos de 500ms com 10.000 registros] | Teste de carga com massa de dados sintética |
| **RNF-02** | Segurança / Auditoria | [Ex.: Toda exclusão registra obrigatoriamente usuário, timestamp e IP em log] | Cenário de aceitação automatizado de auditoria |
| **RNF-03** | Usabilidade / Interface | [Ex.: Formulário exibe feedback de sucesso e erro sem recarregar tela] | Teste E2E de interface |

---

## 5. Modelo Conceitual e Entidades Envolvidas

### 5.1. Entidades do Domínio Utilizadas
- **Entidades Reaproveitadas:** [Liste as entidades do `domain-model.md` consumidas sem alteração estrutural]
- **Entidades Novas ou Alteradas:** [Liste o que é criado ou estendido nesta funcionalidade]

### 5.2. Checklist de Suficiência de Entidades (Pré-Planejamento)
*A IA e os desenvolvedores devem checar estas 5 perguntas para cada entidade antes de avançar para o plano técnico:*
- [ ] **Identidade:** O que torna a instância única (chave de negócio / natural e chave substituta)?
- [ ] **Atributos:** Quais informações são obrigatórias (`NOT NULL`) e quais são opcionais?
- [ ] **Relacionamentos:** Quais as cardinalidades mínimas e máximas com outras entidades?
- [ ] **Regras & Invariantes:** Quais restrições de domínio, limites ou estados válidos aplicam-se?
- [ ] **Ciclo de Vida & Histórico:** A entidade sofre exclusão física ou lógica (*soft delete*)? Há necessidade de guardar histórico de alterações?

---

## 6. Regras de Negócio (RN)

- **RN-01:** [Regra de negócio mandatória, invariante ou cálculo de domínio]
- **RN-02:** [Restrição de acesso, validação de integridade ou bloqueio condicional]

---

## 7. Critérios de Aceite (Cenários BDD / Gherkin)

### Cenário 1: [Nome do Cenário de Sucesso / Caminho Feliz]
```gherkin
Dado [contexto inicial e pré-condições no sistema]
Quando [o ator executa a ação descrita]
Então [o sistema processa a operação com sucesso e gera o resultado observável]
E [o log de auditoria correspondente é registrado]
```

### Cenário 2: [Nome do Cenário de Regra de Negócio Violada / Exceção]
```gherkin
Dado [contexto inicial com restrição ativa]
Quando [o ator tenta executar uma ação inválida]
Então [o sistema rejeita a operação com código e mensagem informativa apropriada]
E [nenhum dado inconsistente é persistido no banco]
```

---

## 8. Casos de Borda e Tratamento de Exceções

- [Tratamento de timeout, concorrência ou arquivos corrompidos]
- [Tratamento de dados nulos ou limites de caracteres]

---

## 9. Questões em Aberto e Esclarecimentos (IA & Equipe)

> *Espaço destinado para a IA apontar inconsistências, suposições ou perguntas antes de produzir o plano técnico.*

- [ ] **Dúvida 1:** [Pergunta de esclarecimento sobre regra ou cardinalidade]
- [ ] **Dúvida 2:** [Pergunta sobre validação ou comportamento de borda]
