# Fase 8 — Qualidade e acabamento do MVP

## 1. Identificador e nome

- **ID:** `fase-8-qualidade-e-acabamento`
- **Nome:** Qualidade, Acessibilidade, Auditoria de Escopo e Acabamento do MVP
- **Status:** `APPROVED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28 para orientar a implementação direta pelos modelos executores. Decisões fechadas: verificação completa de acessibilidade (WCAG AA, áreas de toque >= 48dp), auditoria rigorosa de proibições do PRD (zero dependências de rede, zero permissões desnecessárias, zero gamificação/XP/moedas), suíte de testes 100% verde e documentação final do projeto no README.

---

## 2. Problema e objetivo

Um produto em fase de fechamento de MVP pode acumular inconsistências visuais, falhas de contraste, dependências não utilizadas ou desvios em relação aos princípios fundacionais de privacidade e escopo.

O objetivo da Fase 8 é auditar, refinar e certificar o aplicativo como pronto para entrega do MVP:
1. Validar contraste de cores, rótulos de acessibilidade para leitores de tela e áreas mínimas de toque (48dp).
2. Revisar tratamento de erros e resiliência a encerramento de processos.
3. Garantir estados vazios amigáveis e explicativos em todas as telas.
4. Auditar permissões do AndroidManifest (nenhuma permissão indevida ou de internet).
5. Certificar conformidade com as regras do PRD (sem login, sem nuvem, sem notificações, sem XP/níveis).
6. Executar suíte completa de testes e atualizar a documentação de execução no README.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seções 4, 8 e 9: Princípios de privacidade, local-first, lista de itens estritamente fora de escopo (nuvem, login, notificações, gamificação artificial), funcionamento offline total.
- **`docs/02-USER-FLOWS.md`**:
  - Seção 8: Exclusão segura, mensagens de erro e clareza de dados.
- **`docs/03-SDD.md`**:
  - Seção 9, 10 e 11: Tratamento de erros, estratégia completa de testes e diretrizes de privacidade.
- **`docs/04-ROADMAP.md`**:
  - Seção Fase 8 — Qualidade e acabamento do MVP.

---

## 4. Escopo

1. **Acessibilidade e Usabilidade:**
   - Conferir contraste de textos e ícones contra o fundo escuro (atender WCAG AA).
   - Adicionar descrições de conteúdo (`contentDescription`) significativas a todos os botões e ícones clicáveis.
   - Garantir área de toque mínima de 48dp x 48dp em todos os elementos interativos.
2. **Estados Vazios e Tratamento de Erros:**
   - Conferir telas de Hábitos, Tarefas, Treinos, Cuidados Capilares e Histórico quando não houver dados cadastrados, garantindo mensagens encorajadoras e botões de ação clara.
   - Garantir que entradas com erro exibam mensagens informativas na UI em vez de falhas silenciosas.
3. **Auditoria de Permissões e Dependências:**
   - Inspecionar `AndroidManifest.xml`: confirmar ausência de permissões como `INTERNET`, `ACCESS_FINE_LOCATION`, `POST_NOTIFICATIONS`, etc.
   - Limpar dependências não utilizadas em `libs.versions.toml` e `app/build.gradle.kts`.
4. **Auditoria de Escopo Negativo do PRD:**
   - Verificar ausência de login, bibliotecas analíticas/telemetria, ou elementos de gamificação punitiva (XP, moedas, níveis).
5. **Documentação e Build Final:**
   - Atualizar `README.md` com instruções reais de compilação, requisitos de JDK e execução no emulador/dispositivo.
   - Executar `./gradlew testDebugUnitTest` e `./gradlew assembleDebug` sem qualquer aviso impeditivo.

---

## 5. Exclusões

- Não adicionar novas funcionalidades de produto além das aprovadas no MVP.
- Não refatorar arquiteturas funcionais sem motivo de defeito ou desempenho.

---

## 6. Critérios de aceite verificáveis

1. **`AC-01`**: Todos os botões, ícones clicáveis e campos interativos possuem `contentDescription` ou rótulo semântico e área mínima de toque de 48dp.
2. **`AC-02`**: O tema escuro atende aos padrões de contraste legível em todas as 5 abas da aplicação.
3. **`AC-03`**: Todas as abas exibem um componente de estado vazio informativo quando não há registros cadastrados.
4. **`AC-04`**: O arquivo `AndroidManifest.xml` não declara permissões desnecessárias de rede, localização ou notificações.
5. **`AC-05`**: O codebase não contém código ou dependências de login remoto, telemetria, servidores externos ou sistemas de XP/moedas.
6. **`AC-06`**: O `README.md` raiz contém instruções claras, testadas e precisas para abrir, compilar e rodar o projeto.
7. **`AC-07`**: A suíte integral de testes unitários passa com 100% de sucesso via `./gradlew testDebugUnitTest`.

---

## 7. Dependências técnicas

- Toda a base do projeto consolidada das Fases 0 a 7.
