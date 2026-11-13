# Fase 6 — Cuidados capilares

## 1. Identificador e nome

- **ID:** `fase-6-cuidados-capilares`
- **Nome:** Rotinas e Cronograma de Cuidados Capilares
- **Status:** `IN_PROGRESS`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: predefinições totalmente editáveis (ponto de partida facultativo), suporte a etapas típicas (Hidratação, Nutrição, Reconstrução, Umectação, etc.), registro de execução real com timestamp e reagendamento sem perdas.

---

## 2. Problema e objetivo

Rotinas de autocuidado e cronogramas capilares frequentemente falham em aplicativos genéricos porque impõem calendários engessados ou cobram dias fixos sem flexibilidade. Se a pessoa lava o cabelo em dias alternados ou precisa adiar uma etapa por chuva ou falta de tempo, o cronograma não pode se desestruturar.

O objetivo da Fase 6 é implementar:
1. Configuração de rotina personalizada ou adoção de predefinição editável (ex: Cronograma Básico: Hidratação / Nutrição / Reconstrução).
2. Agendamento flexível de etapas.
3. Registro de execução real (com data e notas de produtos usados se desejado).
4. Reagendamento ágil de etapas pendentes sem corromper o histórico pregresso.
5. Histórico dedicado às etapas realizadas.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção 6.4: Registrar rotinas/cuidados capilares; permitir configurar rotina personalizada ou usar predefinição; permitir registrar execução real e reagendar; não forçar rotina única.
  - Seção 7: Decisões aprovadas (predefinição editável, execução real e reagendamento).
- **`docs/02-USER-FLOWS.md`**:
  - Seção 6: Fluxo de cuidados capilares (criar personalizada ou predefinição editável, registrar execução real, reagendar, histórico).
- **`docs/03-SDD.md`**:
  - Seção 5: Modelo de dados inicial de `HaircareRoutine` e `HaircareOccurrence`.
  - Seção 6: Regras de data/tempo e fuso `America/Sao_Paulo`.
  - Seção 10: Estratégia de testes para frequências e estados.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 6 — Cuidados capilares: itens de rotina personalizada, predefinição editável, execução real, reagendamento, histórico e testes.

---

## 4. Escopo

1. **Entidades e Persistência:**
   - Tabelas Room `haircare_routines` e `haircare_occurrences` (`routineId` FK com cascade).
   - Tipos de etapas fundamentais (`HaircareStepType`): `HIDRATACAO`, `NUTRICAO`, `RECONSTRUCAO`, `UMECTACAO`, `OUTRO`.
   - `HaircareDao` e `HaircareRepository` com métodos para buscar rotina ativa, etapas da semana/ciclo e histórico.
2. **Regras de Negócio e Domínio:**
   - Predefinições embutidas como modelos imutáveis que servem de modelo inicial copiado para a rotina do usuário: "Cronograma Básico Equilibrado" (H-N-H, H-N-R).
   - O usuário pode alterar qualquer etapa, ordem ou frequência mesmo tendo iniciado a partir de uma predefinição.
   - Registro de execução: grava a data e hora em que a etapa foi efetivamente realizada (`executedAt`).
   - Reagendamento: atualiza a data prevista (`scheduledDate`) para o futuro sem apagar notas ou histórico anterior.
3. **Interface e ViewModel (Compose):**
   - Integrado na tela de "Rotinas" (aba Cuidados Capilares):
     - Visão da próxima etapa prevista com destaque visual do tipo de tratamento.
     - Botão "Registrar execução" (1 toque ou com diálogo para notas).
     - Botão "Reagendar" para adiar para o dia seguinte ou data selecionada.
     - Botão "Configurar rotina" com opção de escolher uma predefinição ou criar do zero.
     - Histórico das etapas concluídas.

---

## 5. Exclusões

- Não vender produtos ou recomendar marcas comerciais.
- Não usar câmera para "análise capilar por IA" (fora de escopo).
- Não exigir login ou conexão de rede.

---

## 6. Comportamentos esperados

1. **Primeiro Acesso:**
   - Se nenhuma rotina estiver ativa, a tela apresenta convite amigável: "Começar com Cronograma Capilar Padrão" ou "Criar rotina personalizada".
2. **Adoção de Predefinição:**
   - Ao escolher o padrão, as etapas são geradas e salvas no banco do usuário como dados editáveis. Ele pode mudar nomes, dias ou ordem livremente.
3. **Registro de Execução:**
   - Tocar em "Registrar agora" marca a etapa atual como concluída com a data/hora real e gera/avança para a próxima etapa do ciclo.
4. **Reagendamento:**
   - Se o usuário não puder realizar hoje, toca em "Reagendar", escolhe a nova data, e o agendamento é adiado sem registrar falha.

---

## 7. Critérios de aceite verificáveis

1. **`AC-01`**: Adoção de uma predefinição gera a rotina e ocorrências no Room permitindo edição completa de qualquer etapa.
2. **`AC-02`**: Criação de rotina personalizada de cuidados capilares persiste nome, passos e frequência escolhida.
3. **`AC-03`**: Registrar a execução grava `executedAt` e avança o ciclo para a próxima etapa agendada.
4. **`AC-04`**: Reagendar uma etapa pendente atualiza a `scheduledDate` sem apagar registros ou notas anteriores.
5. **`AC-05`**: O histórico de cuidados capilares exibe todas as etapas executadas ordenadas por data com seus respectivos tipos e notas.
6. **`AC-06`**: Todos os testes unitários e de persistência da funcionalidade executam com sucesso via `./gradlew testDebugUnitTest`.

---

## 8. Persistência e dados

- Tabela `haircare_routines`:
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `name` (TEXT NOT NULL)
  - `source` (TEXT NOT NULL: 'PRESET', 'CUSTOM')
  - `description` (TEXT)
  - `createdAt` (INTEGER NOT NULL)
  - `updatedAt` (INTEGER NOT NULL)
- Tabela `haircare_occurrences`:
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `routineId` (INTEGER NOT NULL, FK → `haircare_routines.id` ON DELETE CASCADE)
  - `stepType` (TEXT NOT NULL)
  - `stepTitle` (TEXT NOT NULL)
  - `scheduledDate` (TEXT NOT NULL, formato 'YYYY-MM-DD')
  - `executedAt` (INTEGER)
  - `status` (TEXT NOT NULL: 'PENDING', 'COMPLETED', 'RESCHEDULED', 'SKIPPED')
  - `notes` (TEXT)

---

## 9. Dependências técnicas

- Room 2.6.1 + KSP.
- Material 3 (Cards, Chips de tipos de cuidado, DatePicker).
- Módulo `feature/routines/haircare`.
