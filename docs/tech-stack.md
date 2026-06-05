# Tech Stack

## Chosen libraries and versions

| Component | Choice | Version |
| --------- | ------ | ------- |
| Language | Kotlin | 2.4.0 |
| UI | Compose Multiplatform | 1.11.1 |
| Backend | Ktor Server (Netty) | 3.5.0 |
| Serialization | kotlinx.serialization JSON | 1.8.0 |
| Async | kotlinx.coroutines | 1.11.0 |
| Navigation | Navigation 3 (Compose) | 2.9.0-alpha01 — catalogued, implemented Sprint 3 |
| Build | Gradle (Kotlin DSL) | 9.1.0 |
| AGP | Android Gradle Plugin | 9.0.1 |
| Android compileSdk / targetSdk | 36 | — |
| Android minSdk | 26 (Android 8.0) | NF-01 |

## Project structure

Follows the new KMP default structure introduced May 2026
(blog.jetbrains.com/kotlin/2026/05/new-kmp-default-structure/).

| Module | Responsibility |
| ------ | -------------- |
| `core/` | Shared KMP library: data models, serialization. Used by server and all app modules. |
| `server/` | JVM-only Ktor server. No Android or Compose dependencies. |
| `app/shared/` | Shared Compose UI and ViewModels. Used by androidApp and desktopApp only. |
| `app/androidApp/` | Android application entry point only. |
| `app/desktopApp/` | Desktop application entry point only. |

iOS and web targets from the wizard were removed. Project scope is Android + Desktop only (NF-01).

## Spike results

Validated during Sprint 1:

- `./gradlew build` succeeds on a clean Linux checkout with no classpath conflicts.
- `PingMessageTest` in `core/` confirms kotlinx.serialization encodes and decodes a shared data class on the JVM target.
- `PingRouteTest` in `server/` confirms Ktor serves `PingMessage` as JSON with HTTP 200 via content negotiation.
- `./gradlew :app:desktopApp:run` opens a Compose Desktop window without errors.
- Both `server/` and `app/shared/` declare `api(projects.core)` — the shared model module is used by both backend and app.

## Constraints

- `commonMain` in `core/` must stay KMP-compatible: no Android or Desktop APIs.
- `server/` must not be a dependency of any `app/` module.
- All dependency versions pinned in `gradle/libs.versions.toml`. No hardcoded version strings in any module build file.
- Navigation 3 (`org.jetbrains.androidx.navigation:navigation-compose`) handles in-app navigation (Sprint 3, issue #13). Version already catalogued.
- MVVM: ViewModels in `app/shared/`, data models in `core/`.
