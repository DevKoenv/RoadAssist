# Contributing to RoadAssist

## Commit format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>
```

| Type | Use for |
| ---- | ------- |
| `feat` | New feature or user-visible behavior |
| `fix` | Bug fix |
| `chore` | Build, tooling, or config changes |
| `docs` | Documentation only |
| `test` | Adding or updating tests |
| `refactor` | Code structure change with no behavior change |

**Examples:**
```
feat(server): implement JWT authentication endpoint
fix(core): correct incident status enum serialization
chore: update Ktor to 3.5.1
test(server): add edge case for missing location field
```

## Branch naming

- `feature/<issue-number>-short-description` — e.g. `feature/9-jwt-auth`
- `fix/<issue-number>-short-description` — e.g. `fix/26-offline-banner`

## Pull requests

Target `staging`. Fill in the PR template before requesting review.
PRs to `master` are release merges only.

## Architecture guidelines

- **MVVM**: ViewModels in `app/shared/`. Data models in `core/`.
- **Navigation**: Use Navigation 3 (`org.jetbrains.androidx.navigation:navigation-compose`). Do not use navigation-compose 2.x.
- **Module boundaries**: `server/` must not be a dependency of any `app/` module. `core/commonMain` must stay KMP-compatible (no Android or Desktop APIs).
- **Versions**: All dependency versions go in `gradle/libs.versions.toml`. No hardcoded version strings in module build files.
