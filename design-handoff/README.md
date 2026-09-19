# Handoff: RecipeHub visual redesign

## Overview
A visual redesign of the existing RecipeHub Android app (`3erqi/RecipeHub`, branch `main`) — the
share-target bookmarking app for Instagram Reels / TikTok recipes. No new functionality: the same
one-screen library, multi-select list filter, search, roulette, recipe edit/delete, list CRUD, and
the share → save flow, restyled around the app icon's palette (forest / cream / grass green).

## About the Design Files
The files in this bundle are **design references created in HTML** — prototypes that show intended
look and behavior. They are not production code to copy.

The target codebase is **Kotlin + Jetpack Compose (Material 3)**. Recreate these designs there using
the existing patterns: `RecipeHubTheme` in `ui/theme/Theme.kt`, the existing composables
(`RecipeListScreen`, `RecipeCard`, `CollectionPickerContent`, `RouletteOverlay`,
`RecipeDetailScreen`, `RecipeEditScreen`, `SavingScreen`), Hilt view models and Room repositories.
Do not restructure data, navigation, or view models — this is a styling and layout pass, with a few
named interaction changes listed under "Deviations from current behavior".

## Fidelity
**High-fidelity.** Colors, type sizes/weights, radii, and spacing are final and exact. Recreate them
closely. Where a value isn't listed, follow the nearest listed value rather than inventing one.

## Design Tokens

### Colors
| Token | Hex | Use |
| --- | --- | --- |
| forest | `#0A2318` | top bar, primary button fill, step numerals, primary text |
| forest-deep | `#052116` | icon background; dark-variant screen background |
| cream | `#F6F2E6` | app background, sheets, text on forest |
| white | `#FFFFFF` | cards, inputs |
| grass | `#5CB944` | accent: FAB/shuffle, checkboxes, chips, primary CTA |
| grass-hover | `#6BCC52` | pressed/hover on grass |
| grass-deep | `#3F6B29` | small accent text on cream (links, source line) |
| chip-ink | `#2C4F1C` | text inside grass-tint chips |
| chip-fill | `rgba(92,185,68,.18)` | list chip background |
| danger | `#B5471B` | destructive text/button (matches existing `secondary`) |
| ink-60 | `rgba(10,35,24,.6)` | body secondary text |
| ink-50 | `rgba(10,35,24,.5)` | section labels, counts |
| ink-45 | `rgba(10,35,24,.45)` | card meta line |
| hairline | `rgba(10,35,24,.1)` | dividers, card borders (`.09` on cards) |
| thumb-stripe | `repeating-linear-gradient(135deg,#E8E3D2 0 7px,#DFD9C4 7px 14px)` | thumbnail placeholder only — replace with the real cached og:image |

Existing `Theme.kt` maps cleanly: keep `primary = #3F6B29` for M3 semantics but set
`background`/`surface = #F6F2E6`, `surfaceVariant = #FFFFFF`, and use `#5CB944` as the accent fill
for FAB/checkbox/CTA. Top app bar is a solid `#0A2318` container with `#F6F2E6` content.

### Typography
Two families. Android substitutes: **Nunito** (display/titles, ships on Google Fonts) and
**Figtree** (UI text). If only one can be bundled, use Nunito for titles and the platform default
for body.

| Role | Font | Size / weight | Notes |
| --- | --- | --- | --- |
| wordmark | Nunito 800 | 19sp, tracking -0.2 | cream on forest |
| screen title (detail) | Nunito 800 | 25sp / line-height 1.2, tracking -0.4 |  |
| dialog title | Nunito 800 | 19sp |  |
| card title | Nunito 700 | 13.5sp / 1.3, max 2 lines, ellipsis | min height reserved for 2 lines |
| list-row title | Nunito 700 | 14sp / 1.3, 1 line, ellipsis |  |
| filter pill / list name | Figtree 600 | 15sp |  |
| body (ingredients, steps) | Figtree 400 | 15.5sp / 1.4 (steps 1.5) |  |
| section label | Figtree 700 | 13sp, uppercase, tracking .06em, ink-50 |  |
| meta / count | Figtree 500 | 11–12.5sp, ink-45/50 |  |
| button label | Figtree 700 | 13.5–15sp |  |
| thumbnail placeholder tag | JetBrains Mono 500 | 8.5sp uppercase, tracking .06em | design-only |

