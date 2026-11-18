# Decomposição de tarefas — Fase 9: UI/UX e identidade visual

- **Feature ID:** `fase-9-ui-ux-identidade-visual`
- **Especificação:** `docs/specs/fase-9-ui-ux-identidade-visual/spec.md` (Status: `DRAFT`)
- **Status das tarefas:** `PENDING` até a aprovação explícita da especificação e do plano técnico.

Esta decomposição não autoriza implementação enquanto os portões da `docs/specs/README.md` não forem atendidos. Em cada tarefa com código, seguir TDD estrito: executar primeiro teste que falhe pelo comportamento esperado (RED), implementar o mínimo necessário (GREEN), refatorar com testes verdes (REFACTOR). Falha de compilação/configuração/ambiente não conta como RED.

> **Nota de execução:** os relatórios de TASK-04, TASK-08 e TASK-09 registram trabalho anterior. TASK-04 muda de critério para a V1 e exige nova validação. TASK-08 e TASK-09 preservam suas evidências.

## Sumário

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Criar tokens do tema escuro monocromático | `IMPLEMENTED` | Nenhuma |
| `TASK-02` | Atualizar navegação principal e acesso ao Histórico interno | `PENDING` | `TASK-01` |
| `TASK-03` | Padronizar componentes comuns de seção e estado vazio | `VERIFIED` | `TASK-01` |
| `TASK-04` | Calcular grade de hábitos previstos em sete dias | `PENDING` para a V1 | `TASK-13` |
| `TASK-05` | Atualizar Home com indicadores e listas acionáveis do dia | `PENDING` | `TASK-02`, `TASK-03`, `TASK-04` |
| `TASK-06` | Atualizar Hábitos com metas, dias preferidos e ações | `VERIFIED` | `TASK-01`, `TASK-03`, `TASK-13` |
| `TASK-07` | Agrupar Tarefas por prazo e integrar o painel de detalhes | `PENDING` | `TASK-01`, `TASK-03`, RF-06 da Spec 10 |
| `TASK-08` | Persistir modelos locais de treino com migration Room | `IN_PROGRESS` | Nenhuma |
| `TASK-09` | Calcular estatísticas e evolução de carga por exercício | `IN_PROGRESS` | Nenhuma |
| `TASK-10` | Atualizar tela Treinos com modelos, estatísticas e sessão atual | `PENDING` | `TASK-02`, `TASK-03`, `TASK-08`, `TASK-09` |
| `TASK-11` | Adicionar calendário mensal à tela Cabelo | `PENDING` | `TASK-02`, `TASK-03` |
| `TASK-12` | Verificar a integração da fase completa | `PENDING` | `TASK-07`, `TASK-10`, `TASK-11`, `TASK-14` |
| `TASK-13` | Fechar períodos, preservar previsões e corrigir histórico de hábitos | `VERIFIED` | Base de hábitos da Spec 10 |
| `TASK-14` | Validar a V1 em Início, Hábitos e Histórico | `PENDING` | `TASK-05`, `TASK-06`, `TASK-13`, RF-06 da Spec 10 |

## Detalhamento

### `TASK-01`: Criar tokens do tema escuro monocromático

- **Objetivo:** Substituir o esquema Material 3 implícito por tokens semânticos centralizados, mantendo contraste e sem adicionar dependências.
- **Contexto:** `EgoApp` chama `darkColorScheme()` sem valores explícitos; várias telas usam cores, espaçamentos e formas diretamente.
- **Arquivos afetados:**
  - `app/src/main/java/com/ego/mobile/app/EgoApp.kt`
  - Novo pacote `app/src/main/java/com/ego/mobile/core/ui/theme/` para cores, tema e tokens de forma/espaçamento, se aprovado no plano técnico.
  - `app/src/test/java/com/ego/mobile/app/DarkThemeContrastTest.kt`
