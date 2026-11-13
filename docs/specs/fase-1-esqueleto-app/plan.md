# Plano Técnico — Fase 1: Esqueleto do aplicativo

- **Feature ID:** `fase-1-esqueleto-app`
- **Especificação:** `docs/specs/fase-1-esqueleto-app/spec.md` (Status: `APPROVED`)
- **Tarefas:** `docs/specs/fase-1-esqueleto-app/tasks.md`
- **Data da revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas
- `docs/specs/fase-1-esqueleto-app/spec.md`: status `APPROVED`, decisão de Q1 definida (voltar para Início a partir de destinos secundários).
- `docs/specs/fase-1-esqueleto-app/tasks.md`: 5 tarefas atômicas sequenciais (`TASK-01` a `TASK-05`).
- `docs/01-PRD.md`: Requisitos de navegação inferior (5 destinos), tema escuro e acessibilidade.
- `docs/02-USER-FLOWS.md`: Fluxo principal de navegação e áreas Início, Hábitos, Tarefas, Rotinas, Histórico.
- `docs/03-SDD.md`: Decisões técnicas com Jetpack Compose, Material 3 e navegação simples e direta.
- `docs/04-ROADMAP.md`: Fase 1 — Esqueleto do aplicativo.

---

## 2. Problemas concretos encontrados e correções necessárias
- **Problema 1 (Infraestrutura de Testes):** O repositório não possuía dependências de teste configuradas no `build.gradle.kts`.
  - *Correção:* A `TASK-01` foi isolada especificamente para configurar a infraestrutura de teste JUnit 4 antes de qualquer código de produção, garantindo que o ciclo TDD (RED → GREEN → REFACTOR) seja estritamente viável.
- **Problema 2 (Ambiente Java no Windows):** O comando `gradlew` precisa de `JAVA_HOME` apontando para o JDK válido.
  - *Correção:* Validado o uso da JBR do Android Studio instalada localmente em `C:\Program Files\Android\Android Studio\jbr`, garantindo execução de build e testes.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

### Cobertura
- **Critério 1 (Inicialização sem crash):** Coberto por `TASK-01`, `TASK-04` e `TASK-05`.
- **Critério 2 (Tema escuro):** Coberto por `TASK-04`.
- **Critério 3 (Cinco destinos):** Coberto por `TASK-02` e `TASK-04`.
- **Critério 4 (Seleção e alternância):** Coberto por `TASK-02` e `TASK-04`.
- **Critério 5 (Placeholders identificáveis):** Coberto por `TASK-03`.
- **Critério 6 (Tamanhos de tela):** Coberto por `TASK-05`.
- **Critério 7 (Acessibilidade básica):** Coberto por `TASK-03`, `TASK-04` e `TASK-05`.
- **Critério 8 (Navegação de retorno):** Coberto por `TASK-02` e `TASK-04`.
- **Critério 9 (Validação e evidências):** Coberto em todas as tarefas.

### Ordem e dependências
- A ordem proposta é linear e estrita:
  `TASK-01` (Infra de teste) → `TASK-02` (Estado e modelo de navegação) → `TASK-03` (Placeholders) → `TASK-04` (Integração UI / BackHandler) → `TASK-05` (Validação multiplataforma/telas).

### Testabilidade (TDD)
- O gerenciamento de navegação será encapsulado em um modelo testável unitariamente (`NavigationState`), permitindo validar todas as regras de navegação, seleção e retorno (Back) com testes de unidade puros (JUnit), garantindo o ciclo RED → GREEN → REFACTOR sem fragilidades ou dependências pesadas.

### Riscos de regressão e casos-limite
- **Duplo clique ou re-seleção da mesma aba:** `NavigationState` tratará a seleção repetida como idempotente.
- **Back no destino Início:** `BackHandler` deve estar desativado (`enabled = false`) quando o destino for `Home`, permitindo que o sistema Android feche/minimize a aplicação nativamente.
- **Corte em telas pequenas:** Os rótulos da barra de navegação inferior serão concisos e testados para evitar sobreposição ou quebra de linha inadequada.

---

## 4. Testes e comandos de validação planejados
- **Execução de testes unitários:**
  `.\gradlew.bat testDebugUnitTest`
- **Compilação e build da aplicação:**
  `.\gradlew.bat assembleDebug`
- **Ambiente:**
  Variável `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`.

---

## 5. Decisão de Aprovação
- **Decisão:** `APROVADO`
- **Justificativa:** O plano cobre integralmente os 9 critérios da especificação aprovada, elimina bloqueios de infraestrutura com a `TASK-01`, estabelece um caminho realista e determinístico para TDD e respeita as decisões arquiteturais do SDD sem adicionar complexidade desnecessária.