### Spacing, radii, elevation
- Screen padding: 18dp horizontal. Top bar: 16dp top / 18dp bottom, 18dp sides.
- Grid: 2 columns, 12dp gap. List rows: 9dp gap, 9/11dp inner padding.
- Radii: cards 16, list rows 14, inputs & small buttons 12–13, filter pill 12, shuffle button 14,
  sheets 26 (top corners only), dialogs 22, chips/pills 20 (fully round), checkbox 7, thumbnails 10–13.
- Card border 1dp `rgba(10,35,24,.09)`, shadow `0 1dp 2dp rgba(10,35,24,.05)`.
- Menus/dialogs: shadow `0 10dp 28dp rgba(10,35,24,.18)` / `0 24dp 60dp rgba(10,35,24,.4)`.
- Scrim over content: `rgba(10,35,24,.35)`; over full-screen overlays `rgba(5,33,22,.88)`.
- Minimum touch target 44dp (card overflow button is 30dp visual inside a 44dp target).

## Screens / Views

### 1. Library (`RecipeListScreen`)
**Purpose:** the whole app — browse, filter, search, shuffle, open, edit, delete.

**Layout, top to bottom:**
1. **Top bar block**, solid `#0A2318`, not scrolled away.
   - Wordmark row: two 10×3.5dp grass rounded bars rotated −18° (the icon's spark marks), 3dp apart,
     then "RecipeHub" (Nunito 800 19sp, cream). 16dp below.
   - Row: **filter pill** (flex 1) + **shuffle button** (44×44dp), 10dp gap.
     - Filter pill: background `rgba(246,242,230,.08)`, 1dp border `rgba(246,242,230,.16)`,
       radius 12, padding 11/14dp. Label (Figtree 600 15sp cream, ellipsized) + 10×6dp grass
       downward triangle. Label text: `All Recipes` / the single list's name / `N lists selected`.
     - Shuffle button: grass fill, radius 14, forest 2dp-stroke shuffle glyph (reuse
       `res/drawable/ic_shuffle.xml`). Disabled when nothing is on screen.
2. **Search field** on cream: white fill, 1dp hairline border, radius 13, padding 10/13dp, 17dp
   magnifier icon in `rgba(10,35,24,.45)`, placeholder "Search recipes", trailing text button
   "Clear" (Figtree 500 13sp, `#3F6B29`) only when non-empty. 14dp below.
3. **Count row:** "N recipes" (Figtree 500 12.5sp ink-50) left; "Show all" text button
   (`#3F6B29`, 600 12.5sp) right, only when a list filter is active. 12dp below.
4. **Grid** — 2 columns, 12dp gap. Card: white, radius 16, 1dp border, clipped.
   - Square thumbnail (aspect 1:1), full card width.
   - Platform badge, top-left inset 8dp: `rgba(10,35,24,.82)` pill, radius 20, padding 3/8dp,
     Figtree 600 9.5sp cream, text "Instagram" / "TikTok" (from `recipe.sourcePlatform`).
   - Overflow button, top-right inset 6dp: 30dp circle `rgba(10,35,24,.55)` → `#0A2318` pressed,
     three 3dp cream dots, 2.5dp apart. Replaces the current long-press affordance (long-press
     should keep working).
   - Text block padding 10/11/12dp: title (2 lines, reserved 35dp) then meta line
     "${platform} · ${author}" (Figtree 500 11sp ink-45, 1 line ellipsized).
   - Tapping the card body → detail. Tapping overflow → menu anchored below it: white, radius 12,
     min-width 120dp, items 11/14dp — "Edit" (forest) and "Delete" (`#B5471B`) split by a hairline.
5. **Empty state**, centred, 54dp vertical padding: Nunito 700 16sp title + Figtree 400 13.5sp/1.5
   ink-55 body.
   - No recipes: "No recipes here yet" / "Share a Reel or TikTok to RecipeHub and it lands here."
   - No search match: "Nothing matches that" / "Try a shorter word — search only looks at titles."

**Compact-rows alternative (optional, see `1b` in the prototype):** single column, 9dp gap; row is
a 14dp-radius white card, 9/11dp padding, 56dp thumbnail (radius 10), title + "${platform} ·
${firstListName or 'No list'}", trailing 32dp overflow button.

### 2. List filter dropdown (`CollectionPickerContent`, manageable)
Anchored under the filter pill, inset 18dp left/right, top 64dp, over a `rgba(10,35,24,.35)` scrim.
Cream sheet, radius 18, shadow `0 20dp 50dp rgba(10,35,24,.35)`, 140ms fade in.
- Header: "SHOW LISTS", Figtree 700 12sp uppercase tracking .06em ink-50, padding 14/18/6dp.
- Row (11/18dp, pressed tint `rgba(10,35,24,.04)`): 22dp checkbox — radius 7, 2dp border
  `rgba(10,35,24,.25)`, fill grass when checked with a 2.2dp forest tick — then name (Figtree 600
  15sp), then recipe count (Figtree 500 12.5sp `rgba(10,35,24,.4)`) right-aligned, then a small
  "Edit" text button (`#3F6B29` 600 12sp) that opens the rename dialog. Row tap toggles selection;
  the Edit button must not toggle it.
- Footer row (14/18dp, hairline above): 22dp grass rounded square with a forest "+", then "New list".
- Empty selection = all recipes, as today.

### 3. List rename / create dialog
Cream, radius 22, 24dp padding, 28dp screen margin. Nunito 800 19sp title ("New list" /
"Rename list"), 16dp gap, single-line text field (white, radius 13, 1dp `rgba(10,35,24,.16)`,
13/14dp, Figtree 600 16sp), 20dp gap, footer row: "Delete list" (`#B5471B` 700 14sp, hidden when
creating) hard left, then "Cancel" and a grass "Create"/"Save" button (radius 13, padding 11/18dp).
Confirm disabled on blank input. Deleting a list also clears it from every recipe and from the
current filter selection.

### 4. Delete recipe confirmation
Same dialog shell. Title "Delete this recipe?"; body Figtree 400 14.5sp/1.5 ink-60:
"${title} will be removed from every list. This can't be undone." Buttons right-aligned:
"Keep it" (text) and "Delete" (`#B5471B` fill, white label).

### 5. Roulette (`RouletteOverlay`)
Full-screen `rgba(5,33,22,.88)` scrim, 200ms fade, centred column.
- Title Nunito 800 20sp cream: "Picking a recipe" → "Tonight, then" once settled.
- Subtitle Figtree 400 13sp `rgba(246,242,230,.55)`: "From the N on screen" → "${platform} · ${author}".
- Window: 240×300dp, radius 18, 2dp grass border, cream fill, clipped. Strip items are 300dp tall:
  240dp thumbnail + title (Nunito 700 14sp/1.25, 2 lines) in 10/12dp.
- Spin: candidates repeated 5×; scroll to `(4 * n + targetIndex) * 300dp` over **4400ms**,
  `cubic-bezier(.12, .7, .15, 1)`. Current code uses 5000ms / 4 cycles — either is fine, keep one.
- While spinning: a "Cancel" text button (`rgba(246,242,230,.5)` 600 13sp) 22dp below.
- **Deviation:** on settle, do **not** auto-navigate. Fade in two buttons (250ms): "Spin again"
  (1.5dp cream-35% outline, radius 14) and "Cook this" (grass fill, forest label) → opens the recipe.
- Candidate pool is what's on screen: current list filter **and** search query applied.

### 6. Recipe detail (`RecipeDetailScreen`)
Scrolling column on cream.
- **Hero** 250dp, full-bleed thumbnail. Back button top-left (38dp circle `rgba(10,35,24,.75)`,
  cream 2dp chevron) and an "Edit" pill top-right (padding 9/15dp, radius 20, same fill, Figtree 600
  13sp cream). Both inset 14dp.
- **Sheet** pulled up 22dp over the hero, radius 22 top corners, cream, padding 22/20/34dp:
  1. Source line: Figtree 600 11.5sp `#3F6B29` uppercase tracking .05em — "${platform} · ${author}".
  2. Title: Nunito 800 25sp/1.2, tracking −0.4.
  3. Chip row (15dp above / 22dp below, 7dp gap, wraps): one chip per list — chip-fill background,
     1dp `rgba(63,107,41,.25)`, radius 20, padding 6/12dp, Figtree 600 12.5sp `#2C4F1C`. Last chip is
     "Edit lists": dashed 1dp `rgba(10,35,24,.28)`, ink-55 label, opens the existing list picker dialog.
  4. **Ingredients** (section label, 11dp below): rows 8dp vertical, hairline bottom border, 7dp
     grass dot + text. **Steps**: 14dp gap, 26dp forest rounded square (radius 9) with Nunito 700
     13sp cream numeral + text.
  5. **No-notes state** instead of 4: 20dp box, `rgba(10,35,24,.045)` fill, 1dp dashed
     `rgba(10,35,24,.18)`, radius 16 — Nunito 700 15sp "No ingredients yet", body "Watch the clip and
     jot them down — they stay with the recipe.", then a grass "Add notes" button (radius 12,
     padding 10/16dp) → edit screen.
  6. **"View original post"**: full-width forest button, radius 15, 15dp padding, cream 700 15sp
     label + grass 16dp external-link glyph, 9dp gap. Same `ACTION_VIEW` intent as today.

