# Verificação — Fase 9 / TASK-01

- **Tarefa:** Criar tokens do tema escuro monocromático
- **Status:** `IMPLEMENTADO`

## Evidências

- **RED:** com o mapeamento inicial de `egoDarkColorScheme()` retornando o esquema padrão do Material 3, o teste direcionado `testDebugUnitTest --tests com.ego.mobile.app.DarkThemeContrastTest` falhou por asserção comportamental (`java.lang.AssertionError at DarkThemeContrastTest.kt:21` ao validar equivalência dos tokens aprovados).
- **GREEN:** após a definição canônica dos tokens em `com.ego.mobile.core.ui.theme` (`EgoColors`, `EgoShape`, `EgoSpacing`, `EgoTypography`, `egoDarkColorScheme`, `EgoTheme`), a vinculação do tema em `EgoApp.kt` e a adoção dos tokens na tela de referência `PlaceholderScreen.kt`, os testes `DarkThemeContrastTest` passaram com 100% de sucesso.
- **Regressão:** `testDebugUnitTest` e `assembleDebug` executados com `BUILD SUCCESSFUL` sem falhas.

## Tokens criados e mapeados para TASK-02 / TASK-03

### Pacote canônico: `com.ego.mobile.core.ui.theme`
(Compatibilidade mantida via `com.ego.mobile.app.theme`)

1. **Cores (`EgoColors` / `Colors`)**:
   - `background`: `#0B0B0C` (Fundo principal)
   - `surface`: `#141416` (Superfície padrão)
   - `surfaceContainer`: `#1D1D20` (Superfície elevada/cartão)
   - `surfaceVariant`: `#29292D` (Superfície secundária e agrupamentos)
   - `outline`: `#414146` (Contornos e separadores)
   - `onBackground` / `onSurface`: `#F5F5F5` (Texto/ícones principais)
   - `onSurfaceVariant`: `#B9B9BF` (Texto auxiliar e metadados)
   - `primary`: `#F2F2F2` (Ação ou seleção primária)
   - `onPrimary`: `#111111` (Conteúdo sobre ação primária)
   - `primaryContainer`: `#333337` (Seleção/realce monocromático)
   - `onPrimaryContainer`: `#F5F5F5` (Conteúdo sobre contêiner primário)
   - `error`: `#FF8A80` (Erro e atraso)
   - `errorContainer`: `#5A1B1B` (Fundo de erro/atraso)
   - `onErrorContainer`: `#FFDAD6` (Texto sobre erro/atraso)
   - `HabitGrid`:
     - `noForecast`: `#29292D`
     - `zeroPercent`: `#FF8A80`
     - `lowProgress`: `#FFFFB74D`
     - `mediumProgress`: `#43A047`
     - `fullProgress`: `#81C784`

2. **Formas (`EgoShape` / `Shape`)**:
   - `control`: `12.dp` (controles e elementos compactos)
   - `surface`: `16.dp` (superfícies e Bottom Sheets)
   - `pill`: `999.dp` (chips e badges)
   - `EgoShapes`: M3 `Shapes` configurado com `small/medium = control`, `large/extraLarge = surface`

3. **Espaçamento (`EgoSpacing` / `Spacing`)**:
   - `xs`: `4.dp`
   - `sm`: `8.dp`
   - `md`: `12.dp`
   - `lg`: `16.dp` (gutter padrão de tela compacta)
   - `xl`: `20.dp`
   - `xl2`: `24.dp` (entre seções)
   - `xxxl`: `32.dp` (separação de blocos maiores na Home)

4. **Tema e esquema**:
   - `EgoTheme`: Composable de tema que aplica `egoDarkColorScheme()`, `EgoShapes` e `EgoTypography`.
   - `egoDarkColorScheme()`: Mapeamento de `EgoColors` para `darkColorScheme` do Material 3 com todos os contrastes WCAG AA superiores a 4,5:1.
   - `EgoApp.kt` atualizado para envolver a hierarquia sob `EgoTheme`.
   - `PlaceholderScreen.kt` atualizado como tela de referência consumindo `EgoSpacing` e `EgoShape`.
