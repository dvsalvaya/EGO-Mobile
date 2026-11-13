# Decomposição de Tarefas — Fase 1: Esqueleto do aplicativo

- **Feature ID:** `fase-1-esqueleto-app`
- **Especificação:** `docs/specs/fase-1-esqueleto-app/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Configurar infraestrutura de testes unitários do app | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar modelo de destinos e gerenciador de estado de navegação | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar telas de placeholder identificáveis para os 5 destinos | `PENDING` | `TASK-02` |
| `TASK-04` | Integrar barra de navegação inferior, tema escuro e tratamento de retorno (Back) | `PENDING` | `TASK-03` |
| `TASK-05` | Validar acessibilidade, diferentes tamanhos de tela e verificação final | `PENDING` | `TASK-04` |

---

## Detalhamento das Tarefas

### `TASK-01`: Configurar infraestrutura de testes unitários do app
- **Status:** `VERIFIED`
- **Evidências:** `.\gradlew.bat testDebugUnitTest` executado com sucesso em 29s. Testes unitários JUnit 4 plenamente operacionais.

---

### `TASK-02`: Implementar modelo de destinos e gerenciador de estado de navegação
- **Status:** `VERIFIED`
- **Evidências:** Ciclo TDD estrito executado. 6 testes em `NavigationStateTest` criados (RED com falha esperada de asserções funcionais → GREEN com `NavigationState` e `AppDestination` passando → REFACTOR com suporte a `mutableStateOf` Compose). Suíte completa de testes unitários executada com sucesso.

---

### `TASK-03`: Implementar telas de placeholder identificáveis para os 5 destinos
- **Status:** `IN_PROGRESS`
- **Objetivo:** Criar componentes composable para cada um dos cinco destinos que sirvam de placeholder claro e sem simulação de regras de negócio.
- **Referência à especificação:** Critérios 5, 7.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/home/HomeScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/HabitsScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/TasksScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/RoutinesScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryScreen.kt`
  - `app/src/test/java/com/ego/mobile/feature/PlaceholderScreensTest.kt`
- **Critérios de aceite:**
  1. Cada destino possui sua tela correspondente exibindo claramente o nome da área.
  2. Nenhuma tela implementa dados fictícios, botões de ação falsos ou lógica de negócio fora de escopo.
  3. Textos identificadores utilizam tipografia adequada e rótulos acessíveis.
- **Testes necessários (TDD):**
  - Testes garantindo que cada tela expõe o título esperado e respeita o isolamento de escopo.
- **Definição de pronto:** Classes criadas, compilando e validadas por testes.

---

### `TASK-04`: Integrar barra de navegação inferior, tema escuro e tratamento de retorno (Back)
- **Status:** `PENDING`
- **Objetivo:** Integrar os destinos e o estado de navegação com a barra inferior (`NavigationBar`) no `AppNavigation`, conectando ao `BackHandler` e garantindo o tema escuro fixo.
- **Referência à especificação:** Critérios 1, 2, 3, 4, 8.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/app/navigation/AppNavigation.kt`
  - `app/src/main/java/com/ego/mobile/app/EgoApp.kt`
  - `app/src/main/java/com/ego/mobile/MainActivity.kt`
  - `app/src/test/java/com/ego/mobile/app/navigation/AppNavigationIntegrationTest.kt`
- **Critérios de aceite:**
  1. A barra de navegação inferior exibe os cinco destinos com ícone e rótulo de texto legível.
  2. O item selecionado é visualmente destacado e altera o conteúdo central para o placeholder correspondente.
  3. O tema escuro (`darkColorScheme`) é aplicado de forma consistente independentemente da configuração do sistema operacional.
  4. O `BackHandler` intercepta o botão voltar quando o usuário está em destino secundário, retornando para Início; quando em Início, não intercepta (permitindo fechar/minimizar a Activity).
- **Testes necessários (TDD):**
  - Validação da alternância entre abas e acionamento do callback de retorno.
- **Definição de pronto:** Aplicação compila, testes passam e fluxo de navegação funciona de ponta a ponta.

---

### `TASK-05`: Validar acessibilidade, diferentes tamanhos de tela e verificação final
- **Status:** `PENDING`
- **Objetivo:** Verificar o comportamento do layout em dimensões compactas e médias, avaliar escala de fonte e gerar evidências completas para encerramento da Fase 1.
- **Referência à especificação:** Critérios 6, 7, 9.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - Relatório de verificação de layout e build.
  - Documentação final de encerramento da fase.
- **Critérios de aceite:**
  1. Em telas de dimensões compactas (ex: 360dp de largura) e médias (ex: 412dp), todos os cinco destinos permanecem visíveis e acessíveis sem truncamento indevido.
  2. Build do projeto (`assembleDebug` e `testDebugUnitTest`) executa sem avisos ou erros impeditivos.
  3. Relatório final com evidências registrado.
- **Testes necessários:** Execução completa da suíte de testes e verificação de layout.
- **Definição de pronto:** Todos os 9 critérios da especificação verificados e aprovados.

---

## Cobertura dos Critérios de Aceite da Especificação

| Critério do spec.md | Tarefas responsáveis |
|---|---|
| 1. Inicialização sem crash | `TASK-01`, `TASK-04`, `TASK-05` |
| 2. Tema escuro fixo | `TASK-04` |
| 3. Exatamente 5 destinos | `TASK-02`, `TASK-04` |
| 4. Seleção e alternância | `TASK-02`, `TASK-04` |
| 5. Placeholders identificáveis | `TASK-03` |
| 6. Tamanhos de tela diferentes | `TASK-05` |
| 7. Acessibilidade básica | `TASK-03`, `TASK-04`, `TASK-05` |
| 8. Navegação de retorno (Q1) | `TASK-02`, `TASK-04` |
| 9. Validação e testes | `TASK-01`, `TASK-02`, `TASK-03`, `TASK-04`, `TASK-05` |
