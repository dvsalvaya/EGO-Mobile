# FIX.md — Relatório de QA (auditoria histórica das Specs 1–8)

> **Revisão em 18/11/2026:** este arquivo registra uma auditoria feita em 14–16/11/2026 no commit `7aa20d4`; não deve ser lido como retrato validado do estado atual. A especificação da Fase 9 ainda está em `DRAFT`, e as tarefas 04, 08 e 09 foram executadas parcialmente depois daquela auditoria. Na árvore atual, `.\gradlew.bat testDebugUnitTest assembleDebug` falha em `:app:compileDebugKotlin` com erros de compilação em navegação, tema e telas; portanto, não foi possível validar os fluxos de UI atuais em execução.

## Situação na revisão atual

| Item | Situação atual | Observação |
|---|---|---|
| BUG-001–BUG-003, BUG-005, BUG-008–BUG-014 | **Achados históricos, não revalidados** | A auditoria original tem evidências no commit e ambiente indicados acima. Como a compilação atual falha, não afirmar que esses comportamentos ainda reproduzem ou foram corrigidos sem nova validação. |
| BUG-004 (`java.time` / `minSdk 24`) | **Ainda plausível por inspeção** | `minSdk` continua 24 e `coreLibraryDesugaring` não aparece em `app/build.gradle.kts`; lint e execução em API 24/25 precisam ser revalidados após corrigir a compilação. |
| BUG-007 (`3x por semana`) | **Ainda plausível por inspeção** | `HabitFrequency.TimesPerWeek.requiresOccurrenceOn` ainda limita a ocorrência aos primeiros dias ISO da semana; conferir a regra de frequência antes de alterar. |
| BUG-006 (prazo hoje já atrasado) | **Remover da lista de bugs** | A proposta da Fase 9 preserva `Task.isOverdue(now)` como `dueAt < agora`, inclusive para prazo salvo no início do dia. A seção 8.3 e o critério AC-07 da `docs/specs/fase-9-ui-ux-identidade-visual/spec.md` tratam esse resultado como intencional. Como a proposta está em `DRAFT`, não é uma aprovação formal da fase; ainda assim, o comportamento está explicitado na especificação e nas tarefas atuais. Não mudar a regra sem revisão do produto. |
| Navegação e tema da Fase 9 | **Divergência atual confirmada por inspeção** | `AppNavigation.kt` ainda mantém cinco itens com Histórico fora da barra, mas renderiza iniciais invisíveis em vez de ícones; `EgoApp.kt` ainda aplica `darkColorScheme()` sem tokens explícitos. Além da divergência visual, há erros de compilação nesses arquivos. |
| Suíte e build atuais | **Falham** | A execução da revisão parou em `compileDebugKotlin`; tarefas de teste/build não chegaram a validar o app. Consulte a saída do Gradle desta revisão antes de usar os resultados históricos abaixo. |

As seções seguintes preservam o registro e as evidências da auditoria original. Prioridades, contagens, testes aprovados/não realizados e ordem de correção referem-se exclusivamente ao snapshot de 14–16/11/2026. Não use a ordem antiga como backlog atualizado sem conferir a especificação vigente.

## 1. Resumo

- **Data da auditoria:** 14/11/2026 a 16/11/2026
- **Commit testado:** `7aa20d4` — "sepcs 7 e 8"
  - Árvore de trabalho com alterações já existentes em `docs/` e `docs/specs/fase-9-ui-ux-identidade-visual/` (não tocadas pela auditoria).
