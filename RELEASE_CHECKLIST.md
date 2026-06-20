# RoadAssist Delivery Checklist

Verification items from section 11 of the RoadAssist case document (v1.4).
Test on both Android and Desktop before submission. Mark each item Pass or Fail with notes.

| # | Requirement | How to verify | Android | Desktop | Notes |
|---|-------------|---------------|---------|---------|-------|
| V-01 | App runs on Android and Desktop (Windows or Linux) | Live demo on both platforms | | | |
| V-02 | Login works; app shows the correct view per role | Live demo with `user` (road user) and `dispatcher` accounts | | | |
| V-03 | Road user can submit a new incident with location and optional photo | Live demo: create an incident, attach a photo | | | |
| V-04 | Location is retrieved automatically via the device location function | Live demo on Android: location field populated without manual input | | N/A (desktop uses manual entry) | |
| V-05 | Road user can view active incidents and history in separate views | Live demo: switch between Active and History tabs | | | |
| V-06 | Dispatcher can update incident status | Live demo: open an incident, change status, confirm change | | | |
| V-07 | App has its own backend that manages all data | Code review (`server/`) and live demo | | | |
| V-08 | Shared data models are in a separate module used by both backend and app | Code review: `core/` module imported by both `server/` and `app/shared/` | | | |
| V-09 | App follows a layered structure with UI and logic separated | Code review: ViewModels in `app/shared/`, Composables in screen files | | | |
| V-10 | App shows a banner when the backend is not reachable | Stop the server, observe the offline banner within 10 seconds | | | |
| V-11 | Project is in Git with regular, clear commits | `git log --oneline`: commits span the project timeline with conventional commit messages | | | |
| V-12 | At least 3 automated tests for business logic | `./gradlew :core:jvmTest :server:test :app:shared:jvmTest` passes | | | |

## How to run

```bash
# Backend
JWT_SECRET=changeme ./gradlew :server:run

# Android
./gradlew :app:androidApp:installDebug

# Desktop
./gradlew :app:desktopApp:run

# Tests
./gradlew :core:jvmTest :server:test :app:shared:jvmTest
```

## Seed accounts

| Username | Password | Role |
|----------|----------|------|
| `user` | `user123` | Road user |
| `dispatcher` | `dispatch123` | Dispatcher |