### 7. Edit recipe (`RecipeEditScreen`)
- Forest top bar, 16/18dp: back chevron, "Edit recipe" (Nunito 800 18sp cream), grass "Save" pill
  (radius 20, padding 9/16dp, forest label). Shows "Saving…" while `isSaving`.
- Body 20/18/34dp, 18dp between fields. Each field: uppercase section label above (Figtree 700 12sp
  ink-50), with a right-aligned "one per line" hint (Figtree 400 11.5sp `rgba(10,35,24,.4)`) for the
  two multi-line fields. Inputs: white, radius 13, 1dp `rgba(10,35,24,.16)`, 13/14dp padding; title
  Figtree 600 16sp single line; ingredients and steps 6 rows, Figtree 400 15sp/1.6, non-resizable.
- Splitting stays as today: trim each line, drop blanks.

### 8. Share → save (`SavingScreen`, launched by `ShareReceiverActivity`)
Bottom sheet over a `rgba(5,33,22,.5)` scrim; cream, radius 26 top, padding 22/20/28dp, rising in
280ms `cubic-bezier(.2,.8,.2,1)`. 44×4dp `rgba(10,35,24,.18)` grabber, 18dp below.
- **Loading:** 42dp ring (4dp `rgba(10,35,24,.12)` track, grass head, 800ms linear spin), 18dp gap,
  Nunito 800 18sp "Reading the post", Figtree 400 13.5sp ink-55 "Grabbing the thumbnail and caption".