- **Ambiente:**
  - Windows 11 / PowerShell
  - Emulador `Medium_Phone_API_37.0` (Android API 37, 1080×2400), dispositivo `emulator-5554`
  - adb: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`
  - APK testado: `app\build\outputs\apk\debug\app-debug.apk` (debug)
  - Observação de ambiente: o pacote `com.ego.mobile` desapareceu intermitentemente do `pm list` (registro `deletePackageX`), aparentemente por interferência de `pm clear`/instrumentação durante os testes; o workaround foi reinstalar com `adb install -r` (sem `pm clear`). Isso **apagou os dados locais do emulador** em algum ponto do teste (histórico de treinos/hábitos anteriores sumiram) — comportamento do ambiente de teste, não do app.
- **Resultados de build/teste:**
  - `gradlew.bat test` → **BUILD SUCCESSFUL** — **98 testes unitários em 27 classes, 0 falhas, 0 erros**
  - `gradlew.bat assembleDebug` → **BUILD SUCCESSFUL**
  - `gradlew.bat lint` → **FAILED — 109 erros, 8 avisos** (todos `NewApi`: `java.time`/`ZoneId` com `minSdk = 24` e sem core library desugaring; primeiro erro em `GetHomeSummaryUseCase.kt:28`; relatório em `app\build\intermediates\lint_intermediate_text_report\debug\lintReportDebug\lint-results-debug.txt`)
- **Contagem de problemas confirmados:** **14 bugs**
  - **P0 (crash/perda de dados/inutilizável): 1**
  - **P1 (funcionalidade quebrada/dados errados): 3**
  - **P2 (comportamento incorreto/usabilidade): 9**
  - **P3 (menor): 1**
- **Suspeitas (não 100% confirmadas): 8** · **Nenhuma correção foi aplicada** (auditoria somente leitura; único arquivo criado: este `FIX.md`).
- **Screenshots/evidências:** `C:\Users\sal\AppData\Local\Temp\ego_*.png` e Logcat (`adb logcat -b crash`).

---

## 2. Problemas encontrados

### BUG-001 — Crash ao ver histórico/concluir treino (LazyColumn aninhado)

- **Prioridade:** P0
- **Categoria:** Crash / UI
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\routines\workouts\components\WorkoutHistoryView.kt:56` (dentro de `WorkoutScreen.kt:138`)
- **Descrição:** `WorkoutHistoryView` usa um `LazyColumn` próprio e é exibido dentro de um `item {}` de outro `LazyColumn` (o `LazyColumn` externo de `WorkoutScreen`), produzindo constraint de altura infinita e crash fatal.
- **Esperado:** O histórico de treinos (Spec 7 — histórico/visões de treino) é exibido normalmente com 1 ou mais treinos concluídos.
- **Reprodução:**
  1. Abrir o app → aba **Rotinas** → iniciar um treino (ou usar o treino ativo).
  2. Tocar **Finalizar** → **Sim, Finalizar** (ou abrir a visão de histórico com ≥1 treino concluído).
- **Observado:** O app fecha imediatamente e volta ao launcher. Reproduzido **4×** no total (3× em 14/11 ao abrir o histórico com treinos concluídos; 1× em 16/11 16:22:37 logo após finalizar o treino).
- **Evidência:** Logcat `-b crash` (16/11 16:22:37):
  `java.lang.IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints, which is disallowed...` — `InlineClassHelperKt.throwIllegalStateException(InlineClassHelper.kt:26)`; processo `com.ego.mobile`, PID 21075. Screenshots: `ego_fin1.png`, `ego_fin2.png`.
- **Causa:** `LazyColumn` (WorkoutHistoryView.kt:56) aninhado dentro de `item {}` do `LazyColumn` de `WorkoutScreen.kt:138`; o pai fornece altura infinita ao filho.
- **Sugestão:** Remover o `LazyColumn` interno (usar `Column` simples, já que o container é rolável) ou mover o histórico para um item único do `LazyColumn` externo.

---

### BUG-002 — Crash ao criar hábito com nome > 80 caracteres (aba Hábitos)

- **Prioridade:** P1
- **Categoria:** Crash / Validação
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitsViewModel.kt:91-104` (chamada em `HabitsScreen.kt:163`, via `HabitFormDialog.kt:85`) → `domain\model\Habit.kt:20`
- **Descrição:** `HabitsViewModel.onCreateHabit` constrói `Habit(...)` sem `try/catch`; o `require(name.trim().length in 1..80)` lança `IllegalArgumentException` não tratada dentro da coroutine e derruba o app. O formulário (`HabitFormDialog`) não impõe limite de caracteres no campo Nome. (O mesmo diálogo na **Home** é protegido — `HomeViewModel.onCreateHabit` tem `catch (IllegalArgumentException)` — o que mostra a inconsistência.)
- **Esperado:** Validação inline (erro no campo) ou mensagem de erro sem crash (Spec 3, AC-02 — exibição de erro de validação).
- **Reprodução:** Aba **Hábitos** → FAB **+** → Nome com **81+ caracteres** → **Salvar**.
- **Observado:** O app fecha imediatamente.
- **Evidência:** Logcat `-b crash` (14/11): `java.lang.IllegalArgumentException: Habit name must be 1 to 80 characters.` em `Habit.<init>(Habit.kt:20)` ← `HabitsViewModel.onCreateHabit(HabitsViewModel.kt:94)`.
- **Causa:** Falta de validação na UI + `Habit(...)` sem captura de exceção no caminho da aba Hábitos.
- **Sugestão:** Validar no formulário (limitar/erro inline) e envolver a criação em `try/catch` como já faz a Home.

---

### BUG-003 — Crash ao criar hábito com descrição > 250 caracteres (aba Hábitos)

- **Prioridade:** P1
- **Categoria:** Crash / Validação
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitFormDialog.kt:52-57,85` → `HabitsViewModel.kt:94` → `domain\model\Habit.kt:21`
- **Descrição:** O campo "Descrição (opcional)" não tem limite e o domínio exige `description.length <= 250`; o excesso lança `IllegalArgumentException` não tratada e derruba o app.
- **Esperado:** Campo limitado a 250 caracteres ou erro de validação sem crash.
- **Reprodução:** Aba **Hábitos** → FAB **+** → Nome qualquer (ex.: `DescTeste`) → Descrição com **251+ caracteres** (ex.: colar/ digitá-la 2×) → **Salvar**.
- **Observado:** O app fecha imediatamente, voltando ao launcher.
- **Evidência:** Logcat `-b crash` (14/11 15:58:10):
  `java.lang.IllegalArgumentException: Habit description must be 250 characters or less.` ← `Habit.<init>(Habit.kt:21)` ← `HabitsViewModel$onCreateHabit$1.invokeSuspend(HabitsViewModel.kt:94)` ← `HabitsScreenKt.HabitsScreen$lambda$28$lambda$27(HabitsScreen.kt:163)` ← `HabitFormDialogKt.HabitFormDialog$lambda$12$lambda$11$lambda$10(HabitFormDialog.kt:85)`. Screenshot do formulário preenchido: `ego_desc5.png`.
