# Fase 5 — Treinos

## 1. Identificador e nome

- **ID:** `fase-5-treinos`
- **Nome:** Registro Manual de Treinos e Persistência de Sessão em Andamento
- **Status:** `APPROVED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: persistência automática no SQLite para evitar perda ao fechar/sair do app, proibição estrita de criar duplicatas ao reabrir, registro manual flexível de exercícios (séries, repetições, peso) e finalização com histórico.

---

## 2. Problema e objetivo

Muitos aplicativos de treino perdem o progresso do usuário caso ele saia da tela para checar outra coisa ou se o sistema operacional encerrar o processo em segundo plano. Além disso, rotinas rígidas frustram quem deseja apenas anotar rapidamente o que realizou.

O objetivo da Fase 5 é implementar:
1. Início de uma sessão de treino com estado ativo e timestamp de início.
2. Registro manual e fluido de exercícios realizados (nome, séries, repetições, carga).
3. Pausa e retomada sem perda de dados.
4. Persistência contínua da sessão em andamento no Room, garantindo que o retorno ao app permita continuar a mesma sessão sem gerar duplicatas.
5. Finalização formal da sessão com disponibilização imediata no histórico.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção 6.3: Registrar treinos; iniciar, pausar e retomar; registrar exercícios manualmente; preservar estado de treino em andamento quando sair da tela ou o app for interrompido; histórico de treinos acessível separadamente.
  - Seção 7: Decisões aprovadas (iniciar/pausar/retomar, registrar manualmente, preservar estado).
- **`docs/02-USER-FLOWS.md`**:
  - Seção 5: Fluxo de treinos (iniciar, pausar/retomar, persistir localmente, não criar duplicata por engano, finalizar).
- **`docs/03-SDD.md`**:
  - Seção 5: Modelo de dados inicial de `WorkoutSession` e `WorkoutExercise`.
  - Seção 8: Regras de navegação para não recriar sessões nem duplicar registros.
  - Seção 10: Estratégia de testes para retomada de sessão e persistência.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 5 — Treinos: itens de iniciar sessão, registrar manualmente, pausar/retomar, persistir em andamento, finalizar, exibir histórico e testar saída/reabertura.

---

## 4. Escopo

1. **Entidades e Persistência:**
   - Tabelas Room `workout_sessions` e `workout_exercises` (com chave estrangeira `sessionId` e `onDelete = CASCADE`).
   - `WorkoutSessionDao` e `WorkoutExerciseDao` com suporte a buscar sessão ativa (`status IN ('IN_PROGRESS', 'PAUSED')`), listar exercícios e finalizar sessão.
   - `WorkoutRepository` com controle de sessão única em andamento.
2. **Regras de Negócio e Domínio:**
   - Garantia de no máximo 1 sessão ativa simultânea. Se já houver sessão ativa, navegar para ela em vez de criar uma nova.
   - Transições de estado válidas: `IN_PROGRESS` ↔ `PAUSED` → `COMPLETED` (ou `CANCELLED`).
   - Adição, edição e remoção manual de exercícios durante a sessão.
   - Cálculo de duração da sessão (`endedAt - startedAt`).
3. **Interface e ViewModel (Compose):**
   - `WorkoutViewModel` gerenciando `WorkoutUiState`.
   - Subdestino ou tela de Treinos dentro do destino "Rotinas":
     - Painel superior com banner de "Sessão em andamento" caso exista, com botão "Continuar".
     - Botão "Iniciar novo treino" (desabilitado ou alertando caso já haja sessão aberta).
     - Tela de execução da sessão ativa com cronômetro decorrido, lista de exercícios registrados, botão "Adicionar exercício", botões de "Pausar/Retomar" e "Finalizar treino".
     - Diálogo de adição manual de exercício (Nome, Séries, Reps, Peso kg opcional).
     - Diálogo de confirmação para finalizar ou descartar o treino.
     - Histórico resumido de treinos concluídos.

---

## 5. Exclusões

- Não integrar com sensores wearables (smartwatches, frequencímetros).
- Não criar bibliotecas fixas e gigantescas de exercícios fechadas (o usuário é livre para digitar o nome de qualquer exercício).
- Não calcular queima calórica estimada.
- Não exportar para Apple Health ou Google Fit no MVP.

---

## 6. Comportamentos esperados

1. **Iniciar Treino:**
   - Usuário digita um título opcional (padrão: "Treino" + data) e toca em "Iniciar".
   - A sessão é persistida imediatamente como `IN_PROGRESS` no Room.
2. **Adicionar Exercícios:**
   - Usuário toca em "Adicionar exercício", preenche o nome e séries/repetições/peso, e salva. O item é inserido imediatamente no banco vinculado à sessão.
3. **Interrupção e Retomada:**
   - Se o usuário navegar para outra aba, fechar o app ou o sistema matar o processo, ao reabrir a aba de Rotinas/Treinos, o app detecta a sessão em andamento e permite continuar de onde parou com todos os exercícios preservados.
4. **Finalizar Treino:**
   - Usuário toca em "Finalizar".
   - O app grava `endedAt`, muda status para `COMPLETED`.
   - O treino é movido para o histórico de treinos concluídos.

---

## 7. Regras de negócio

1. **Sessão Ativa Única:**
   - Nunca iniciar nova sessão enquanto houver uma em andamento com status `IN_PROGRESS` ou `PAUSED`. O sistema deve conduzir o usuário a continuar ou descartar a existente.
2. **Campos do Exercício:**
   - Nome obrigatório (1 a 80 caracteres).
   - Séries (inteiro > 0). Repetições (texto flexível, ex: "10-12" ou "12"). Carga (número opcional em kg).

---

## 8. Critérios de aceite verificáveis

1. **`AC-01`**: Iniciar sessão persiste `WorkoutSession` com status `IN_PROGRESS` e timestamp `startedAt`.
2. **`AC-02`**: Adicionar exercício grava `WorkoutExercise` vinculado à sessão e o exibe imediatamente na tela de execução.
3. **`AC-03`**: Se o app for destruído e recriado com uma sessão aberta no banco, ao carregar a tela a sessão ativa é recuperada intacta com todos os seus exercícios.
4. **`AC-04`**: Tentar iniciar um novo treino quando já existe sessão ativa exibe aviso e redireciona para a sessão existente sem duplicar registros.
5. **`AC-05`**: Pausar e retomar alternam o status entre `PAUSED` e `IN_PROGRESS` persistindo no banco.
6. **`AC-06`**: Finalizar a sessão grava `endedAt`, altera status para `COMPLETED` e a exibe no histórico de treinos.
7. **`AC-07`**: Todos os testes automatizados de DAO, repositório e ViewModel executam com sucesso via `./gradlew testDebugUnitTest`.

---

## 9. Persistência e dados

- Tabela `workout_sessions`:
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `title` (TEXT NOT NULL)
  - `startedAt` (INTEGER NOT NULL)
  - `endedAt` (INTEGER)
  - `status` (TEXT NOT NULL: 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED')
  - `notes` (TEXT)
- Tabela `workout_exercises`:
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `sessionId` (INTEGER NOT NULL, FK → `workout_sessions.id` ON DELETE CASCADE)
  - `name` (TEXT NOT NULL)
  - `orderIndex` (INTEGER NOT NULL)
  - `setsCount` (INTEGER NOT NULL)
  - `reps` (TEXT)
  - `weightKg` (REAL)
  - `notes` (TEXT)

---

## 10. Dependências técnicas

- Room 2.6.1 + KSP.
- Material 3 (Scaffold, Card, Dialog).
- Módulo `feature/routines/workouts`.
