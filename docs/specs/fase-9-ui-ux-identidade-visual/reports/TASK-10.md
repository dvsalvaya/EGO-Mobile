# Verificação — Fase 9 / TASK-10

- **Tarefa:** Atualizar Treinos com modelos, estatísticas e sessão atual
- **Status:** `PENDING` — a especificação e o plano da Fase 9 ainda constam como `DRAFT`/pendentes de aprovação; implementação e validações foram executadas por autorização direta da tarefa.
- **Base integrada:** commit `4422265` (`feat/spec9-integracao`), com navegação principal, Room 7, modelos de treino, migration 5→6 e estatísticas de TASK-08/TASK-09.

## Implementação

- A tela de Treinos expõe sessão ativa/continuação, criação manual, modelos, estatísticas e histórico na mesma página rolável. O fluxo de execução ativo, pausa/retomada, finalização, descarte e histórico existente foram mantidos.
- O ViewModel observa modelos locais e estatísticas junto das sessões. A fábrica injeta `WorkoutTemplateRepositoryImpl` sobre os DAOs Room já existentes.
- A lista de modelos oferece criação/edição em Bottom Sheet, exclusão com confirmação e início a partir do modelo. O comando de iniciar modelo fica desabilitado durante sessão ativa; o ViewModel também não inicia outro se já há sessão corrente.
- O formulário aceita vários exercícios ordenados com séries, repetições textuais, carga opcional e nota. A sessão criada recebe cópia dos exercícios e não altera o modelo persistido.
- Estatísticas mostram total concluído e duração média. Para cada exercício, a sequência cronológica exibe a maior carga positiva por sessão; exercícios sem carga continuam visíveis com contagem de sessões sem inferência numérica de repetições.
- O teste Compose novo cobre renderização do modelo, resumo estatístico, evolução, histórico e seleção do modelo em sessão independente. A regressão instrumentada foi ajustada para rolar até o histórico e contemplar que nomes de exercício aparecem tanto nas estatísticas quanto no histórico.

## Evidências de validação

| Comando | Resultado |
|---|---|
| `ANDROID_HOME=<SDK> .\gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin` | `BUILD SUCCESSFUL` |
| `ANDROID_HOME=<SDK> .\gradlew.bat :app:testDebugUnitTest` | `BUILD SUCCESSFUL` — suíte unitária completa |
| `ANDROID_HOME=<SDK> .\gradlew.bat :app:connectedDebugAndroidTest` | `BUILD SUCCESSFUL` — 51 testes instrumentados no `Medium_Phone_API_37.0` |
| `ANDROID_HOME=<SDK> .\gradlew.bat :app:assembleDebug` | `BUILD SUCCESSFUL` |

O Android SDK usado está em `C:\Users\sal\AppData\Local\Android\Sdk`; foi fornecido por variável de ambiente apenas para os comandos, sem adicionar `local.properties` ao repositório. A suíte instrumentada completa foi executada com o emulador reservado exclusivamente para esta tarefa.

## Pendências e limites

- A documentação da fase ainda registra especificação em `DRAFT` e aprovação do plano pendente. Por isso, este relatório não muda TASK-10 para `VERIFIED` nem altera o status global da fase. A autorização direta desta tarefa permitiu implementar o escopo solicitado; a aprovação documental permanece pendente.
- O novo teste foi criado antes das alterações de produção, porém a etapa RED comportamental não foi executada antes da implementação; não há evidência de RED e ela não é declarada concluída.
- A instrumentação cobre renderização e aplicação independente do modelo; criação/edição via Bottom Sheet e confirmação de exclusão não têm asserções diretas de interação nesta tarefa. Também não houve validação manual específica em 360 dp/412 dp ou escala de fonte ampliada; essa cobertura pertence à integração geral da fase.
