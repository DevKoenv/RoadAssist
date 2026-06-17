# RoadAssist

Cross-platform roadside assistance and incident reporting app built with Kotlin Multiplatform and Compose Multiplatform.

Road users submit incidents (description, GPS location, optional photo) from their Android phone or desktop. Dispatchers monitor all incoming incidents, filter by status and category, and update incident status in real time. Changes are pushed to connected clients via Server-Sent Events without requiring a manual refresh.

**Targets:** Android (API 26+), Desktop (Windows and Linux). **Backend:** Ktor on the JVM.

## 1. Features

- Role-based login: road users and dispatchers see different views after sign-in
- Road users can report a new incident with category, description, GPS location, and an optional photo
- Road users can track active incidents and browse their full history
- Dispatchers see all incoming incidents, filter by status or category, and update status with a note
- Both roles see full incident detail including photo, location, and dispatcher comments
- Real-time updates via SSE: incident and comment changes appear without manual refresh
- Offline detection: a banner appears whenever the server is unreachable

## 2. Requirements

| Tool | Minimum version |
|------|----------------|
| JDK | 17 |
| Android SDK | API 26 (build tools for API 36 recommended) |
| Gradle | 8.x (Gradle wrapper included; no separate install required) |

No other tools need to be installed manually. The Gradle wrapper downloads all dependencies on first run.

## 3. Starting the backend

### Environment variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JWT_SECRET` | Yes | HMAC-256 secret used to sign and verify JWT tokens. Set to any strong random string in production. A development fallback is provided in `application.yaml`; never use it outside local dev. |
| `DB_MODE` | No | `sqlite` for a persistent file database; omit (or set to `h2`) for H2 in-memory (data resets on restart). |
| `DB_PATH` | No | Path to the SQLite database file. Required when `DB_MODE=sqlite`. Example: `/var/data/roadassist.db`. |

### Run

```bash
# In-memory H2 database (local dev, data resets on restart)
JWT_SECRET=changeme ./gradlew :server:run

# Persistent SQLite
DB_MODE=sqlite DB_PATH=./roadassist.db JWT_SECRET=changeme ./gradlew :server:run
```

The server starts on port **8080** by default.

## 4. Building and running on Android

Connect a device or start an emulator, then:

```bash
./gradlew :app:androidApp:installDebug
```

The app is installed and launched automatically.

**Server URL:** By default the app points to `https://roadassist.koenv.dev`. To connect to a local server, add a `server_url` string resource in `app/androidApp/src/main/res/values/strings.xml`:

```xml
<string name="server_url">http://10.0.2.2:8080</string>
```

Use `10.0.2.2` for an Android emulator connecting to localhost, or your machine's LAN IP for a physical device.

## 5. Running on Desktop

```bash
./gradlew :app:desktopApp:run
```

This targets the current OS (Windows or Linux). To point the desktop app at a local server, pass the system property:

```bash
./gradlew :app:desktopApp:run --args='-Droadassist.serverUrl=http://localhost:8080'
```

Or set it in the JVM args inside `desktopApp/build.gradle.kts`. Without this, the app also defaults to `https://roadassist.koenv.dev`.

## 6. Seed accounts

The server creates two accounts on first startup.

| Username | Password | Role |
|----------|----------|------|
| `user` | `user123` | Road user |
| `dispatcher` | `dispatch123` | Dispatcher |

Passwords are stored as bcrypt hashes. These credentials are for local development only.

## 7. Running tests

```bash
./gradlew :core:jvmTest :server:test :app:shared:jvmTest
```

All tests run on the JVM; no Android device or emulator is required.

## Project layout

| Path | Contents |
|------|----------|
| `core/` | Shared KMP library: data models and serialization |
| `server/` | Ktor backend (JVM) |
| `app/shared/` | Shared Compose UI and ViewModels |
| `app/androidApp/` | Android entry point |
| `app/desktopApp/` | Desktop entry point |

## Branch strategy

| Branch | Purpose |
|--------|---------|
| `master` | Protected. Stable, releasable code. Merge via PR only. |
| `staging` | Acceptance environment. All feature branches merge here first. |
| `feature/<name>` | Short-lived branches per issue, e.g. `feature/9-jwt-auth`. |

PRs target `staging`. See [CONTRIBUTING.md](./CONTRIBUTING.md) for commit conventions.
