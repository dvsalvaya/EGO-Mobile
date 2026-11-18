# Plano técnico — Fase 9: UI/UX e identidade visual

- **Feature ID:** `fase-9-ui-ux-identidade-visual`
- **Especificação:** `docs/specs/fase-9-ui-ux-identidade-visual/spec.md` (Status: `DRAFT`)
- **Tarefas:** [lista e evidências](tasks.md).
- **Decisão técnica:** Pendente de revisão/aprovação; não iniciar implementação antes dos portões da documentação do projeto.

## 1. Objetivo da fase

Entregar primeiro a V1 de Início, Hábitos e Histórico de hábitos sobre a base estabilizada da Spec 10. Completar depois os redesenhos de Tarefas, Treinos e Cabelo.

## 2. Contexto técnico e diagnóstico

- Projeto Android Kotlin, módulo único `app`, Jetpack Compose e Material 3.
- Estado e fluxo: Composable → ViewModel (`StateFlow`) → Repository → Room/Flow; DI manual.
- `EgoApp` usa apenas `darkColorScheme()` padrão; cores, padding e shapes são locais. Não existe pacote `core/ui` no código atual.
- Navegação própria em `AppNavigation.kt`, `AppDestination.kt` e `NavigationState.kt`; `NavigationBar` já está fixa no `Scaffold`. Navigation Compose não está no catálogo.
- Recuperar e validar o build da Spec 10 antes de integrar a V1. Reutilizar repositórios e estado persistido nos fluxos atuais.
- `HabitFormDialog` e `TaskFormDialog` são usados para criação; a tela de Hábitos não expõe edição apesar do ciclo de domínio aprovado.
- RF-06 da Spec 10 define atraso pelo dia civil do prazo em America/Sao_Paulo. Consumir essa regra na Home e em Tarefas.
- A árvore já contém Room 6 e migration 5→6 para modelos de treino. Conferir a versão após a Spec 10 antes de planejar novas migrações.
- `HaircareViewModel` já observa rotina, ocorrências futuras e histórico; o calendário pode derivar a grade a partir dessas ocorrências, filtrando por mês/data sem persistência duplicada.
- A timeline consolidada segue disponível. O Histórico de hábitos precisa identificar registros por hábito/data ou hábito/semana para abrir e corrigir o período.

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

- Home: derivar a grade de sete dias de previsões e conclusões canônicas por hábito/data. Guardar o nome editável em preferência local. Usar fonte de tempo do produto para saudação e viradas de data.
- Hábitos e Histórico: compartilhar elegibilidade, fechamento civil e correção retroativa. Preservar o plano vigente em datas passadas e registrar fechamentos ainda que o app tenha ficado fechado, de forma idempotente. Evitar gerar retrospectivamente previsões que o banco não permite reconstruir.
- Tarefas: reutilizar a classificação por data civil de RF-06, com atualização na virada do dia.
- Templates: reutilizar entidades, repositório e migration 5→6 da implementação já registrada. Validar em dispositivo ao integrar a tela.
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
- `HomeSummary`, projeção de previsões e conclusões por data, leitura e edição do nome local.
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

1. Recuperar o build e estabilizar as regras reaproveitadas da Spec 10, incluindo RF-06 e a meta semanal por contagem.
2. Preparar tema, navegação e componentes comuns (TASK-01 a TASK-03).
3. Implementar preferências, fechamento de períodos e correções históricas (TASK-13). Atualizar a grade (TASK-04), a Home (TASK-05) e a aba Hábitos (TASK-06).
4. Validar a V1 em TASK-14. Repetir os fluxos estabilizados atingidos pela mudança de UI.
5. Entregar os redesenhos de Tarefas, Treinos e Cabelo em TASK-07, TASK-10 e TASK-11. Reaproveitar TASK-08 e TASK-09 conforme a evidência disponível. Executar a regressão completa em TASK-12.

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
| Projeção histórica muda ao editar um hábito | Preservar previsões por hábito/data e a vigência da configuração. Testar edição em meio à semana e correção retroativa. |
| Home e Tarefas divergem sobre prazos | Usar RF-06 da Spec 10 e testar virada de data nas duas telas. |
| Perda de sessão ao reestruturar navegação | ViewModel/repositório continuam fonte de verdade persistida; teste alternar destinos e retomar sessão |
| Crescimento de escopo visual (cards em todas as telas) | Cards grandes prioritários na Home; componentes internos compactos e semânticos |
| Tamanho de calendário e barra inferior em telefone compacto | Alvos mínimos, calendário full-width, teste instrumentado em 360 dp e rolagem vertical |
| Repetições textuais impedem série numérica comparável | Estatística usa apenas `weightKg`; `reps` permanece histórico de texto |
| Status de fase anterior inconsistentes | Reconciliar apenas documentos/itens com evidência; manter Fase 0 commit e validação de processo da Fase 5 como pendências já registradas |

## 9. Critérios de conclusão da fase

- `AC-01`–`AC-18` da especificação aprovados por evidência.
- Todas as tarefas em `VERIFIED` com relatórios rastreáveis e testes executados.
- Build e suítes apropriadas concluídas com sucesso; limitações não são tratadas como aprovação.
- Nenhum fluxo existente de hábitos/tarefas/treinos/cabelo/histórico removido.
- Nenhuma dependência desnecessária, recurso de rede, permissão, XP/moedas/níveis ou mudança de regra fora da spec aprovada.
- Nenhum status `VERIFIED` é atribuído antes da verificação e aprovação correspondentes.

