# Verificação — Fase 8 / TASK-04

- **Tarefa:** Limpeza de dependências e configuração Gradle
- **Status:** `VERIFIED`

## Alterações e decisões

- Removidas as dependências `androidx.test.ext.junit`, `androidx.test.core` e `androidx.test.runner` da configuração `testImplementation`: os testes JVM usam JUnit e `kotlinx-coroutines-test`, mas não importam APIs AndroidX Test. As dependências seguem em `androidTestImplementation`, onde são utilizadas.
- `androidx.test.espresso:espresso-core` foi mantida explicitamente. A remoção inicial causou falhas nos testes instrumentados (`NoSuchMethodException: android.hardware.input.InputManager.getInstance`); com ela presente, os 24 testes instrumentados passaram.
- A configuração Kotlin agora fixa o alvo de bytecode em JVM 17, alinhado a `sourceCompatibility`/`targetCompatibility` do Java.
- Os demais aliases e plugins do catálogo seguem referenciados na configuração do módulo.

## Validação

- `.\gradlew.bat testDebugUnitTest assembleDebug` — passou.
- `.\gradlew.bat connectedDebugAndroidTest` — passou, incluindo 24 testes instrumentados.
- O build ainda reporta avisos do toolchain JDK 25/Kotlin 2.2.10 e de stripping de uma biblioteca nativa AndroidX; não são dependências órfãs nem impediram build/testes.
