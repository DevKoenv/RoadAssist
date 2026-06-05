# Sprint 3: En Route
**Dates:** 15 mei – 21 mei  
**Goal:** Full authentication flow working end-to-end. SecureStorage, HTTP client with auth interceptor, login screen, role-based navigation, and logout all functional on both Android and Desktop.

---

## Issues

### [#10](https://github.com/DevKoenv/RoadAssist/issues/10) App: implement SecureStorage (expect/actual)
**Category:** Authentication | **Type:** Task | **Scope:** Must | **Estimate:** 2pt

Implement a SecureStorage expect/actual in :shared or :composeApp. The interface must support saveToken(token: String), getToken(): String?, and clearToken(). Android actual: use EncryptedSharedPreferences (Jetpack Security Crypto). Desktop actual: store token in an AES-256-GCM encrypted file in the platform user directory. Never store the token in plaintext.

**Acceptance criteria:**
- [ ] SecureStorage interface defined in commonMain with save, get, and clear operations.
- [ ] Android actual uses EncryptedSharedPreferences.
- [ ] Desktop actual uses AES-256-GCM encrypted file storage.
- [ ] Token survives app restart on Android.
- [ ] Token survives session restart on Desktop.
- [ ] clearToken() fully removes the token from storage.

---

### [#11](https://github.com/DevKoenv/RoadAssist/issues/11) App: configure Ktor HTTP client with auth interceptor
**Category:** Authentication | **Type:** Task | **Scope:** Must | **Estimate:** 2pt

Configure a shared Ktor Client instance in :composeApp commonMain. Install the following plugins: ContentNegotiation (kotlinx.serialization), HttpTimeout (10 second request timeout), and a custom interceptor that reads the JWT from SecureStorage and attaches it as an Authorization: Bearer <token> header on every request except POST /auth/login. Handle 401 responses globally by clearing the token and emitting a navigation event to the login screen.

**Acceptance criteria:**
- [ ] Shared HttpClient instance is created once and reused.
- [ ] All requests except /auth/login include the Authorization header.
- [ ] Request timeout set to 10 seconds.
- [ ] 401 response triggers token clear and navigation to login.
- [ ] JSON serialization uses the shared kotlinx.serialization configuration.

---

### [#12](https://github.com/DevKoenv/RoadAssist/issues/12) App: login screen UI and LoginViewModel
**Category:** Authentication | **Type:** Story | **Scope:** Must | **Estimate:** 3pt  
**Sub-issues:** #13, #14

Build the login screen in Compose Multiplatform (shared UI for Android and Desktop). The screen has a username TextField, a password TextField (obscured), and a "Log in" button. The LoginViewModel manages state: Idle, Loading, Error(message), Success(role). On submit it calls the auth repository, stores the JWT via SecureStorage, and emits Success. The UI reflects each state: loading spinner during Loading, inline error message during Error.

**Acceptance criteria:**
- [ ] Login screen renders correctly on Android and Desktop.
- [ ] Username and password fields accept input.
- [ ] Loading spinner visible during API call.
- [ ] Error message displayed for invalid credentials (401).
- [ ] Error message displayed on network timeout.
- [ ] On Success the ViewModel emits the correct role for navigation.
- [ ] JWT stored via SecureStorage after successful login.

---

### [#13](https://github.com/DevKoenv/RoadAssist/issues/13) App: role-based navigation after login
**Category:** Authentication | **Type:** Story | **Scope:** Must | **Estimate:** 2pt  
**Parent:** #12

Implement the root navigation graph using Compose Navigation (or a custom NavHost). Define routes for LoginScreen, RoadUserHome, and DispatcherHome. After login Success: navigate to RoadUserHome if role == ROAD_USER, or DispatcherHome if role == DISPATCHER. On app launch, check SecureStorage: if a token exists, navigate directly to the appropriate home screen, skipping login. Configure the back stack so back from Home does not return to Login.

**Acceptance criteria:**
- [ ] Road user navigates to RoadUserHome after login.
- [ ] Dispatcher navigates to DispatcherHome after login.
- [ ] App skips login screen on relaunch if a valid token is stored.
- [ ] Back from Home does not navigate to Login.
- [ ] Missing token on a protected route redirects to Login.

---

### [#14](https://github.com/DevKoenv/RoadAssist/issues/14) App: log out
**Category:** Authentication | **Type:** Story | **Scope:** Should | **Estimate:** 1pt  
**Parent:** #12

Add a log out action accessible from the top app bar or a settings/overflow menu on all home screens. Tapping Log out calls SecureStorage.clearToken(), clears the navigation back stack, and navigates to LoginScreen.

**Acceptance criteria:**
- [ ] Log out action visible on home screens.
- [ ] Token cleared from SecureStorage on logout.
- [ ] Navigation back stack cleared on logout.
- [ ] Back button after logout does not return to a protected screen.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #10 | App: implement SecureStorage (expect/actual) | 2 |
| #11 | App: configure Ktor HTTP client with auth interceptor | 2 |
| #12 | App: login screen UI and LoginViewModel | 3 |
| #13 | App: role-based navigation after login | 2 |
| #14 | App: log out | 1 |
| | **Total** | **10** |