- **Causa:** Sem `maxLength`/validação na UI e sem `try/catch` no ViewModel do caminho da aba Hábitos.
- **Sugestão:** `limit = 250` no `OutlinedTextField` + validação inline + `try/catch`.

---

### BUG-004 — `gradlew lint` falha com 109 erros `NewApi` (java.time com minSdk 24)

- **Prioridade:** P1
- **Categoria:** Build / Compatibilidade
- **Arquivo:** `app\build.gradle.kts:13` (`minSdk = 24`, sem `coreLibraryDesugaring`) — código afetado: `GetHomeSummaryUseCase.kt:28` (primeiro erro) e dezenas de outros (`HomeViewModel`, `TasksScreen`, `HabitFormDialog`, `HaircareRepositoryImpl`, `WorkoutHistoryView`, etc. usam `java.time`/`ZoneId`)
- **Descrição:** O app usa `java.time` (requer API 26) com `minSdk = 24` e sem desugaring. O gate `lint` falha com **109 erros** `NewApi`.
- **Esperado:** `gradlew lint` aprovado e app compatível com todo o intervalo `minSdk..targetSdk`.
- **Reprodução:** `.\gradlew.bat lint` no repositório.
- **Observado:** `BUILD FAILED — 109 errors, 8 warnings`, todos `NewApi`.
- **Evidência:** `app\build\intermediates\lint_intermediate_text_report\debug\lintReportDebug\lint-results-debug.txt`.
- **Causa:** Ausência de `coreLibraryDesugaringEnabled` + dependência `coreLibraryDesugaring`, ou `minSdk` abaixo de 26.
- **Sugestão:** Habilitar desugaring (mantendo `minSdk 24`) ou subir `minSdk` para 26; rodar `lint` como etapa de CI. **Observação:** em aparelhos Android 7.0/7.1 (API 24/25) o app provavelmente falharia com `NoClassDefFoundError` em `java.time` (não reproduzido em dispositivo real — ver "Testes não realizados").

---

### BUG-005 — Cronômetro do treino continua contando com o treino PAUSADO

- **Prioridade:** P2
- **Categoria:** Lógica de domínio / Dados incorretos
- **Arquivo:** `app\src\main\java\com\ego\mobile\domain\model\WorkoutSession.kt:37-43` (`durationMillis()`)
- **Descrição:** `durationMillis()` calcula `now - startedAt` e ignora o estado `PAUSED` (não há `pausedAt`/acúmulo de tempo pausado). O tempo decorrido exibido — e o tempo final salvo no histórico — inclui o período pausado.
- **Esperado:** Com o treino pausado, "Tempo decorrido" congela; o tempo total não conta pausas (Spec 7 — sessão de treino com pausa/retomada).
- **Reprodução:** Rotinas → iniciar treino → **Pausar** → observar o cartão "Tempo decorrido" por ~10 s.
- **Observado:** Estado exibe **PAUSADO** (badge vermelho) e o timer continua subindo: `35:17` → `35:26` em ~9 s com o treino pausado. Screenshots: `ego_paused.png` (35:17) e `ego_paused2.png` (35:26).
- **Causa:** `durationMillis()` não considera `status == PAUSED` nem acumula pausas.
- **Sugestão:** Persistir `pausedAt`/`pausedDurationMillis` e congelar o cálculo enquanto pausado.

