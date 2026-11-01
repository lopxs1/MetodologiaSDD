# Diretrizes de Design System: AMDE Digital

> **Status:** Consolidado  
> **Versão:** 1.0.0  
> **Data:** 2026-11-01  
> **Arquivo de Tokens:** [`design/tokens.json`](../../design/tokens.json)  
> **Biblioteca Base:** Tailwind CSS + Shadcn/ui (Radix UI)

---

## 1. Princípios Visuais e Identidade Institucional

A interface do **AMDE Digital** foi concebida para transmitir confiança, clareza clínica e eficiência operacional no atendimento multidisciplinar de crianças e adolescentes com TEA.

1. **Simplicidade e Clareza:** Menos sobrecarga cognitiva para recepcionistas, psicólogos, fonoaudiólogos e médicos.
2. **Uso Exclusivo de Tokens:** Todo componente visual deve consumir as classes Tailwind configuradas a partir de `design/tokens.json`. Nenhuma cor hexadecimal arbitrária deve ser declarada diretamente no código.
3. **Consistência Semântica de Cores:**
   - **Azul Primário (`#1A5276`):** Identidade institucional da AMDE, cabeçalhos, botões primários de ação ("Salvar", "Cadastrar", "Entrar").
   - **Verde Sucesso (`#16A34A`):** Consultas com status `Concluído`, presenças confirmadas e badge do perfil `Paciente`.
   - **Azul Informativo (`#2563EB`):** Consultas com status `Agendado` e badge do perfil `Médico/Funcionário`.
   - **Laranja/Âmbar Alerta (`#D97706`):** Consultas do tipo `Retorno` ou `Avaliação`.
   - **Vermelho Perigo (`#DC2626`):** Consultas com status `Cancelado`, ações destrutivas (excluir usuário/documento) e badge do perfil `Administrador`.

---

## 2. Padrões Estruturais de Telas

O sistema adota o padrão de **Painel Administrativo com Barra Lateral de Navegação (Sidebar)**:
- **Menu Lateral Fixo:**
  - `Visão Geral / Dashboard` (Gráficos e contadores gerenciais)
  - `Pacientes` (Lista, busca, visualização de prontuários)
  - `Agendamentos` (Calendário e listagem de consultas)
  - `Usuários` (Gestão exclusiva do Administrador)
  - `Sair / Logout` (Encerramento de sessão)
- **Cabeçalho Superior (Header):**
  - Identificação da Instituição ("AMDE Administração")
  - Informações do usuário logado (Nome, Perfil de acesso e botão de perfil)

---

## 3. Padrões de Componentes e Formulários

1. **Campos de Formulário:**
   - Rótulo textual acima do campo com indicação de obrigatoriedade (`*`).
   - Validação inline ao desfocar o campo (*onBlur*) e bloqueio do botão de submissão enquanto houver erros.
   - Mensagens de erro em texto vermelho claro com explicação da regra violada (ex.: *"E-mail já cadastrado"*, *"A data inicial deve anteceder a data final"*).
2. **Tabelas de Dados:**
   - Cabeçalhos com ordenação indicada por setas (`ASC`/`DESC`).
   - Linha de busca textual no topo com filtro por nome e e-mail.
   - Paginação na base da tabela.
   - Coluna fixa de ações com ícones padronizados (Visualizar Prontuário, Editar, Excluir).
3. **Modais e Diálogos:**
   - Modais centralizados para edição rápida (ex.: prontuário do paciente, novo agendamento).
   - Diálogo de confirmação com fundo escurecido (*backdrop*) para ações de exclusão.
4. **Estados Obrigatórios:**
   - **Loading:** Esqueletos (*Skeleton*) simulando o formato da tabela/card.
   - **Vazio:** Ícone ilustrativo acompanhado de texto explicativo (ex.: *"Nenhum agendamento encontrado para esta data"*).
   - **Erro:** Alerta com opção de recarregar a requisição.

---

## 4. Acessibilidade (WCAG 2.1 AA) e Proteção de Dados

- **Contraste de Cores:** Relação mínima de contraste de 4.5:1 para textos normais sobre fundos.
- **Operação por Teclado:** Foco visualmente perceptível em botões e campos de entrada; fechamento de modais com tecla `Esc`.
- **Proteção Visual de Dados Pessoais:** CPFs e telefones devem ser renderizados na listagem com caracteres ocultos (ex.: `***.456.789-**`), em conformidade com a Constituição do Projeto e a LGPD.

