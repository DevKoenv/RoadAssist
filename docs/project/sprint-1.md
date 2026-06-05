# Sprint 1: Blueprint
**Dates:** 1 mei – 7 mei  
**Goal:** Project foundation — repository, monorepo structure, CI pipeline, and commit conventions in place. Everything subsequent sprints depend on.

---

## Issues

### [#1](https://github.com/DevKoenv/RoadAssist/issues/1) Create GitHub repository and configure branch strategy
**Category:** Project Setup | **Type:** Chore | **Scope:** Must | **Estimate:** 2pt

Create the RoadAssist GitHub repository. Set visibility to private. Add a .gitignore for Kotlin/Gradle, a stub README.md, and the MIT license. Configure branch protection on main (PR required). Create the develop branch as the default working branch. Document the branch strategy (main/develop/feature/*) in the README.

**Acceptance criteria:**
- [ ] Repository created and visible on GitHub.
- [ ] main branch has branch protection: PR required to merge.
- [ ] develop branch exists as default working branch.
- [ ] .gitignore covers Kotlin, Gradle, and IDE files.
- [ ] README.md stub is present.
- [ ] Branch strategy documented in README.

---

### [#2](https://github.com/DevKoenv/RoadAssist/issues/2) Initialise KMP multi-module Gradle project structure
**Category:** Project Setup | **Type:** Chore | **Scope:** Must | **Estimate:** 3pt  
**Sub-issues:** #3

Set up the Kotlin Multiplatform project using the KMP wizard or manual Gradle configuration. Define three modules: :shared (commonMain with expect/actual, targets Android + JVM + Desktop), :backend (JVM-only Ktor Server), :composeApp (Android + Desktop targets). Use a version catalog (libs.versions.toml) for all dependency versions. Verify the build succeeds on a clean checkout.

**Acceptance criteria:**
- [ ] :shared compiles for Android, JVM, and Desktop without errors.
- [ ] :backend is JVM-only and does not include Android or Desktop targets.
- [ ] :composeApp includes Android and Desktop (JVM) targets.
- [ ] gradle build succeeds on a clean checkout with no warnings about missing versions.
- [ ] libs.versions.toml contains all dependency versions; no hardcoded versions in build files.

---

### [#3](https://github.com/DevKoenv/RoadAssist/issues/3) Tech spike: validate KMP + Ktor Server + Compose compatibility
**Category:** Project Setup | **Type:** Spike | **Scope:** Must | **Estimate:** 2pt  
**Parent:** #2

Before committing to the full architecture, validate that Ktor Server (JVM) and Compose Multiplatform coexist in the same Gradle multi-module build without classpath conflicts. Also verify kotlinx.serialization works across all targets. Timebox to half a day. Deliverable: a short ADR or README section with findings, confirmed library versions, and any known constraints.

**Acceptance criteria:**
- [ ] A minimal Ktor "hello world" endpoint runs from :backend alongside a Compose Desktop window from :composeApp.
- [ ] No dependency conflicts reported by Gradle dependency insight.
- [ ] kotlinx.serialization encodes and decodes a shared data class in both :backend and :composeApp.
- [ ] Findings and confirmed versions documented in docs/adr/001-tech-stack.md or equivalent.

---

### [#4](https://github.com/DevKoenv/RoadAssist/issues/4) Set up GitHub Actions CI workflow
**Category:** Project Setup | **Type:** Chore | **Scope:** Must | **Estimate:** 2pt

Create .github/workflows/ci.yml. The workflow must trigger on every push to develop and on pull requests targeting main. It should run two jobs in sequence: (1) build all Gradle modules, (2) run all unit tests. Configure Gradle dependency caching to keep run times reasonable.

**Acceptance criteria:**
- [ ] Workflow file exists at .github/workflows/ci.yml.
- [ ] Workflow triggers on push to develop.
- [ ] Workflow triggers on pull requests to main.
- [ ] Build job fails and blocks merge on any compile error.
- [ ] Test job fails and blocks merge on any failing test.
- [ ] Gradle cache is configured; a second run uses the cache.

---

### [#5](https://github.com/DevKoenv/RoadAssist/issues/5) Configure commit conventions and PR template
**Category:** Project Setup | **Type:** Chore | **Scope:** Must | **Estimate:** 1pt

Add a PULL_REQUEST_TEMPLATE.md to .github/ with a standard checklist (description of change, linked issue, tests passing, self-review done). Document conventional commit format (feat/fix/chore/docs/test) in CONTRIBUTING.md or the README.

**Acceptance criteria:**
- [ ] PR template exists at .github/PULL_REQUEST_TEMPLATE.md.
- [ ] Template includes: description, linked issue, checklist.
- [ ] Commit convention documented.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #1 | Create GitHub repository and configure branch strategy | 2 |
| #2 | Initialise KMP multi-module Gradle project structure | 3 |
| #3 | Tech spike: validate KMP + Ktor Server + Compose compatibility | 2 |
| #4 | Set up GitHub Actions CI workflow | 2 |
| #5 | Configure commit conventions and PR template | 1 |
| | **Total** | **10** |
