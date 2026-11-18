# Tasks — Spec 10: Estabilização, Correção de Bugs e Integridade Funcional

> Marque uma tarefa como concluída somente após implementar, testar e revisar os critérios indicados. Não marque tarefas de investigação das suspeitas como parte desta Spec.

## Fase 0 — Preparação

- [ ] **T10.01 — Inspecionar o estado do repositório**
  - Confirmar branch, commit e working tree.
  - Identificar alterações preexistentes e não sobrescrevê-las.
  - Ler Specs 3, 4, 7 e 9, além dos arquivos citados no `FIX.md`.
  - **Concluído quando:** contexto técnico e alterações preexistentes estiverem registrados.
  - **Registro (18/11/2026):** branch `main`, HEAD `7153554f85a57056252b71b277bb2ca7a0bf0ebc`. Estado inicial: `FIX.md`, `spec.md`, `plan.md` e `tasks.md` já modificados; `docs/specs/spec-10-estabilizacao.zip` já removido. Alterações preexistentes preservadas. `FIX.md` está na raiz. A compilação confirmou erros atuais em navegação, tema e telas; `TimesPerWeek`/persistência não foram alterados. As Specs 3, 4, 7 e 9 e demais arquivos citados em FIX.md ainda precisam de leitura dedicada.

- [ ] **T10.02 — Registrar baseline**
  - Executar `.\gradlew.bat test`, `.\gradlew.bat lint` e `.\gradlew.bat assembleDebug`.
  - Registrar resultados e relatórios.
  - **Concluído quando:** baseline estiver documentado sem atribuir falhas preexistentes aos fixes.
  - **Registro:** compilação inicial `:app:compileDebugKotlin` falhou antes dos gates, confirmando a falha histórica. O wrapper solicitou Gradle 9.5.0, baixado para viabilizar a execução. Gates pós-ajustes registrados em T10.00; não há baseline verde anterior disponível nesta rodada.

- [x] **T10.00 — Recuperar a compilação (RF-00)**
  - Corrigir `:app:compileDebugKotlin` antes de validar os fluxos.
  - **Concluído quando:** `assembleDebug` gerar um APK e os resultados de testes unitários e lint estiverem registrados.
  - **Registro (18/11/2026):** alterações de compilação em `AppDestination.kt` e `AppNavigation.kt`; compilação ainda falha com erros em `AppNavigation.kt`, `Shape.kt`, `Spacing.kt`, `HabitsScreen.kt`, `TasksScreen.kt` e outras telas. `:app:assembleDebug` — **FAILED**, bloqueado por `:app:compileDebugKotlin`; APK não gerado nesta execução. `:app:testDebugUnitTest` — **FAILED** antes da execução dos testes, bloqueado pela mesma compilação. `:app:lint` — **FAILED** antes da análise lint, bloqueado pela mesma compilação. Relatórios detalhados em `app/build/reports/` se produzidos; lint ainda não forneceu resultado funcional. Causas observadas: arquivo de navegação/theme com sintaxe e imports inválidos, referências de API Compose inexistentes e código incompleto/inconsistente nas telas (incluindo contratos de callback e tipos). RF-06 e migrações não foram alterados.

  - **Recuperação:** as telas e a navegação incompletas foram restauradas à versão funcional de `7aa20d4`. O redesenho segue na Spec 9. O domínio, os modelos de treino, as estatísticas e as migrações da base `7153554` foram preservados.
  - **Resultado:** `:app:compileDebugKotlin`, `:app:assembleDebug` e `:app:testDebugUnitTest` passaram. O APK foi gerado em `app/build/outputs/apk/debug/app-debug.apk`. Foram executados 123 testes unitários sem falhas ou erros. O lint executou e registrou incompatibilidades `NewApi` com API 24, cobertas por T10.07.

- [ ] **T10.03 — Preparar testes de regressão**
  - Identificar infraestrutura de testes unitários, Room e UI/instrumentados.
  - Revalidar os achados após T10.00 e organizar casos para os requisitos aplicáveis.
  - **Concluído quando:** cada requisito aplicável tiver evidência atual ou cenário de regressão definido.

## Fase 1 — Estabilidade P0/P1

- [ ] **T10.04 — Corrigir BUG-001: crash do histórico de treinos**
  - Inspecionar `WorkoutScreen` e `WorkoutHistoryView`.
  - Remover a rolagem vertical aninhada ou reorganizar o conteúdo sob um único container rolável.
  - Testar histórico vazio, com um treino e com vários treinos.
  - Testar finalizar treino e abrir histórico.
  - **Concluído quando:** nenhum cenário causar crash e a rolagem funcionar.