- **Dependências:** Nenhuma.
- **Passos:**
  1. RED: testar tokens aprovados e contraste dos pares semânticos efetivamente usados.
  2. Mapear os tokens aprovados a `darkColorScheme` e ao tema do app.
  3. Adotar os tokens compartilhados em uma tela de referência, removendo valores locais equivalentes.
  4. REFACTOR: reduzir duplicação sem criar abstrações de componentes sem necessidade.
- **Critérios de aceitação:**
  - Todas as telas usam tema escuro explícito com tokens de superfície/texto/acento/error.
  - Paleta é monocromática exceto estados semânticos aprovados.
  - Nenhum texto depende de combinação com contraste inferior a 4,5:1.
- **Testes e validações:** `DarkThemeContrastTest`; `testDebugUnitTest`; `assembleDebug`.
- **Condição de conclusão:** tokens e pares usados passam os testes; nenhuma dependência foi adicionada.

### `TASK-02`: Atualizar navegação principal e acesso ao Histórico interno

- **Objetivo:** Alterar os cinco destinos principais sem trocar a solução de navegação própria.
- **Contexto:** `NavigationState` já guarda destino e estado da aba interna Rotinas; Histórico hoje é destino inferior, Treinos/Cabelo estão dentro de Rotinas.
- **Arquivos afetados:**
  - `app/src/main/java/com/ego/mobile/app/navigation/AppDestination.kt`
  - `app/src/main/java/com/ego/mobile/app/navigation/NavigationState.kt`
  - `app/src/main/java/com/ego/mobile/app/navigation/AppNavigation.kt`
  - `app/src/test/java/com/ego/mobile/app/navigation/NavigationStateTest.kt`
  - Novo/atualizado teste instrumentado da navegação em `app/src/androidTest/java/com/ego/mobile/app/navigation/`
- **Dependências:** `TASK-01`.
- **Passos:**
  1. RED: testar exatamente cinco destinos, seleção de Treinos/Cabelo, acesso interno ao Histórico e comportamento Back.
  2. Atualizar o estado de navegação e os callbacks sem Navigation Compose.
  3. Garantir rótulos visíveis e semântica em cada item inferior.
  4. REFACTOR: remover o estado da aba Rotinas que não for mais necessário, sem afetar sessão ativa.
- **Critérios de aceitação:**
  - Barra inferior tem exatamente Início, Hábitos, Tarefas, Treinos e Cabelo.
  - Histórico é aberto internamente pela Home e Back retorna à Home.
  - Back em destinos principais secundários retorna a Início; Back em Início é nativo.
  - Alternar destinos não duplica nem descarta sessão ativa.
- **Testes e validações:** `NavigationStateTest`, teste Compose instrumentado de navegação, `testDebugUnitTest`, `connectedDebugAndroidTest`.
- **Condição de conclusão:** cinco destinos e fluxos de retorno passam nos testes, com destinos corretamente rotulados.

### `TASK-03`: Padronizar componentes comuns de seção e estado vazio

- **Objetivo:** Adotar hierarquia, espaçamento e vazio acessível consistentes, preservando estruturas específicas de cada área.
- **Contexto:** Existem estados vazios locais nas telas; o padrão aprovado exige ícone discreto, mensagem explicativa e ação válida.
- **Arquivos afetados:**
  - `app/src/main/java/com/ego/mobile/feature/common/`
  - `HomeScreen.kt`, `HabitsScreen.kt`, `TasksScreen.kt`, `WorkoutScreen.kt`, `HaircareScreen.kt`, `HistoryScreen.kt`
  - `app/src/androidTest/java/com/ego/mobile/feature/EmptyStateScreensTest.kt`
- **Dependências:** `TASK-01`.
- **Passos:**
  1. RED: testar acessibilidade/visibilidade dos elementos de estado vazio sem confundir erro com ausência de dados.
  2. Criar ou adaptar um componente vazio compartilhado, com ícone/semântica, texto e callback opcional válido.
  3. Aplicar apenas onde reduz duplicação e mantém a ação apropriada do destino.
  4. REFACTOR: remover estilos duplicados equivalentes.
