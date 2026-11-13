# Decomposição de Tarefas — Fase 2: Persistência local

- **Feature ID:** `fase-2-persistencia-local`
- **Especificação:** `docs/specs/fase-2-persistencia-local/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Configurar KSP, Room e dependências de teste no catálogo e Gradle | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar modelo de domínio de Hábito e entidade Room (`HabitEntity`) com mappers | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar `HabitDao` e banco de dados `EgoDatabase` | `VERIFIED` | `TASK-02` |
| `TASK-04` | Implementar repositório `HabitRepository` e provedor de banco (`EgoDatabaseProvider`) | `VERIFIED` | `TASK-03` |
| `TASK-05` | Validar persistência e concorrência via testes unitários/integração | `VERIFIED` | `TASK-04` |

---

## Detalhamento das Tarefas

### `TASK-01`: Configurar KSP, Room e dependências de teste no catálogo e Gradle
- **Status:** `VERIFIED`
- **Evidência de validação:** Plugin KSP `2.2.10-2.0.2`, Room `2.6.1` e Coroutines Test configurados e validados via `./gradlew testDebugUnitTest`. Build concluído com sucesso (`26 actionable tasks executed`).
- **Objetivo:** Adicionar os plugins KSP e Room runtime, KTX e compiler no catálogo de versões (`libs.versions.toml`) e `app/build.gradle.kts`, além de suporte para testes unitários com coroutines e Room test utilities.
- **Referência à especificação:** Critério 1 (Compilação e Toolchain).
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:** `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`.
- **Critérios de aceite:**
  1. Plugin KSP é compatível com Kotlin 2.2.10.
  2. Dependências `room-runtime`, `room-ktx` e o processador KSP `room-compiler` estão configuradas.
  3. Bibliotecas de teste (`kotlinx-coroutines-test`, `room-testing`, JUnit) estão disponíveis no escopo de testes.
  4. Execução de `./gradlew assembleDebug` e `./gradlew testDebugUnitTest` compila e executa com sucesso.
- **Testes necessários:** Teste básico de sanidade/compilação validando execução da toolchain.
- **Definição de pronto:** Build e testes do Gradle concluem com sucesso sem erros.

---

### `TASK-02`: Implementar modelo de domínio de Hábito e entidade Room (`HabitEntity`) com mappers
- **Status:** `VERIFIED`
- **Evidência de validação:** `Habit`, `HabitEntity` e `HabitMapper` implementados seguindo TDD (RED confirmado com `NotImplementedError`, GREEN com `testDebugUnitTest` 100% passando).
- **Objetivo:** Criar a entidade Room `HabitEntity` na camada `data/local/entity`, o modelo puro `Habit` em `domain/model`, e funções de extensão de mapeamento bidirecional.
- **Referência à especificação:** Critério 2, Regras de negócio 2 e 3.
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/Habit.kt`
  - `app/src/main/java/com/ego/mobile/data/local/entity/HabitEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/mapper/HabitMapper.kt`
  - `app/src/test/java/com/ego/mobile/data/local/mapper/HabitMapperTest.kt`
- **Critérios de aceite:**
  1. `HabitEntity` possui colunas para `id`, `name`, `description`, `frequency`, `status`, `createdAt` e `updatedAt`.
  2. `Habit` no domínio é desacoplado de anotações do Room.
  3. Mappers convertem perfeitamente entre domínio e entidade, preservando todos os atributos.
- **Testes necessários (TDD):**
  - RED: Escrever `HabitMapperTest` validando conversão nos dois sentidos com dados válidos e limites de string.
  - GREEN: Implementar `Habit`, `HabitEntity` e mappers.
  - REFACTOR: Garantir imutabilidade e boas práticas.
- **Definição de pronto:** `HabitMapperTest` passa com sucesso em `./gradlew testDebugUnitTest`.

---

