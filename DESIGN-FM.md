---
version: alpha
name: Fairway Morning
description: >
  A warm, calm visual language for cross-platform mobile apps. Built on
  Material 3 with custom light-mode container tones, a warm-leaning neutral
  ramp derived from the primary's hue, and three brand colors — fairway
  green, sun gold, sky blue — that evoke a fresh outdoor setting at a good
  hour without being literal about it.

colors:
  # ---------------------------------------------------------------------------
  # Light scheme (default) — Material 3 role tokens
  # ---------------------------------------------------------------------------
  primary: "#176B4B"
  on-primary: "#FFFFFF"
  primary-container: "#7BC8A1"
  on-primary-container: "#002113"
  secondary: "#7A5900"
  on-secondary: "#FFFFFF"
  secondary-container: "#F2BF4F"
  on-secondary-container: "#261900"
  tertiary: "#31647C"
  on-tertiary: "#FFFFFF"
  tertiary-container: "#8EBFDB"
  on-tertiary-container: "#001E2B"
  neutral: "#FBFDF8"
  surface: "#FBFDF8"
  on-surface: "#191C1A"
  surface-variant: "#DCE5DD"
  on-surface-variant: "#404943"
  surface-container-lowest: "#FFFFFF"
  surface-container-low: "#F2F4F0"
  surface-container: "#ECEEEA"
  surface-container-high: "#E7E9E5"
  surface-container-highest: "#E1E3DF"
  outline: "#707973"
  outline-variant: "#C0C9C1"
  scrim: "#000000"
  error: "#BA1A1A"
  on-error: "#FFFFFF"
  error-container: "#FFDAD5"
  on-error-container: "#410002"

  # ---------------------------------------------------------------------------
  # Dark scheme — Material 3 role tokens (no container overrides)
  # ---------------------------------------------------------------------------
  dark-primary: "#88D6AF"
  dark-on-primary: "#003824"
  dark-primary-container: "#005236"
  dark-on-primary-container: "#A4F3CA"
  dark-secondary: "#F2BF4F"
  dark-on-secondary: "#402D00"
  dark-secondary-container: "#5C4200"
  dark-on-secondary-container: "#FFDEA1"
  dark-tertiary: "#9BCDE9"
  dark-on-tertiary: "#003548"
  dark-tertiary-container: "#124C63"
  dark-on-tertiary-container: "#C1E8FF"
  dark-surface: "#111412"
  dark-on-surface: "#E1E3DF"
  dark-surface-variant: "#404943"
  dark-on-surface-variant: "#C0C9C1"
  dark-surface-container-lowest: "#0C0F0D"
  dark-surface-container-low: "#191C1A"
  dark-surface-container: "#1D201E"
  dark-surface-container-high: "#272B28"
  dark-surface-container-highest: "#323633"
  dark-outline: "#8A938C"
  dark-outline-variant: "#404943"
  dark-error: "#FFB4AB"
  dark-on-error: "#690004"
  dark-error-container: "#930009"
  dark-on-error-container: "#FFDAD5"

typography:
  # Canonical values reflect the Material 3 type scale (Roboto) used on Android.
  # iOS implementations substitute the native iOS type system — see Typography
  # section in the body for the platform mapping rules.
  display-large:
    fontFamily: Roboto
    fontSize: 57px
    fontWeight: 400
    lineHeight: 1.12
    letterSpacing: -0.25px
  display-medium:
    fontFamily: Roboto
    fontSize: 45px
    fontWeight: 400
    lineHeight: 1.16
  display-small:
    fontFamily: Roboto
    fontSize: 36px
    fontWeight: 400
    lineHeight: 1.22
  headline-large:
    fontFamily: Roboto
    fontSize: 32px
    fontWeight: 400
    lineHeight: 1.25
  headline-medium:
    fontFamily: Roboto
    fontSize: 28px
    fontWeight: 400
    lineHeight: 1.29
  headline-small:
    fontFamily: Roboto
    fontSize: 24px
    fontWeight: 400
    lineHeight: 1.33
  title-large:
    fontFamily: Roboto
    fontSize: 22px
    fontWeight: 400
    lineHeight: 1.27
  title-medium:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 500
    lineHeight: 1.5
    letterSpacing: 0.15px
  title-small:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.43
    letterSpacing: 0.1px
  body-large:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: 0.5px
  body-medium:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.43
    letterSpacing: 0.25px
  body-small:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.33
    letterSpacing: 0.4px
  label-large:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.43
    letterSpacing: 0.1px
  label-medium:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: 500
    lineHeight: 1.33
    letterSpacing: 0.5px
  label-small:
    fontFamily: Roboto
    fontSize: 11px
    fontWeight: 500
    lineHeight: 1.45
    letterSpacing: 0.5px

spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px

rounded:
  none: 0px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 28px
  full: 9999px

components:
  button-filled:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    typography: "{typography.label-large}"
    padding: 10px 24px
  button-outlined:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    rounded: "{rounded.full}"
    typography: "{typography.label-large}"
    padding: 10px 24px
  button-text:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    rounded: "{rounded.full}"
    typography: "{typography.label-large}"
    padding: 10px 12px
  card:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: 16px
  text-field:
    backgroundColor: "{colors.surface-container-highest}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.xs}"
    padding: 16px
  chip:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.sm}"
    typography: "{typography.label-medium}"
    padding: 6px 12px
---

# Fairway Morning

Fairway Morning is a Material 3-based design system for cross-platform mobile apps. It targets Android and iOS with a single visual language: three brand colors, a warm-leaning neutral ramp, and a deliberately tuned set of light-mode container tones that hold their weight against light surfaces.

The visual character is calm and competent. The three brand colors — a fresh fairway green, a warm sun gold, and a muted sky blue — sit on neutrals that carry a faint warm tint because they're derived from the primary's hue at very low chroma. Surfaces feel related to the brand without becoming visibly chromatic.

## Platform consistency

The default is **identical UI across Android and iOS**. The two implementations should look the same to a user — the same color tokens, the same shape scale, the same spacing rhythm, the same component styling, the same screen layouts. Visual parity is the goal, not platform tailoring.

Divergence between platforms is the exception, reserved only for cases where there's a clear idiomatic difference that users on that platform expect. Examples of acceptable divergence:

- **Create affordances** — a Material FAB on Android vs. a "+" icon in the navigation bar on iOS
- **Modal presentation** — a Material modal bottom sheet on Android vs. an iOS sheet sliding up from the bottom
- **System gestures** — Android system back gesture vs. iOS swipe-back
- **Pickers** — Material date/time pickers on Android vs. UIKit pickers on iOS

If a screen *could* be styled the same way on both platforms, it should be. When in doubt, default to parity.

### Material 3 on both platforms

The Material 3 color scheme — including all brand colors, container tones, surface tokens, and the role semantics that go with them — is used on iOS as well as Android. iOS implementations do not substitute Apple's HIG color palette. The visual identity comes through the M3 tokens, and that identity must carry across both platforms without dilution.

The one exception is typography (see Typography below). Otherwise, the design tokens defined in this file apply identically to both platforms.

### Light and dark mode

Both light and dark modes are first-class. Every screen should look intentional in both modes; dark mode is not an afterthought. The two schemes are documented as separate sets of role tokens (the default set is light; the `dark-`-prefixed set is dark).

The two modes use the same color palettes underneath, just different tones from each palette. Mode selection happens at the screen level, not per-component — a screen is in one mode or the other.

## Colors

The palette has four brand colors and one error color, plus two neutral palettes generated from the brand primary's hue for surface and surface-variant.

The brand colors:

- **Fairway green** (primary, source `#2E7D5B`). The dominant brand color. Used for the most important action on a screen and for the primary affordance of any view. Source color lands at HCT tone 47, close to M3's standard primary tone 40.
- **Sun gold** (secondary, source `#DBAA3C`). The warm supporting color. Used for role badges, active-state indicators, identity surfaces (avatars, attribution). Source color lands at HCT tone 72 because yellows are inherently bright; the dark olive-gold M3 produces at tone 40 (`#7A5900`) is what shows up in body-weight Secondary usage. The bright source gold lives in container/fill contexts.
- **Sky blue** (tertiary, source `#4A7C95`). The cool accent color. Used for ambient/informational surfaces — status indicators, neutral metadata — where the meaning is "noted" rather than "act on this." Source lands at HCT tone 50.
- **Error red** (`#BA1A1A`). Standard M3 error red. Used for destructive actions and validation states. No reason to be creative here.
- **Neutral** and **Neutral variant**. Derived from the primary's hue (164.6°) at chroma 4 and 8 respectively. Surfaces carry a faint warm-leaning green tint that ties them to the brand without becoming visibly chromatic. The variant is used for outlines, dividers, and medium-emphasis text.

### Light-mode container deviation

Material 3's default scheme generation puts containers at tone 90 of each palette, which reads as washed-out pastel against a near-white surface. Fairway Morning overrides three container values in the light scheme to give them more presence:

