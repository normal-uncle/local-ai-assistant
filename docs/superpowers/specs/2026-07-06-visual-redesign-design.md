# Visual Redesign — "Calm & Focused" Design System

**Date:** 2026-07-06
**Status:** Approved (design), pending implementation plan
**Author:** kichan.kim (brainstormed with Claude)

## Goal

Replace the app's stock-Material3, light-only, flat visual style with a cohesive
"Calm & Focused" design system: soft neutrals, a refined blue accent, generous
whitespace, rounded cards, subtle elevation, and **full light + dark support**.
Apply it across the whole app (design tokens + shared components + all 6 screens).

The current UI is functional but visually "crude": no shape/elevation/spacing
tokens, chat has no bubbles (text aligned left/right only), memo list uses dividers
instead of cards, and the Capture screen crams three filled buttons into the top
app bar as navigation. This redesign fixes all of that.

## Design Direction

**Calm & Focused** — chosen over Warm/Friendly and Sleek/Pro. AI-assistant apps
live on trust and focus; soft neutrals with one confident blue accent read as calm
and dependable, and the palette extends cleanly to dark mode.

## Design Tokens

All tokens live in the `ui_component` module (`com.just.assistant.ui.component.theme`).

### Color

Two schemes wired via `isSystemInDarkTheme()`. Semantic roles map to Material3
`ColorScheme` slots.

| Role | Light | Dark |
|------|-------|------|
| background | `#F7F8FA` | `#0F1216` |
| surface | `#FFFFFF` | `#171B21` |
| surfaceVariant | `#EEF0F3` | `#1F242B` |
| onSurface (primary text) | `#1A1D21` | `#E6E9EF` |
| onSurfaceVariant (secondary text) | `#5B6472` | `#9AA4B2` |
| outline | `#DDE1E7` | `#2A3038` |
| primary | `#3B6EF5` | `#6E8DFF` |
| onPrimary | `#FFFFFF` | `#0B1220` |
| primaryContainer | `#E4ECFF` | `#22304F` |
| onPrimaryContainer | `#2A57C4` | `#AFC2FF` |
| success | `#2E9E6B` | `#3FBE86` |
| error | `#E5484D` | `#FF6169` |
| onError | `#FFFFFF` | `#2A0A0C` |

`ColorSet` keeps raw palette tokens; `AssistantTheme` builds `lightColorScheme` /
`darkColorScheme` from them. Success is a custom role (not in Material `ColorScheme`),
exposed via a small `AssistantColors` extension holder provided through a
`CompositionLocal` (used by schedule chips).

### Typography

Keep the system sans font. Refine the scale, add `headlineSmall`, set line heights
and slight negative letter-spacing on large titles.

| Token | Size / Weight | Line height | Letter spacing |
|-------|---------------|-------------|----------------|
| headlineSmall | 24 / SemiBold | 32 | -0.2 |
| titleLarge | 22 / SemiBold | 28 | -0.1 |
| titleMedium | 18 / Medium | 24 | 0 |
| titleSmall | 15 / Medium | 20 | 0 |
| bodyLarge | 16 / Normal | 24 | 0 |
| bodyMedium | 14 / Normal | 20 | 0 |
| labelLarge | 14 / Medium | 20 | 0.1 |
| labelSmall | 12 / Medium | 16 | 0.2 |

### Spacing

New `Spacing` object (Dp constants): `xs=4, sm=8, md=12, lg=16, xl=24, xxl=32`.
Replaces hardcoded `.dp` literals across screens.

### Shape / Radius

New `Shapes` (Material3 `Shapes`): `small=8, medium=12, large=16`, plus app
constants `Radius.xl=24` and `Radius.full=CircleShape` for chips/FAB.

### Elevation

New `Elevation` tokens: `level0=0, card=1 (soft shadow in light, 1px outline in
dark), sheet=2, fab=3`. Cards use a soft shadow in light mode; in dark mode they
use a `surface` fill + `outline` border instead of shadow.

## Shared Components (new, in `ui_component`)

Each is a small, single-purpose composable with a preview.

