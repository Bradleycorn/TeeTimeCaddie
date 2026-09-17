---
name: design-criteria
description: Adds Design Criteria and Development Notes to a TeeTimeCaddie (TTC) Jira story by mining the Tee Time Caddie Claude Design project for behavior the story's functional acceptance criteria don't cover. Use when the user asks to add, fill in, or finish design criteria on a TTC issue; says a story's ACs are functional-only or that design criteria are still needed before it can be pulled; or asks to groom or prep a TTC story against the designs.
---

# Adding Design Criteria to a TTC story

Story preparation: edits two Jira fields.

- `references/design-project.md` — project ids, file layout, screen-to-story map, fixtures
- `references/jira-mechanics.md` — cloudId, field ids, ADF rules, verification script
- `references/example-ttc-67.md` — accepted output, plus rejected drafts and why

## Workflow

`EnterPlanMode` first — the wording needs approval before it hits a live issue.

```
- [ ] 1. Atlassian:getJiraIssue with fields ["*all"] — the ACs are omitted by default
- [ ] 2. Read the parent epic and every sibling story — a screen is usually split across
         several, and what a sibling covers is out of scope here
- [ ] 3. Read the designs — canvas composition, screen source, and prototype wiring.
         DesignSync is main-session only; don't delegate this step to a subagent
- [ ] 4. Inventory ui/common components (Explore agent) — for Development Notes only
- [ ] 5. Draft all three edits into the plan file as final text, not an outline
- [ ] 6. AskUserQuestion on scope gaps
- [ ] 7. ExitPlanMode, wait for approval
- [ ] 8. Atlassian:editJiraIssue — ADF, one call per field
- [ ] 9. Read back and check structure
```

## What counts as a design criterion

A product reviewer checks these against the running app on both platforms with the design open
beside them. They cannot read code.

**Filter:** readable off the Screen Designs → out. Only findable by clicking the prototype → in.

| Out | In |
|---|---|
| Screens use `TtcTextField`; no radius hard-coded | Both actions stay disabled until the email has an `@` and a dot and the password is non-empty |
| Fields are grey blocks, top corners rounded | Nine digits keeps Create account off; input stops at ten |
| The Profile tab matches its frame | The design's settings list is intentionally out of scope — its absence is correct |

Four categories:

1. Prototype-only behavior — disabled states, field transforms, what clears an error, what each
   button inside a message does, dead affordances (a control that only raises a "coming soon"
   toast, a menu with no handler), what survives navigation, and what a static frame can't show
   (keyboard occlusion, scrolling, light/dark following the OS).
2. Drawn in the design but out of scope here, plus what's conspicuously absent by design
   (password reset, social sign-in, verification). Without this a reviewer files correct work as
   incomplete.
3. Error and confirmation copy, verbatim. Labels and placeholders are in the frames; skip those.
4. Deliberate iOS/Android divergence.

Traps:

- The prototype's platform/theme toggles and tweaks panel are harness scaffolding — invert them
  (the app has *no* in-app theme switch).
- Design shows data no AC requires → propose a functional AC, not a design criterion.
- Prototype contradicts an AC → propose an AC edit, never a criterion papering over the conflict.
- An AC has no design at all → say so and ask. Never invent design behavior to fill the gap.

12–20 items, grouped by screen then omissions, cross-cutting, platform, copy, numbered
continuously. Open with the design link, which canvas sections to open, and the prototype
fixtures.

## Development Notes

`customfield_10131`, developer-facing. Lead with: build from the existing `ui/common` components
wherever one fits — they carry the design system's colors, type, and shape scale. Then the gaps
from the component inventory, preferring composition and naming what to compose from (a `TtcCard`
with an `Error` color role beats a new `TtcBanner`). Leave unknowns as questions.
