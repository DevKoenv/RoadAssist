# Sprint 6: Inspection
**Dates:** 5 juni – 11 juni  
**Goal:** Should-have features complete. Full test coverage for business logic and backend endpoints. All Must-scope issues closed. Codebase ready for final review.

---

## Issues

### [#27](https://github.com/DevKoenv/RoadAssist/issues/27) Dispatcher: filter bar (status and category)
**Category:** Dispatcher | **Type:** Story | **Scope:** Should | **Estimate:** 2pt  
**Parent:** #24

Add a filter bar above the incident list on the dispatcher home screen. Filters: status chips (All / NEW / IN_PROGRESS / EN_ROUTE / RESOLVED) and category chips (All / BREAKDOWN / ACCIDENT / OBSTRUCTION / OTHER). Filtering is applied client-side to the loaded dataset without additional API calls. Active filters shown as highlighted chips. Multiple filters combine with AND logic. A "Clear filters" option resets to showing all.

**Acceptance criteria:**
- [ ] Filter bar shows status and category chips.
- [ ] Selecting a status chip filters the list client-side.
- [ ] Selecting a category chip filters the list client-side.
- [ ] Combined status + category filters apply AND logic.
- [ ] Active filters are visually highlighted.
- [ ] Clear filters restores the full list.
- [ ] Filter state preserved during auto-poll refresh.

---

### [#28](https://github.com/DevKoenv/RoadAssist/issues/28) Backend: validate and sanitise notes field
**Category:** Backend / API | **Type:** Task | **Scope:** Should | **Estimate:** 1pt  
**Parent:** #16

The notes field accepted by PATCH /incidents/{id}/status should be validated and sanitised. Maximum length: 1000 characters. Strip leading/trailing whitespace. Reject requests where notes exceeds the limit with HTTP 422 and a clear error message.

**Acceptance criteria:**
- [ ] Notes saved correctly when within 1000 characters.
- [ ] Notes exceeding 1000 characters return 422.
- [ ] Leading and trailing whitespace stripped before saving.
- [ ] Error message in 422 response is descriptive.

---

### [#29](https://github.com/DevKoenv/RoadAssist/issues/29) Unit tests: ViewModels and business logic
**Category:** Quality / Tests | **Type:** Task | **Scope:** Must | **Estimate:** 3pt

Write unit tests using kotlin.test in commonTest (no device, no screen required). Cover: (1) LoginViewModel: successful login transitions state to Success with correct role. (2) LoginViewModel: network error transitions state to Error. (3) LoginViewModel: 401 response transitions state to Error with credential message. (4) NewIncidentViewModel: submitting without description emits validation error. (5) IncidentRepository: createIncident called with correct parameters. (6) Offline detection: IOException from any repository call emits offline state. (7) Status filter logic: combined status+category filter returns correct subset. (8) HistoryViewModel: only RESOLVED incidents are loaded. Target: at least 8 passing tests.

**Acceptance criteria:**
- [ ] Minimum 8 tests defined and passing.
- [ ] All tests run in commonTest without an emulator or device.
- [ ] Tests are included in the CI workflow and block merge on failure.
- [ ] Each test class and method is clearly named.

---

### [#30](https://github.com/DevKoenv/RoadAssist/issues/30) Integration tests: backend endpoint happy paths
**Category:** Quality / Tests | **Type:** Task | **Scope:** Must | **Estimate:** 2pt

Write integration tests in :backend using Ktor's testApplication { } builder and an in-memory H2 database. Test the happy path for each endpoint group: POST /auth/login returns 200 + token, POST /incidents returns 201, GET /incidents returns correct role-filtered list, GET /incidents/{id} returns correct incident, PATCH /incidents/{id}/status returns 200 + updated incident. All tests must be stateless (fresh DB per test class).

**Acceptance criteria:**
- [ ] POST /auth/login happy path test passes.
- [ ] POST /incidents happy path test passes and verifies 201 response.
- [ ] GET /incidents validates road user sees only own incidents.
- [ ] GET /incidents validates dispatcher sees all incidents.
- [ ] PATCH /incidents/{id}/status test passes for dispatcher role.
- [ ] All tests run without external services or a running server.
- [ ] Tests included in CI workflow.

---

### [#31](https://github.com/DevKoenv/RoadAssist/issues/31) Integration tests: auth and authorisation edge cases
**Category:** Quality / Tests | **Type:** Task | **Scope:** Must | **Estimate:** 2pt

Extend the backend integration tests to cover auth and authorisation failure scenarios: wrong credentials return 401, missing JWT returns 401, ROAD_USER accessing another user's incident returns 403, ROAD_USER attempting PATCH /status returns 403, photo upload by non-owner returns 403.

**Acceptance criteria:**
- [ ] Wrong credentials test returns 401.
- [ ] Missing JWT test returns 401 on a secured endpoint.
- [ ] ROAD_USER accessing another user's incident returns 403.
- [ ] ROAD_USER attempting PATCH /status returns 403.
- [ ] Non-owner photo upload attempt returns 403.
- [ ] All tests stateless and run in CI.

---

### [#32](https://github.com/DevKoenv/RoadAssist/issues/32) Road user: push notification on status change (Android)
**Category:** Road User | **Type:** Story | **Scope:** Could | **Estimate:** 3pt

Integrate Firebase Cloud Messaging (FCM) for Android push notifications. When the dispatcher updates an incident's status, the backend sends an FCM data message to the incident owner's device token. The road user app receives the notification and shows a system notification with the new status. Requires: FCM SDK added to Android, a device token registration endpoint on the backend, and google-services.json. Optional extension per FR-16.

**Acceptance criteria:**
- [ ] Android app registers FCM device token on login.
- [ ] Backend stores device token per user.
- [ ] PATCH /incidents/{id}/status triggers FCM message to incident owner.
- [ ] System notification displayed on Android with incident ID and new status.
- [ ] google-services.json setup documented in README.
- [ ] Desktop: no notification (out of scope, explicitly noted).

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #27 | Dispatcher: filter bar (status and category) | 2 |
| #28 | Backend: validate and sanitise notes field | 1 |
| #29 | Unit tests: ViewModels and business logic | 3 |
| #30 | Integration tests: backend endpoint happy paths | 2 |
| #31 | Integration tests: auth and authorisation edge cases | 2 |
| #32 | Road user: push notification on status change (Android) | 3 |
| | **Total** | **13** |