- `primary-container` uses tone 75 (`#7BC8A1`) instead of M3's tone 90
- `tertiary-container` uses tone 75 (`#8EBFDB`) instead of M3's tone 90
- `secondary-container` uses tone 80 (`#F2BF4F`) instead of M3's tone 90

Secondary stays at tone 80 rather than dropping to 75 because yellow at tone 75 muddies into mustard/dijon territory and loses the sun-gold character. Green and blue at tone 75 stay confident and clear.

The dark scheme uses standard M3 tone 30 containers with no overrides. Dark surfaces support saturated fills naturally, so containers don't need the same treatment to feel substantive.

### Container usage hierarchy

When more than one container could plausibly be used for a given surface, prefer them in this order:

- **Primary container** — for the screen's main attention surface, the thing the user is meant to act on
- **Secondary container** — for the user's role/identity surfaces and persistent indicators
- **Tertiary container** — for ambient or informational surfaces that don't require action
- **Error container** — only for genuine error or destructive-warning states

### Token usage

Always reference colors by their role token name, never by hex value. Generated UI should reach for `primary`, `on-primary`, `surface-container`, and so on — not for `#176B4B` or `#ECEEEA`. The hex values in this file are implementation detail; the agent's job is to compose role-token references that resolve to those values at render time.

If the full tonal palette is ever made available alongside this file (in a tokens file, language-specific source file, or similar), those raw tones are reference material only. They should not be used directly in screen designs. Reaching for a specific palette tone instead of a role token is a signal that the role mapping needs fixing, not a workaround.

When generating light-mode UI, use the default role tokens. When generating dark-mode UI, use the `dark-`-prefixed equivalents. Tokens from the two schemes are not interchangeable and should not appear in the same view.

The semantic role of each brand color is fixed and should not be repurposed:

- **Primary (fairway green)** is the call-to-action color — the main affordance to act on a given screen. Not for identity, status, or ambient indicators.
- **Secondary (sun gold)** is the identity color — role badges, user attribution, active-state markers. Not for action affordances.
- **Tertiary (sky blue)** is the informational accent — ambient status, noted-but-not-actionable indicators. Not for primary actions or attention surfaces.
- **Error (red)** is reserved for destructive actions and validation states. It should not appear in normal application flow.

## Typography

Typography is the one place where Android and iOS intentionally diverge.

On **Android**, the system uses the standard Material 3 type scale with Roboto across all 15 levels (display, headline, title, body, label — each in large, medium, and small). The YAML typography block specifies these as the canonical values.

On **iOS**, the system uses the standard iOS type system — SF Pro with Dynamic Type — rather than reproducing the M3 values. The agent should pick the iOS type style whose semantic intent best matches the M3 role being used (for example, `title-medium` maps to iOS `.headline`, `body-large` to `.body`, `label-small` to `.caption1`). Exact size matching is not the goal; matching semantic intent is.

Why the divergence: text rendering is so deeply platform-native that forcing one font system on both makes iOS feel slightly off and Android slightly wrong. Both Roboto-on-Android and SF-on-iOS are tuned for their respective platforms' rendering pipelines, hinting, and Dynamic Type / accessibility infrastructure.

The minimum allowable text size is `label-small` on Android (11px) or its iOS equivalent. Do not use anything smaller for user-readable text on either platform. Decorative numerals and dividers may go smaller.

## Layout

The system uses a 4dp baseline grid with an 8dp spacing scale. Standard scale levels are `xs` (4dp), `sm` (8dp), `md` (16dp), `lg` (24dp), `xl` (32dp), `2xl` (48dp), `3xl` (64dp). The `base` token (4dp) represents the underlying grid; any spacing not on a scale level should still be a multiple of the base.

The system is mobile-first. Phone layouts are full-bleed single-column. Tablet and desktop layouts are out of scope for this version of the design system; phones in landscape use the same screens reflowed.

Edge gutters on phone are typically `md` (16dp). Vertical rhythm between major sections is typically `lg` (24dp); between cards in a list, `sm` (8dp).

The same spacing scale applies on both platforms. iOS implementations should map dp to point values 1:1 (since both platforms target the same physical density target on phone), and use the same scale level for the same purpose across platforms.

## Elevation & Depth

The system uses tonal surface layers rather than shadows to convey hierarchy. The M3 surface containers (lowest, low, default, high, highest) provide five levels of "elevation" through progressively shifted neutral tones. Cards sit on `surface-container`; elevated affordances use `surface-container-high`; the lowest layer (`surface-container-lowest`, which equals pure white in light mode) is reserved for surfaces that should feel like floating above everything else.