- **Critérios de aceitação:**
  - Estados vazios têm mensagem e ação válida; Histórico continua sem criação direta de registro.
  - Estados de erro e loading continuam distintos.
  - Sem nova abstração genérica além dos padrões realmente compartilhados.
- **Testes e validações:** `EmptyStateScreensTest`, testes instrumentados relevantes e `assembleDebug`.
- **Condição de conclusão:** todas as telas afetadas passam nos testes de vazio/erro e os alvos de ação permanecem acessíveis.

### `TASK-04`: Calcular a grade de sete dias

- Usar as previsões e conclusões por hábito/data de TASK-13 para calcular a fração diária. A agregação anterior, baseada em eventos de várias áreas, não atende à métrica da V1.
- Validar as faixas de cor, dia vazio, dia atual aberto, alteração de frequência e correção histórica, com relógio controlado.

### `TASK-05`: Atualizar a Home da V1

- Implementar saudação por horário e edição do nome local, retorno de rolagem, continuação de treino e aviso capilar quando aplicável.
- Implementar “Para hoje” com contagens totais, limites, prioridades, detalhes e desfazer. Reutilizar a regra civil de RF-06.
- Colocar o “+” fixo no canto inferior direito, acima da barra, com menu de criação. Validar que todos os itens continuam alcançáveis por rolagem.
- Mostrar os sete quadrados de TASK-04, descrição acessível e acesso ao Histórico.
- Validar primeiro uso, estado “Tudo em dia”, carregamento, erros e os fluxos de criação, conclusão e retorno.

### `TASK-06`: Atualizar Hábitos com progresso, edição e estados compactos

- **Objetivo:** Tornar estados/progresso e edição utilizáveis mantendo as regras de ocorrência/frequência já aprovadas.
- **Contexto:** Hábitos ativos/pausados e conclusão rápida existem; edição não está exposta na lista atual.
- **Arquivos afetados:**
  - `feature/habits/HabitsScreen.kt`, `HabitsUiState.kt`, `HabitsViewModel.kt`, `HabitFormDialog.kt`
  - `feature/habits/components/` (novos itens/sheets apenas se necessário)
  - Testes `HabitsViewModelTest` e novos testes instrumentados de tela/formulário
- **Dependências:** `TASK-01`, `TASK-03`.
- **Passos:**
  1. RED: testar seção ativa/pausada, progresso, semântica da conclusão e edição de item sem apagar histórico.
  2. Expor edição e ações existentes na lista/detalhes.
  3. Trocar criação/edição para Bottom Sheet e reaproveitar os modelos/viewmodel.
  4. REFACTOR: alinhar linha compacta, rótulos e alvos de toque aos tokens.
- **Critérios de aceitação:**
  - Ativos e pausados são distinguíveis; pausados não podem ser concluídos.
  - Nome, frequência e progresso do dia/período são legíveis.
  - Edição mantém dados e histórico; conclusão continua idempotente por hábito/data.
- **Testes e validações:** `HabitsViewModelTest`, Compose UI test, acessibilidade e `testDebugUnitTest`.
- **Condição de conclusão:** fluxo completo acessível e sem regressão de persistência/frequência.

### `TASK-07`: Agrupar Tarefas por prazo e adicionar detalhes em Bottom Sheet

- **Objetivo:** Agrupar tarefas abertas sem alterar a regra temporal ou operações já aprovadas.
- **Contexto:** reutilizar RF-06 da Spec 10 para agrupar pela data civil de vencimento.
- **Arquivos afetados:**
  - `feature/tasks/TasksScreen.kt`, `TasksUiState.kt`, `TasksViewModel.kt`
  - `feature/tasks/components/TaskItem.kt`, formulários e novo Bottom Sheet de detalhes
  - Testes `TasksViewModelTest`, `TaskItemAccessibilityTest` e teste instrumentado de agrupamento/detalhes
