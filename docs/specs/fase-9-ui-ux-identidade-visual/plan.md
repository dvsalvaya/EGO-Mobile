# Plano técnico — Fase 9: UI/UX e identidade visual

- **Feature ID:** `fase-9-ui-ux-identidade-visual`
- **Especificação:** `docs/specs/fase-9-ui-ux-identidade-visual/spec.md` (Status: `DRAFT`)
- **Tarefas:** `docs/specs/fase-9-ui-ux-identidade-visual/tasks.md` (todas `PENDING`)
- **Decisão técnica:** Pendente de revisão/aprovação; não iniciar implementação antes dos portões da documentação do projeto.

## 1. Objetivo da fase

Aplicar sistema visual escuro monocromático compartilhado, ajustar destinos principais para Início/Hábitos/Tarefas/Treinos/Cabelo, modernizar apresentação das cinco áreas e acrescentar os comportamentos visuais aprovados (consistência, modelos/estatísticas de treino e calendário mensal capilar), sem reescrever a arquitetura de negócio existente.

## 2. Contexto técnico e diagnóstico

- Projeto Android Kotlin, módulo único `app`, Jetpack Compose e Material 3.
- Estado e fluxo: Composable → ViewModel (`StateFlow`) → Repository → Room/Flow; DI manual.
- `EgoApp` usa apenas `darkColorScheme()` padrão; cores, padding e shapes são locais. Não existe pacote `core/ui` no código atual.
- Navegação própria em `AppNavigation.kt`, `AppDestination.kt` e `NavigationState.kt`; `NavigationBar` já está fixa no `Scaffold`. Navigation Compose não está no catálogo.
- Hábitos/Tarefas/Home/Histórico e componentes de Treinos/Cabelo estão funcionais. Reutilizar repositórios e regras existentes, não duplicar estado persistido em UI.
- `HabitFormDialog` e `TaskFormDialog` são usados para criação; a tela de Hábitos não expõe edição apesar do ciclo de domínio aprovado.
- `TasksViewModel` atual classifica por `Task.isOverdue(now)`, que é `dueAt < now`; o DatePicker grava o começo do dia local. Decisão do responsável: preservar esse comportamento da Fase 4, documentando os efeitos sem alterar o critério.
- `EgoDatabase` está na versão 5 e tem migrations 1→2→3→4→5. Templates de treino serão novos registros locais e exigem migration sequencial 5→6.
- `HaircareViewModel` já observa rotina, ocorrências futuras e histórico; o calendário pode derivar a grade a partir dessas ocorrências, filtrando por mês/data sem persistência duplicada.
- `HistoryItem` contém data, tipo, título, subtítulo e status, mas não ID de origem. A grade de consistência exige chave estável `(tipo, sourceId)` para não contar uma atividade exibida em telas diferentes mais de uma vez.

## 3. Estratégia técnica

### Tema e componentes compartilhados

- Criar `core/ui/theme` para `Color`, `Theme` e tokens de forma/espaçamento, usando os valores propostos e aprovados na especificação antes de implementar.
- Manter Material 3 e dependências existentes. Utilizar VectorDrawables locais ou ícones que já estejam disponíveis; não adicionar biblioteca.
- Criar/adaptar um componente compartilhado de estado vazio e padrões de seção somente onde reduzirem repetição. Não padronizar layouts de todas as telas como cards.

### Navegação

- Estender `AppDestination` para cinco destinos e ajustar `NavigationState`/`AppNavigation` sem alterar a solução para Navigation Compose.
- Reutilizar os Composables e ViewModels existentes de Treinos e Cabelo como rotas principais.
- Abrir `HistoryScreen` por callback da ação “Ver histórico” na Home e definir retorno para Home; preservar Back para Home nos demais destinos e sessão de treino ao alternar destinos.

### Dados e domínio