---

### BUG-006 — (Histórico) Tarefa com prazo "hoje" nasce marcada como Atrasada

> **Reclassificado em 18/11/2026:** este achado não é um bug segundo a regra registrada na proposta da Fase 9: `dueAt < agora` é mantido, mesmo quando a data civil escolhida é hoje. Veja a situação atual no início deste arquivo. A sugestão de mudar para comparação por dia civil está obsoleta e não deve ser aplicada sem revisão explícita do produto.

- **Prioridade:** P2
- **Categoria:** Lógica de datas / Dados incorretos
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\tasks\components\TaskFormDialog.kt:166` (`toDueAt` = `atStartOfDay`) + regra de `isOverdue` em `domain\model\Task`
- **Descrição:** Escolher uma data no formulário armazena `dueAt` à **meia-noite (00:00)** do dia escolhido. Como "agora" é sempre posterior a 00:00, a tarefa criada para **hoje** já é considerada atrasada no mesmo dia.
- **Esperado:** Tarefa com prazo no dia atual só fica atrasada após o fim do dia (Spec 4, AC-06/fluxo de atraso).
- **Reprodução:** Tarefas → **+** → título qualquer → Prazo = **hoje** → Salvar.
- **Observado:** O cartão exibe imediatamente o badge **"⚠ Atrasada"** em vermelho (screenshot `ego_task_overdue.png`, tarefa criada em 14/11 com prazo 14/11/2026).
- **Causa:** `dueAt` armazenado como `LocalDate.atStartOfDay` e comparado com `System.currentTimeMillis()` sem normalizar "hoje".
- **Sugestão:** Considerar atraso apenas se `dueAt < hoje (início do dia de ontem/hoje)` — i.e., comparar dias civis, ou armazenar o fim do dia.

---

### BUG-007 — Hábito "3x por semana" só gera ocorrência seg/ter/qua

- **Prioridade:** P2
- **Categoria:** Lógica de domínio
- **Arquivo:** `app\src\main\java\com\ego\mobile\domain\model\HabitFrequency.kt:30-33`
- **Descrição:** `TimesPerWeek.requiresOccurrenceOn` usa `date.dayOfWeek.value <= count` (ISO: seg=1 … dom=7). Para `count = 3`, o hábito só é previsto **segunda, terça e quarta**; nos demais dias da semana (qui–dom) não aparece, desperdiçando 4/7 dias e concentrando tudo no início da semana.
- **Esperado:** Ocorrências distribuídas em 3 dias da semana (qualquer 3 dias), conforme "3x por semana" (Spec 3 — frequência flexível).
- **Reprodução:** Criar hábito "Academia_3x" com frequência **3x/sem** → abrir a **Home** numa **sábado** (14/11/2026).
- **Observado:** Home exibe **"Nenhum hábito previsto para hoje"** (screenshot `ego_home_habits3x.png`); o hábito não é listado em qua, qui, sex, sáb, dom.
- **Causa:** `weekWindow <= count` amarra os dias aos primeiros dias ISO da semana.
- **Sugestão:** Definir/escolher os dias (ex.: domingo como início da semana + escolha do usuário, ou dias sorteados/registrados) e persistir os dias junto da frequência.

---

### BUG-008 — Salvar hábito com nome vazio não faz nada e não exibe erro

- **Prioridade:** P2
- **Categoria:** Validação / UX (viola AC-02)
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitFormDialog.kt:84`
- **Descrição:** O botão **Salvar** só chama `onSave` se `name.isNotBlank()`; caso contrário, **silenciosamente não faz nada** — sem mensagem, sem destaque de erro, diálogo continua aberto sem feedback.
- **Esperado:** Spec 3, **AC-02**: "Tentativa de salvar hábito com nome vazio ou somente espaços **exibe erro de validação** e não persiste no banco."
- **Reprodução:** Aba **Hábitos** → FAB **+** → deixar Nome vazio → **Salvar**.
- **Observado:** Nenhuma mudança visível; nenhuma mensagem de erro (o diálogo apenas permanece aberto, sem indicação do motivo).
- **Evidência:** Código `HabitFormDialog.kt:83-87` (`if (name.isNotBlank()) { onSave(...) }` — sem `else`).
- **Causa:** Falta do ramo de erro/estado `isError` no campo.
- **Sugestão:** Exibir erro inline no campo e desabilitar/mostrar feedback ao salvar.

---

