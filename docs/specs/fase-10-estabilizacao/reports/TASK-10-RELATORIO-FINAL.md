# Relatório final — Spec 10 (Estabilização, Correção de Bugs e Integridade Funcional)

- **Branch:** `feat/spec-10-estabilizacao`
- **Commit base:** `7aa20d4` (commit auditado pelo `FIX.md`)
- **Data:** 16/11/2026
- **Escopo:** BUG-001 a BUG-014. SUS-001 a SUS-008 **não** foram investigados nem corrigidos.

## Status dos bugs

| Bug | Correção | Evidência |
|---|---|---|
| BUG-001 | `WorkoutHistoryView` usa `Column` em vez de `LazyColumn` aninhado | Testes instrumentados com 0, 1 e 3 sessões |
| BUG-002/003 | `HabitFormDialog` valida nome (1–80) e descrição (≤250) inline; `HabitsViewModel` captura falhas | Teste instrumentado do formulário; testes unitários do ViewModel |
| BUG-004 | Core Library Desugaring habilitado (`desugar_jdk_libs` 2.1.5), `minSdk = 24` mantido; `windowLightNavigationBar` movido para `values-v27` | `lint` de 109 erros → 0 erros |
| BUG-005 | `WorkoutSession` registra `pausedAt`/`pausedDurationMillis`; duração congela na pausa e exclui todos os intervalos; migration 5→6 | Testes de domínio com relógio controlado + teste instrumentado de migration |
| BUG-006 | `Task.isOverdue` compara datas civis em `America/Sao_Paulo`; prazo hoje só atrasa no dia seguinte | Testes de domínio (ontem/hoje/amanhã e virada de dia) e do ViewModel |
| BUG-007/011 | `TimesPerWeek` aceita qualquer dia; `HabitProgressRules` conta conclusões na semana ISO e controla elegibilidade; repositório bloqueia 4ª conclusão | Testes de domínio, repositório e ViewModel |
| BUG-008 | Erro inline para nome vazio/whitespace; diálogo permanece aberto | Teste instrumentado do formulário |
| BUG-009 | `FilterChip` com `selected`, semântica e atualização imediata | Teste instrumentado do formulário |
| BUG-010 | Progresso `x/N` / `Concluído hoje` no cartão + ação "Desfazer" | Testes unitários do ViewModel; regra de domínio testada |
| BUG-012 | `rememberSaveable` para destino e subaba de Rotinas | Código; cenário manual de rotação não executado (ver limitações) |
| BUG-013 | Chip `3x/sem` com `maxLines = 1`, `softWrap = false` e peso igual na linha | Teste instrumentado do formulário |
| BUG-014 | Edição, alteração de frequência com confirmação e arquivamento (`ARCHIVED`) na UI | Testes unitários do ViewModel (editar/frequência/arquivar) |

## Decisões técnicas

- **BUG-006:** a regra da Spec 10 (data civil) prevalece sobre a regra `dueAt < agora` da Spec 4/9, conforme RF-06/RF-5.2. Fuso centralizado em `America/Sao_Paulo`, injetável via parâmetro em `Task.isOverdue`.
- **BUG-005:** campo `pausedDurationMillis` acumula intervalos fechados; `pausedAt` representa a pausa aberta. Ao finalizar/cancelar durante pausa, o intervalo aberto é acumulado antes de salvar. Migração 5→6 usa `ALTER TABLE ... ADD COLUMN` com default 0, preservando durações históricas.
- **BUG-007:** nenhum dia fixo é persistido. `HabitProgressRules` é a fonte única de progresso/elegibilidade, usada por Home, Hábitos e repositório.
- **BUG-004:** `coreLibraryDesugaring` exigiu alinhar `kotlinx-serialization` 1.8.1 no classpath instrumentado (room-testing 2.8.5) para evitar `AbstractMethodError` ao carregar schemas.
- Atributo `android:windowLightNavigationBar` (API 27) foi movido para `res/values-v27/themes.xml`, mantendo `minSdk = 24`.

## Arquivos principais alterados

- `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/res/values{,-v27}/themes.xml`
- `app/src/main/java/com/ego/mobile/app/navigation/{AppNavigation,NavigationState}.kt`
- `app/src/main/java/com/ego/mobile/domain/model/{Task,WorkoutSession,HabitFrequency,HabitProgress}.kt`
- `app/src/main/java/com/ego/mobile/domain/usecase/{GetHomeSummaryUseCase,ObserveHabitProgressUseCase}.kt`
- `app/src/main/java/com/ego/mobile/domain/repository/HabitRepository.kt`
- `app/src/main/java/com/ego/mobile/data/repository/{HabitRepositoryImpl,WorkoutRepositoryImpl}.kt`
- `app/src/main/java/com/ego/mobile/data/local/{EgoDatabase,EgoDatabaseProvider}.kt`, entity/mapper/DAO de sessão e ocorrência
- `app/src/main/java/com/ego/mobile/feature/habits/{HabitsScreen,HabitsUiState,HabitsViewModel,HabitFormDialog}.kt`
- `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/WorkoutHistoryView.kt`

## Comandos executados e resultados

| Comando | Resultado |
|---|---|
| `.\gradlew.bat test lint assembleDebug` (baseline, antes dos fixes) | `test`/`assembleDebug` OK; `lint` **FALHOU** com 109 erros `NewApi` |
| `.\gradlew.bat test` | **BUILD SUCCESSFUL** — 107 testes unitários |
| `.\gradlew.bat lint` | **BUILD SUCCESSFUL** — 0 erros |
| `.\gradlew.bat assembleDebug` | **BUILD SUCCESSFUL** |
| `.\gradlew.bat connectedDebugAndroidTest` | **BUILD SUCCESSFUL** — 29 testes no emulador `Medium_Phone_API_37.0`, 0 falhas |

Novos testes: `HabitProgressRulesTest`, `WorkoutPauseMigrationTest`, `Spec10RegressionTest`, testes de duração com pausa, de prazo civil, de frequência semanal e de edição/arquivamento no ViewModel.

## Limitações

- Cenários manuais de rotação em cada aba, force-stop/reabertura e invocação real dos fluxos de edição/arquivamento pelo app instalado não foram executados nesta sessão; a restauração de navegação e as ações dos cartões foram verificadas por código e testes automatizados, não por manipulação manual do emulador.
- A migração 5→6 foi executada com sucesso em Room no emulador; não foi testada em dispositivo físico com dados reais de versões anteriores.
- SUS-001 a SUS-008 permanecem explicitamente fora do escopo.
