# RoadAssist

Cross-platform roadside assistance and incident reporting app.
Built with Kotlin Multiplatform and Compose Multiplatform.

Targets: Android (API 26+), Desktop (Windows/Linux). Backend: Ktor Server.

## Project layout

| Path | Contents |
| ---- | -------- |
| `core/` | Shared KMP library — data models, serialization |
| `server/` | Ktor backend (JVM only) |
| `app/shared/` | Shared Compose UI and ViewModels |
| `app/androidApp/` | Android entry point |
| `app/desktopApp/` | Desktop entry point |

## Getting started

**Prerequisites:** JDK 17, Android SDK (API 36 build tools).

```bash
# Build all modules
./gradlew build

# Run the backend (port 8080)
./gradlew :server:run

# Run the desktop app
./gradlew :app:desktopApp:run
```

## Development credentials

The server seeds one test account on first startup (when the users table is empty).

| Username | Password | Role      |
| -------- | -------- | --------- |
| `user`   | `user123` | ROAD_USER |

Passwords are stored as bcrypt hashes. These credentials are for local development only and must not be used in any other environment.

## Branch strategy

| Branch | Purpose |
| ------ | ------- |
| `master` | Protected. Stable, releasable code. Merge via PR only. |
| `staging` | Acceptance environment. All feature branches merge here. |
| `feature/<name>` | Short-lived branches per issue — e.g. `feature/9-jwt-auth`. |

PRs target `staging`. Merges to `master` require a passing CI build.

See [CONTRIBUTING.md](./CONTRIBUTING.md) for commit conventions.