- Home: acrescentar um agregador testável para sete datas locais, usando ocorrências concluídas de hábitos, tarefa por ID/`completedAt` e sessão por ID/`endedAt`. Expor identidade tipada da origem, sem usar o número de ID isoladamente entre tabelas.
- Hábitos: expor edição e progresso com APIs existentes de ocorrências; preservar frequência/histórico e idempotência por hábito/data.
- Tarefas: derivar agrupamentos da lista observada e do relógio já injetado em `TasksViewModel`; classificação mantém `Task.isOverdue(now)` sem conversão para comparação civil.
- Templates: criar modelos e tabelas de template/exercícios independentes de sessão, DAO/repositório e migration Room 5→6. A seleção instancia exercícios em sessão nova; não altera o modelo.
- Estatísticas: derivar contagem de sessões concluídas/duração média e maior carga positiva por exercício/sessão; não interpretar `reps` textual.
- Calendário: estender o estado/ViewModel capilar com mês e dia selecionados. Filtrar `HaircareOccurrence` existente; registrar/reagendar pelas operações atuais.

## 4. Componentes reutilizados e novos/modificados

### Reutilizar

- `EgoApp`, `AppNavigation`, `NavigationState`, `AppDestination`.
- `HomeScreen`, `HomeViewModel`, `HomeSummary`, `GetHomeSummaryUseCase` e componentes atuais de ações/hábitos/tarefas/rotina.
- `HabitsViewModel`, `HabitRepository`, `HabitOccurrence`, `HabitFormDialog`.
- `TasksViewModel`, `Task.isOverdue`, TaskRepository, TaskItem, `TaskFormDialog`, `RescheduleDialog`, `DeleteConfirmDialog` e Snackbar de Undo.
- `WorkoutViewModel`, `WorkoutRepository`, `WorkoutSession`, `WorkoutExercise`, `WorkoutDao`, telas/components de sessão e histórico.
- `HaircareViewModel`, `HaircareRepository`, `HaircareOccurrence`, `CurrentStepCard`, `RescheduleStepDialog`, `HaircareHistoryList`.
- `HistoryViewModel`, `GetConsolidatedHistoryUseCase`, filtros e timeline.

### Criar/modificar

- `core/ui/theme/*` e, se justificado, componente compartilhado em `feature/common/`.
- Rotas/callbacks de navegação; a tela de Histórico deixa de ser item inferior.
- `HomeSummary`/modelo de evento canônico e agregador de consistência com teste de domínio.
- Bottom Sheet de detalhes de tarefa; Bottom Sheets de criação/edição para formulários afetados.
- Modelo/entidades/DAO/repositório de `WorkoutTemplate` e exercícios do modelo, mapper e migration 5→6.
- Modelo/UseCase/UI state para estatísticas de treino.
- Componente mensal de calendário capilar e estado de mês/dia selecionados.
- Testes unitários e instrumentados focados em tokens, cálculo temporal, deduplicação, migração, navegação, calendários, estados e acessibilidade.

## 5. Arquivos e módulos previstos

Todo o trabalho fica em `app`; não há novo módulo Gradle nem dependência planejada.

- `app/src/main/java/com/ego/mobile/app/EgoApp.kt`
- `app/src/main/java/com/ego/mobile/app/navigation/{AppDestination,NavigationState,AppNavigation}.kt`
- Novo `app/src/main/java/com/ego/mobile/core/ui/theme/`
- `app/src/main/java/com/ego/mobile/feature/common/`
- `app/src/main/java/com/ego/mobile/feature/home/` e `components/`
- `app/src/main/java/com/ego/mobile/feature/habits/`
- `app/src/main/java/com/ego/mobile/feature/tasks/` e `components/`
- `app/src/main/java/com/ego/mobile/feature/routines/workouts/` e `components/`
- `app/src/main/java/com/ego/mobile/feature/routines/haircare/` e `components/`
- `app/src/main/java/com/ego/mobile/feature/history/` e `components/`
- `app/src/main/java/com/ego/mobile/domain/model/`, `domain/repository/`, `domain/usecase/`
- `app/src/main/java/com/ego/mobile/data/local/{EgoDatabase.kt,dao,entity,mapper}/`
- Testes correspondentes em `app/src/test` e `app/src/androidTest`.

