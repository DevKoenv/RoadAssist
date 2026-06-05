# Sprint 2: Groundwork
**Dates:** 8 mei – 14 mei  
**Goal:** Shared data models defined, database schema in place, seed data available, and JWT auth endpoint working. The full data layer is ready before the app starts consuming it.

---

## Issues

### [#6](https://github.com/DevKoenv/RoadAssist/issues/6) Define shared data models in :shared
**Category:** Backend / API | **Type:** Task | **Scope:** Must | **Estimate:** 3pt

Define all Kotlin data classes and enums used by both backend and app in :shared commonMain. All classes must be annotated with @Serializable (kotlinx.serialization). Models to define: User(id, username, role), Role enum (ROAD_USER, DISPATCHER), Incident(id, userId, category, description, latitude, longitude, photoUrl?, status, createdAt, updatedAt), IncidentStatus enum (NEW, IN_PROGRESS, EN_ROUTE, RESOLVED), IncidentCategory enum (BREAKDOWN, ACCIDENT, OBSTRUCTION, OTHER), LoginRequest(username, password), AuthResponse(token, role).

**Acceptance criteria:**
- [ ] All listed classes and enums exist in :shared commonMain.
- [ ] All classes annotated with @Serializable.
- [ ] Module compiles for Android, JVM, and Desktop without errors.
- [ ] No platform-specific imports in :shared.
- [ ] LoginRequest and AuthResponse are usable in both :backend and :composeApp.

---

### [#7](https://github.com/DevKoenv/RoadAssist/issues/7) Backend: configure Exposed ORM and database schema
**Category:** Backend / API | **Type:** Task | **Scope:** Must | **Estimate:** 3pt  
**Sub-issues:** #8

Configure Exposed ORM in :backend. Use H2 in-memory database for development and tests; SQLite file for production. Define table objects: UsersTable (id, username, passwordHash, role) and IncidentsTable (all Incident fields). Write a DatabaseFactory singleton with an init() function that establishes the connection and runs SchemaUtils.create(). Configure database path and mode via environment variables DB_MODE (h2/sqlite) and DB_PATH.

**Acceptance criteria:**
- [ ] DatabaseFactory.init() runs without error on Ktor startup.
- [ ] UsersTable and IncidentsTable created correctly on first run.
- [ ] H2 in-memory used when DB_MODE=h2.
- [ ] SQLite file created at DB_PATH when DB_MODE=sqlite.
- [ ] Changing DB_MODE without code changes works correctly.

---

### [#8](https://github.com/DevKoenv/RoadAssist/issues/8) Backend: seed database with test accounts
**Category:** Backend / API | **Type:** Task | **Scope:** Must | **Estimate:** 2pt  
**Parent:** #7

Write a DatabaseSeeder that runs after DatabaseFactory.init() on startup. It should only seed if the users table is empty. Create two accounts: username "user" / password "user123" (role: ROAD_USER) and username "dispatcher" / password "dispatch123" (role: DISPATCHER). Passwords must be hashed with bcrypt before storage. Document credentials in the README.

**Acceptance criteria:**
- [ ] Seeder runs automatically on startup if users table is empty.
- [ ] Seeder does not duplicate accounts on subsequent startups.
- [ ] Passwords are stored as bcrypt hashes, never plaintext.
- [ ] Both accounts can be used to log in via the auth endpoint.
- [ ] Credentials documented in README.

---

### [#9](https://github.com/DevKoenv/RoadAssist/issues/9) Backend: implement JWT authentication endpoint
**Category:** Backend / API | **Type:** Story | **Scope:** Must | **Estimate:** 3pt

Implement POST /auth/login in Ktor. Accept request body: LoginRequest(username, password). Look up user in database, compare password using bcrypt. On success: generate a JWT with claims sub (userId as string), role (ROAD_USER or DISPATCHER), and exp (24 hours from now). Return AuthResponse(token, role) with HTTP 200. On invalid credentials: return 401 with a plain error message. Install and configure the Ktor JWT plugin to verify tokens on all routes outside /auth.

**Acceptance criteria:**
- [ ] POST /auth/login returns 200 + AuthResponse for valid credentials.
- [ ] POST /auth/login returns 401 for wrong password.
- [ ] POST /auth/login returns 401 for unknown username.
- [ ] JWT token contains claims: sub, role, exp.
- [ ] Expired or missing token on a secured route returns 401.
- [ ] JWT secret is read from environment variable JWT_SECRET, not hardcoded.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #6 | Define shared data models in :shared | 3 |
| #7 | Backend: configure Exposed ORM and database schema | 3 |
| #8 | Backend: seed database with test accounts | 2 |
| #9 | Backend: implement JWT authentication endpoint | 3 |
| | **Total** | **11** |
