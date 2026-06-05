# Sprint 7: Road Ready
**Dates:** 12 juni – 19 juni  
**Goal:** Final polish, documentation, and submission preparation. Delivery checklist verified on both platforms. README complete. Git history clean. Optional Could-scope features tackled if time allows.

---

## Issues

### [#33](https://github.com/DevKoenv/RoadAssist/issues/33) Dispatcher: map view of incident location
**Category:** Dispatcher | **Type:** Story | **Scope:** Could | **Estimate:** 3pt  
**Parent:** #25

Show the incident location on a map in the dispatcher detail screen. Android: integrate OSMDroid (OpenStreetMap tiles, no API key required). Add a map composable below the location coordinates showing a marker at the incident's latitude/longitude. Desktop: render the coordinates as a clickable text link that opens https://www.openstreetmap.org/?mlat={lat}&mlon={lon} in the system browser. Optional extension per FR-17.

**Acceptance criteria:**
- [ ] Android: map composable renders below the coordinates section.
- [ ] Android: marker placed at correct latitude/longitude.
- [ ] Android: map loads without crash when location data is present.
- [ ] Desktop: coordinates render as a clickable link.
- [ ] Desktop: link opens correct OpenStreetMap URL in browser.
- [ ] No external API key required.

---

### [#34](https://github.com/DevKoenv/RoadAssist/issues/34) Run through delivery checklist V-01 to V-12
**Category:** Quality / Tests | **Type:** Chore | **Scope:** Must | **Estimate:** 2pt

Execute all minimum delivery requirements from section 11 of the RoadAssist case document. Test on a physical Android device or emulator AND on Desktop (Windows or Linux). Document the result of each V-number as Pass or Fail with notes. Fix all Fail results before submission. Save results as RELEASE_CHECKLIST.md in the repository root.

**Acceptance criteria:**
- [ ] V-01 through V-12 all tested on Android and Desktop.
- [ ] All V-numbers marked Pass before submission.
- [ ] Any fixes applied and re-tested.
- [ ] RELEASE_CHECKLIST.md committed to the repository.

---

### [#35](https://github.com/DevKoenv/RoadAssist/issues/35) Write complete README
**Category:** Ops / Docs | **Type:** Chore | **Scope:** Must | **Estimate:** 2pt

Write a thorough README.md. Sections: (1) Project description with a brief feature overview and a screenshot or two. (2) Requirements: JDK 17+, Android SDK (API 26+), Gradle 8.x. (3) How to start the backend: environment variables (DB_MODE, DB_PATH, JWT_SECRET), command to run (./gradlew :backend:run). (4) How to build and run on Android (./gradlew :composeApp:installDebug). (5) How to run on Desktop (./gradlew :composeApp:run). (6) Seed accounts. (7) Running tests (./gradlew test). Satisfies NF-06 and V-06.

**Acceptance criteria:**
- [ ] All seven sections present and accurate.
- [ ] Backend start instructions verified on a clean machine.
- [ ] Android and Desktop build instructions verified.
- [ ] Seed account credentials listed.
- [ ] Environment variable names and descriptions complete.
- [ ] ./gradlew test command documented.

---

### [#36](https://github.com/DevKoenv/RoadAssist/issues/36) Review Git history and commit quality
**Category:** Ops / Docs | **Type:** Chore | **Scope:** Must | **Estimate:** 1pt

Review the full Git log before submission. Check: commits are spread across the project timeline (no single mega-commit at the end), commit messages follow the conventional commit format (feat/fix/chore/docs/test), no secrets or .env files appear anywhere in history, no large binary files committed accidentally. If needed, amend recent commit messages via git rebase -i. Satisfies V-11.

**Acceptance criteria:**
- [ ] Git log shows commits spread across all 7 sprint periods.
- [ ] No passwords, tokens, or .env files present in any commit.
- [ ] Commit messages follow conventional commit format.
- [ ] No unintended binary or generated files in history.

---

### [#37](https://github.com/DevKoenv/RoadAssist/issues/37) Code review pass: add KDoc to public APIs
**Category:** Quality / Tests | **Type:** Chore | **Scope:** Should | **Estimate:** 1pt

Review all public classes and functions in :shared, :backend, and :composeApp. Add KDoc comments to anything non-obvious. Priority targets: SecureStorage (explain encryption approach), LocationProvider (explain platform differences), MediaPicker (explain platform differences), DatabaseFactory (explain init flow), JWT configuration (explain claims and expiry). Remove any dead code, commented-out blocks, or TODO comments not tracked as issues.

**Acceptance criteria:**
- [ ] SecureStorage, LocationProvider, MediaPicker, DatabaseFactory, and JWT config all have KDoc.
- [ ] No dead code or orphaned TODOs in final codebase.
- [ ] All public expect/actual declarations have comments explaining platform behaviour.

---

### [#38](https://github.com/DevKoenv/RoadAssist/issues/38) Explore Compose for Web / WASM target
**Category:** Ops / Docs | **Type:** Chore | **Scope:** Could | **Estimate:** 2pt

Explore adding a web target to :composeApp via Compose for Web (WASM/JS). Add the target to the Gradle configuration and attempt to compile. Document what works, what requires platform-specific workarounds (SecureStorage, LocationProvider, MediaPicker), and whether the login screen is functional in a browser. Only attempt if all Must and Should tasks are complete. Satisfies FR-18 optionally.

**Acceptance criteria:**
- [ ] Web target added to :composeApp Gradle config.
- [ ] Project compiles for WASM/JS target without errors.
- [ ] Login screen renders and is interactive in a browser.
- [ ] Platform-specific limitations documented in README.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #33 | Dispatcher: map view of incident location | 3 |
| #34 | Run through delivery checklist V-01 to V-12 | 2 |
| #35 | Write complete README | 2 |
| #36 | Review Git history and commit quality | 1 |
| #37 | Code review pass: add KDoc to public APIs | 1 |
| #38 | Explore Compose for Web / WASM target | 2 |
| | **Total** | **11** |
