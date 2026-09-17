# Worked example — TTC-67, "Create an account and sign in"

## Contents
- Three rejected drafts — and the reason each was wrong
- The accepted criteria — all 18, grouped as shipped
- Two functional ACs added alongside
- Development Notes written at the same time

The accepted output, plus the drafts that were rejected on the way there. The rejections are the
more useful half: each one was a plausible reading of "design criteria" that turned out to be
wrong, and the same mistakes are easy to repeat.

## Three rejected drafts

**Draft 1 — developer-focused conformance checklist.** Items like "screens are built from the
shared Ttc components, no bespoke field or button; no corner radius is hard-coded — every one
resolves through the shape scale" and "all four theme/platform combinations use theme roles;
nothing is a literal hex."

> *Rejected:* "They are not going to be able to tell if a `TtcTextField` was used, or if the
> corner rounding was 4.dp (and certainly not if it was hard coded or a shape value was used).
> They are going to be visually inspecting the app and making sure it looks like the designs, not
> inspecting the code."

The reviewer is the constraint. Component names, measurements, and token names describe
implementation, and implementation guidance belongs in Development Notes.

**Draft 2 — visual-fidelity checklist.** The tokens were gone, but items still walked through what
each screen looks like: "the sign-in screen has the same things in the same order as the frame:
the round badge with the name and tagline under it, then Email, then Password with an eye icon…"

> *Rejected:* "What I'm looking for is a list of things that aren't really covered (and probably
> shouldn't be covered) in the functional ACs — the 'extra functionality' that comes out in the
> design."

Restating the frame is redundant: the reviewer has the frame, and so does the developer.

**Draft 3 — the copy question.** An early round spelled out every string, including labels,
placeholders and the tagline.

> *Refined to:* "If they can get it from the Design mockups, then it probably doesn't need a
> specific mention. If they have to click through the prototype to *find* it, then it probably
> does warrant a mention." So: error and confirmation copy in, labels and placeholders out.

This is the filter that generalizes to everything, not just copy.

## The accepted criteria

Intro paragraph carried the project link, the four canvas sections, and the prototype fixtures
(`dana@golf.app` / `fairway`; any unused email; phone `(555) 123-4567`). Then, numbered 1–18:

### Credentials screen

1. Password masked by default; the eye icon toggles it to readable text and back, and the icon
   changes to reflect the current state.
2. **Sign in** and **Create account** are both disabled until the email looks like an email — it
   needs an `@` and a dot, so a half-typed address keeps both off — and the password is non-empty.
   Disabled buttons look disabled, not merely unresponsive.
3. Email field has focus on arrival with the email keyboard up; neither email nor password
   auto-capitalises or autocorrects. On the create-account step focus starts on the name field,
   which does capitalise each word.
4. Errors clear as soon as you fix them: editing the email dismisses a message block, editing the
   password clears its inline error.
5. **Two distinct error treatments, and they don't get merged into one.** A wrong password shows
   *on the password field*, replacing the hint. The three "account already exists" cases show as a
   message block *above the fields*. No error navigates away or empties the form; the email
   survives every failure.
6. **Each message block's button does something specific:** unrecognised email → **Create
   account** goes straight to the create-account step with the email carried over; email already
   in use → **Sign in instead** stays put, keeps the email, clears just the password, and
   dismisses the block; phone already in use → **Sign in instead** returns to the credentials
   screen.

### Create-account screen

7. **Create account** disabled until the name is non-empty *and* the mobile number is a complete
   ten digits — nine digits keeps it off.
8. Mobile number takes digits only (numeric keypad, anything else ignored), formats itself as you
   type into `(555) 123-4567`, and stops at ten digits. The stored number is digits only, so
   formatting never affects how invitations match a person.
9. The photo is genuinely optional — you can complete sign-up without it. Tapping the target adds
   one; tapping again removes it, and the caption and badge icon change with it.
10. Going back to the credentials screen preserves the email that got you here.
11. Phone-already-in-use shows **both** treatments at once: the message block *and* an inline
    error on the mobile field. Editing the number clears both.

### In the design, but deliberately not in this story

12. The Profile tab shows identity and **Sign out** only. The design's settings list — *Edit
    profile*, *Notifications*, *Change password*, *Help & support* — and the version line are
    **intentionally out of scope**. Their absence is correct and shouldn't be raised as missing
    work.
