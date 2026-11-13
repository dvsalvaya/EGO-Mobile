# Plano Técnico — Fase 8: Qualidade e acabamento do MVP

- **Feature ID:** `fase-8-qualidade-e-acabamento`
- **Especificação:** `docs/specs/fase-8-qualidade-e-acabamento/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-8-qualidade-e-acabamento/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-8-qualidade-e-acabamento/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-8-qualidade-e-acabamento/tasks.md`
- `docs/01-PRD.md` (Seção 4, 8 e 9: Princípios, escopo negativo e offline-first)
- `docs/03-SDD.md` (Seção 9, 10 e 11: Tratamento de erros, testes e privacidade)
- `docs/04-ROADMAP.md` (Fase 8: Qualidade e acabamento do MVP)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Risco de permissões ocultas adicionadas por bibliotecas terceiras**
  - *Análise:* Algumas dependências podem injetar permissões (como `INTERNET` ou `ACCESS_NETWORK_STATE`) via merge do Manifest.
  - *Correção aplicada:* Em `TASK-03`, inspecionar o arquivo `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml` para garantir que nenhuma permissão de rede foi adicionada automaticamente.
- **Problema 2: Instruções desatualizadas no README**
  - *Análise:* Desenvolvedores ou outros modelos precisam de instruções reprodutíveis para executar a build sem adivinhação.
  - *Correção aplicada:* Em `TASK-05`, validar todos os comandos no terminal antes de consolidar o README.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Acessibilidade e touch targets) | `TASK-01` | Auditoria de Composables com conferência de `contentDescription` |
| `AC-02` (Contraste escuro WCAG AA) | `TASK-01` | Verificação visual e de tokens de cor |
| `AC-03` (Estados vazios em todas as 5 abas) | `TASK-02` | Teste de UI com repositórios vazios |
| `AC-04` (Sem permissões desnecessárias) | `TASK-03` | Verificação do Manifest compilado |
| `AC-05` (Conformidade com escopo negativo) | `TASK-03` | Auditoria estrita contra a seção 8 do PRD |
| `AC-06` (README completo e reprodutível) | `TASK-05` | Teste dos comandos do README |
| `AC-07` (Suíte completa de testes verdes) | `TASK-06` | `./gradlew testDebugUnitTest` sem falhas |

---

## 4. Testes e comandos de validação

- Compilação total: `./gradlew assembleDebug`
- Execução de testes de regressão: `./gradlew testDebugUnitTest`