### BUG-009 — Chips de frequência ignoram o estado `selected` (nenhuma seleção visível)

- **Prioridade:** P2
- **Categoria:** UX / Visual
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitFormDialog.kt:100-109`
- **Descrição:** `FrequencyChoiceChip` recebe `selected` mas **não o utiliza** (renderiza sempre o mesmo `OutlinedButton`). O usuário não vê qual frequência está ativa ("Diária", "Semanal", "3x/sem" parecem idênticos) e pode salvar sem perceber a seleção vigente.
- **Esperado:** A frequência selecionada destacada visualmente (chip preenchido/checked).
- **Reprodução:** Aba **Hábitos** → FAB **+** → observar os chips de Frequência; tocar em "Semanal" e comparar com "Diária".
- **Observado:** Nenhuma diferença visual entre selecionado e não selecionado (screenshot `ego_habform3.png`).
- **Evidência:** `FrequencyChoiceChip` ignora o parâmetro `selected` no `content`.
- **Causa:** Estado recebido e não aplicado na UI.
- **Sugestão:** Usar `FilterChip`/`SelectedButton` com cor/borda de destaque quando `selected = true`.

---

### BUG-010 — Aba Hábitos não indica hábito concluído hoje

- **Prioridade:** P2
- **Categoria:** UX / Requisito (Spec 3: "indicador de progresso do dia")
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitsScreen.kt:170-262` (`HabitCard` não consulta a ocorrência do dia)
- **Descrição:** O cartão do hábito na aba **Hábitos** é idêntico antes e depois da conclusão do dia: mesmo botão ✓ roxo, sem estado "Concluído hoje", sem progresso do dia. Só a **Home** reflete a conclusão (mostra "Concluído"). Tocar de novo re-grava a mesma ocorrência sem feedback (não há desfazer).
- **Esperado:** Spec 3 — "Hábitos ativos aparecem com destaque e **indicador de progresso do dia/período**"; AC-03 — conclusão "reflete imediatamente na UI".
- **Reprodução:**
  1. Concluir "Ler_livro" (Home → botão **Concluir**, ou ✓ na aba Hábitos).
  2. Conferir Home: exibe **"Concluído"** ✓.
  3. Conferir aba **Hábitos**: cartão **inalterado** (mesmo ✓) — `ego_quickcomplete.png` vs `ego_home_after_complete.png`.
- **Observado:** Sem distinção visual entre hábito concluído e não concluído na aba Hábitos.
- **Causa:** `HabitCard` recebe apenas o `Habit`, não a `HabitOccurrence` de hoje.
- **Sugestão:** Passar a ocorrência do dia e exibir estado "Concluído ✓" (desabilitado/checked) + permitir desmarcar.

---

### BUG-011 — Botão "Pausar" é inalcançável (hábito nunca pode ser pausado pela UI)

