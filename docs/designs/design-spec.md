# RoadAssist - Design Spec

> Visual foundations for the RoadAssist road-user app and dispatcher console.
> **Light is the source of truth; every color carries a derived dark-mode counterpart.**
> Tokens are named by **role**, not by hue - the same spec drives both products.

- **Type:** Space Grotesk (display) · IBM Plex Sans (UI/body) · IBM Plex Mono (metadata)
- **Companion files:** [`tokens.css`](./tokens.css) (drop-in CSS variables) · [`RoadAssist Design Spec.html`](./RoadAssist%20Design%20Spec.html) (interactive, live light/dark toggle)
- **Status:** Stable · Last updated 2026-06-09

---

## Table of contents

- [RoadAssist - Design Spec](#roadassist---design-spec)
  - [Table of contents](#table-of-contents)
  - [Principles](#principles)
  - [Semantic color tokens](#semantic-color-tokens)
  - [Status \& lifecycle colors](#status--lifecycle-colors)
  - [Brand \& neutrals](#brand--neutrals)
    - [Brand](#brand)
    - [Neutral ladder](#neutral-ladder)
  - [Typography](#typography)
    - [Type scale](#type-scale)
  - [Radius \& elevation](#radius--elevation)
    - [Radius](#radius)
    - [Elevation](#elevation)
  - [Usage rules](#usage-rules)
  - [CSS variables (drop-in)](#css-variables-drop-in)

---

## Principles

- **Name by role, not hue.** Build with `--primary`, not "amber". Renaming a hue should never require touching product code.
- **Surfaces pair with foregrounds.** Every surface token (`background`, `card`, `primary`, …) has a matching `-foreground` guaranteed to meet contrast in both themes. Never put `--foreground` on `--primary`.
- **Light is canonical.** Dark values are tuned for contrast - surfaces lift off near-black, the amber primary brightens a step, tints become darkened versions of their light counterparts. Not a mechanical inversion.
- **Depth from hairlines, not shadows.** Most elevation is surface steps + 1px borders. Reserve drop shadows for cards, popovers, and device/modal frames.

---

## Semantic color tokens

These are the names you build with. Reach for these first - raw hexes belong only in the token definitions.

| Token                      | Role                            | Light     | Dark      |
| -------------------------- | ------------------------------- | --------- | --------- |
| `--background`             | App canvas                      | `#ffffff` | `#121419` |
| `--foreground`             | Primary text & icons            | `#15171c` | `#f3f5f8` |
| `--card`                   | Elevated surface                | `#ffffff` | `#1a1d23` |
| `--card-foreground`        | Text on cards                   | `#15171c` | `#f3f5f8` |
| `--popover`                | Menus, sheets, tooltips         | `#ffffff` | `#1f232a` |
| `--popover-foreground`     | Text on popovers                | `#15171c` | `#f3f5f8` |
| `--primary`                | Primary action - roadside amber | `#e0590b` | `#f26a17` |
| `--primary-foreground`     | Text/icon on primary            | `#ffffff` | `#1a0f04` |
| `--secondary`              | Quiet neutral surface           | `#f1f3f6` | `#262b33` |
| `--secondary-foreground`   | Text on secondary               | `#15171c` | `#f3f5f8` |
| `--muted`                  | Subtle fills & row tints        | `#f7f8fa` | `#1a1d23` |
| `--muted-foreground`       | Secondary / supporting text     | `#646b7a` | `#9aa2b1` |
| `--accent`                 | Brand-tinted highlight          | `#fbede2` | `#3a2310` |
| `--accent-foreground`      | Text/icon on accent             | `#9c3c05` | `#f7b07a` |
| `--destructive`            | Errors, danger, delete          | `#d92d20` | `#f0654a` |
| `--destructive-foreground` | Text on destructive             | `#ffffff` | `#1a0f04` |
| `--border`                 | Hairlines & dividers            | `#e4e7ec` | `#2b313b` |
| `--input`                  | Field & control borders         | `#e4e7ec` | `#343b46` |
| `--ring`                   | Focus ring (= `--primary`)      | `#e0590b` | `#f26a17` |

> **Note:** `--destructive` is the one token added beyond the original component set, which had no true red distinct from the amber brand. Use it only for error/danger/delete states.

---

## Status & lifecycle colors

An incident moves through four states. Each has a **foreground** (text / icon / dot) and a **soft background** for badges and row tints. These are semantic states - never use them decoratively.

| Status          | Meaning                             | FG (light) | BG (light) | FG (dark) | BG (dark) |
| --------------- | ----------------------------------- | ---------- | ---------- | --------- | --------- |
| **New**         | First report - not yet picked up    | `#2563eb`  | `#e8effe`  | `#6ea8fe` | `#17233f` |
| **In progress** | Dispatcher is handling the incident | `#b7791f`  | `#fbf0d9`  | `#e0a93c` | `#2c2410` |
| **En route**    | Assistance is on the way            | `#7c3aed`  | `#f0e9fd`  | `#b794f6` | `#271b3d` |
| **Resolved**    | Incident closed                     | `#0f8a5f`  | `#e2f4ec`  | `#34d399` | `#0e2a20` |

Token names: `--s-new` / `--s-new-bg`, `--s-prog` / `--s-prog-bg`, `--s-route` / `--s-route-bg`, `--s-done` / `--s-done-bg`.

---

## Brand & neutrals

The raw scales the semantic tokens draw from.

### Brand

| Name              | Use                                         | Light     | Dark      | Semantic alias        |
| ----------------- | ------------------------------------------- | --------- | --------- | --------------------- |
| `primary` / amber | Brand, primary actions, focus               | `#e0590b` | `#f26a17` | `--primary`           |
| `accent-ink`      | Text on amber tints, links                  | `#9c3c05` | `#f7b07a` | `--accent-foreground` |
| `accent-soft`     | Amber tint - selected rows, fills           | `#fbede2` | `#3a2310` | `--accent`            |
| `road` / blue     | Secondary mark - map routes, road-user role | `#1f6feb` | `#5b9bf9` | `--road`              |

### Neutral ladder

Five ink steps, four surface steps, two hairlines. Depth comes from stacking surfaces, not shadows.

| Name      | Use                       | Light     | Dark      | Semantic alias       |
| --------- | ------------------------- | --------- | --------- | -------------------- |
| `ink`     | Headlines, primary text   | `#15171c` | `#f3f5f8` | `--foreground`       |
| `ink-2`   | Body text                 | `#3a3f4b` | `#cdd3dc` | -                    |
| `ink-3`   | Supporting text, captions | `#646b7a` | `#9aa2b1` | `--muted-foreground` |
| `ink-4`   | Muted meta, placeholders  | `#8b93a3` | `#6b7280` | -                    |
| `ink-5`   | Disabled, faint detail    | `#aab1bf` | `#4b5563` | -                    |
| `paper`   | Base canvas               | `#ffffff` | `#121419` | `--background`       |
| `paper-2` | Raised / muted surface    | `#f7f8fa` | `#1a1d23` | `--muted`            |
| `paper-3` | Chips, inset wells        | `#f1f3f6` | `#262b33` | `--secondary`        |
| `paper-4` | Pressed / deepest fill    | `#eaedf2` | `#2f353f` | -                    |
| `line`    | Borders, dividers         | `#e4e7ec` | `#2b313b` | `--border`           |
| `line-2`  | Subtle inner hairlines    | `#eef1f4` | `#23282f` | `--hair`             |

---

## Typography

Three families, three jobs.

| Family            | Role                                                                            | Weights                      | CSS var          |
| ----------------- | ------------------------------------------------------------------------------- | ---------------------------- | ---------------- |
| **Space Grotesk** | Display & headings - brand wordmark, page titles, section heads, card titles    | 400 · 500 · 600 · 700        | `--font-display` |
| **IBM Plex Sans** | UI & body - paragraphs, rows, descriptions, buttons, fields                     | 400 · 500 · 600 · 700 · 400i | `--font-sans`    |
| **IBM Plex Mono** | Metadata & labels - timestamps, incident IDs, uppercase eyebrows, status badges | 400 · 500 · 600              | `--font-mono`    |

**Web font import:**

```html
<link rel="preconnect" href="https://fonts.googleapis.com" />
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=IBM+Plex+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&family=IBM+Plex+Mono:wght@400;500;600&display=swap" rel="stylesheet" />
```

### Type scale

| Role        | Size / line-height | Tracking | Weight | Family        |
| ----------- | ------------------ | -------- | ------ | ------------- |
| Display     | 46 / 1.04          | -0.025em | 700    | Space Grotesk |
| Heading 2   | 30                 | -0.02em  | 600    | Space Grotesk |
| Heading 3   | 19                 | -0.01em  | 600    | Space Grotesk |
| Title       | 15                 | -0.01em  | 600    | Space Grotesk |
| Body        | 15.5 / 1.62        | -        | 400    | IBM Plex Sans |
| Body strong | 15.5               | -        | 600    | IBM Plex Sans |
| Small       | 13                 | -        | 400    | IBM Plex Sans |
| Label       | 11 · UPPERCASE     | 0.1em    | 500    | IBM Plex Mono |
| Meta / code | 11                 | -        | 400    | IBM Plex Mono |

Tight negative tracking on display sizes; comfortable 1.6 line-height on body; mono labels use wide positive tracking + uppercase to read as system chrome.

---

## Radius & elevation

### Radius

| Token           | Value   | Applied to                    |
| --------------- | ------- | ----------------------------- |
| `--radius-sm`   | 5–7px   | Badges, chips, pills, tags    |
| `--radius-md`   | 8–10px  | Buttons, inputs, list tiles   |
| `--radius-lg`   | 12–14px | Cards, panels, notes          |
| `--radius-xl`   | 16–22px | Modal sheets, large surfaces  |
| `--radius-full` | 9999px  | Avatars, status dots, toggles |

### Elevation

| Level         | Use                                            | Value (light)                                                 |
| ------------- | ---------------------------------------------- | ------------------------------------------------------------- |
| flat          | Default surfaces, list rows - hairlines only   | `none`                                                        |
| `--shadow`    | Cards, badges, popovers, raised controls       | `0 1px 2px rgba(20,23,28,.05), 0 8px 24px rgba(20,23,28,.06)` |
| `--shadow-lg` | Device frames, modal sheets, dispatcher window | `0 30px 80px rgba(20,23,28,.18)`                              |

Dark: shadows deepen to `rgba(0,0,0,.35–.55)`.

---

## Usage rules

- **Pairing.** Always pair a surface with its `-foreground`. Never `--foreground` on `--primary` / `--destructive`.
- **Focus.** `--ring` always equals `--primary`. Render focus as a 2px ring with a 2px offset (`box-shadow: 0 0 0 2px var(--background), 0 0 0 4px var(--ring)`).
- **Primary is scarce.** One primary action per view. Amber draws the eye - overuse flattens its meaning.
- **Status ≠ decoration.** The four status colors carry meaning (lifecycle state). Don't borrow them for charts, tags, or emphasis.
- **Destructive is red, not amber.** Keep `--destructive` visually distinct from the amber brand so danger never reads as a normal action.
- **Theme switching.** Apply `data-theme="dark"` (or `light`) on the root element; all tokens cascade. No per-component overrides.

---

## CSS variables (drop-in)

The full token set lives in [`tokens.css`](./tokens.css). Import it once at the root:

```css
@import "./tokens.css";
```

```css
:root, [data-theme="light"] {
  /* surfaces */
  --background:#ffffff;   --foreground:#15171c;
  --card:#ffffff;         --card-foreground:#15171c;
  --popover:#ffffff;      --popover-foreground:#15171c;
  /* primary - roadside amber */
  --primary:#e0590b;      --primary-foreground:#ffffff;
  /* neutral surfaces & text */
  --secondary:#f1f3f6;    --secondary-foreground:#15171c;
  --muted:#f7f8fa;        --muted-foreground:#646b7a;
  /* brand-tinted highlight */
  --accent:#fbede2;       --accent-foreground:#9c3c05;
  /* errors */
  --destructive:#d92d20;  --destructive-foreground:#ffffff;
  /* lines & controls */
  --border:#e4e7ec;       --input:#e4e7ec;   --ring:#e0590b;
  /* status */
  --s-new:#2563eb;   --s-new-bg:#e8effe;
  --s-prog:#b7791f;  --s-prog-bg:#fbf0d9;
  --s-route:#7c3aed; --s-route-bg:#f0e9fd;
  --s-done:#0f8a5f;  --s-done-bg:#e2f4ec;
  /* secondary brand */
  --road:#1f6feb;
  /* type */
  --font-display:"Space Grotesk", system-ui, sans-serif;
  --font-sans:"IBM Plex Sans", system-ui, sans-serif;
  --font-mono:"IBM Plex Mono", ui-monospace, monospace;
  /* radius */
  --radius-sm:7px; --radius-md:9px; --radius-lg:13px; --radius-xl:18px; --radius-full:9999px;
  /* elevation */
  --shadow:0 1px 2px rgba(20,23,28,.05), 0 8px 24px rgba(20,23,28,.06);
  --shadow-lg:0 30px 80px rgba(20,23,28,.18);
}

[data-theme="dark"] {
  --background:#121419;   --foreground:#f3f5f8;
  --card:#1a1d23;         --card-foreground:#f3f5f8;
  --popover:#1f232a;      --popover-foreground:#f3f5f8;
  --primary:#f26a17;      --primary-foreground:#1a0f04;
  --secondary:#262b33;    --secondary-foreground:#f3f5f8;
  --muted:#1a1d23;        --muted-foreground:#9aa2b1;
  --accent:#3a2310;       --accent-foreground:#f7b07a;
  --destructive:#f0654a;  --destructive-foreground:#1a0f04;
  --border:#2b313b;       --input:#343b46;   --ring:#f26a17;
  --s-new:#6ea8fe;   --s-new-bg:#17233f;
  --s-prog:#e0a93c;  --s-prog-bg:#2c2410;
  --s-route:#b794f6; --s-route-bg:#271b3d;
  --s-done:#34d399;  --s-done-bg:#0e2a20;
  --road:#5b9bf9;
  --shadow:0 1px 2px rgba(0,0,0,.4), 0 8px 24px rgba(0,0,0,.35);
  --shadow-lg:0 30px 80px rgba(0,0,0,.55);
}
```
