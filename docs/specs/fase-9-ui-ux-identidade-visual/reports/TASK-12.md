# Verificação integrada — Fase 9 / TASK-12

- **Tarefa:** Verificar integração visual, acessibilidade e regressão
- **Status:** `VERIFIED` como execução técnica desta tarefa. A Fase 9 permanece `DRAFT`; a aprovação explícita da spec e do plano segue pendente nos metadados formais.
- **Base:** HEAD integrado `1d3a254` (`Merge commit 'd7234e2' into feat/spec9-integracao`), com TASK-07, TASK-10, TASK-11 e TASK-14 disponíveis.
- **AVD:** `Medium_Phone_API_37.0`, API 37, `emulator-5554`; perfil padrão 412 dp e font scale 1.0 após verificação. Foi desligado ao final e liberado ao coordenador.

## Correção encontrada nesta integração

A inspeção manual em 412 dp com font scale 1.3 mostrou que `QuickActionHeader` mantinha saudação, edição e “Ver histórico” na mesma linha; o texto de histórico era comprimido/truncado. O cabeçalho agora organiza o acesso ao histórico em uma segunda linha quando a escala da fonte é ≥1.25 (ou a largura é menor que 380 dp), preservando os alvos de toque. A captura posterior confirma saudação e histórico legíveis. Os cartões da Home permanecem roláveis; a captura inferior mostra a mensagem integral e a grade de sete dias acessível.

Dois testes da regressão completa também dependiam de itens roláveis invisíveis na dobra. `EmptyStateScreensTest` agora rola até o histórico vazio e verifica o título/ação; `WorkoutScreenTest` verifica a carga com `assertExists`, pois o ponto da sequência `LazyRow` é acessível fora do viewport e a tela rolável não o torna totalmente visível sem deslocamento próprio.

## Validações executadas

| Comando/inspeção | Resultado |
|---|---|
| `ANDROID_HOME=C:\Users\sal\AppData\Local\Android\Sdk .\gradlew.bat testDebugUnitTest` | `BUILD SUCCESSFUL`; suíte unitária completa. |
| `... .\gradlew.bat connectedDebugAndroidTest` final | `BUILD SUCCESSFUL`; 54/54 testes instrumentados no `Medium_Phone_API_37.0`. |
| `... .\gradlew.bat assembleDebug` final | `BUILD SUCCESSFUL`. |
| `... .\gradlew.bat testDebugUnitTest connectedDebugAndroidTest assembleDebug` final | `BUILD SUCCESSFUL` após a correção de layout; 54/54 instrumentados. |
| Focados após correções | `EmptyStateScreensTest` 13/13 e `WorkoutScreenTest` 1/1 passaram; `HomeScreenTest` 7/7 e teste de criação integrada 1/1 passaram. |
| `git diff --check` | Sem erros de whitespace. |
| Manifesto e dependências | Sem permissões ou dependências novas nesta integração; `app/build.gradle.kts` manteve as declarações existentes. |
| Inspeção visual em emulador | Capturas 360 dp e 412 dp/font scale 1.0 e 1.3; navegação por cinco destinos; estados vazios de Tarefas, Treinos e Cabelo; Home rolada até grade. Tarefas mostra título, concluídas, vazio, ação e FAB; Treinos mostra entrada, modelos, estatísticas e histórico; Cabelo mostra ações válidas para rotina ausente. |

Capturas em `evidence/`: `TASK-12-home-360.png`, `TASK-12-home-412.png`, `TASK-12-home-412-font130.png`, `TASK-12-home-412-font130-bottom.png`, `TASK-12-tasks-412.png`, `TASK-12-workout-412.png`, `TASK-12-haircare-412.png` e `TASK-12-habits-412.png`.

## Acessibilidade, limites e estados reais

- Os testes cobrem rótulos/semântica da navegação, sete descrições acessíveis da grade, alvo ≥48 dp do botão Criar, estados vazios/erro e interações de agrupamento/detalhe. A suíte final cobriu navegação, migração Room, persistência, modelos/estatísticas, calendário, hábitos e tarefas.
- A inspeção visual manual cobriu estados vazios de Tarefas/Treinos/Cabelo e rolagem da Home. Não foi feita sessão manual populada end-to-end para todas as áreas; dados/população e fluxos correspondentes são validados pelos testes de integração/Compose existentes. Não foi usado TalkBack real nem aparelho físico, então esses itens não são reivindicados como inspeção manual.
- A primeira execução instrumentada foi bloqueada porque o AVD estava offline. Após o boot, passou a executar normalmente. Uma regressão completa intermediária identificou duas asserções de visibilidade/fluxo obsoletas nos testes de estado vazio e Treinos; após correção, as classes focadas e a suíte completa passaram. Houve ainda um timeout isolado no teste integrado de criação Home; o teste passou isoladamente e na repetição completa final.
- `TASK-10` não tem evidência histórica de RED comportamental pré-implementação, conforme seu relatório. TASK-12 não reescreve essa lacuna como RED atendido, não altera aprovação formal e não promove tarefas anteriores com status inconsistentes para `VERIFIED`.
- Esta verificação não declara `AC-01`–`AC-18` globalmente aprovados: dependências ainda marcadas `PENDING`/`IN_PROGRESS`, a spec em `DRAFT`, o plano pendente de aprovação e a cobertura manual limitada permanecem explícitos.