- **Prioridade:** P2
- **Categoria:** Lógica de UI / Viola AC-04 e AC-05
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitsScreen.kt:241-258`
- **Descrição:** No `HabitCard`, a primeira condição `if (habit.status == ACTIVE && onQuickComplete != null)` **sempre vence** para hábitos ativos (a lista ativa sempre passa `onQuickComplete != null`, linha 119). O ramo `else if (status == ACTIVE && onPause != null)` (linhas 254-258) é **código morto** — nunca exibido. Consequência: não é possível pausar nenhum hábito pela interface; a seção "Pausados" (AC-04) e o retomar (AC-05) tornam-se inalcançáveis.
- **Esperado:** Spec 3 — pausar/retomar hábitos (Seção 6.1, AC-04, AC-05).
- **Reprodução:** Aba **Hábitos** → observar os cartões de hábitos ativos: todos mostram apenas **✓**; não há botão "Pausar" em lugar algum da UI.
- **Observado:** Nenhuma via de pausa existe (confirmado por inspeção e por UI); seção "Pausados" nunca aparece porque nada pode ser pausado.
- **Evidência:** `HabitsScreen.kt:241` — condição sempre verdadeira para o ramo ativo; ramo das linhas 254-258 nunca alcançável.
- **Causa:** Ordem das condições no `if/else if` sem considerar o callback de pausa para hábitos ativos.
- **Sugestão:** Exibir ações secundárias (menu/"Pausar") além do ✓ rápido, e tratar os três estados corretamente.

---

### BUG-012 — Rotacionar o aparelho reseta a navegação para a Home

- **Prioridade:** P2
- **Categoria:** UX / Estado
- **Arquivo:** `app\src\main\java\com\ego\mobile\app\navigation\AppNavigation.kt:23`
- **Descrição:** `navigationState` é criado com `remember { ... }` (não `rememberSaveable`/não ligado a `SavedStateHandle`). Na rotação (recriação da Activity) todo o estado de navegação é perdido e o usuário volta para a aba **Início**, perdendo a tela em que estava.
- **Esperado:** Manter a aba/tela atual após rotação.
- **Reprodução:** Ir para a aba **Tarefas** → rotacionar o aparelho (`user_rotation 1`).
- **Observado:** Após a rotação o app exibe a aba **Início** com o layout em paisagem (screenshot `ego_rot.png`).
- **Causa:** Estado de navegação não sobrevive a `onSaveInstanceState`.
- **Sugestão:** `rememberSaveable` ou `rememberNavController`/`SavedStateHandle`.

---

### BUG-013 — Chip "3x/sem" quebra o texto em 3 linhas (defeito visual)

- **Prioridade:** P3
- **Categoria:** Visual
- **Arquivo:** `app\src\main\java\com\ego\mobile\feature\habits\HabitFormDialog.kt:72-76` (label `"3x/sem"` no chip)
- **Descrição:** O chip "3x/sem" renderiza o texto quebrado em três linhas ("3x/ / se/ / m") em botão circular, parecendo um círculo grande com texto ilegível.
- **Esperado:** Rótulo em uma linha ou chip dimensionado adequadamente.
- **Reprodução:** Aba **Hábitos** → FAB **+** → observar o terceiro chip de Frequência.
- **Observado:** Screenshot `ego_habform3.png` — chip com texto em 3 linhas.
- **Causa:** Largura/`softWrap` do `OutlinedButton` no `Row` com espaçamento fixo.
- **Sugestão:** `singleLine = true`/`maxLines = 1`, padding ou rótulo mais curto ("3x/semana" com `widthIn`).

---

### BUG-014 — Edição, arquivamento e mudança de frequência de hábito não implementados (Spec 3, Seção 6.1)

- **Prioridade:** P2
- **Categoria:** Funcionalidade ausente (requisito da Spec 3, em escopo)
- **Arquivo:** `feature\habits\` (não há `onEdit`/`onArchive` em lugar algum) — `HabitsViewModel.onUpdateFrequency` (`HabitsViewModel.kt:106-118`) **nunca é chamado** pela UI; `HabitStatus.ARCHIVED` existe no domínio (`Habit.kt:6`) mas não há fluxo que o utilize.
- **Descrição:** A Spec 3 (Seção 6.1) exige "Criar, **editar**, pausar, retomar e **arquivar** hábitos" e "**diálogo ao mudar frequência**" (AC-06). Na prática: o formulário de hábito só existe para **criação** (`HabitFormDialog` é sempre chamado sem `initial*`), não há入口 de edição/arquivamento, e `onUpdateFrequency` é código morto — o diálogo de confirmação de mudança de frequência (AC-06) não existe.
- **Esperado:** Editar nome/descrição/frequência e arquivar hábitos conforme Spec 3 (AC-06 preserva ocorrências ao mudar frequência).
- **Reprodução:** Percorrer toda a UI de Hábitos (lista ativos/pausados, FAB +, cartões): não há ação de editar, arquivar ou alterar frequência.
- **Observado:** Somente criação, conclusão rápida e (teoricamente) pausa/retomar — sendo a pausa inalcançável (BUG-011).
- **Evidência:** Buscas por `onEdit|Editar|onArchive|combinedClickable|onUpdateFrequency` em `feature/habits` e `feature/home`: nenhuma chamada de UI.
- **Causa:** Recursos da Spec 3 ainda não implementados na camada de UI.
- **Sugestão:** Implementar edição (reaproveitando `HabitFormDialog` com `initial*`), ação de arquivar e o diálogo de mudança de frequência conectando `onUpdateFrequency` (que já existe e tem teste unitário `UpdateHabitFrequencyUseCaseTest`).

---

## 3. Suspeitas (não confirmadas / requerem verificação adicional)

| ID | Prio | Suspeita | Base |
|----|------|----------|------|
| SUS-001 | P2 | **`OnConflictStrategy.REPLACE` + `ForeignKey.CASCADE` pode apagar dados**: `HabitDao.insert`, `HaircareDao.insertRoutine/insertOccurrences`, `WorkoutDao.insertSession/insertExercise`, `HabitOccurrenceDao.insert` usam REPLACE; inserir uma linha com PK existente **deleta e re-insere**, disparando o CASCADE dos filhos (ex.: ocorrências do hábito, exercícios da sessão). Hoje os updates usam `@Update`, então é um risco latente — mas qualquer caminho futuro que chame `insert` com id existente causa perda silenciosa. | `HabitOccurrenceEntity.kt:15`, `HaircareOccurrenceEntity.kt:15`, `WorkoutExerciseEntity.kt:15` (CASCADE) + DAOs; não reproduzido em UI |
| SUS-002 | P3 | **Reagendar cuidado capilar não aceita "hoje"**: `require(newDate.isAfter(today()))` (`HaircareRepositoryImpl.kt:99`) e o seletor `FutureSelectableDates` exclui hoje (`RescheduleStepDialog.kt:63`). Usuário não pode adiar uma etapa para o próprio dia. A Spec 6 diz "dia seguinte ou data selecionada" — pode ser intencional, mas impede ajustes intra-dia. | Código; não testado em UI (emulador reiniciado) |
| SUS-003 | P3 | **Sem "novo ciclo" quando todas as etapas capilares terminam**: `HaircareScreen.kt:203-207` mostra "Tudo em dia / Não há etapas pendentes" sem ação para iniciar um novo ciclo/rotina; não há caminho para recriar ocorrências concluídas. | Código; Spec 6 não exige explicitamente, avaliar com PO |
| SUS-004 | P3 | **Mensagens de erro podem sumir sem o usuário ver**: os Flows do Room re-emitem a cada mudança e `onEach` regrava o estado com `errorMessage = null` (`HabitsViewModel.kt:46`, `HomeViewModel.kt:52`), podendo apagar um erro recém-exibido (ex.: erro de conclusão) quando outro registro mudar. | Código; não reproduzido |
| SUS-005 | P3 | **Fuso inconsistentes**: `WorkoutHistoryView.kt:125` usa `ZoneId.systemDefault()` enquanto todo o resto do app usa `America/Sao_Paulo` (15 ocorrências). Em aparelho com fuso diferente de SP, datas/horas do histórico de treinos podem exibir dia diferente das demais telas. | Código |
| SUS-006 | P3 | **ViewModels criados com `remember` sem `ViewModelStore`**: `rememberHabitsViewModel`, `rememberHomeViewModel`, etc. usam `remember(context) { HabitsViewModel(...) }` — `onCleared()` nunca é chamado; coletores/coroutines do `viewModelScope` podem continuar vivos após sair da tela (vazamento) e o estado não é compartilhado entre destinos. | Código (`HabitsViewModel.kt:127-138` e análogos); não medido |
| SUS-007 | P3 | **Sub-aba de Rotinas não é preservada**: ao trocar para "Cuidados capilares", sair para a Home e voltar, a tela retorna em "Treinos" (`RoutinesScreen(navigationState.routinesInitialTab)`, `NavigationState.kt:11,19`). Observado nos screenshots `ego_hair_on.png` → `ego_hair_return.png`. | Observação manual |
| SUS-008 | P3 | **Conclusão rápida sem checar agendamento**: `completeOccurrence` não verifica `frequency.requiresOccurrenceOn(hoje)` — um ✓ em hábito não previsto para hoje (ex.: "Academia_3x"num sábado) provavelmente cria ocorrência fora do agendamento, poluindo estatísticas. Não testado se persiste/conta. | Código (`HabitRepositoryImpl.kt:47-78`) |

---

## 4. Testes aprovados

Executados com sucesso no emulador (14/11–16/11) e/ou por teste unitário:

- **Build/testes:** `gradlew test` — 98 testes unitários, 0 falhas (27 classes: domínio, DAO, repositórios, ViewModels, use cases, contraste de tema escuro, navigation state, sanity). `gradlew assembleDebug` — sucesso.
- **Navegação:** troca entre as 5 abas (Início, Hábitos, Tarefas, Rotinas, Histórico); botão voltar do Android (tela secundária → Home; Home → sai do app).
- **Hábitos:** criação diária e "3x por semana"; conclusão em 1 toque com propagação imediata para Home ("Concluído") e Histórico; exibição de "Nenhum hábito cadastrado" (estado vazio, AC-07); persistência após force-stop.
- **Tarefas:** criação; edição; conclusão; badge **"⚠ Atrasada"** (vermelho) para tarefa vencida; exibir concluídas; exclusão com **Snackbar "Desfazer"** e reversão funcionando; validação de título vazio e título de 110 caracteres com erro inline; reschedule; persistência sem duplicatas após force-stop.
- **Treinos:** iniciar treino; adicionar exercício (séries/cargas); pausar/retomar; finalizar com diálogo de confirmação; sessão sobrevive à morte do processo; banner Home "Treino em andamento" / "Continuar treino"; cartão "Iniciar Novo Treino" corretamente exibindo **"Ver Treino Ativo"** quando há sessão ativa; "Ver Visão Geral".
- **Cuidados capilares:** criação a partir da predefinição; registrar execução (ciclo avança para a próxima etapa); reagendar (badge "Reagendada"); histórico de cuidados.
- **Histórico:** filtros Todas / Hábitos / Tarefas / Treinos / Cuidados; conclusão de tarefa pela Home reflete no Histórico.
- **Rotação:** layout em paisagem renderiza corretamente (o *estado* de navegação é BUG-012).
- **Logcat:** nenhum erro não-fatal do app além dos crashes já registrados (únicos `E AndroidRuntime` = BUG-001/002/003).

---

## 5. Testes não realizados

- **Spec 9 (UI/UX/identidade visual):** fora de escopo por instrução — não testada.
- **Fluxos não implementados:** editar hábito, mudar frequência com diálogo (AC-06), arquivar hábito (BUG-014); pausar/retomar hábito pela UI (inalcançável, BUG-011); "novo ciclo" capilar (SUS-003); reagendar cuidado capilar para **hoje** (SUS-002 — impossível por regra).
- **Histórico de treinos com múltiplos treinos concluídos:** o crash (BUG-001) impede a exibição — não foi possível avaliar layout/conteúdo da tela de histórico em uso normal.
- **Dispositivo real / API 24–25:** não testado em Android 7.0/7.1 — o risco de `NoClassDefFoundError` de `java.time` (BUG-004) é estático (lint), não reproduzido em runtime.
- **Testes instrumentados:** `gradlew connectedCheck`/`androidTest` não executados.
- **Testes de dispositivo:** acessibilidade (TalkBack), localização/i18n, fuso fora de `America/Sao_Paulo` (ver SUS-005), tamanho de fonte do sistema, tema claro (app parece escuro-only).
- **Migrações de banco Room:** nenhuma migração/schema evolution testada; dados novos vs atualização de versão não verificados.
- **Performance, bateria, memória:** não avaliadas (o vazamento de SUS-006 não foi medido com profiler).
- **Grandes volumes de dados:** scroll com centenas de tarefas/hábitos e ordenações não testados.
- **Reinstalação vs atualização:** devido à instabilidade do pacote no emulador, a preservação de dados entre atualizações (`adb install -r` com dados existentes) não foi validada de forma controlada.

---

## 6. Ordem recomendada de correção

1. **BUG-001 (P0)** — remova o `LazyColumn` interno em `WorkoutHistoryView.kt:56`; é o único crash que atinge o fluxo principal (concluir/ver treino) e bloqueia a tela de histórico inteira.
2. **BUG-002 e BUG-003 (P1)** — valide Nome (≤80) e Descrição (≤250) no `HabitFormDialog` (erro inline) **e** envolva `Habit(...)` em `try/catch` em `HabitsViewModel.onCreateHabit` (espelhando `HomeViewModel`); corrige dois crashes de um só golpe.
3. **BUG-004 (P1)** — habilite core library desugaring (ou suba `minSdk` para 26) e coloque `lint` na CI; destrava o build de qualidade e evita crash em API 24/25.
4. **BUG-005 (P2)** — faça o cronômetro congelar em pausa (modelar `pausedAt`); afeta o dado exibido e o histórico.
5. **BUG-006 (P2)** — corrija a regra de atraso para prazo do dia atual; afeta todo o fluxo de tarefas (badge "Atrasada" falso).
6. **BUG-007 (P2)** — redistribua os dias de `TimesPerWeek`; hábitos "3x/sem" ficam invisíveis 4 dias por semana.
7. **BUG-011 + BUG-010 (P2)** — reposicione as ações do `HabitCard` (pausar/retomar alcançáveis, AC-04/05) e mostre o estado "concluído hoje" (AC-03/indicador de progresso).
8. **BUG-008 + BUG-009 (P2)** — validação inline do nome vazio (AC-02) e destaque visual do chip selecionado; mesmo componente, correção rápida.
9. **BUG-012 (P2)** — `rememberSaveable` no estado de navegação.
10. **BUG-014 (P2)** — implementar editar/arquivar/mudança de frequência (conectar `onUpdateFrequency`, que já existe).
11. **BUG-013 (P3)** — ajuste visual do chip "3x/sem".
12. **Suspeitas SUS-001…SUS-008** — revisar após os fixes: SUS-001 (REPLACE+CASCADE) primeiro, por risco de perda de dados latente; depois SUS-004/SUS-006 (erros e vazamentos) e o restante como melhorias.
