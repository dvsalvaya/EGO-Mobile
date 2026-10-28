# EGO Mobile

Base inicial do aplicativo Android nativo EGO, em Kotlin e Jetpack Compose. A tela atual é provisória; ainda não há funcionalidades de negócio implementadas.

## Requisitos

- Android Studio compatível com Android Gradle Plugin 9.3
- JDK 17 ou superior (o Gradle 9.5 também suporta o JDK 25 incluído no Android Studio usado nesta configuração)
- Android SDK Platform 37 e SDK Build-Tools 36.0.0

## Executar

Abra a raiz do projeto no Android Studio e aguarde a sincronização do Gradle. Para compilar pelo PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

Para instalar em um emulador ou dispositivo conectado:

```powershell
.\gradlew.bat :app:installDebug
```

## Toolchain inicial

| Componente | Versão |
|---|---:|
| Kotlin (integrado ao AGP) | 2.2.10 |
| Gradle | 9.5.0 |
| Android Gradle Plugin | 9.3.3 |
| Android SDK | compile/target 37; min 24 |
| Compose BOM | 2026.09.00 |

As versões foram alinhadas à compatibilidade publicada pelo Android, Kotlin e Gradle. Consulte `docs/03-SDD.md` para as decisões e referências técnicas.

"# EGO-Mobile" 
