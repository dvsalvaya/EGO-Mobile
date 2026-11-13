# Fase 3 — Hábitos

## 1. Identificador e nome

- **ID:** `fase-3-habitos`
- **Nome:** Gestão e Registro de Hábitos com Frequência Flexível
- **Status:** `APPROVED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: modelo de frequência tipado (diário, dias da semana, X vezes por semana), separação estrita de `Habit` e `HabitOccurrence`, diálogo de transição na mudança de frequência e tratamento sem penalidade cega para ocorrências não cumpridas.

---

## 2. Problema e objetivo

O acompanhamento de hábitos frequentemente falha quando aplicativos impõem rigidez excessiva (marcando falhas sumárias sem contexto) ou apagam/alteram o histórico quando o usuário ajusta sua rotina.

O objetivo da Fase 3 é implementar o ciclo de vida completo de hábitos no EGO Mobile:
1. Criação e edição com frequências flexíveis.
2. Registro rápido e manual de conclusão.
3. Pausa e retomada claras, sem perda de consistência temporal.
4. Diálogo explícito sobre como tratar mudanças de frequência e ocorrências pendentes.
5. Apresentação visual coerente no tema escuro na tela de Hábitos, respeitando os padrões de UI/MVVM/Room do projeto.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção 6.1: Criar, editar, pausar, retomar e arquivar hábitos; frequência flexível; registro direto e manual de conclusão; histórico separado; diálogo ao mudar frequência; diálogo ao lidar com ocorrência não cumprida; hábitos pausados visualmente identificados.
  - Seção 7: Decisões aprovadas (frequência flexível, fuso `America/Sao_Paulo`, armazenamento 100% local, sem gamificação punitiva ou XP/moedas).
- **`docs/02-USER-FLOWS.md`**:
  - Seção 3: Fluxos detalhados de criação, registro de conclusão, pausar/retomar, alteração de frequência e tratamento de ocorrência não cumprida.
- **`docs/03-SDD.md`**:
  - Seção 5: Modelo de dados inicial para `Habit` e `HabitOccurrence`.
  - Seção 6: Regras de data/tempo usando tipos adequados e fuso `America/Sao_Paulo`.
  - Seção 7: Arquitetura unidirecional (UI Composable → ViewModel → Repository → Room/DAO).
  - Seção 10: Estratégia de testes para frequência, transições e persistência.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 3 — Hábitos: tarefas de criação, listagem, edição, pausa, registro, frequência flexível, histórico e testes.

---

## 4. Escopo

1. **Entidades e Persistência:**
   - Complementar `HabitEntity` se necessário e criar `HabitOccurrenceEntity` com chave estrangeira apontando para `HabitEntity` (`onDelete = CASCADE`).
   - Mapeadores bidirecionais entre entidades Room e modelos de domínio puro (`Habit`, `HabitOccurrence`, `FrequencyConfig`).
   - `HabitOccurrenceDao` com operações de inserção, atualização, consulta por hábito e por intervalo de datas.
   - Atualização do repositório `HabitRepository` para incluir ocorrências e transações atômicas de ciclo de vida.
2. **Regras de Negócio e Domínio:**
   - Suporte a frequências: Diária, Dias específicos da semana, ou Meta semanal (X vezes por semana).
   - Cálculo e geração determinística de ocorrências para a data atual / ciclo no fuso `America/Sao_Paulo`.
   - Lógica de mudança de frequência: aplicar a partir de hoje mantendo intocado o histórico passado.
   - Lógica de ocorrência não cumprida: permitir justificar/pular ou concluir retroativamente, sem classificar automaticamente como falha sumária.
   - Mudança de estado: Ativo, Pausado, Arquivado.
3. **Interface e ViewModel (Compose):**
   - `HabitsViewModel` gerenciando o estado da UI via `StateFlow<HabitsUiState>`.
   - `HabitsScreen`:
     - Lista organizada de hábitos ativos (com botão de conclusão rápida de 1 toque).
     - Seção recolhível ou visualmente diferenciada para hábitos pausados.
     - Botão flutuante ou de ação para criar hábito.
   - Diálogo/Tela de criação e edição de hábito (`HabitFormDialog` / Composable) com validação de campos.
   - Diálogo de tratamento de ocorrência pendente / não cumprida.
   - Diálogo confirmatório na alteração de frequência.

---

## 5. Exclusões

- Não sincronizar dados com nuvem ou servidores remotos.
- Não disparar notificações push ou alarmes do sistema Android (lembretes estão fora do MVP).
- Não criar sistemas de pontos, XP, moedas, níveis ou penalidades artificiais.
- Não misturar tarefas ou treinos dentro do repositório de hábitos (cada funcionalidade possui seu módulo).

---

## 6. Comportamentos esperados

1. **Criação de Hábito:**
   - Usuário fornece nome (obrigatório, não vazio) e descrição (opcional).
   - Seleciona frequência: Diária (todos os dias), Dias fixos (ex: Seg, Qua, Sex) ou X vezes na semana.
   - Ao salvar, o hábito é persistido no Room e passa a constar na lista imediatamente.
2. **Listagem e Estados Visuais:**
   - Hábitos ativos aparecem com destaque e indicador de progresso do dia/período.
   - Hábitos pausados têm badge/rótulo textual explícito e opacidade ou agrupamento diferenciado.
3. **Registro de Conclusão:**
   - Conclusão rápida: tocar no botão de check marca a ocorrência do dia civil como concluída com timestamp atual.
   - Registro manual: usuário pode abrir a ocorrência, adicionar nota opcional ou confirmar data retroativa.
4. **Pausa e Retomada:**
   - Pausar um hábito suspende a geração de ocorrências ativas sem apagar ou corromper qualquer ocorrência pregressa.
   - Retomar reativa a geração para as datas a partir da retomada.
5. **Alteração de Frequência:**
   - Ao editar a frequência de um hábito existente, o aplicativo exibe opções:
     - "Aplicar a partir de hoje" (mantém histórico e ocorrências anteriores inalteradas).
     - "Cancelar alteração".
   - O histórico anterior nunca é reescrito.
6. **Tratamento de Ocorrência Não Cumprida:**
   - Quando o usuário visualiza uma ocorrência do ciclo que não foi marcada no tempo previsto, o app oferece opções acionáveis:
     - Marcar como realizada (conclusão tardia).
     - Marcar como justificada/pulada (sem prejuízo à consistência).
     - Manter pendente.

---

## 7. Regras de negócio

1. **Validação de Entrada:**
   - Nome do hábito é obrigatório, trimmed, com comprimento entre 1 e 80 caracteres.
   - Descrição opcional limitada a 250 caracteres.
2. **Datas e Fuso Horário:**
   - Todas as datas civis de agendamento de ocorrência devem ser expressas em `LocalDate` ou epoch day correspondente ao fuso `America/Sao_Paulo`.
   - `completedAt` armazena timestamp UTC (`System.currentTimeMillis()`).
3. **Idempotência de Ocorrências:**
   - Para um dado hábito e data civil fixa, não pode haver ocorrências duplicadas geradas acidentalmente.
4. **Imutabilidade Histórica:**
   - Alterações em parâmetros do hábito (nome, frequência, descrição) só têm efeito `updatedAt` para frente; ocorrências já consolidadas retêm seus estados originais.

---

## 8. Critérios de aceite verificáveis

1. **`AC-01`**: Criação de hábito com nome e frequência válida persiste no banco Room e emite atualização no Flow de hábitos ativos.
2. **`AC-02`**: Tentativa de salvar hábito com nome vazio ou somente espaços exibe erro de validação e não persiste no banco.
3. **`AC-03`**: Marcar conclusão de 1 toque atualiza a ocorrência para `COMPLETED` com `completedAt > 0` e reflete imediatamente na UI.
4. **`AC-04`**: Hábitos pausados aparecem na seção/estado "Pausados" com indicador visual e textual, sem botão de check ativo.
5. **`AC-05`**: Retomar um hábito pausado retorna seu status para `ACTIVE` e restabelece a disponibilidade de conclusão para o dia.
6. **`AC-06`**: Ao alterar frequência de um hábito com histórico, a confirmação preserva 100% das ocorrências registradas em datas anteriores.
7. **`AC-07`**: A tela `HabitsScreen` lida elegantemente com estado vazio (nenhum hábito cadastrado) apresentando mensagem instrutiva e botão de criação.
8. **`AC-08`**: Todos os testes unitários de regras de domínio, DAO e ViewModel executam com sucesso via `./gradlew testDebugUnitTest`.

---

## 9. Persistência e dados

- Tabela `habits`: `id`, `name`, `description`, `frequencyType`, `frequencyValue`, `status`, `createdAt`, `updatedAt`.
- Tabela `habit_occurrences`: `id`, `habitId` (FK), `dateCivil` (String YYYY-MM-DD ou Long epoch day), `status` (PENDING, COMPLETED, SKIPPED), `completedAt` (Long?), `notes` (String?).
- Índices: `habit_occurrences(habitId, dateCivil)` com unicidade para evitar duplicatas.

---

## 10. Dependências e restrições técnicas

- Depende de Room 2.6.1 + KSP configurados na Fase 2.
- Injeção manual de dependências via repositório singleton/factory em `EgoDatabaseProvider` ou `HabitRepositoryProvider`.
- Respeitar Material 3 tema escuro já estabelecido.
- Nenhuma dependência externa de rede ou serviços do Google Play.