- **Dependências:** `TASK-01`, `TASK-03`.
- **Passos:**
  1. RED: testar classificação por data civil, grupos únicos, ordem, itens sem prazo e detalhe.
  2. Agrupar Atrasadas, Hoje e Próximas; inserir itens sem prazo no final de Próximas com rótulo.
  3. Abrir detalhes ao tocar item e manter conclusão, edição, reagendamento, exclusão confirmada e Undo.
  4. Migrar criação/edição para Bottom Sheet; manter DatePicker/diálogo/Snackbar quando apropriado.
  5. REFACTOR: aplicar cor semântica e indicação não cromática.
- **Critérios de aceitação:**
  - Tarefas com vencimento hoje passam a atrasadas na próxima data civil em America/Sao_Paulo.
  - Cada tarefa aberta aparece em um grupo; tarefas sem prazo nunca são atrasadas.
  - Concluídas continuam ocultas por padrão e consultáveis.
- **Testes e validações:** `TasksViewModelTest`, `TaskItemAccessibilityTest`, Compose UI, `testDebugUnitTest`.
- **Condição de conclusão:** atraso não depende só de cor e nenhum fluxo de ciclo de vida é removido.

### `TASK-08`: Persistir modelos locais de treino com migration Room

- **Objetivo:** Adicionar templates locais persistidos como registros independentes de sessões.
- **Contexto:** `EgoDatabase` está na versão 5; sessões/exercícios usam entidades próprias. Não há templates atuais.
- **Arquivos afetados:**
  - Novos modelos em `domain/model/WorkoutTemplate*.kt`
  - Novas entidades/DAO em `data/local/entity/` e `data/local/dao/`
  - `EgoDatabase.kt`, nova `MIGRATION_5_6`, provider se necessário
  - `WorkoutRepository` e `WorkoutRepositoryImpl`
  - Testes de domínio, repositório e `app/src/androidTest/.../WorkoutTemplateMigrationTest.kt`
- **Dependências:** Nenhuma.
- **Passos:**
  1. RED: testar validação/ordem de template, CRUD local, sessão pré-preenchida independente e migration contendo dados prévios.
  2. Implementar modelo e tabelas de template/exercícios separados de `WorkoutSession`.
  3. Implementar migration 5→6 sem apagar/transformar sessões e exercícios existentes.
  4. Implementar operações de salvar/listar/atualizar/excluir e instanciar exercícios numa nova sessão sem modificar template.
  5. REFACTOR: manter entidade Room desacoplada do modelo de domínio.
- **Critérios de aceitação:**
  - Template tem nome e lista ordenada de nome/séries/repetições textuais/carga/nota opcionais conforme spec.
  - Seleção cria dados de sessão independentes; reabrir app mantém modelos.
  - Upgrade de banco versão 5 preserva integralmente os dados existentes.
- **Testes e validações:** testes unitários de modelo/repositório; teste instrumentado de migration; `testDebugUnitTest`, `connectedDebugAndroidTest`.
- **Condição de conclusão:** migration e CRUD verificados com Room, sem fallback destrutivo.

### `TASK-09`: Calcular estatísticas e evolução de carga por exercício

- **Objetivo:** Derivar estatísticas simples apenas de sessões concluídas.
- **Contexto:** `WorkoutExercise` já contém `setsCount`, `reps` textual e `weightKg` opcional; não derivar valor numérico de texto livre.
- **Arquivos afetados:**
  - Novo modelo/UseCase em `domain/model/` ou `domain/usecase/`
  - Integração mínima ao `WorkoutViewModel`/`WorkoutUiState`
  - Testes em `app/src/test/java/com/ego/mobile/domain/usecase/`
- **Dependências:** Nenhuma.
- **Passos:**
  1. RED: testar sessões completas versus pausadas/descartadas, carga nula/zero, agrupamento por nome e ordem cronológica.
  2. Calcular total de treinos concluídos, duração média e pontos de maior carga por exercício/sessão.
  3. Omitir pontos sem carga comparável sem remover sessão do histórico.
  4. REFACTOR: injetar relógio quando necessário e manter métrica pura.
