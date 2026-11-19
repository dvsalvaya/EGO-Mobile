# TASK-11 — Calendário mensal de cuidados capilares

## Escopo entregue

- O estado da tela agora mantém mês e dia selecionados e deriva as ocorrências do mês a partir dos fluxos do repositório, sem criar uma fonte de dados ou persistência duplicada.
- A tela Cabelo mostra uma grade mensal iniciada na segunda-feira, navegação entre meses, seleção de dia e indicadores textuais de ocorrências pendentes/concluídas com rótulo acessível por data.
- O dia selecionado lista as etapas; etapas pendentes ou reagendadas mantêm as ações persistentes existentes de registrar execução e reagendar.
- A lista de etapa atual, próximas etapas e histórico foi preservada.

## Evidências de validação

- `$env:ANDROID_HOME='C:\Users\sal\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.ego.mobile.feature.routines.haircare.HaircareViewModelTest"` — **PASSOU** (teste direcionado).
- `$env:ANDROID_HOME='C:\Users\sal\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` — **PASSOU**; testes JVM, compilação dos testes instrumentados e APK debug concluídos.
- Foi acrescentado teste de ViewModel para filtragem mensal, navegação e limite de dia ao mudar para mês com menos dias.
- Foi acrescentado teste Compose para navegar mês, selecionar dia com ocorrência concluída e conferir a lista do dia. O teste compilou, mas ainda não foi executado em dispositivo.

## Pendência

`connectedDebugAndroidTest` aguarda liberação exclusiva do emulador pelo coordenador. Na última confirmação, TASK-10 tinha a reserva atual e TASK-07 era a próxima; ainda não havia horário para liberação. Portanto, não atribuir `VERIFIED` à tarefa até executar a suíte instrumentada, incluindo o novo caso `calendarNavigatesMonthsAndSelectsDayWithOccurrence`.

## Processo e metadados

O merge `feat/spec9-integracao` foi incorporado. Os metadados formais de aprovação em `spec.md` e `plan.md` foram preservados sem alterações. A primeira tentativa de Gradle parou antes da execução dos testes por falta de `ANDROID_HOME`; as validações acima foram repetidas com o caminho do SDK disponível no ambiente.