Os caminhos são alvos prováveis, a confirmar com os componentes existentes ao iniciar cada tarefa. Não criar classes ou pacotes vazios antecipadamente.

## 6. Sequência e dependências

1. `TASK-01` tokens de tema. Base visual.
2. `TASK-02` navegação; `TASK-03` componentes comuns; `TASK-04` agregador de consistência podem avançar após suas dependências declaradas. `TASK-08` templates e `TASK-09` estatísticas são trilhas de domínio/dados independentes.
3. `TASK-05` Home depende de navegação, componentes e modelo de consistência.
4. `TASK-06` Hábitos e `TASK-07` Tarefas dependem de tokens/estado vazio; preservar contratos atuais.
5. `TASK-10` Treinos depende de navegação, componentes, persistência de templates e estatísticas.
6. `TASK-11` Cabelo depende de navegação e componentes comuns.
7. `TASK-12` executa verificação integrada, regressão, acessibilidade e adaptação de tela.

Dependências reais estão detalhadas por ID em `tasks.md`; independências não devem ser serializadas sem razão.

## 7. TDD e validação planejada

Para cada mudança funcional/comportamental, escrever e executar primeiro o teste RED por falha de asserção do comportamento. Depois implementar GREEN e refatorar com testes verdes. Testes unitários já existem e testes Compose instrumentados estão configurados; não adicionar infraestrutura Gradle presumindo que está ausente.

Validações finais planejadas:

- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat connectedDebugAndroidTest`
- `.\gradlew.bat assembleDebug`
- Testes focados de navegação, Home, task groups/overdue, migration Room 5→6, templates, estatísticas e calendário.
- Verificação manual/instrumentada de 360 dp, 412 dp, fonte ampliada, alvos 48 dp, labels semânticos, estados vazios/erro e contraste WCAG AA.

Falha de emulador/configuração/build não será registrada como teste comportamental RED ou como aceite concluído. Cada tarefa deve salvar evidências em `reports/TASK-XX.md`.

## 8. Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Migration dos templates danificar registros antigos | Teste instrumentado de migration a partir da versão 5 com sessão/exercícios reais, verificar estado após versão 6 |
| Contagem duplicada de atividade na grade | Chave composta por tipo e ID de origem; testes com os mesmos dados projetados em várias telas |
| Divergência de `dueAt < now` e data civil | Preservar classificação Fase 4 por decisão explícita; incluir teste no limite e não migrar prazos |
| Perda de sessão ao reestruturar navegação | ViewModel/repositório continuam fonte de verdade persistida; teste alternar destinos e retomar sessão |
| Crescimento de escopo visual (cards em todas as telas) | Cards grandes prioritários na Home; componentes internos compactos e semânticos |
| Tamanho de calendário e barra inferior em telefone compacto | Alvos mínimos, calendário full-width, teste instrumentado em 360 dp e rolagem vertical |
| Repetições textuais impedem série numérica comparável | Estatística usa apenas `weightKg`; `reps` permanece histórico de texto |
| Status de fase anterior inconsistentes | Reconciliar apenas documentos/itens com evidência; manter Fase 0 commit e validação de processo da Fase 5 como pendências já registradas |

## 9. Critérios de conclusão da fase

- `AC-01`–`AC-16` da especificação aprovados por evidência.
- Todas as tarefas em `VERIFIED` com relatórios rastreáveis e testes executados.
- Build e suítes apropriadas concluídas com sucesso; limitações não são tratadas como aprovação.
- Nenhum fluxo existente de hábitos/tarefas/treinos/cabelo/histórico removido.
- Nenhuma dependência desnecessária, recurso de rede, permissão, XP/moedas/níveis ou mudança de regra fora da spec aprovada.
- Nenhum status `VERIFIED` é atribuído antes da verificação e aprovação correspondentes.