- **Critérios de aceitação:**
  - Só sessões finalizadas integram estatísticas.
  - Cada ponto é a maior carga positiva para o exercício naquela sessão.
  - Não existe score geral nem interpretação de `reps`.
- **Testes e validações:** testes de domínio/UseCase e `testDebugUnitTest`.
- **Condição de conclusão:** agregados determinísticos e cobertos pelos casos limite.

### `TASK-10`: Atualizar tela Treinos com modelos, estatísticas e sessão atual

- **Objetivo:** Apresentar acesso equilibrado à sessão ativa, modelos, histórico e evolução por exercício.
- **Contexto:** `WorkoutScreen` tem início/continuação, exercícios e histórico; deve virar destino de navegação principal.
- **Arquivos afetados:**
  - `feature/routines/workouts/WorkoutScreen.kt`, ViewModel, UiState
  - `feature/routines/workouts/components/`
  - `app/src/androidTest/java/com/ego/mobile/feature/routines/workouts/`
- **Dependências:** `TASK-02`, `TASK-03`, `TASK-08`, `TASK-09`.
- **Passos:**
  1. RED: testar entrada em sessão existente, escolher modelo, operações de modelos, vazio de histórico e evolução.
  2. Adicionar acesso a criar/editar modelo por Bottom Sheet e seleção antes de iniciar.
  3. Apresentar resumo estatístico e sequência histórica por exercício com texto/valores acessíveis.
  4. Preservar execução, pausa/retomada, finalização, descarte e histórico atual.
  5. REFACTOR: priorizar listas compactas e ação de sessão ativa.
- **Critérios de aceitação:**
  - Nenhuma navegação ou seleção de modelo duplica sessão.
  - Modelo e sessão são editáveis/armazenados separadamente.
  - Estatísticas não substituem nem escondem o histórico.
- **Testes e validações:** novo teste Compose de Treinos, testes de ViewModel e regressão instrumentada.
- **Condição de conclusão:** fluxos de treino anteriores e novos passam, incluindo processo recriado/sessão em andamento.

### `TASK-11`: Adicionar calendário mensal à tela Cabelo

- **Objetivo:** Exibir ocorrências de cronograma capilar em calendário e preservar operações existentes.
- **Contexto:** `HaircareRepository` observa ocorrências da rotina; `HaircareScreen` hoje mostra etapa atual, próximas e histórico.
- **Arquivos afetados:**
  - `feature/routines/haircare/HaircareScreen.kt`, `HaircareUiState.kt`, `HaircareViewModel.kt`
  - Novos componentes em `feature/routines/haircare/components/`
  - Testes ViewModel e `HaircareCalendarScreenTest` em `app/src/androidTest/`
- **Dependências:** `TASK-02`, `TASK-03`.
- **Passos:**
  1. RED: testar agrupamento mensal em `America/Sao_Paulo`, semana iniciando segunda-feira, navegação de mês, seleção do dia e ações pendentes.
  2. Incluir mês/seleção no estado de tela sem duplicar fonte de dados.
  3. Implementar grade acessível, indicadores por tipo/estado e lista do dia selecionado.
  4. Conectar “Registrar execução” e “Reagendar” às operações persistentes atuais.
  5. REFACTOR: manter células selecionáveis com alvos úteis em 360 dp e sem cor como único sinal.
- **Critérios de aceitação:**
  - Mês anterior/próximo e seleção de dia exibem as ocorrências corretas.
  - Execução e reagendamento mantêm dados/histórico.
  - Não é possível inserir etapa fora da rotina configurada nessa interação.
- **Testes e validações:** testes de ViewModel, `HaircareCalendarScreenTest`, `connectedDebugAndroidTest`.
- **Condição de conclusão:** calendário funciona em tela compacta/padrão e mantém fluxo atual.

