# Sprint 4: Under the Hood
**Dates:** 22 mei – 28 mei  
**Goal:** All backend incident endpoints implemented and secured. Platform-specific capabilities (location, camera) abstracted behind expect/actual. New incident creation fully functional end-to-end.

---

## Issues

### [#15](https://github.com/DevKoenv/RoadAssist/issues/15) Backend: incident CRUD endpoints
**Category:** Backend / API | **Type:** Story | **Scope:** Must | **Estimate:** 3pt  
**Sub-issues:** #16, #17

Implement the core incident endpoints in Ktor:
- POST /incidents (ROAD_USER only): create incident with category, description, latitude, longitude. Set status to NEW, createdAt and updatedAt to now. Return 201 + Incident.
- GET /incidents: ROAD_USER gets own incidents sorted by createdAt DESC. DISPATCHER gets all incidents sorted by createdAt DESC. Return 200 + List<Incident>.
- GET /incidents/{id}: ROAD_USER may only access own incidents (403 otherwise). DISPATCHER unrestricted. Return 200 + Incident.
All endpoints require a valid JWT (401 if missing/invalid).

**Acceptance criteria:**
- [ ] POST /incidents returns 201 + Incident with status NEW.
- [ ] POST /incidents by DISPATCHER returns 403.
- [ ] GET /incidents for ROAD_USER returns only their own incidents.
- [ ] GET /incidents for DISPATCHER returns all incidents.
- [ ] GET /incidents/{id} returns 403 when ROAD_USER requests another user's incident.
- [ ] All endpoints return 401 without a valid JWT.

---

### [#16](https://github.com/DevKoenv/RoadAssist/issues/16) Backend: PATCH status and notes endpoint
**Category:** Backend / API | **Type:** Story | **Scope:** Must | **Estimate:** 2pt  
**Parent:** #15

Implement PATCH /incidents/{id}/status (DISPATCHER only). Request body: { status: IncidentStatus, notes: String? }. Update the incident status and notes in the database, set updatedAt to now. Return 200 + updated Incident. ROAD_USER attempting this endpoint receives 403.

**Acceptance criteria:**
- [ ] PATCH by DISPATCHER updates status and returns 200 + Incident.
- [ ] PATCH with optional notes field saves the note correctly.
- [ ] PATCH by ROAD_USER returns 403.
- [ ] updatedAt is updated on every successful PATCH.
- [ ] Invalid status value returns 400.

---

### [#17](https://github.com/DevKoenv/RoadAssist/issues/17) Backend: photo upload endpoint
**Category:** Backend / API | **Type:** Task | **Scope:** Must | **Estimate:** 2pt  
**Parent:** #15

Implement POST /incidents/{id}/photo. Accept multipart/form-data with a single file field named "photo". Validate: file must be image/jpeg or image/png, max 5 MB. Save the file to a local /uploads/ directory using the incident ID and a UUID as filename. Update the incident's photoUrl field. Return 200 + updated Incident. Add a static file server route so /uploads/{filename} serves the file. Only the incident owner or a DISPATCHER may upload.

**Acceptance criteria:**
- [ ] Valid JPEG/PNG upload returns 200 + Incident with photoUrl set.
- [ ] Upload exceeding 5 MB returns 413.
- [ ] Upload with wrong MIME type returns 415.
- [ ] Uploaded file is accessible via GET /uploads/{filename}.
- [ ] Upload by a non-owner ROAD_USER returns 403.

---

### [#18](https://github.com/DevKoenv/RoadAssist/issues/18) App: LocationProvider (expect/actual)
**Category:** Road User | **Type:** Task | **Scope:** Must | **Estimate:** 3pt

Implement a LocationProvider with expect/actual. The shared interface exposes a suspend fun getCurrentLocation(): LatLon? where LatLon(latitude: Double, longitude: Double) is defined in :shared. Android actual: use FusedLocationProviderClient. Handle the ACCESS_FINE_LOCATION runtime permission within the Composable that calls the provider; show a rationale dialog if denied. Desktop actual: provide a manual entry dialog (latitude/longitude TextFields) as a fallback.

**Acceptance criteria:**
- [ ] LocationProvider interface defined in commonMain.
- [ ] Android actual returns GPS coordinates after permission grant.
- [ ] Android: denying permission shows a rationale dialog with a manual entry fallback.
- [ ] Desktop actual shows a manual coordinate entry dialog.
- [ ] LatLon is defined in :shared and usable in both targets.
- [ ] LocationProvider is injectable/replaceable for use in unit tests.

---

### [#19](https://github.com/DevKoenv/RoadAssist/issues/19) App: MediaPicker (expect/actual)
**Category:** Road User | **Type:** Task | **Scope:** Must | **Estimate:** 2pt

Implement a MediaPicker with expect/actual. The shared interface exposes a suspend fun pickMedia(): ByteArray?. Android actual: show a dialog offering camera (ActivityResultContracts.TakePicture with FileProvider) or gallery (GetContent filtered to images). Desktop actual: open a JFileChooser or FileDialog filtered to JPEG/PNG files and read the selected file into a ByteArray.

**Acceptance criteria:**
- [ ] MediaPicker interface defined in commonMain.
- [ ] Android actual shows camera/gallery choice dialog.
- [ ] Android: camera uses FileProvider; no FileUriExposedException.
- [ ] Desktop actual opens a file picker filtered to image types.
- [ ] Returns null if the user cancels without selecting.
- [ ] Returned ByteArray is the raw image bytes ready for upload.

---

### [#20](https://github.com/DevKoenv/RoadAssist/issues/20) Road user: "New incident" screen and ViewModel
**Category:** Road User | **Type:** Story | **Scope:** Must | **Estimate:** 3pt

Build the new incident form in Compose. Fields: category dropdown (BREAKDOWN, ACCIDENT, OBSTRUCTION, OTHER), description TextField (max 500 characters, character counter shown), location row (shows coordinates or "Fetching..." while loading, with a manual refresh button), photo row (optional, shows thumbnail preview after selection). A NewIncidentViewModel manages form state, calls LocationProvider on init, calls MediaPicker on photo tap, validates on submit, and calls the incident repository. On success: show a brief confirmation snackbar and navigate back to the active incidents screen.

**Acceptance criteria:**
- [ ] All four categories selectable in dropdown.
- [ ] Description field shows character count and blocks input beyond 500.
- [ ] Location fetched automatically on screen open.
- [ ] Photo picker opens on tap; thumbnail preview shown after selection.
- [ ] Submitting without a description shows an inline validation error.
- [ ] On successful submit, app navigates back and shows confirmation.
- [ ] New incident appears in the active incidents list with status NEW.

---

## Sprint summary
| Issue | Title | Pts |
| ----- | ----- | --- |
| #15 | Backend: incident CRUD endpoints | 3 |
| #16 | Backend: PATCH status and notes endpoint | 2 |
| #17 | Backend: photo upload endpoint | 2 |
| #18 | App: LocationProvider (expect/actual) | 3 |
| #19 | App: MediaPicker (expect/actual) | 2 |
| #20 | Road user: "New incident" screen and ViewModel | 3 |
| | **Total** | **15** |