| Component | Purpose |
|-----------|---------|
| `AssistantScaffold` | App-standard Scaffold: background color, optional top bar + bottom bar, content padding via `Spacing`. |
| `AssistantTopBar` | Title (titleLarge) + optional back nav icon + icon-only action slot. Replaces text-button-heavy bars. |
| `AssistantBottomBar` | Bottom navigation for the 4 top-level destinations. |
| `ChatBubble` | Role-styled message bubble: user = `primary`/`onPrimary`, assistant = `surfaceVariant`/`onSurface`, radius 16 with a 4dp tail corner, max width 85%. |
| `MemoCard` | Card list row: surface, radius large, `Elevation.card`, clipped rounded thumbnail, title/preview/schedule chip. |
| `SectionCard` | Grouped container for settings/detail sections (surface card with inner dividers). |
| `PrimaryButton` / `SecondaryButton` | Filled (primary) and outlined button wrappers, height 52, radius medium. |
| `AssistantChip` | Schedule/status chip: `primaryContainer` tint (or `success` for calendar), full radius, leading icon, non-interactive by default. |
| `EmptyState` | Centered icon + title + subtitle for empty lists / not-found. |

Existing `PreviewSheet` and `DateTimePickerSheet` are restyled to use the tokens
(not rebuilt).

## Navigation Restructure (approved)

Introduce a **bottom navigation bar** with 4 top-level destinations:
**Capture (home) · Chat · Memo · Settings**.

- Implemented at the `AssistantApp` level: wrap `NavDisplay` in `AssistantScaffold`
  with `AssistantBottomBar`.
- Uses the existing Navigation3 `backStack`. Tapping a tab makes that route the
  single top-level entry (clear-and-add of the top-level route), preserving the
  existing deep-link and boot-route logic.
- The bottom bar is **hidden** on `Onboarding` and on the Memo **detail** view.
  Detail is a nested state inside `MemoScene`, so `MemoScene` hoists a
  `bottomBarVisible: Boolean` up to `AssistantApp` (true on list, false on detail);
  `AssistantApp` shows the bar only when the current top-level route wants it and
  `bottomBarVisible` is true.
- Removes the three filled buttons from `CaptureScene`'s top bar. `CaptureScene`
  loses its `onOpenChat/onOpenMemo/onOpenSettings` params (nav now handled by the bar).
- The primary capture action becomes a FAB / prominent primary button on the
  Capture screen.

## Per-Screen Changes

1. **Capture (home)** — remove nav buttons from top bar; text field + capture
   action row (gallery/camera/voice as icon buttons) + prominent "준비" primary
   button; restyle preview thumbnail (clipped, rounded); tokenized spacing.
2. **Chat** — messages become `ChatBubble`s; top bar keeps TTS/New as icon actions;
   input row restyled (rounded field + circular send/voice button); error/model
   banners become tinted inline cards.
3. **Memo List** — rows become `MemoCard`s (no dividers); rounded thumbnails;
   `EmptyState` for empty; completed items styled (strikethrough + muted).
4. **Memo Detail** — schedule section grouped in a `SectionCard`; chips via
   `AssistantChip`; image clipped/rounded; action buttons via Primary/Secondary;
   `Spacer` ladders replaced by arranged spacing.
5. **Settings** — grouped `SectionCard`s with leading icons, dividers, and a
   chevron on "About"; consistent row padding.
6. **Onboarding** — add a logo/brand mark; styled progress (rounded bar + clean
   "X / Y MiB" caption); primary button; error state uses `error` color + icon.

## Architecture / Module Layout

- **`ui_component`** becomes the design-system hub:
  - `theme/ColorSet.kt` (+ dark tokens), `theme/TextSet.kt` (refined), `theme/AssistantTheme.kt` (light+dark), new `theme/Spacing.kt`, `theme/Shapes.kt`, `theme/Elevation.kt`, `theme/AssistantColors.kt` (success role via CompositionLocal).
  - `component/` package for the shared composables listed above.
- **Feature modules** depend on `ui_component` (already do for theme) and refactor
  their `*Screen.kt` to use the shared components + tokens. No ViewModel/logic
  changes — this is presentation-only.
- **`app`** hosts the bottom-bar shell in `AssistantApp.kt`.

## Non-Goals (YAGNI)

- No dynamic color / Material You wallpaper theming.
- No custom/downloaded font family (system font stays).
- No animations beyond default Compose transitions (polish pass can come later).
- No ViewModel, data, or business-logic changes.
- No new features — visual/navigation only.

## Success Criteria

- Light and dark both render correctly (system toggle switches instantly).
- Every screen uses tokens (no stray hardcoded colors/dp for design values).
- Chat shows bubbles; memo list shows cards; bottom nav replaces top-bar buttons.
- Existing tests still pass; existing deep-link and boot-route behavior unchanged.
- No regression in the capture/chat/memo/settings/onboarding flows.