- [ ] **T10.05 — Corrigir BUG-002: nome de hábito acima do limite**
  - Aplicar limite/validação de 1–80 caracteres após `trim`.
  - Exibir erro útil no formulário.
  - Tratar erro esperado no `HabitsViewModel`, mantendo validação no domínio.
  - Testar criação pela aba Hábitos e pela Home.
  - **Concluído quando:** 81+ caracteres não derrubarem o app e não persistirem.

- [ ] **T10.06 — Corrigir BUG-003: descrição acima do limite**
  - Aplicar limite/validação de até 250 caracteres.
  - Exibir feedback no formulário.
  - Tratar erro esperado no ViewModel.
  - Testar 250 e 251 caracteres e ambos os caminhos de criação.
  - **Concluído quando:** descrição inválida não derrubar o app nem ser persistida.

- [ ] **T10.07 — Corrigir BUG-004: Core Library Desugaring e lint**
  - Verificar versões atuais de Gradle/AGP/Kotlin.
  - Habilitar Core Library Desugaring no módulo Android.
  - Adicionar dependência compatível.
  - Manter `minSdk = 24`.
  - Executar lint, testes e build.
  - **Concluído quando:** os erros `NewApi` do relatório forem eliminados e os três comandos passarem.

## Fase 2 — Regras de domínio e datas

- [ ] **T10.08 — Corrigir BUG-005: duração de treino pausado**
  - Modelar instante de pausa e duração pausada acumulada, ou alternativa equivalente.
  - Atualizar pause, resume, duration e finalize.
  - Garantir persistência/recuperação de sessão pausada.
  - Adicionar testes com relógio controlável, incluindo múltiplas pausas.
  - Avaliar necessidade de migração Room e defaults para sessões antigas.
  - **Concluído quando:** tempo pausado não entrar na duração exibida nem na duração final persistida.

- [ ] **T10.09 — Aplicar vencimento por data civil (RF-06)**
  - Comparar a data civil do prazo com a data atual em America/Sao_Paulo. Um prazo de hoje só fica atrasado no dia seguinte.
  - Validar vencimento ontem, hoje e amanhã, tarefas sem prazo e virada do dia na Home e em Tarefas.

- [ ] **T10.10 — Corrigir BUG-007: frequência semanal por contagem**
  - Remover a regra `dayOfWeek <= count`.
  - Contar ocorrências concluídas na semana ISO atual (segunda a domingo).
  - Expor progresso e elegibilidade por uma regra de domínio única.
  - Não definir nem sortear dias fixos.
  - Testar N=1, N=3 e outros valores suportados, progresso parcial/completo e troca de semana.
  - Testar registro retroativo pela data da ocorrência e duas conclusões concorrentes disputando a última vaga na meta.
  - **Concluído quando:** quaisquer N dias podem satisfazer a meta, e a meta atingida não aceita conclusão extra.

- [ ] **T10.11 — Integrar frequência semanal às telas e à conclusão**
  - Aplicar a regra compartilhada na Home, Hábitos e pontos de estatística relevantes.
  - Impedir conclusão rápida quando a meta já foi atingida.
  - Garantir que desfazer uma ocorrência atual atualize o progresso.
  - **Concluído quando:** todas as telas apresentam o mesmo progresso e elegibilidade.

## Fase 3 — Módulo de hábitos: validação e estado

- [ ] **T10.12 — Corrigir BUG-008: erro para nome vazio**
  - Criar estado de erro inline para vazio/whitespace.
  - Manter diálogo aberto e não persistir.
  - Limpar erro após correção válida.
  - **Concluído quando:** tentativa inválida produz feedback explícito.

- [ ] **T10.14 — Corrigir BUG-010: conclusão e progresso no cartão**
  - Disponibilizar ao cartão o estado de conclusão do período.
  - Exibir `Concluído hoje` para hábitos diários e progresso `x/N` para frequência semanal.
  - Implementar ação de desfazer sem duplicar/apagar ocorrências históricas.
  - Testar propagação imediata entre Home, Hábitos e Histórico.
  - **Concluído quando:** o cartão diferencia corretamente pendente, concluído e meta semanal atingida.

- [ ] **T10.15 — Corrigir BUG-011: ações de pausar e retomar**
  - Reorganizar ações do `HabitCard` para conclusão rápida e pausa coexistirem.
  - Exibir ação de retomar em hábitos pausados.
  - Verificar se pausados deixam de ser elegíveis para conclusão.
  - Testar persistência após reabrir o app.
  - **Concluído quando:** pausar e retomar são alcançáveis e funcionam pela interface.

