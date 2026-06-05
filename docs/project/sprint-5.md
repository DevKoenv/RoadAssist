# Sprint 5: Test Drive
**Dates:** 29 mei – 4 juni  
**Goal:** All user-facing screens complete for both roles. Road user can view active incidents, history, and detail. Dispatcher can view and update all incidents. Offline error handling in place.

---

## Issues

### [#21](https://github.com/DevKoenv/RoadAssist/issues/21) Road user: "Active incidents" screen and ViewModel
**Category:** Road User | **Type:** Story | **Scope:** Must | **Estimate:** 2pt

Build the road user home screen. A LazyColumn lists all own incidents with status != RESOLVED, sorted by createdAt DESC. Each row shows: a category icon, truncated description (2 lines max), relative timestamp, and a status badge. An ActiveIncidentsViewModel loads data from the repository on init and exposes a refresh() function. Pull-to-refresh on Android; a refresh icon button in the top bar on Desktop. Empty state: centered message "No active incidents" and a primary button "Report an incident".

**Acceptance criteria:**
- [ ] Only non-resolved incidents of the logged-in user are shown.
- [ ] Rows show category icon, truncated description, timestamp, and status badge.
- [ ] Pull-to-refresh works on Android.
- [ ] Refresh button in top bar works on Desktop.
- [ ] Empty state visible with message and button when list is empty.
- [ ] Tapping a row navigates to the incident detail screen.

---

### [#22](https://github.com/DevKoenv/RoadAssist/issues/22) Road user: "Incident detail" screen
**Category:** Road User | **Type:** Story | **Scope:** Must | **Estimate:** 2pt

Build the read-only detail screen for a single road user incident. Show: category chip, full description, location as latitude/longitude coordinates, photo (loaded asynchronously via Coil or Ktor client; placeholder shown during load; hidden if no photo), status badge, and dispatcher notes section (hidden if empty). A back button returns to the previous screen. No editing controls visible to the road user.

**Acceptance criteria:**
- [ ] All incident fields displayed correctly.
- [ ] Photo loads asynchronously; placeholder shown during load.
- [ ] Photo section hidden when photoUrl is null.
- [ ] Dispatcher notes section hidden when notes is null or blank.
- [ ] Status badge matches current incident status.
- [ ] Back navigation returns to previous screen correctly.

---

### [#23](https://github.com/DevKoenv/RoadAssist/issues/23) Road user: "History" screen and ViewModel
**Category:** Road User | **Type:** Story | **Scope:** Must | **Estimate:** 2pt

Add a History screen accessible from a bottom navigation bar or a top bar overflow menu. A HistoryViewModel loads all RESOLVED incidents of the logged-in user, sorted by updatedAt DESC. Display in a LazyColumn. Each row shows: category, truncated description, resolution timestamp. Tapping a row navigates to the incident detail screen (same read-only screen as active incidents). Empty state: "No resolved incidents yet."

**Acceptance criteria:**
- [ ] Only RESOLVED incidents of the logged-in user are shown.
- [ ] Sorted by most recently resolved first.
- [ ] Tapping a row opens the read-only detail screen.
- [ ] Empty state message visible when list is empty.
- [ ] Screen accessible from bottom nav or overflow menu.

---

### [#24](https://github.com/DevKoenv/RoadAssist/issues/24) Dispatcher: "All incidents" screen and ViewModel
**Category:** Dispatcher | **Type:** Story | **Scope:** Must | **Estimate:** 2pt

Build the dispatcher home screen. An AllIncidentsViewModel loads all incidents from the repository, sorted by createdAt DESC, and polls for updates every 30 seconds. Display in a LazyColumn. Each row shows: category chip, truncated description, a user identifier (userId), relative timestamp, and status badge. A refresh button in the top bar triggers an immediate reload.

**Acceptance criteria:**
- [ ] All incidents from all users are displayed.
- [ ] Sorted by createdAt descending (newest first).
- [ ] Auto-poll every 30 seconds refreshes the list silently.
- [ ] Manual refresh button triggers immediate reload.
- [ ] Tapping a row navigates to the dispatcher detail screen.
- [ ] Status badge colour matches current status.

---

### [#25](https://github.com/DevKoenv/RoadAssist/issues/25) Dispatcher: "Incident detail + update status" screen and ViewModel
**Category:** Dispatcher | **Type:** Story | **Scope:** Must | **Estimate:** 3pt

Build the dispatcher detail screen. Show all incident fields (same layout as road user detail). Additionally: a status dropdown (all four IncidentStatus values), a notes TextField (optional, pre-filled if notes exist), and a Save button. A DispatcherDetailViewModel sends PATCH /incidents/{id}/status on save. Use optimistic UI: update the local state immediately on tap, then confirm or rollback based on the API response. On error: show a snackbar and revert the status to its previous value.

**Acceptance criteria:**
- [ ] All incident fields displayed.
- [ ] Status dropdown shows current status as selected value.
- [ ] Notes field pre-filled with existing notes.
- [ ] Save button sends PATCH request.
- [ ] Optimistic update: status changes immediately in UI before API responds.
- [ ] On PATCH error: snackbar shown and status reverted.
- [ ] On PATCH success: updated incident reflected in the list on back navigation.

---

### [#26](https://github.com/DevKoenv/RoadAssist/issues/26) App: offline/error banner
**Category:** Road User | **Type:** Story | **Scope:** Must | **Estimate:** 2pt

Add a global network error banner to the app shell (above the main content area, below the top bar). The banner shows "No connection to the server — check your internet connection" when any API call fails with an IOException or a timeout. It disappears automatically when the next successful API response is received. Previously loaded data remains visible during the offline state. Applies to all screens.

**Acceptance criteria:**
- [ ] Banner appears on IOException or timeout on any screen.
- [ ] Banner text is clear and non-technical.
- [ ] Banner disappears automatically when connectivity is restored.
- [ ] No crash occurs during network failure.
- [ ] Previously loaded list data remains visible behind the banner.
- [ ] Satisfies FR-12 and V-10.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #21 | Road user: "Active incidents" screen and ViewModel | 2 |
| #22 | Road user: "Incident detail" screen | 2 |
| #23 | Road user: "History" screen and ViewModel | 2 |
| #24 | Dispatcher: "All incidents" screen and ViewModel | 2 |
| #25 | Dispatcher: "Incident detail + update status" screen and ViewModel | 3 |
| #26 | App: offline/error banner | 2 |
| | **Total** | **13** |
