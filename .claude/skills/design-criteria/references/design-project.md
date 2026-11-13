# The Tee Time Caddie design project

## Contents
- The two artifacts — links, and which one wins
- Reading the files — the Artifact tool, and why the prototype lives in `flow/`
- App project layout — screen sources, prototype wiring, canvas, screenshots
- Canvas sections and screenshots by story — TTC-67 through TTC-73
- Prototype fixtures — how to reach each state

The UI designs live in **Claude artifacts** (migrated from the retired standalone Claude Design),
not Figma. Read them with the `Artifact` tool.

## The two artifacts

| | Name | Artifact |
|---|---|---|
| **App project** (source of truth) | Tee Time Caddie | https://claude.ai/artifact/8vv8LAVwoNnUDUahyFgFzk |
| **Design system** (supporting context) | Fairway Morning Design System | https://claude.ai/artifact/QtksCiiJ3rTcgvR1XeL5fk |

Use the app project link as the "design link" in a story's Design Criteria.

**On conflict the app project wins.** The design system is shared foundations — colors, type,
shape scale, component kit — and is worth reading for Development Notes, but it doesn't override
a screen.

The app project contains a `project/_ds/fairway-morning-design-system-a09a6cd5-.../` folder. That's
a partial **mirror**, four files only, and its README describes the *design system's* layout rather
than the app project's — don't judge it against the app project's file list and conclude it's
stale. The uuid in the folder name is the old Claude Design project id; it is not an artifact id
and can't be looked up. Use the design-system link above.

## Reading the files

- List: `Artifact` `action: "list"`, `scope: "files"`, `url: <artifact>`.
- Read: `Artifact` `action: "read"`, `url: <artifact>`, `paths: [...]` (up to 256 per call). Small
  text files come back inline; every file is also saved under the scratchpad, so PNG screenshots can
  be opened with `Read` and looked at.
- Every design file sits under `project/` — e.g. `project/screens/details.jsx`. The other top-level
  entries (`index.html`, `SKILL.md`, `artifact-type/`) belong to the artifact type, not the design.
- Read the designs in the main session. The `Artifact` tool isn't available to Explore or Plan
  agents, so don't delegate this step.

**Why `flow/`.** The Artifact tool refuses any path containing the reserved word `prototype`, so
the prototype wiring was moved from `prototype/` to `flow/` (and `v2/prototype/` to `v2/flow/`).
Keep it that way — don't name any design folder or file `prototype`, `constructor` or
`__proto__`, or it becomes unreadable.

## App project layout

All paths below are relative to `project/`.

Screen sources — the interesting part:

- `screens/auth.jsx` — the three auth bodies (credentials, profile setup, profile tab), including
  all validation and error logic and the demo fixtures
- `screens/create.jsx` — new-game screen; `screens/add-players.jsx` — adding invitees;
  `screens/games.jsx` — games list and empty state; `screens/details.jsx` — game details, with
  `screens/details-invitee.jsx` and `screens/details-organizer.jsx` for the role-specific states;
  `screens/parts.jsx` — shared pieces and the tab bars
- `screens/auth-static.jsx` and `screens/create-static.jsx` — those two bodies wrapped in device
  chrome, showing which error fixtures each frame demonstrates.

Prototype wiring (`flow/`) — where behavior lives:

- `flow/app.jsx` — the state machine: what each callback does, what's persisted, which
  transitions fire which toast. **This is where most category-1 criteria come from.**
- `flow/shells.jsx` — `AndroidShell` and `IOSShell`: the per-platform chrome, app bars, tab
  bars, FAB vs nav "+", and the `Toast` component. **This is where platform divergences come
  from.**
- `flow/organizer.jsx` — organizer-side flows; `flow/screens.jsx`, `flow/harness.jsx` — body
  dispatch and the tweaks harness
- `v2/` and *TeeTimeCaddie Prototype v2* — Brad's personal sandbox, not the design. **Never read,
  cite, or build from anything under `v2/`.** The top-level `screens/` and `flow/` sources (v1) are
  current.

Canvas and exports:

- `app.jsx` — the Screen Designs canvas composition. **Read this first** for a story: it names
  every section and artboard, and each section's `subtitle` is a short design rationale worth
  quoting from.
- `design-canvas.jsx` — the canvas scaffold itself (generic; rarely worth reading)
- `library.jsx`, `frames/{ios,android}-frame.jsx`, and the standalone HTML exports:
  *TeeTimeCaddie Screen Designs*, *TeeTimeCaddie Prototype*, *TeeTimeCaddie Component Library*
- `debug/` and `uploads/` — working scraps; ignore

Reference PNGs (~70): `screenshots/{ios,android}-{light,dark}/` each hold 14 numbered screens,
plus `screenshots/error-states/` (5). Cite these by path in criteria — they're the flat reference
a reviewer can pull up fast — and open them to check what a screen shows.

## Canvas sections and screenshots by story

A starting map for the TTC-66 slice. Verify against `app.jsx` rather than trusting it blindly —
sections get added.

| Story | Canvas sections | Screenshots |
|---|---|---|
| TTC-67 Create an account and sign in | *Sign in*, *Sign in — error states*, *Create account*, *Profile* | `01-sign-in`, `02-create-account`, `06-profile`, `14-sign-in-error`, `error-states/01`–`04` |
| TTC-68 Create and publish a Game | *New game* | `05-create-game`, `09-create-date-picker`, `10-create-time-picker`, `error-states/05` |
| TTC-69 Invite people from contacts | *New game* (contacts portion) | `11-create-contacts-preprompt`, `12-create-contacts-permission-dialog`, `13-create-contacts-picker` |
| TTC-70 Discover and respond to an invitation | *Games*, *Games — multiple pending invites*, *Game details — invitee* | `03-games-list`, `08-games-invite-accepted`, `04-game-details` |
| TTC-71 See up-to-date RSVP status | *Game details* | `04-game-details` |
| TTC-72 Record a response for a non-app invitee | *Game details — organizer records a response*, *Invitee's app after the organizer records a response* | `04-game-details` |
| TTC-73 Invite by name and phone number | *New game* (contacts portion) | `13-create-contacts-picker` |

The *Games — empty state* section (`07-games-empty`) serves the epic's "guided toward creating
your first Game" AC — check which story currently owns it.

Most sections carry four artboards: *iOS · Light*, *iOS · Dark*, *Android · Light*, *Android ·
Dark*. The newer state sections pick a few labelled states instead.

## Prototype fixtures

Defined at the top of `screens/auth.jsx`. These are how a reviewer reaches each state, so cite
them in the Design Criteria intro paragraph — but they're test data, not requirements.

- `dana@golf.app` / `fairway` — an existing account (also `brad@golf.app` / `fairway`)
- any unused email — drives the "no account" and create-account paths
- phone `(555) 123-4567` — already in use, triggers the phone-taken rejection

Equivalent fixtures for other screens live at the top of the corresponding `screens/*.jsx`.
