# RoadAssist – Issue Index

All 38 issues tracked in [DevKoenv/RoadAssist](https://github.com/DevKoenv/RoadAssist).

| #   | Title | Category | Type | Scope | Sprint |
| --- | ----- | -------- | ---- | ----- | ------ |
| [#1](https://github.com/DevKoenv/RoadAssist/issues/1)   | Create GitHub repository and configure branch strategy          | Project Setup   | Chore | Must   | Sprint 1 |
| [#2](https://github.com/DevKoenv/RoadAssist/issues/2)   | Initialise KMP multi-module Gradle project structure            | Project Setup   | Chore | Must   | Sprint 1 |
| [#3](https://github.com/DevKoenv/RoadAssist/issues/3)   | Tech spike: validate KMP + Ktor Server + Compose compatibility  | Project Setup   | Spike | Must   | Sprint 1 |
| [#4](https://github.com/DevKoenv/RoadAssist/issues/4)   | Set up GitHub Actions CI workflow                               | Project Setup   | Chore | Must   | Sprint 1 |
| [#5](https://github.com/DevKoenv/RoadAssist/issues/5)   | Configure commit conventions and PR template                    | Project Setup   | Chore | Must   | Sprint 1 |
| [#6](https://github.com/DevKoenv/RoadAssist/issues/6)   | Define shared data models in :shared                            | Backend / API   | Task  | Must   | Sprint 2 |
| [#7](https://github.com/DevKoenv/RoadAssist/issues/7)   | Backend: configure Exposed ORM and database schema              | Backend / API   | Task  | Must   | Sprint 2 |
| [#8](https://github.com/DevKoenv/RoadAssist/issues/8)   | Backend: seed database with test accounts                       | Backend / API   | Task  | Must   | Sprint 2 |
| [#9](https://github.com/DevKoenv/RoadAssist/issues/9)   | Backend: implement JWT authentication endpoint                  | Backend / API   | Story | Must   | Sprint 2 |
| [#10](https://github.com/DevKoenv/RoadAssist/issues/10) | App: implement SecureStorage (expect/actual)                    | Authentication  | Task  | Must   | Sprint 3 |
| [#11](https://github.com/DevKoenv/RoadAssist/issues/11) | App: configure Ktor HTTP client with auth interceptor           | Authentication  | Task  | Must   | Sprint 3 |
| [#12](https://github.com/DevKoenv/RoadAssist/issues/12) | App: login screen UI and LoginViewModel                         | Authentication  | Story | Must   | Sprint 3 |
| [#13](https://github.com/DevKoenv/RoadAssist/issues/13) | App: role-based navigation after login                          | Authentication  | Story | Must   | Sprint 3 |
| [#14](https://github.com/DevKoenv/RoadAssist/issues/14) | App: log out                                                    | Authentication  | Story | Should | Sprint 3 |
| [#15](https://github.com/DevKoenv/RoadAssist/issues/15) | Backend: incident CRUD endpoints                                | Backend / API   | Story | Must   | Sprint 4 |
| [#16](https://github.com/DevKoenv/RoadAssist/issues/16) | Backend: PATCH status and notes endpoint                        | Backend / API   | Story | Must   | Sprint 4 |
| [#17](https://github.com/DevKoenv/RoadAssist/issues/17) | Backend: photo upload endpoint                                  | Backend / API   | Task  | Must   | Sprint 4 |
| [#18](https://github.com/DevKoenv/RoadAssist/issues/18) | App: LocationProvider (expect/actual)                           | Road User       | Task  | Must   | Sprint 4 |
| [#19](https://github.com/DevKoenv/RoadAssist/issues/19) | App: MediaPicker (expect/actual)                                | Road User       | Task  | Must   | Sprint 4 |
| [#20](https://github.com/DevKoenv/RoadAssist/issues/20) | Road user: "New incident" screen and ViewModel                  | Road User       | Story | Must   | Sprint 4 |
| [#21](https://github.com/DevKoenv/RoadAssist/issues/21) | Road user: "Active incidents" screen and ViewModel              | Road User       | Story | Must   | Sprint 5 |
| [#22](https://github.com/DevKoenv/RoadAssist/issues/22) | Road user: "Incident detail" screen                             | Road User       | Story | Must   | Sprint 5 |
| [#23](https://github.com/DevKoenv/RoadAssist/issues/23) | Road user: "History" screen and ViewModel                       | Road User       | Story | Must   | Sprint 5 |
| [#24](https://github.com/DevKoenv/RoadAssist/issues/24) | Dispatcher: "All incidents" screen and ViewModel                | Dispatcher      | Story | Must   | Sprint 5 |
| [#25](https://github.com/DevKoenv/RoadAssist/issues/25) | Dispatcher: "Incident detail + update status" screen and ViewModel | Dispatcher   | Story | Must   | Sprint 5 |
| [#26](https://github.com/DevKoenv/RoadAssist/issues/26) | App: offline/error banner                                       | Road User       | Story | Must   | Sprint 5 |
| [#27](https://github.com/DevKoenv/RoadAssist/issues/27) | Dispatcher: filter bar (status and category)                    | Dispatcher      | Story | Should | Sprint 6 |
| [#28](https://github.com/DevKoenv/RoadAssist/issues/28) | Backend: validate and sanitise notes field                      | Backend / API   | Task  | Should | Sprint 6 |
| [#29](https://github.com/DevKoenv/RoadAssist/issues/29) | Unit tests: ViewModels and business logic                       | Quality / Tests | Task  | Must   | Sprint 6 |
| [#30](https://github.com/DevKoenv/RoadAssist/issues/30) | Integration tests: backend endpoint happy paths                 | Quality / Tests | Task  | Must   | Sprint 6 |
| [#31](https://github.com/DevKoenv/RoadAssist/issues/31) | Integration tests: auth and authorisation edge cases            | Quality / Tests | Task  | Must   | Sprint 6 |
| [#32](https://github.com/DevKoenv/RoadAssist/issues/32) | Road user: push notification on status change (Android)         | Road User       | Story | Could  | Sprint 6 |
| [#33](https://github.com/DevKoenv/RoadAssist/issues/33) | Dispatcher: map view of incident location                       | Dispatcher      | Story | Could  | Sprint 7 |
| [#34](https://github.com/DevKoenv/RoadAssist/issues/34) | Run through delivery checklist V-01 to V-12                     | Quality / Tests | Chore | Must   | Sprint 7 |
| [#35](https://github.com/DevKoenv/RoadAssist/issues/35) | Write complete README                                           | Ops / Docs      | Chore | Must   | Sprint 7 |
| [#36](https://github.com/DevKoenv/RoadAssist/issues/36) | Review Git history and commit quality                           | Ops / Docs      | Chore | Must   | Sprint 7 |
| [#37](https://github.com/DevKoenv/RoadAssist/issues/37) | Code review pass: add KDoc to public APIs                       | Quality / Tests | Chore | Should | Sprint 7 |
| [#38](https://github.com/DevKoenv/RoadAssist/issues/38) | Explore Compose for Web / WASM target                           | Ops / Docs      | Chore | Could  | Sprint 7 |

---

## By sprint

- [Sprint 1: Blueprint](./sprint-1.md) — 1 mei – 7 mei — issues #1–#5
- [Sprint 2: Groundwork](./sprint-2.md) — 8 mei – 14 mei — issues #6–#9
- [Sprint 3: En Route](./sprint-3.md) — 15 mei – 21 mei — issues #10–#14
- [Sprint 4: Under the Hood](./sprint-4.md) — 22 mei – 28 mei — issues #15–#20
- [Sprint 5: Test Drive](./sprint-5.md) — 29 mei – 4 juni — issues #21–#26
- [Sprint 6: Inspection](./sprint-6.md) — 5 juni – 11 juni — issues #27–#32
- [Sprint 7: Road Ready](./sprint-7.md) — 12 juni – 19 juni — issues #33–#38