### `TASK-03`: Implementar `HabitDao` e banco de dados `EgoDatabase`
- **Status:** `VERIFIED`
- **Evidência de validação:** `HabitDao` e `EgoDatabase` implementados com métodos suspend e Flow reativo. Validação via KSP e build bem-sucedido (`assembleDebug` e `testDebugUnitTest` gerando o schema e implementações Room sem erros).
- **Objetivo:** Criar a interface `HabitDao` com operações assíncronas e consultas retornando `Flow`, e a classe abstrata `EgoDatabase` estendendo `RoomDatabase`.
- **Referência à especificação:** Critérios 2 e 3.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/dao/HabitDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/test/java/com/ego/mobile/data/local/dao/HabitDaoTest.kt`
- **Critérios de aceite:**
  1. `HabitDao` declara métodos para `insert`, `update`, `delete`, `getById` (suspend) e `getAll` (retornando `Flow<List<HabitEntity>>`).
  2. `EgoDatabase` registra `HabitEntity` e versão `1` do schema.
- **Testes necessários (TDD):**
  - RED: Testes de unidade/banco de dados usando SQLite in-memory ou Robolectric/Room test harness validando inserção, consulta reativa e atualização.
  - GREEN: Implementar DAO e Database.
  - REFACTOR: Padronizar nomes de queries e anotações do Room.
- **Definição de pronto:** Testes de `HabitDaoTest` passam com sucesso.

---

### `TASK-04`: Implementar repositório `HabitRepository` e provedor de banco (`EgoDatabaseProvider`)
- **Status:** `VERIFIED`
- **Evidência de validação:** `HabitRepository`, `HabitRepositoryImpl` e `EgoDatabaseProvider` implementados com TDD (RED confirmado com falha esperada, GREEN confirmado com 100% dos testes unitários passando em `testDebugUnitTest`).
- **Objetivo:** Criar a interface de domínio `HabitRepository`, sua implementação concreta `HabitRepositoryImpl` consumindo o DAO, e `EgoDatabaseProvider` como singleton manual para acesso ao banco.
- **Referência à especificação:** Critério 4, Regras de negócio 2 e 4, Decisão Q2.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/repository/HabitRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/HabitRepositoryImpl.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabaseProvider.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/HabitRepositoryImplTest.kt`
- **Critérios de aceite:**
  1. `HabitRepository` expõe apenas modelos de domínio `Habit` e `Flow<List<Habit>>`.
  2. `HabitRepositoryImpl` orquestra conversões e chamadas assíncronas ao DAO.
  3. `EgoDatabaseProvider` garante singleton thread-safe para a instância do `EgoDatabase`.
- **Testes necessários (TDD):**
  - RED: Testes de unidade em `HabitRepositoryImplTest` com mock ou fake do DAO validando isolamento e conversões.
  - GREEN: Implementar repositório e provider.
  - REFACTOR: Tratar exceções de banco mapeando para contratos limpos.
- **Definição de pronto:** `HabitRepositoryImplTest` passa com sucesso.

---

### `TASK-05`: Validar persistência e concorrência via testes unitários/integração
- **Status:** `VERIFIED`
- **Objetivo:** Validar o ciclo completo de persistência local, conferindo que dados sobrevivem a fechamentos de conexões e que o fluxo reativo reflete atualizações imediatas.
- **Referência à especificação:** Critérios 5 e 6, Critério de conclusão do Roadmap da Fase 2.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/test/java/com/ego/mobile/data/local/PersistenceIntegrationTest.kt`
- **Critérios de aceite:**
  1. Teste demonstra que salvar um hábito, fechar a conexão de banco e abrir uma nova conexão sobre o mesmo arquivo mantém o registro íntegro.
  2. Modificações e deleções mantêm integridade referencial e consistência.
- **Testes necessários:** Teste de ciclo de vida de persistência local.
- **Definição de pronto:** Todos os testes unitários e de persistência passam via `./gradlew testDebugUnitTest`.