Shadows are not used as a default hierarchy device. The warm-neutral ramp does the work of differentiating layers, which keeps the design feeling flat and modern rather than skeumorphic, and reads well in dark mode where shadows are visually weak.

Platform-conventional floating or overlay patterns may render their default elevation shadows where users expect them on iOS and Android (these are rendered by the platform automatically and would feel wrong without). This is the only case where elevation overlaps content rather than being arranged via stacking surfaces.

## Shapes

The system uses the Material 3 shape scale: `none` (0dp), `xs` (4dp), `sm` (8dp), `md` (12dp), `lg` (16dp), `xl` (28dp), and `full` (pill / fully rounded). Default mappings for atomic UI elements:

- Buttons use `full` (pill shape)
- Cards use `md` (12dp)
- Text fields use `xs` (4dp) on their bottom corners
- Chips use `sm` (8dp)

Within a single view, do not mix shape scales unnecessarily. Picking either rounded-leaning or sharper-leaning per view is fine; mixing them creates visual noise.

The same shape scale and default mappings apply on both platforms.

## Components

This section describes how components should be styled when they're used. It does not prescribe an inventory of components that must appear, or specify which patterns a given screen should contain — those are layout decisions made screen by screen, informed by the task at hand.

Whatever components a screen does use follow Material 3 conventions by default, with these additional styling principles:

- The single most prominent action on a screen uses `primary` as its fill. Lower-emphasis actions use outlined, text, or `secondary-container` variants. Typically there is one primary-filled affordance per screen.
- Application chrome (top regions, navigation regions, container backgrounds) uses `surface` and `surface-*` tokens, not `primary`. Brand presence lives in actions and content, not in chrome.
- Surfaces grouping related information sit on `surface-container` and rely on tonal contrast against `surface` for hierarchy. Shadows are not the default elevation mechanism.
- Active selection indicators (selected nav item, active filter, pressed toggle) use `secondary-container` as their fill with `on-secondary-container` for text and icons.
- State badges and small categorical indicators use container colors to communicate semantic state, per the Container usage hierarchy in the Colors section.
- Text inputs default to the M3 outlined variant. The filled variant is acceptable for forms where multiple fields stack closely.
- Standard interactive states (hover, pressed, focused, disabled) follow Material 3 conventions without explicit overrides.

The same styling principles apply on both Android and iOS. When an iOS implementation chooses to use a UIKit/SwiftUI-native control instead of a Material-styled equivalent (e.g., a UIKit picker, an iOS sheet presentation), the control's chrome remains native but its content colors should still draw from the Fairway Morning color tokens.

## Do's and Don'ts

**Do** strive for visual parity between Android and iOS implementations. Divergence is the exception, reserved for clear idiomatic platform differences.

**Do** use the Material 3 color scheme on iOS as well as Android. The visual identity must carry across both platforms.

**Do** substitute the iOS native type system (SF Pro, Dynamic Type) when implementing on iOS. Typography is the one approved cross-platform divergence.

**Do** use fairway green sparingly — only for the most important action on each screen. Two prominent green elements per screen is usually one too many.

**Do** lean on M3 surface containers for visual hierarchy rather than reaching for shadows or borders. The warm-neutral ramp is doing real work.

**Do** maintain WCAG AA contrast (4.5:1 for normal text). The on-* role colors are tuned to meet this against their paired containers; if a custom pairing is created, verify contrast explicitly.

**Do** reference colors by their role token name, never by literal hex value. If a color you need doesn't correspond to a defined role token, that's a signal the token system needs an addition — not that you should reach into the raw palette.

**Don't** introduce iOS-specific styling that breaks visual parity with Android (e.g., using Apple's standard color palette on iOS, or adopting iOS-default fills for chrome). The design system is the source of truth on both platforms.

**Don't** use all three brand colors prominently in the same view. Typically only one or two should be in active use; the third reads as ambient context.

**Don't** mix tokens from the light and dark schemes in the same view. A screen is either in light mode or dark mode; tokens from the other scheme are never valid references in that context.

**Don't** introduce shadows as a default hierarchy device. The system is intentionally flat and tonal.

**Don't** use type sizes below `label-small` (11px) on Android, or below the iOS equivalent on iOS, for any user-readable text. Decorative numerals and dividers may go smaller, but text people are expected to read should not.

**Don't** mix the brand-deviation light-mode container tones (75/80/75) with M3's default tone 90 containers in the same view. Either use the system-specified containers consistently, or use surface containers instead.

**Don't** use sun gold for "action" affordances. Gold communicates *identity* (role, user, "your space") in this system; it should not be conflated with calls to action, which are green.
