# Fase 7 — Tela inicial e histórico

## 1. Identificador e nome

- **ID:** `fase-7-tela-inicial-e-historico`
- **Nome:** Tela Inicial Unificada e Histórico Consolidado
- **Status:** `APPROVED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: tela inicial como resumo equilibrado de hoje + atalhos rápidos para Hábito e Tarefa; destino Histórico como área separada e dedicada (sem transformar a Home em feed infinito), consolidando hábitos, tarefas, treinos e cuidados capilares com total consistência local.

---

## 2. Problema e objetivo

Um painel inicial que tenta exibir todo o histórico sobrecarrega o usuário e dificulta o foco nas ações imediatas do dia. Ao mesmo tempo, um aplicativo de rotinas necessita de um local centralizado e confiável onde o usuário possa consultar sua consistência ao longo do tempo sem depender de conexão externa.

O objetivo da Fase 7 é implementar:
1. A **Tela Inicial (Início)** com resumo equilibrado das atividades de hoje e dois atalhos rápidos ("Criar Hábito" e "Criar Tarefa").
2. A **Tela de Histórico** separada e acessível via barra inferior, unificando os registros passados de hábitos, tarefas concluídas, treinos finalizados e cuidados capilares realizados.
3. Garantia de consistência estrita de dados: ações feitas na Home (ex: marcar hábito ou tarefa) refletem instantaneamente no banco e no Histórico.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção 6.5: Tela inicial com resumo equilibrado e ações rápidas; atalhos de criação rápida para hábito e tarefa; histórico em área separada; tema escuro.
  - Seção 7: Decisões aprovadas (Home equilibrada, histórico separado, consistência de dados).
- **`docs/02-USER-FLOWS.md`**:
  - Seção 2: Início (resumo de hábitos, resumo de tarefas abertas/atrasadas, indicação de treino em andamento, atalhos de criação).
  - Seção 7: Histórico (destino próprio, consulta de consistência, distinção de estados reais).
- **`docs/03-SDD.md`**:
  - Seção 8: Navegação e atalhos a partir da tela inicial.
  - Seção 7 e 10: Fluxo reativo MVVM e testes de consistência.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 7 — Tela inicial e histórico: resumo equilibrado, atalhos, tarefas atrasadas, estados, área de histórico separada e consistência.

---

## 4. Escopo

1. **Camada de Dados e Consolidação:**
   - Criação de use case ou repositório agregador (`HomeDashboardRepository` ou `GetHomeSummaryUseCase`) que observa via `Flow` os dados de hábitos do dia, tarefas abertas/atrasadas e rotina/treino ativo.
   - `HistoryRepository` ou `GetHistoryRecordsUseCase` que agrega registros históricos de hábitos, tarefas concluídas, treinos e cuidados capilares ordenados por data.
2. **Interface da Tela Inicial (`HomeScreen`):**
   - Bloco de Ações Rápidas em destaque superior: Botões de acesso direto "Novo Hábito" e "Nova Tarefa".
   - Card "Hábitos de Hoje": lista resumida com checkbox/botão de 1 toque para conclusão rápida.
   - Card "Tarefas Prioritárias": exibição de tarefas vencidas em destaque vermelho/ícone e tarefas do dia.
   - Card "Rotinas Ativas": indicação visual se houver treino em andamento (com botão direto "Continuar") ou próxima etapa capilar.
3. **Interface da Tela de Histórico (`HistoryScreen`):**
   - Filtros ou abas: "Todos", "Hábitos", "Tarefas", "Treinos", "Cuidados".
   - Itens de histórico ricos em detalhes com data civil e horário de conclusão.
   - Estado vazio amigável quando nenhum registro existir ainda.

---

## 5. Exclusões

- Não misturar o feed completo de histórico dentro da tela inicial (o PRD exige separação estrita).
- Não criar dashboards com gráficos matemáticos complexos ou cálculo de XP no MVP.
- Não usar rede para consultar relatórios externos.

---

## 6. Comportamentos esperados

1. **Visualização da Home:**
   - O usuário abre o app e vê de imediato o que precisa fazer hoje.
   - Se houver tarefa atrasada, ela ganha destaque prioritário na Home.
   - Se houver sessão de treino ativa, a Home exibe um banner chamativo com botão "Continuar treino".
2. **Atalhos Rápidos:**
   - Tocar em "+ Hábito" na Home abre diretamente o diálogo/fluxo de criação de hábito.
   - Tocar em "+ Tarefa" na Home abre diretamente o diálogo/fluxo de criação de tarefa.
3. **Acesso ao Histórico:**
   - Ao tocar no ícone "Histórico" na barra de navegação inferior, o usuário navega para a área de histórico dedicada.
   - Consegue filtrar por tipo de atividade e ver a linha do tempo das suas realizações.

---

## 7. Critérios de aceite verificáveis

1. **`AC-01`**: A tela inicial exibe atalhos funcionais para criação de hábito e tarefa que abrem os respectivos formulários.
2. **`AC-02`**: Tarefas atrasadas são listadas com prioridade no resumo da tela inicial acompanhadas de destaque visual acessível.
3. **`AC-03`**: Se houver um treino em andamento, a tela inicial exibe um card de alerta com botão de ação direta para retornar à sessão ativa.
4. **`AC-04`**: O destino "Histórico" na barra inferior apresenta a lista cronológica consolidada de atividades concluídas.
5. **`AC-05`**: Concluir um hábito ou tarefa na Home reflete de forma instantânea e idêntica na tela de Histórico e nas telas de Hábitos/Tarefas (consistência de fonte única da verdade).
6. **`AC-06`**: Todos os testes unitários e de integração de Home e Histórico passam via `./gradlew testDebugUnitTest`.

---

## 8. Persistência e dados

- Não introduz novas tabelas de banco; consome as tabelas já existentes (`habits`, `habit_occurrences`, `tasks`, `workout_sessions`, `haircare_occurrences`) garantindo que o SQLite/Room continue como a única fonte da verdade.

---

## 9. Dependências técnicas

- Repositórios das fases 2, 3, 4, 5 e 6.
- `NavigationState` para disparo de diálogos ou navegação entre abas.
- Jetpack Compose e Material 3 tema escuro.