## Fase 4 — Navegação e funcionalidades ausentes

- [ ] **T10.16 — Corrigir BUG-012: restaurar navegação**
  - Persistir/restaurar aba atual usando o mecanismo apropriado do Compose/Android.
  - Testar rotação em cada destino do build estabilizado. Repetir na navegação da nova UI durante a integração da Spec 9.
  - **Concluído quando:** recriação da Activity não retorna indevidamente para Início.

- [ ] **T10.18 — Implementar edição de hábito (BUG-014)**
  - Criar ação de editar em hábito ativo/pausado, conforme UX existente.
  - Reutilizar `HabitFormDialog` com dados iniciais quando adequado.
  - Atualizar nome e descrição sem alterar ocorrências.
  - Testar salvar, cancelar e persistência.
  - **Concluído quando:** edição funciona pela UI e preserva histórico.

- [ ] **T10.19 — Implementar alteração de frequência com confirmação (BUG-014)**
  - Conectar a ação de UI ao fluxo `onUpdateFrequency` ou ajustar o contrato existente.
  - Exibir confirmação antes da alteração.
  - Preservar ocorrências anteriores e aplicar a nova regra dali em diante.
  - Testar confirmar, cancelar e persistência.
  - **Concluído quando:** frequência pode ser alterada com confirmação e sem perda de histórico.

- [ ] **T10.20 — Implementar arquivamento de hábito (BUG-014)**
  - Disponibilizar ação de arquivar em hábitos ativos e pausados.
  - Usar estado `ARCHIVED` existente, se compatível.
  - Remover arquivados das listas ativas/pausadas e impedir novas conclusões.
  - Preservar ocorrências e histórico.
  - Testar persistência após reabertura.
  - **Concluído quando:** arquivamento não equivale a exclusão e o histórico continua acessível.

## Fase 5 — Regressão e entrega

- [ ] **T10.21 — Executar regressão funcional completa**
  - Executar todos os cenários mínimos da Spec 10.
  - Testar force-stop/reabertura e persistência.
  - Testar migração Room se houver alteração de schema.
  - **Concluído quando:** evidências forem registradas para todos os fluxos.

- [ ] **T10.22 — Executar gates de qualidade**
  - Executar `.\gradlew.bat test`.
  - Executar `.\gradlew.bat lint`.
  - Executar `.\gradlew.bat assembleDebug`.
  - Executar testes instrumentados disponíveis ou registrar impedimento concreto.
  - **Concluído quando:** testes unitários, lint e build passarem. Registrar separadamente qualquer validação em dispositivo ainda pendente.

- [ ] **T10.23 — Produzir relatório final da Spec 10**
  - Listar o resultado de RF-00 e o status dos achados históricos, incluindo a decisão de RF-06 e os critérios encaminhados à Spec 9.
  - Listar arquivos alterados e decisões técnicas.
  - Registrar testes, comandos, resultados e evidências.
  - Declarar explicitamente que SUS-001 a SUS-008 não foram investigadas/corrigidas nesta Spec.
  - **Concluído quando:** outra pessoa conseguir auditar o que foi corrigido e reproduzir a validação.

## Critérios para a integração da Spec 9

- **T10.13 / RF-09 → TASK-06:** distinguir a opção selecionada visualmente e pela semântica acessível. Verificar que o valor salvo corresponde à seleção.
- **T10.17 / RF-13 → TASK-06:** validar a legibilidade do rótulo semanal em tela compacta e fonte ampliada.
- **TASK-12:** repetir os cenários de Hábitos, Treinos, Tarefas e navegação nas telas redesenhadas, incluindo restauração após rotação.

## Fora do escopo — próximas investigações

Não criar tarefas de implementação para estes itens dentro da Spec 10:

- SUS-001 — `REPLACE` + `CASCADE` e risco de perda de dados.
- SUS-002 — reagendamento capilar para hoje.
- SUS-003 — iniciar novo ciclo capilar.
- SUS-004 — mensagens de erro removidas por reemissão de Flow.
- SUS-005 — inconsistência de fuso no histórico de treinos.
- SUS-006 — ciclo de vida dos ViewModels criados com `remember`.
- SUS-007 — preservação da subaba de Rotinas.
- SUS-008 — validação de conclusão rápida fora do agendamento, exceto a regra necessária para impedir ultrapassar a meta semanal em RF-07.

Esses itens devem ser triados em uma Spec posterior. A regra de frequência semanal e o bloqueio de conclusão após atingir a meta fazem parte da Spec 10 por serem necessários para corrigir BUG-007.
