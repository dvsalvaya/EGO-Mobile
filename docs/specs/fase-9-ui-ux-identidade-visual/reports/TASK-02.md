# Verificação — Fase 9 / TASK-02

- **Tarefa:** Atualizar navegação principal e acesso ao Histórico interno
- **Status de execução:** `IMPLEMENTADO`
- **Status formal da especificação/plano/tarefa:** preservado; nenhum status documental foi avançado.
- **SHA local da implementação:** `f2cfb53` (`Implement Spec 9 navigation and internal history`).

## Escopo entregue

- Barra inferior com exatamente Início, Hábitos, Tarefas, Treinos e Cabelo; Histórico não é destino inferior.
- Treinos e Cabelo agora abrem seus destinos próprios. A seleção e o estado do Histórico interno são restauráveis pelo `Saver`.
- Home abre Histórico pelo callback “Ver histórico”; voltar fecha Histórico e retorna à Home. Voltar de destino secundário retorna à Home.
- Rótulos da navegação seguem visíveis e possuem descrição semântica; o atalho de Histórico tem alvo mínimo de 48 dp.
- O teste de sessão ativa usa o mesmo `EgoDatabaseProvider` da aplicação, verificando o registro persistido ao alternar destinos.

## Evidências

- **RED:** alterei temporariamente o comportamento de `NavigationState.handleBack()` para manter Histórico aberto e retornar `false`. `testDebugUnitTest --tests com.ego.mobile.app.navigation.NavigationStateTest` falhou na asserção de retorno (`NavigationStateTest.kt:89`), como esperado. A alteração temporária foi revertida.
- **GREEN:** `testDebugUnitTest` passou integralmente após restaurar a implementação correta.
- **Instrumentado:** `connectedDebugAndroidTest` focado em `AppNavigationTest` passou com 5/5 testes no `Medium_Phone_API_37.0 (API 37)`. Cobre os cinco destinos, Histórico na Home com Back, retorno secundário pelo estado, labels e preservação de sessão no banco usado pela aplicação.
- **Build:** `assembleDebug` passou. `git diff --check` passou.

## Arquivos

`AppDestination.kt`, `NavigationState.kt`, `AppNavigation.kt`, `HomeScreen.kt`, `QuickActionHeader.kt`, testes unitário/instrumentado de navegação e os testes existentes que validam os novos rótulos.

## Nota

As alterações de TASK-02 já estavam parcialmente presentes na árvore local; foram revisadas e preservadas. A barra inferior e os callbacks implementam o escopo desta tarefa sem alterar os status formais de `spec.md`, `plan.md` ou `tasks.md`.
