# TeeTimeCaddie

A mobile app (Android and iOS) that helps a recurring golf group coordinate among themselves — chasing RSVPs, assigning players to tee time slots, managing drop-outs and waitlists, sharing organizer duties across multiple people.

This is a **Utility** app for users to manage information, not to feel romantic about golf. Usage of background and banner images should be kept
to a minimum, and only be included when adding meaningful or necessary function, and not as an asthetic. 

The app **does not book or cancel tee times with golf courses**. The person organizing a Game handles all course interactions externally.

## Users

Three rough user types. A given person may be all three on different Games.

- **Organizers** run the group's logistics, book tee times externally, and use the app to coordinate everyone else. They have editorial control over the Games they create. Power-user sessions, deep but less frequent.
- **Regular players** play frequently and need fast, low-friction RSVPs and a quick view of what they're committed to. Short, single-task sessions — most app sessions look like this.
- **Occasional guests and subs** play once in a while, often added by a regular as a Guest. May or may not have the app installed.

The default surface (Games tab) optimizes for regular-player sessions; organizer power lives one tap deeper, behind affordances on specific Games.

## Concepts

- **Game** — a scheduled outing on a specific date at a specific course. Contains one or more Tee Times.
- **Tee Time** — an individual reservation slot within a Game, capable of holding up to 4 players. A small Game has one Tee Time; a larger Game has several.
- **Course** — the golf course where a Game is played. Currently just a name; will support a directory later.
- **Group** — a saved list of regular players (e.g., "Saturday Morning Crew") used as a shortcut when inviting people to a Game. A user can belong to multiple Groups. Groups are invite-list utilities, not structural parents of Games.
- **RSVP** — a player's response to a Game invite: Yes, No, or still Pending. No "Maybe" state.
- **Slot assignment** — which Tee Time a player is assigned to within a multi-Tee-Time Game. Players pick their slot when RSVPing yes; the organizer can move any player between slots at any time.
- **Waitlist** — when all Tee Times are full, additional yeses go on a Game-level waitlist and get auto-promoted as space opens.
- **Guest** — an additional player added to a Game by an existing invitee (the *Host*). Guests can be other app users (who RSVP themselves) or non-app guests (managed by their Host on their behalf). The distinction is invisible to the Host; the app figures it out by phone-number matching.

## Roles

A user can hold different roles in different contexts simultaneously. Roles change which affordances appear on a screen, not which screen the user is on.

- **Player** — invited to a Game. RSVPs, picks a slot, may bring a Guest, may leave the Game.
- **Organizer** — has editorial control over a Game. The *Primary Organizer* is the creator and can promote others to *Co-Organizer*, transfer Primary status, or change the Game's management mode. Co-Organizers have full editorial rights except managing the organizer roster.
- **Host** — the player who invited a particular Guest. Manages that Guest's status if non-app.
- **Group Admin** — manages a Group's membership (Primary Admin / Co-Admin asymmetry mirrors Organizer roles).

A Game has one of three management modes set by its Primary Organizer: *Solo* (only Primary edits), *Co-organized* (Primary plus named Co-Organizers edit), or *Open* (any invited player edits).

## Screens

The app has three bottom tabs: **Games** (default), **Groups**, **Profile**.

- **Onboarding** (first run only). Two screens — credentials (email + password) and profile setup (name, phone number, optional photo).
- **Games tab** (default landing). The home screen. Chronological list of upcoming Games at the bottom; an "attention section" at the top surfacing pending actions (pending RSVPs, decisions waiting on the user, etc.). The attention section is hidden when nothing's pending. Each Game card shows course, date, Tee Time(s), the user's RSVP status, and a role badge if relevant. Includes a create-Game affordance.
- **Game detail**. The most complex screen and the gravity well of the app. Shows the course and date in a header, then each Tee Time with up to 4 player slots assigned, then other invitees (pending, waitlist, declined collapsed), then a role-conditional actions area. The same screen serves everyone; what affordances appear depends on whether the user is a player, organizer, host, or some combination. Editing is mostly inline tap-to-edit (tap the date, tap a Tee Time, tap a player to manage them).
- **Create Game**. Flow for organizers. A single form for course (free-text), date, and one or more Tee Times; pushes to the invitee picker to add invitees; publishes on Done.
- **Invitee picker**. Shared screen used in Create Game, Add Invitees on an existing Game, and Add Members on a Group. Three sources: existing Groups, people you've played with before, device contacts.
- **Groups tab**. List of Groups the user belongs to. Includes a create-Group affordance.
- **Group detail**. Header (name, optional description, optional photo), members list with role badges, role-conditional actions area. Same role-conditional pattern as Game detail.
- **Create Group**. Name, optional description, optional photo, initial members.
- **Profile tab**. Account info, basic settings, sign out.

### Modal and pushed sub-screens

These open from the screens above:

- **Slot picker** — picking or changing a Tee Time assignment, for self or (if organizer) for any player
- **Add Guest** — host-side flow to bring an extra player; the app handles app-user-vs-non-app routing under the hood
- **Manage organizers** — Primary Organizer promotes Co-Organizers, changes management mode, or transfers Primary status
- **Activity log** — chronological list of every organizer-level action on a Game (timestamp + actor + what changed), visible to all invitees
- **Confirmation dialogs** — for destructive actions: Cancel Game, Cancel a Tee Time, Leave Group, Transfer Primary, Remove invitee

## Common actions

- **RSVP** (Yes / No) from the attention section, the Game card, or the Game detail
- **Pick a Tee Time** when RSVPing yes to a multi-Tee-Time Game; auto-assigns if no pick
- **Move between Tee Times** — players can self-move if there's room; organizers can move anyone
- **Add a Guest** — any invitee can bring an extra player
- **Create a Game** — organizers only (anyone can be an organizer on a Game they create)
- **Edit Game** — mostly inline on Game detail
- **Cancel Game** or **Cancel a Tee Time** — via confirmation dialog
- **Leave a Game** — Primary Organizers must hand off Primary status first
- **Create a Group**, **Add members**, **Leave a Group**
- **Promote a Co-Organizer**, **Transfer Primary** — Primary Organizers only

## Out of scope (do not design)

Features that are explicitly not in the app right now. The agent should not produce designs for these.

- Tee time booking with golf courses (course interactions are external)
- Push notifications and in-app notification center (users discover invites and updates on next app open via the attention section)
- Past Games view (only upcoming Games are user-facing)
- In-app chat (Game-level or Group-level)
- Calendar integration
- Password reset, email verification, SMS verification
- Recurring Game templates
- Standby Guests (all Guests count toward capacity from the moment added)
- Shareable invite links
- Tablet or desktop layouts
- Player preferences (preferred Tee Time, preferred playing partners, etc.)
- Course directory with addresses, photos, search
- Activity log filtering, search, or export

## Design context

Game detail is the gravity well — most user journeys lead there, and it must gracefully serve players, organizers, and hosts on the same screen via conditional affordances. Other screens are simpler.

The Games tab is the highest-traffic surface; most sessions land here and either drill into a Game or leave. The attention section is the most important element above the fold when something's pending; the chronological list is the most important when nothing is.

A single-Tee-Time Game is the N=1 case of the multi-Tee-Time model. The same layouts should work for both, with the multi case adding repetition rather than restructuring.

The app's job is to *eliminate* coordination work, not relocate it. Screens should reduce the labor of running a recurring group, not add new ceremony. When a screen could either ask the user to do something or quietly handle it, the right choice is usually the latter.
