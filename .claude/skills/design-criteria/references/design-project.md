# The Tee Time Caddie design project

## Contents
- The two projects — ids, URLs, and which one wins
- App project layout — screen sources, prototype wiring, canvas, screenshots
- Canvas sections and screenshots by story — TTC-67 through TTC-73
- Prototype fixtures — how to reach each state
- Access — why DesignSync may be missing

The UI designs live in **Claude Design**, not Figma. Read them with the `DesignSync` tool —
`get_project`, `list_files`, `get_file`.

## The two projects

| | Name | Project id |
|---|---|---|
| **App project** (source of truth) | Tee Time Caddie | `c53e7c95-1c6b-4468-a7a4-20748a9b1a0c` |
| **Design system** (supporting context) | Fairway Morning Design System | `a09a6cd5-fe23-4aa4-9c49-648497897a8e` |

- App project: https://claude.ai/design/p/c53e7c95-1c6b-4468-a7a4-20748a9b1a0c
- Design system: https://claude.ai/design/p/a09a6cd5-fe23-4aa4-9c49-648497897a8e

**On conflict the app project wins.** The design system is shared foundations — colors, type,
shape scale, component kit — and is worth reading for Development Notes, but it doesn't override
a screen.

The app project contains an `_ds/fairway-morning-design-system-a09a6cd5-.../` folder. That's a
partial **mirror**, five files only, and its README describes the *design system's* layout rather
than the app project's — don't judge it against the app project's file list and conclude it's
stale. The uuid in the folder name is the design-system project's own id.

`DesignSync`'s `list_projects` returns `[]` here even though both projects are readable by id. An
empty list is not proof of no access — go straight to `get_project` / `list_files` with the ids
above.

## App project layout

Screen sources — the interesting part:

- `screens/auth.jsx` — the three auth bodies (credentials, profile setup, profile tab), including
  all validation and error logic and the demo fixtures
- `screens/create.jsx` — new-game screen; `screens/games.jsx` — games list and empty state;
  `screens/details.jsx` — game details; `screens/parts.jsx` — shared pieces and the tab bars
- `screens/auth-static.jsx` and `screens/create-static.jsx` — those two bodies wrapped in device
  chrome, showing which error fixtures each frame demonstrates. There are no static variants for
  games or details; get their per-platform chrome from `prototype/shells.jsx`.

Prototype wiring — where behavior lives:

- `prototype/app.jsx` — the state machine: what each callback does, what's persisted, which
  transitions fire which toast. **This is where most category-1 criteria come from.**
- `prototype/shells.jsx` — `AndroidShell` and `IOSShell`: the per-platform chrome, app bars, tab
  bars, FAB vs nav "+", and the `Toast` component. **This is where platform divergences come
  from.**
- `prototype/screens.jsx`, `prototype/harness.jsx` — body dispatch and the tweaks harness

Canvas and exports:

- `app.jsx` — the Screen Designs canvas composition. **Read this first** for a story: it names
  every section and artboard, and each section's `subtitle` is a short design rationale worth
  quoting from.
- `design-canvas.jsx` — the canvas scaffold itself (generic; rarely worth reading)
- `library.jsx`, `frames/{ios,android}-frame.jsx`, and three standalone HTML exports:
  *TeeTimeCaddie Screen Designs*, *TeeTimeCaddie Prototype*, *TeeTimeCaddie Component Library*

Reference PNGs (~70): `screenshots/{ios,android}-{light,dark}/` each hold 14 numbered screens,
plus `screenshots/error-states/` (5). Cite these by path in criteria — they're the flat reference
a reviewer can pull up fast. Note `get_file` returns them base64-encoded, so they can't be viewed
directly; use them as citations, and read behavior from the source instead.

## Canvas sections and screenshots by story

A starting map for the TTC-66 slice. Verify against `app.jsx` rather than trusting it blindly —
sections get added.

| Story | Canvas sections | Screenshots |
|---|---|---|
| TTC-67 Create an account and sign in | *Sign in*, *Sign in — error states*, *Create account*, *Profile* | `01-sign-in`, `02-create-account`, `06-profile`, `14-sign-in-error`, `error-states/01`–`04` |
| TTC-68 Create and publish a Game | *New game* | `05-create-game`, `09-create-date-picker`, `10-create-time-picker`, `error-states/05` |
| TTC-69 Invite people from contacts | *New game* (contacts portion) | `11-create-contacts-preprompt`, `12-create-contacts-permission-dialog`, `13-create-contacts-picker` |
| TTC-70 Discover and respond to an invitation | *Games*, *Game details* | `03-games-list`, `08-games-invite-accepted`, `04-game-details` |
| TTC-71 See up-to-date RSVP status | *Game details* | `04-game-details` |
| TTC-72 Record a response for a non-app invitee | *Game details* | `04-game-details` |
| TTC-73 Invite by name and phone number | *New game* (contacts portion) | `13-create-contacts-picker` |

The *Games — empty state* section (`07-games-empty`) serves the epic's "guided toward creating
your first Game" AC — check which story currently owns it.

Each section carries four artboards: *iOS · Light*, *iOS · Dark*, *Android · Light*, *Android ·
Dark*.

## Prototype fixtures

Defined at the top of `screens/auth.jsx`. These are how a reviewer reaches each state, so cite
them in the Design Criteria intro paragraph — but they're test data, not requirements.

- `dana@golf.app` / `fairway` — an existing account (also `brad@golf.app` / `fairway`)
- any unused email — drives the "no account" and create-account paths
- phone `(555) 123-4567` — already in use, triggers the phone-taken rejection

Equivalent fixtures for other screens live at the top of the corresponding `screens/*.jsx`.

## Access

`DesignSync` is main-session only — subagents don't get it, so read the designs yourself instead
of delegating. If it's missing from a main session, the claude.ai login needs design scopes;
`CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` in `~/.zshrc` also hides it.
