# UI DESIGN ENGINEERING GUIDELINES — FCMETRIX (Android / Jetpack Compose)

> Interface design, micro-interaction, motion, and visual quality guidelines adapted from [UI Skills](https://www.ui-skills.com/) and [Transitions.dev](https://transitions.dev/) for Jetpack Compose and Material 3.

---

## 1. MOTION & ANIMATION (Inspired by Emil Kowalski & Transitions.dev)

- **Spring Physics (`spring()`) by Default**:
  - For numeric values that update with direct interaction (Global OVR, averages, counters), prefer `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)` or `FastOutSlowInEasing`.
  - Avoid linear animations (`LinearEasing`) for primary UI components unless they represent continuous progress bars.
- **Micro-interactions & Tactile Feedback**:
  - Destructive actions or rank adjustments must provide immediate visual feedback (smooth scale bump or color transitions).
- **Motion Reduction & Durations**:
  - Keep transitions contained with durations between `150ms` and `350ms` to preserve fast user interaction loops.

---

## 2. TRANSITIONS.DEV MOTION TOKENS

- **Standard Easing Curves**:
  - `SmoothOutEasing`: `CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)` — For screen transitions, panel reveals, bottom sheets, and dialogs.
  - `BounceEasing`: `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)` — For badges, numbers, and active pills.
  - `InOutEasing`: `CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)` — For crossfades and icon morphs.
- **P8 — Page Slide Transition**:
  - When navigating between screens (e.g. Calculator ↔ Settings), use `slideInHorizontally(animationSpec = tween(300, easing = SmoothOutEasing))` + `fadeIn()` paired with `slideOutHorizontally` + `fadeOut()`.
- **P12 — Input Shake on Limit / Error**:
  - When entering out-of-range values or reaching legal boundaries, trigger a horizontal micro-oscillation ($\pm 6\,\text{dp}$) across 4 fast keyframe phases ($280\,\text{ms}$) via `Animatable` to physically convey boundary limits.
- **P16 — Sliding Pill Active Indicator**:
  - In mutually exclusive segmented buttons (such as Theme Mode selector or Quick Rank chips), animate the active background pill indicator with continuous position and size transitions.

---

## 3. TYPOGRAPHY AND ANTI-SLOP LAYOUT (Inspired by Jakub Krehel & ibelick)

- **Tabular Numbers (`FontFeatureSettings("tnum")`)**:
  - All text displaying dynamic numeric figures (OVR, decimal averages, missing points, percentages) must include `fontFeatureSettings = "tnum"` in their `TextStyle` / `SpanStyle` or monospaced font family to prevent width jitter and layout shifts.
- **Systematic Spacing Scale**:
  - Use multiples of `4dp` / `8dp` (`4.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`).
- **Surfaces & Tonal Elevation**:
  - In Material 3, use tonal containers (`surfaceContainer`, `surfaceContainerHigh`) and subtle outline strokes (`outlineVariant.copy(alpha = 0.2f..0.3f)`) rather than heavy drop shadows.

---

## 4. ACCESSIBILITY (A11Y) & TOUCH TARGETS

- **Minimum Touch Targets**:
  - Every clickable or interactive element must maintain a minimum touch target of $48\,\text{dp} \times 48\,\text{dp}$ (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or inclusive padding).
- **Semantics & TalkBack**:
  - Related composite elements (such as the OVR card) must unify their description via `Modifier.semantics(mergeDescendants = true) { contentDescription = ... }`.
- **Color Contrast**:
  - Ensure high-contrast ratios (WCAG AA) for rank badges and placeholder states.

---

## 5. UX WRITING

- Keep text brief, concise, and action-oriented.
- Avoid ambiguity in status labels and suggestion cards.