- **Review** (covers the current `NeedsName` + `PickingLists` states in one sheet):
  - Nunito 800 20sp "Save this one?" + Figtree 400 13.5sp ink-55 "We named it from the caption —
    change anything you like."
  - Row, 13dp gap: 78dp thumbnail (radius 13) + column with "INSTAGRAM · @HANDLE"
    (Figtree 700 11.5sp ink-45 uppercase) and the editable title field (white, radius 12, 11/13dp,
    Nunito 700 15sp).
  - "ADD TO LISTS" label, then wrapping chip row, 8dp gap: unselected = 1dp
    `rgba(10,35,24,.18)` outline, transparent; selected = grass fill; both radius 20, padding 9/14dp,
    Figtree 600 13.5sp forest label.
  - Footer: "Discard" text button + full-width grass "Save recipe" (radius 14, 14dp padding).
  - If the heuristic found no title, the field starts empty and Save stays disabled until it isn't
    blank — the old separate "what should we call it?" step goes away.
  - Fetch failure keeps today's behavior, styled to match: title "Couldn't read this post", body
    "Check your connection and try again.", a grass "Retry", an outlined "Save anyway", a "Discard"
    text button.
- **Done:** centred, 14/20dp — 52dp grass circle with a 2.6dp forest tick, Nunito 800 19sp "Saved",
  Figtree 400 13.5sp/1.5 ink-60 body ("${title} is in ${list} and ${list}." or "…is in your library,
  not filed in a list yet."), then a full-width forest "Done" button (radius 14) that finishes the
  activity. A confirmation step is new — today the flow navigates straight out.

## Interactions & Behavior
- Tap card body → detail. Tap overflow (or long-press, as today) → Edit / Delete menu. Any outside
  tap or scrim tap closes menus, the filter sheet and dialogs.
- Filter pill → dropdown; checkbox rows toggle immediately and the grid updates live; the pill label
  and the count row follow. "Show all" clears the selection.
- Search filters titles only, case-insensitive, live, and composes with the list filter (unchanged).
- Shuffle uses the on-screen pool; empty pool disables the button.
- Back from edit → detail; back from detail → library.
- Deleting the open recipe returns to the library.
- Transitions: dialogs/sheets fade 140–250ms; the save sheet rises 280ms; roulette spin 4400ms as
  specified. No other motion.

## State Management
No new state beyond what the view models already hold:
`selectedCollectionIds: Set<Long>`, `searchQuery: String`, `collections`, `recipes` (filtered
flow), plus local UI state for `filterExpanded`, `overflowMenuRecipeId`, `deleteTargetId`,
`listDialogTarget (new | id)`, `roulette(candidates, targetIndex, settled)`, and the saving state
machine — with `NeedsName` and `PickingLists` merged into one `Review` state and a new `Saved`
confirmation state before the activity finishes.

## Deviations from current behavior (decide before building)
1. Roulette stops on a result with "Spin again" / "Cook this" instead of auto-opening.
2. Save flow is one review sheet (name + lists) plus a "Saved" confirmation, replacing the separate
   NeedsName and PickingLists steps.
3. Card gets a visible overflow button in addition to long-press.
4. Recipe detail gains the "no ingredients yet" prompt box as a first-class empty state.
Everything else is styling.

## Assets
- **Thumbnails:** the striped grey-green blocks in the prototypes are placeholders. Use the real
  cached image from `ThumbnailStore` / `recipe.thumbnailPath` (Coil `AsyncImage`, as today).
- **Icons:** shuffle reuses `res/drawable/ic_shuffle.xml`. Search, chevron, back, tick, external-link
  and the three-dot overflow are drawn as simple 2dp strokes in the prototype — use the Material
  Icons equivalents already imported in the codebase.
- **Wordmark spark marks:** two 10×3.5dp grass rounded bars at −18°, lifted from the app icon.
- **Fonts:** Nunito and Figtree (Google Fonts, SIL OFL). Bundle or use downloadable fonts.
- No new raster assets are needed.

## Files
- `RecipeHub App.dc.html` — the design reference. Section `1a` is the interactive prototype
  (library, filter, roulette, detail, edit, save flow); `1b` library layout alternatives;
  `1c` detail alternatives; `1d` save-flow alternatives. Open it in a browser and click through.
- `android-frame.jsx` — device-frame helper the prototype renders inside. Not part of the design.
- `support.js` — runtime for the prototype file. Not part of the design.
- `icon.png` — the app icon the palette came from.
