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
