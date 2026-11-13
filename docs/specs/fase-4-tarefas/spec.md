# Fase 4 — Tarefas

## 1. Identificador e nome

- **ID:** `fase-4-tarefas`
- **Nome:** Gestão de Tarefas, Prazos Opcionais e Reagendamento
- **Status:** `IN_PROGRESS`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: prazo estritamente opcional, tarefas concluídas ocultas da visualização padrão por padrão, destaque duplo em atrasadas (vermelho + ícone/texto acessível), confirmação obrigatória de exclusão com ação "Desfazer" via Snackbar.

---

## 2. Problema e objetivo

O gerenciamento de afazeres diários requer clareza entre o que possui data limite real e o que é pontual sem prazo fixo. Além disso, tarefas vencidas não devem se tornar um ponto de frustração opaco, mas sim oferecer reagendamento simples.

O objetivo da Fase 4 é implementar o ciclo de vida completo de tarefas no EGO Mobile:
1. Criação e edição de tarefas com título obrigatório e prazo opcional.
2. Destaque acessível para tarefas em atraso (cor vermelha + texto/ícone explícito).
3. Conclusão ágil, ocultando tarefas concluídas da visualização ativa padrão, com opção de consulta de concluídas.
4. Reagendamento de tarefas vencidas.
5. Confirmação de exclusão protegida e suporte à ação de desfazer (Undo).

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção 6.2: Criar, editar, concluir e excluir tarefas; prazo opcional; tarefas atrasadas em destaque vermelho; confirmação de exclusão; desfazer exclusão; tarefas concluídas ocultas por padrão; reagendamento de tarefas atrasadas.
  - Seção 7: Decisões aprovadas (prazo opcional; destaque duplo para atrasadas; exclusão com confirmação e desfazer).
- **`docs/02-USER-FLOWS.md`**:
  - Seção 4: Fluxos de criar tarefa, concluir tarefa, reagendar tarefa atrasada e excluir tarefa.
  - Seção 8: Confirmação em ações destrutivas e preservação de dados para desfazer.
- **`docs/03-SDD.md`**:
  - Seção 5: Modelo de dados inicial de `Task` (`id`, `title`, `description`, `dueAt`, `status`, `createdAt`, `completedAt`).
  - Seção 6: Regras de data/tempo usando fuso `America/Sao_Paulo`.
  - Seção 10: Estratégia de testes para prazos, filtros e operações de exclusão.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 4 — Tarefas: itens de criação, edição, prazo opcional, ocultar concluídas, destaque de atraso, reagendamento, exclusão e testes.

---

## 4. Escopo

1. **Entidades e Persistência:**
   - Criação da tabela Room `tasks` com campos `id`, `title`, `description`, `dueAt`, `status`, `createdAt`, `updatedAt`, `completedAt`.
   - `TaskEntity`, `TaskMapper` e `TaskDao` com consultas reativas (`Flow`) para tarefas abertas, tarefas concluídas e tarefas atrasadas.
   - `TaskRepository` com operações de CRUD, conclusão, reagendamento e exclusão atômica.
2. **Regras de Negócio e Domínio:**
   - Validação de título (obrigatório, 1 a 100 caracteres).
   - Classificação de atraso: tarefa aberta com `dueAt != null` e `dueAt < agora` no fuso `America/Sao_Paulo`. Tarefas sem prazo (`dueAt == null`) nunca são classificadas como atrasadas.
   - Lógica de reagendamento: atualização de `dueAt` com novo instante/data.
   - Lógica de exclusão com restauração temporária (Soft-delete / cache de desfazer).
3. **Interface e ViewModel (Compose):**
   - `TasksViewModel` expondo `TasksUiState`.
   - `TasksScreen`:
     - Lista principal exibindo apenas tarefas abertas (ordenadas por prazo: atrasadas primeiro, depois com prazo próximo, depois sem prazo).
     - Toggle ou filtro para "Exibir concluídas".
     - Destaque visual acessível em tarefas atrasadas (borda/fundo avermelhado acompanhado do selo "Atrasada" e ícone de aviso).
     - Checkbox/Ação de 1 toque para conclusão.
   - Diálogo/Formulário de criação e edição com DatePicker opcional.
   - Diálogo de confirmação para exclusão ("Tem certeza que deseja excluir esta tarefa?").
   - Snackbar com botão "Desfazer" ao excluir.

