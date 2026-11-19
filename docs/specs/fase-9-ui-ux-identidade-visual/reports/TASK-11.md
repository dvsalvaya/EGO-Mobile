# TASK-11 — Calendário mensal de cuidados capilares

## Escopo entregue

- O estado da tela agora mantém mês e dia selecionados e deriva as ocorrências do mês a partir dos fluxos do repositório, sem criar uma fonte de dados ou persistência duplicada.
- A tela Cabelo mostra uma grade mensal iniciada na segunda-feira, navegação entre meses, seleção de dia e indicadores textuais de ocorrências pendentes/concluídas com rótulo acessível por data.
- O dia selecionado lista as etapas; etapas pendentes ou reagendadas mantêm as ações persistentes existentes de registrar execução e reagendar.
- A lista de etapa atual, próximas etapas e histórico foi preservada.

## Evidências de validação

- `$env:ANDROID_HOME='C:\Users\sal\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.ego.mobile.feature.routines.haircare.HaircareViewModelTest"` — **PASSOU** (teste direcionado).
- `$env:ANDROID_HOME='C:\Users\sal\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug` — **PASSOU** após a revisão final.
- `$env:ANDROID_HOME='C:\Users\sal\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` — **PASSOU**; testes JVM, compilação dos testes instrumentados e APK debug concluídos.
- Foi acrescentado teste de ViewModel para filtragem mensal, navegação e limite de dia ao mudar para mês com menos dias.
- `connectedDebugAndroidTest` — **PASSOU** com a suíte completa: **51/51** testes no `Medium_Phone_API_37.0` (resolução padrão 1080×2400, densidade 420 dpi, aproximadamente 412 dp de largura).
- O teste Compose de calendário navegou mês, selecionou dia com ocorrência concluída e confirmou a lista. A classe de testes capilares passou **2/2** também com densidade 480 dpi (360 dp de largura).
- O teste de histórico foi ajustado para rolar a LazyColumn até os registros, mantendo a verificação de ordem mais recente primeiro após o calendário aumentar a altura do conteúdo.

## Encerramento

Os critérios de navegação mensal, seleção do dia, ocorrências corretas e preservação das ações existentes foram validados. A densidade do AVD foi restaurada ao valor físico e o emulador foi desligado/liberado após os testes. TASK-11 pode ser marcada `VERIFIED`.

## Processo e metadados

O merge `feat/spec9-integracao` foi incorporado. Os metadados formais de aprovação em `spec.md` e `plan.md` foram preservados sem alterações. A primeira tentativa de Gradle parou antes da execução dos testes por falta de `ANDROID_HOME`; as validações acima foram repetidas com o caminho do SDK disponível no ambiente. Uma tentativa instrumentada inicial também não iniciou sem dispositivo conectado; após iniciar o AVD e aguardar o boot, as execuções registradas acima passaram.