## Portões formais preservados

`spec.md` continua `DRAFT` e `plan.md` continua com decisão técnica pendente. A autorização direta para executar TASK-12 permitiu esta verificação técnica, mas não constitui aprovação do responsável pelo produto nem aprovação técnica formal do plano. Os estados/metadados desses documentos não foram promovidos.

## Adendo — auditoria integrada e AC-15 (19/11/2026)

Este adendo complementa o retrato do commit `1d3a254`; as afirmações de que a aprovação formal e a inspeção populada/TalkBack estavam pendentes descreviam corretamente aquela execução. Depois, a spec foi aprovada (`9e2f040`), o plano foi revisado e aprovado, e a reprodução retrospectiva de TASK-10 foi integrada em `399d732`. O RED comportamental ocorreu na base `7b75eff` e o GREEN em `5b4ec98` (51/51); continua explicitamente retrospectivo, sem alegar RED pré-implementação na cronologia original. O status técnico de TASK-10 foi atualizado em `tasks.md`.

### Inspeção populada, leitor de tela e correções

No AVD `Medium_Phone_API_37.0`, com fluxos populados em Treinos, Cabelo, Hábitos e Tarefas, habilitamos TalkBack e inspecionamos a árvore de acessibilidade, labels, ações e foco visual; logs mostraram pedidos de síntese pt-BR. No calendário após a correção, células mediram 48 dp em 360 dp/font scale 1.3, com descrição “19 de Novembro de 2026, 1 pendente”; depois de rolar, as ações “Registrar execução” e “Reagendar” ficaram visíveis e focáveis. Não reivindicamos avaliação humana da inteligibilidade do áudio nem inspeção de todas as telas/gestos ou aparelho físico. Capturas das telas populadas estão em [workouts 360 dp/font scale 1.3](../evidence/TASK-15-workouts-360-font130-talkback.png), [calendário capilar populado](../evidence/TASK-15-haircare-360-font130-talkback.png) e [ações após rolagem](../evidence/TASK-15-haircare-360-font130-talkback-scrolled.png).

A inspeção e os testes encontraram estes defeitos concretos, já corrigidos:

| Área | Defeito observado | Correção e evidência |
|---|---|---|
| Treinos | Em 412 dp, o título de seção ocupava a largura inteira e ocultava “Criar modelo”; em 360 dp/font scale 1.3 as ações dos modelos podiam sobrepor e ficavam abaixo de 48 dp. | Ação movida para slot de `SectionHeader`; `FlowRow` e altura mínima de 48 dp. `WorkoutScreenTest` verifica visibilidade, editor, dimensões e ausência de sobreposição. |
| Cabelo | Células do calendário ficavam com cerca de 45 dp em 360 dp/font scale 1.3. | Reduzido padding horizontal da lista para 12 dp e removido padding interno da célula. `HaircareHistoryListTest` mede célula ≥48 dp. |
| Regressão Treinos | Teste tentava exigir que três linhas do histórico estivessem simultaneamente visíveis após rolar apenas até o cabeçalho. | `WorkoutRegressionTest` agora rola até cada linha antes de verificar visibilidade, sem enfraquecer a asserção de existência/exibição. Focado: 2/2. |

### Revalidação final desta integração

| Comando | Resultado |
|---|---|
| `ANDROID_HOME=<SDK> .\gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:assembleDebug` | `BUILD SUCCESSFUL`; 54/54 instrumentados no AVD API 37. |
| `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.routines.workouts.WorkoutRegressionTest` | `BUILD SUCCESSFUL`; 2/2, inclusive o teste de histórico corrigido. |
| Testes direcionados em perfil 360 dp/font scale 1.3 | `WorkoutScreenTest` e `HaircareHistoryListTest` passaram após comprovar os defeitos e aplicar as correções; repetição conjunta final: 3/3. |
| `git diff --check` | Sem erros de whitespace. |

O perfil padrão foi usado na regressão completa. Duas primeiras tentativas durante a sessão com TalkBack habilitado interferiram em testes de navegação/formulários; não foram contabilizadas como regressão do produto. TalkBack foi desabilitado e removido da lista de serviços antes da execução final, que passou. A avaliação de labels/foco com TalkBack permaneceu uma sessão separada. A aprovação da spec/plano não é aceite de produto implementado: `RETOMADA.md` registra o estado técnico e aguarda confirmação explícita do usuário para o aceite global. O AVD foi restaurado, TalkBack desligado e emulador liberado.