---

## 5. Exclusões

- Não sincronizar tarefas com Google Tasks ou calendários externos.
- Não disparar notificações locais do sistema operacional (fora do escopo do MVP).
- Não criar subtarefas infinitas ou dependências hierárquicas complexas entre tarefas.

---

## 6. Comportamentos esperados

1. **Criação de Tarefa:**
   - Usuário informa título (obrigatório). Descrição e prazo são opcionais.
   - Ao salvar, a tarefa aparece imediatamente na lista ativa.
2. **Conclusão:**
   - Ao marcar como concluída, a tarefa é imediatamente removida da lista padrão de abertas e salva como `COMPLETED`.
   - Se o usuário alternar para "Exibir concluídas", a tarefa aparece tachada e com data de conclusão.
3. **Identificação de Atraso:**
   - Se o prazo expirou e a tarefa não está concluída, o item exibe distintivo "Atrasada", ícone de alerta e estilização visual avermelhada.
4. **Reagendamento:**
   - A partir dos detalhes ou menu da tarefa atrasada, o usuário escolhe reagendar, seleciona novo prazo e a tarefa perde o estado de atrasada caso a nova data seja futura.
5. **Exclusão e Desfazer:**
   - Ao tocar em excluir, o app exibe diálogo modal de confirmação.
   - Se confirmado, a tarefa é removida da visualização e um Snackbar aparece: "Tarefa excluída. [DESFAZER]".
   - Tocar em "Desfazer" restaura imediatamente a tarefa para a lista com seu estado anterior.

---

## 7. Regras de negócio

1. **Validação:**
   - Título não pode ser em branco (1 a 100 caracteres). Descrição até 300 caracteres.
2. **Prazos:**
   - `dueAt` armazena timestamp em milissegundos UTC. O fuso de avaliação para data limite é `America/Sao_Paulo`.
   - `dueAt` nulo significa tarefa sem prazo determinado (perene).
3. **Estados de Tarefa:**
   - `TaskStatus`: `OPEN`, `COMPLETED`.

---

## 8. Critérios de aceite verificáveis

1. **`AC-01`**: Criação de tarefa com título e sem prazo persiste com sucesso com `dueAt = null`.
2. **`AC-02`**: Criação de tarefa com prazo futuro persiste com o timestamp correto e não é classificada como atrasada.
3. **`AC-03`**: Tarefa com prazo anterior ao momento atual e status `OPEN` é marcada como atrasada com destaque duplo (cor vermelha e selo textual/ícone).
4. **`AC-04`**: Concluir uma tarefa altera seu status para `COMPLETED`, preenche `completedAt` e a oculta da lista padrão de tarefas ativas.
5. **`AC-05`**: Alternar o filtro para exibir tarefas concluídas exibe a tarefa concluída com indicação temporal de quando foi finalizada.
6. **`AC-06`**: Reagendar uma tarefa vencida para uma data futura atualiza seu `dueAt` e remove o estado de atraso.
7. **`AC-07`**: Solicitação de exclusão exibe diálogo de confirmação. Ao confirmar, exibe Snackbar com ação "Desfazer" que, se acionada dentro do tempo hábil, recupera a tarefa.
8. **`AC-08`**: Todos os testes unitários de regras, DAO, repositório e ViewModel executam com sucesso via `./gradlew testDebugUnitTest`.

---

## 9. Persistência e dados

- Tabela `tasks`:
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `title` (TEXT NOT NULL)
  - `description` (TEXT)
  - `dueAt` (INTEGER)
  - `status` (TEXT NOT NULL: 'OPEN', 'COMPLETED')
  - `createdAt` (INTEGER NOT NULL)
  - `updatedAt` (INTEGER NOT NULL)
  - `completedAt` (INTEGER)
- Índices em `status` e `dueAt` para performance em consultas reativas.

---

## 10. Dependências técnicas

- Room 2.6.1 + KSP.
- Material 3 (AlertDialog, Snackbar, Checkbox, DatePicker).
- `EgoDatabase` e `TaskDao`.
