# Relatório de estabilização da Spec 10

Validação em 18/11/2026 no worktree de integração, com Gradle 9.5.0 e emulador `Medium_Phone_API_37.0`.

| Requisitos | Entrega e evidência |
|---|---|
| RF-00 | Compilação e APK recuperados em `d7c45fe`, com as telas funcionais do commit de referência e preservação do código de domínio e das migrações. |
| RF-01 e RF-12 | Rolagem única no histórico de treinos e restauração de navegação. Os testes de regressão exercitam o histórico com treinos concluídos e a recriação da Activity. |
| RF-02, RF-03 e RF-08 | Validação de nome e descrição compartilhada na criação pela Home e por Hábitos, com feedback no formulário. |
| RF-04 | Core Library Desugaring com `minSdk = 24`. Lint executado para verificar o uso de `java.time` e dos recursos por versão Android. |
| RF-05 | A duração congela durante a pausa. Histórico e estatísticas usam a duração efetiva. Os testes abrem o banco legado pelo provedor da aplicação e verificam a duração após reabrir o banco. |
| RF-06 | Home e Tarefas usam a data civil do prazo em `America/Sao_Paulo`. Os testes verificam prazo no mesmo dia, dia anterior, virada de data e tarefa sem prazo. |
| RF-07 e RF-10 | Meta por semana ISO e limite de conclusão na mesma transação Room. Progresso e desfazer usam a ocorrência persistida. |
| RF-11 e RF-14 | Ações de pausar, retomar, editar, confirmar mudança de frequência e arquivar. O histórico dos hábitos arquivados é preservado. |
| RF-09 e RF-13 | Critérios visuais mantidos na Spec 9. |

Os testes de migração percorrem as cadeias 2→7, 5→7 e 6→7 pela configuração de produção. O SQL preserva sessões e exercícios antigos, e os novos campos de pausa são opcionais ou têm valor padrão.

## Resultados da árvore integrada

- `.\gradlew.bat :app:assembleDebug` gerou `app/build/outputs/apk/debug/app-debug.apk`.
- `.\gradlew.bat :app:test` executou 136 testes unitários sem falhas.
- `.\gradlew.bat :app:lint` passou com 0 erros, 9 avisos e 1 dica. O relatório HTML fica em `app/build/reports/lint-results-debug.html`.
- `.\gradlew.bat :app:connectedDebugAndroidTest` executou 33 testes sem falhas no emulador. O relatório fica em `app/build/reports/androidTests/connected/debug/index.html`.

A verificação no emulador incluiu recriação da Activity nos cinco destinos, sessão pausada após reabrir o banco, histórico de treinos com registros, validação de formulários e ciclo de vida básico de hábitos. A rodada manual separada e a edição retroativa com a frequência histórica vigente em cada período não foram verificadas nesta entrega.

As suspeitas SUS-001 a SUS-008 permanecem no `FIX.md`. A única interação com SUS-008 é a regra de elegibilidade semanal de RF-07.
