# Verificação — Fase 8 / TASK-05

- **Tarefa:** Atualização do README com instruções de execução e compilação
- **Status:** `VERIFIED`

## Conteúdo documentado

- Funcionalidades atuais do MVP e arquitetura em camadas com Compose, ViewModels, domínio e Room.
- Requisitos e versões da toolchain: Gradle 9.5.0, JDK 25 exigido pelo daemon e bytecode Java/Kotlin JVM 17.
- Build, testes unitários, testes instrumentados, instalação por Gradle/ADB e localização do APK, com exemplos para PowerShell e macOS/Linux.
- Escopo de privacidade local-first e funcionalidades fora do MVP.

## Validação

- `.\gradlew.bat :app:assembleDebug testDebugUnitTest` — passou.
- `.\gradlew.bat :app:installDebug` — passou e instalou o APK no emulador API 37.
- `adb devices` — confirmou o emulador conectado; `adb shell monkey -p com.ego.mobile 1` iniciou o aplicativo.
- `connectedDebugAndroidTest` — passou na validação da TASK-04; o comando está documentado no README.

O build emite um aviso do Kotlin sobre o JDK 25; isso não impediu a compilação, os testes nem a instalação. O comando `adb` não está no `PATH` deste ambiente, então a validação foi executada pelo caminho do SDK Android.