### `TASK-12`: Verificar integração visual, acessibilidade e regressão

- **Objetivo:** Validar os critérios da fase depois que as telas e navegação estiverem integradas.
- **Contexto:** Fase abrange todas as áreas, Room migration, filtros, estados vazios e navegação.
- **Arquivos afetados:** Suítes `app/src/test` e `app/src/androidTest`; relatórios `docs/specs/fase-9-ui-ux-identidade-visual/reports/TASK-12.md`.
- **Dependências:** `TASK-05`, `TASK-06`, `TASK-07`, `TASK-10`, `TASK-11`.
- **Passos:**
  1. Executar testes direcionados e confirmar evidência RED funcional para cada comportamento novo em seu respectivo relatório de tarefa.
  2. Testar telas compacta (360 dp), padrão (412 dp) e escala de fonte ampliada, além dos estados loading/vazio/erro/conteúdo.
  3. Executar `.\gradlew.bat testDebugUnitTest`, `.\gradlew.bat connectedDebugAndroidTest` e `.\gradlew.bat assembleDebug`.
  4. Revisar acessibilidade, manifesto/dependências inalteradas e preservação dos fluxos das fases anteriores.
  5. Registrar comandos, resultados observados e limitações; não marcar critérios sem evidência.
- **Critérios de aceitação:**
  - Critérios `AC-01`–`AC-18` da spec têm evidência identificável.
  - Nenhuma falha é ignorada e nenhum teste é alterado apenas para passar.
  - Nenhum requisito fora do PRD/spec aprovada foi introduzido.
- **Testes e validações:** três comandos Gradle acima, testes focados por funcionalidade e inspeção de acessibilidade.
- **Condição de conclusão:** todas as tarefas anteriores estão verificadas e o relatório contém evidências reproduzíveis.

### `TASK-13`: Fechar períodos e corrigir histórico de hábitos

- Persistir dias preferidos opcionais na meta semanal. Preservar o plano histórico ao editar frequência ou dias.
- Fechar dias fixos/diários e metas semanais conforme a Spec. Validar fechamento após app fechado e reabertura sem duplicatas.
- Mostrar “Não fiz” em datas encerradas sem conclusão. Expor uma linha por dia ou semana, com ação de corrigir a data original. Aceitar correção histórica mesmo acima da meta.
- Validar contagem, viradas de dia/semana, ausência de previsões após atingir a meta e reflexo da correção na grade e no Histórico. Testar migração Room se o schema mudar.

### `TASK-14`: Validar a V1

- Validar Início, Hábitos e Histórico, com os cinco destinos disponíveis. Confirmar prazos, acesso aos fluxos existentes, persistência, rotação, semântica da grade e toque/rolagem em 360 dp e 412 dp com fonte ampliada.
- Registrar evidências para AC-01 a AC-08 e AC-13 a AC-18 no escopo da V1. TASK-12 cobre a fase completa.

## Cobertura dos critérios de aceite

| Critério | Tarefa(s) |
|---|---|
| `AC-01` | `TASK-01`, `TASK-12` |
| `AC-02`–`AC-03` | `TASK-02`, `TASK-10`, `TASK-12` |
| `AC-04`–`AC-08` | `TASK-04`, `TASK-05`, `TASK-12` |
| `AC-09` | `TASK-07`, `TASK-12` |
| `AC-10` | `TASK-08`, `TASK-10`, `TASK-12` |
| `AC-11` | `TASK-09`, `TASK-10`, `TASK-12` |
| `AC-12` | `TASK-11`, `TASK-12` |
| `AC-13`–`AC-15` | `TASK-03`, `TASK-05`–`TASK-07`, `TASK-10`–`TASK-12` |
| `AC-16` | `TASK-12`, `TASK-14` |
| `AC-17`–`AC-18` | `TASK-04`, `TASK-06`, `TASK-13`, `TASK-14` |