13. Also intentionally absent from the design, and not to be added: forgot/reset password,
    third-party or social sign-in, an email verification step, and a separate confirm-password
    field. One password field is the whole of it.

### Everywhere in the flow

14. A short confirmation message appears low on the screen after each auth transition and fades on
    its own after a couple of seconds — after signing in, after completing sign-up, after signing
    out. Wording in item 18.
15. Light and dark follow the device setting. There is no in-app theme switch — the Light/Dark
    toggle in the prototype is a design-harness control, not part of the app.
16. With the keyboard up, the focused field and the button you're heading for stay reachable;
    content scrolls rather than sitting behind the keyboard. The frames show no keyboard, so this
    only shows up on a device.

### Where the two platforms diverge on purpose

17. Three places to check against each platform's own frames rather than assuming parity:
    - **Text fields.** Android: label starts inside the field and shrinks to the top when focused,
      over an underline. iOS: label sits above the input permanently, and a border appears around
      the whole rounded card when focused.
    - **Create-account header.** Android: back arrow with the title beside it, both left-aligned.
      iOS: "‹ Back" on the left with the title centred.
    - **Credentials screen top.** No title on either platform, but Android leaves the app-bar
      strip empty while iOS has no header at all.

### Copy that isn't readable off the frames

18. Wrong password: *That password doesn't match. Try again.* — Unrecognised email: *We don't
    recognize that email* / *There's no TeeTimeCaddie account for {email}. Want to create one?* /
    button *Create account* — Email in use: *That email is already in use* / *An account with
    {email} already exists. Sign in instead?* / button *Sign in instead* — Phone in use: *That
    phone number is already in use* / *Looks like you already have a TeeTimeCaddie account. Sign
    in instead — we'll keep your invitations together.* / button *Sign in instead*, plus field
    error *Already linked to another account* — Confirmations: *Welcome back, {First}* /
    *Account created · welcome, {First}* / *Signed out*.

## Two functional ACs added alongside

The design showed the Profile tab displaying identity data that no AC required — a gap in the
functional criteria, not a design criterion. Appended to the existing list as 9 and 10:

> 9. On the Profile tab, a signed-in person sees their own identity: their photo if they added
>    one — otherwise their initial — along with their name, their email address, and their phone
>    number.
> 10. The Profile tab presents a Sign out action.

Criterion 12 then does the complementary job of saying what *isn't* wanted there.

## Development Notes written at the same time

Lead paragraph: build from the existing shared components in `ui/common` wherever one fits —
`TtcTextField` / `TtcPasswordField`, `TtcButton` / `TtcOutlinedButton`, `TtcCard` /
`TtcAccentCard`, `TtcPhotoPicker`, `TtcAvatar` — because they already carry the Fairway Morning
colors, type, and shape scale, so using them is what makes the screens match; a bespoke view
inside the auth feature is what drifts.

Then eight numbered gaps. Note how the first two resolved — the initial draft proposed *new*
components and the user redirected both toward composition:

1. Block-level error messages: a `TtcCard` with color role `Error` (`Secondary` for the two
   softer cases), holding icon, title, body, and a dense `TtcOutlinedButton`. **No new
   `TtcBanner`** — the first draft proposed one and it was rejected in favour of composing.
2. Toast: Android uses the Material 3 `Snackbar`; iOS a floating `TtcAccentCard` with accent
   `Primary`. Again, composition over a new component.
3. iOS `TtcTextField` has no public trailing-icon parameter, though the container supports one
   internally — needed for the eye toggle.
4. iOS icon assets missing (Android has `TtcIcons`; iOS has 7 assets and no equivalent).
5. iOS has no explicit type scale.
6. The auth screens are stubs, but ViewModels, DI, navigation and the shared
   `sdk/features/auth` repository all survived and should be reused.
7. **Open question** — photo picking is presentation-only on both platforms; is capture/upload in
   scope or deferred?
8. **Open question** — do Terms and Privacy link anywhere? The design accents them as links; the
   prototype gives them no destination.

The pattern worth carrying forward: when a design element has no component, name the existing
component to compose it from rather than proposing a new one, and leave genuine unknowns as
questions instead of resolving them silently.
