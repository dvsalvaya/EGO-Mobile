# Verificação — Fase 9 / TASK-08

- **Tarefa:** Persistir modelos locais de treino com migration Room
- **Status:** `IMPLEMENTADO` (testes unitários executados; teste instrumentado compilado, não executado por ausência de dispositivo)

## Evidências

- **Modelo/validação:** `testDebugUnitTest --tests com.ego.mobile.domain.WorkoutTemplateDomainTest` — `BUILD SUCCESSFUL`.
- **CRUD e aplicação:** `testDebugUnitTest --tests com.ego.mobile.data.repository.WorkoutTemplateRepositoryImplTest` — `BUILD SUCCESSFUL` (6 testes: ordenação, atualização preservando `createdAt`, exclusão, observação, sessão independente e reuso de sessão ativa sem duplicar exercícios).
- **Migration:** `compileDebugAndroidTestKotlin` — `BUILD SUCCESSFUL` para `WorkoutTemplateMigrationTest`; **não executado** porque `adb devices` não listou dispositivo/emulador.
- **Regressão:** `testDebugUnitTest` completo — `BUILD SUCCESSFUL` na árvore isolada de validação.
- **Schema:** `6.json` gerado pelo KSP; o SQL de `MIGRATION_5_6` foi conferido com as `createSql` das duas tabelas novas e seus índices.

## Correções aplicadas ao trabalho pré-existente

A árvore continha entidades/DAO/migration incompletos e não compiláveis para esta mesma tarefa (FKs com `parent`/`child` invertidos, `Foreign.CASCADE`, tipo `TrainingModelEntity.Id` inexistente, `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`, imports ausentes em `EgoDatabase`). Foram reescritos:

- `TrainingModelEntity`/`TrainingModelExerciseEntity` com FK correta (`parentColumns`/`childColumns`) e `setsCount` obrigatório;
- `TrainingModelDao`/`TrainingModelExerciseDao` com operações `@Transaction` e `Flow`;
- `MIGRATION_5_6` apenas cria as tabelas e índices novos — não altera `workout_sessions` nem `workout_exercises`;
- `EgoDatabaseProvider` registra a migração.

## Limitações

- Não há dispositivo disponível para `connectedDebugAndroidTest`; a migração foi verificada por compilação e comparação de schema, não por execução em Room.
- A validação unitária foi feita em árvore isolada a partir de `HEAD` devido a alterações concorrentes alheias no worktree compartilhado.
