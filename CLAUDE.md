# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TeeTimeCaddie is a Kotlin Multiplatform mobile application (Android + iOS) for managing golf tee times (games). 
The application is primarily aimed at groups of golfers who play together, and need organization to keep
track of who can and cannot play in a game. All players in the group can see the tee times, opt in or out, 
get notifications of upcoming games, etc.

The project uses a modular architecture with shared business logic (80-90% code sharing) and 
platform-specific UI implementations.

**IMPORTANT:** EVERY time I ask you to implement a Jira Issue (Story, Defect, etc), use the
`jira-issue` skill, and follow the **Development Guidelines** section below.

## Jira (TTC)

**Routing.** TTC issues live on the personal site — use the `Atlassian` MCP server, not
TwinSpires. Site `bradleycorn.atlassian.net`, cloudId `5f936620-68a5-4bc4-93c9-2554f20f6511`.
The site rejects unbounded JQL; always constrain by project or date
(`project = TTC AND parent = TTC-66`).

**Custom fields.** Acceptance Criteria = `customfield_10130`, Development Notes =
`customfield_10131`, both rich text (ADF).
- Reads omit them: `getJiraIssue` returns them only with `fields: ["*all"]` (add
  `expand: "names"` to map ids to labels).
- Writes take ADF only. `contentFormat: "markdown"` is rejected for custom fields; it converts
  only `description` and comments.
- An edit replaces the whole field value. There is no append.

**Issue types.** Story, and the sub-task type `Subtask`.

**Assignees.** Brad Ball `5c28e6f1553b6b6604ec4b4e`; "Claude Code"
`712020:1e0aed79-20e7-4762-86ba-8a980721965e`.

**Transitions.** Story and Subtask share one workflow, and every transition is available from
any status. Transition by id, not by matching names.

| Transition | Id |
|---|---|
| To Do | `11` |
| In Progress | `21` |
| IN REVIEW | `2` |
| Done | `31` |

**Commit messages.** Issue work: `<SUB-TASK KEY> <Imperative summary>`, e.g.
`TTC-83 Fix the lost auth confirmations` — key, space, no colon. Work outside an issue:
`chore: <summary>`.


## Building & Running

Standard Gradle tasks (`build`, `test`, `allTests`, `lint`, `:android:app:assembleDebug`, ...) work as usual.

iOS app — use any available iPhone simulator with iOS 17.0+ (`xcrun simctl list devices available`):
```bash
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj -scheme TeeTimeCaddie \
  -configuration Debug -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16'
```
The Xcode build runs the KMP framework build as a build phase, so you do NOT need to run
`./gradlew linkDebugFramework...` first. Bundle id: `net.bradball.teetimecaddie`.

---

# KMP Shared Logic Architecture

## Overview

All business logic is shared between Android and iOS through Kotlin Multiplatform (KMP SDK).
The shared code provides repositories, data management, analytics, and domain models.
You should always strive to keep business logic out of the applications themselves, and
put it in the multiplatform modules instead.

The KMP SDK is made up of multiple gradle modules that are compiled into a single SDK artifact.
This modular approach enforces separation of concerns and encapsulation, exposing a semantic public
API for the applications to consume, and keeping implementation concerns private.

SDK module layout and conventions (`preview*` objects for new models, `toModel()`/`toDocument()`
mappers, private storage/network modules, `*Module` DI) are in `sdk/CLAUDE.md`, which loads when
working under `sdk/`.

---




## Code Sharing Strategy

**Shared (KMP):**
- ✅ All business logic (Repositories)
- ✅ Data layer (Storage, Firestore)
- ✅ Domain models
- ✅ Analytics system (EventManager)
- ✅ Error handling
- ✅ Resources (strings via Moko)

**Platform-Specific:**
- ❌ UI (Compose vs SwiftUI)
- ❌ DI frameworks (Hilt vs Factory)
- ❌ Navigation systems
- ❌ ViewModels
- ❌ Platform initialization
- ❌ Theme implementation (Material3 vs ThemeUI)


### String Resources
This project uses the moko-resources library to provide string resources to the android and ios apps.
Each exported module (usually feature modules, but perhaps some others) should define string resources
for all static strings displayed in the apps. This includes things like titles, labels, content descriptions,
etc. Strings should be defined in the feature module that they are related to.  
For example a string for the Add Tee Time screen title, should be defined in the teetimes feature module.
Generic Strings that are used across features (for example, text for common buttons such as "Save", or "OK")
should be defined in the strings resource file in the core:models module.

## Platform Parity Guidelines

**IMPORTANT**: While the implementations are platform-specific, the **architectural patterns** and
**concepts** must remain parallel between Android and iOS. This ensures consistency in:
- Developer experience across platforms
- Maintenance and updates
- Feature parity
- User experience consistency

### Navigation Parity

Both platforms implement a **custom multi-backstack Navigator** with identical capabilities:

**Pattern Requirements:**
- Per-tab navigation stacks (each tab maintains independent history)
- Type-safe navigation keys (protocol/interface-based)
- Separation of concerns (Navigator NOT passed to views, use callbacks instead)
- Extension methods for type-safe navigation (e.g., `navigateToLogin()`, `navigateToDetail()`)
- Same navigation methods: `navigate()`, `pop()`, `popUpTo()`, `clearbackstack()`

**When adding navigation features:**
1. Implement in both platforms using parallel patterns
2. Keep method names and behavior identical where possible
3. Follow the same destination enum/protocol structure
4. Maintain separation of concerns (views receive callbacks, not Navigator)

### Theme System Parity

Both platforms use **Material3 design tokens** with identical color roles and type scales:

**Pattern Requirements:**
- Same color roles (primary, onPrimary, surface, etc.)
- Same typography scale (displayLarge, headlineMedium, bodySmall, etc.)
- Same shape scale (extraSmall, small, medium, large, extraLarge), declared explicitly on both
  platforms with identical values — `TtcShapes` in `android/.../theme/Shape.kt` and `ttcShapes` in
  `ios/.../ui/theme/TeeTimeCaddieTheme.swift`
- Centralized theme definitions

**When updating themes:**
1. Changes must be applied to both platforms
2. Use identical naming conventions
3. Maintain the same semantic color meanings
4. Android: Update Material3 theme files
5. iOS: Update ThemeUI configuration

**Never hard-code a corner radius.** A component names a size on the shape scale, resolved through
`MaterialTheme.shapes.*` (Android) or `theme.shapes.*` (iOS), and each component's `Ttc*Defaults` /
`Ttc*Style` owns that mapping. The exceptions are shapes that are not radii at all: `CircleShape` /
`Circle()` for round-by-definition elements (avatars, dots), and the pill button, which is
`CircleShape` on Android and `.capsule` on iOS.

Note ThemeUI erases its shapes to `AnyShape`, which is not an `InsettableShape` — so `.strokeBorder`
is unavailable on a themed shape. Draw such a border with `.stroke()` at double the width and apply
`.clipShape()` *after* the overlay; that clips the outer half and is pixel-identical to
`strokeBorder` at the intended width.

### Initialization System Parity

Both platforms have **priority-based initializer systems** (though iOS currently unused):

**Pattern Requirements:**
- Priority-based execution (APP_LAUNCH, ON_CREATE, ON_START)
- Dependency resolution between initializers
- Lifecycle-aware initialization

**When adding initializers:**
1. Create parallel initializers on both platforms
2. Use same priority levels and dependencies
3. Keep initialization logic consistent
4. Android: Hilt-provided initializers
5. iOS: Factory-provided initializers (via AppInitModule)

### Dependency Injection Parity

While using different frameworks, the **patterns** must be identical:

**Pattern Requirements:**
- All KMP repositories wrapped in DI modules
- Singleton repositories provided from TeeTimeCaddieSdk
- Module-per-feature organization
- Same dependency graph structure

**When adding dependencies:**
1. Android: Create/update Hilt module
2. iOS: Create/update Factory module
3. Keep module names and structure parallel
4. Provide same dependencies with same lifecycles

### State Management Parity

Both platforms follow **MVVM with reactive state**:

**Pattern Requirements:**
- ViewModels delegate to KMP repositories
- Reactive state updates (StateFlow/Published properties)
- Same state properties and methods across platforms
- Identical business logic flow

**When adding features:**
1. Keep ViewModel APIs parallel
2. Use same state property names
3. Maintain identical user interaction flows
4. Business logic stays in KMP, UI orchestration in platform layer

# Development Guidelines



## Gradle Properties Notes

- JBR (JetBrains Runtime) from Android Studio required

## Verifying Assumptions Before Writing Workarounds

Before writing custom code to work around a perceived limitation (especially in KMP/Swift interop), verify the assumption first:

1. **Check what already exists**
    - For swift/kotlin interop issus: Look at the generated Swift code and headers to see what is available.

2. **Verify SKIE behavior** - SKIE exposes many Kotlin features to Swift that might not be obvious:
    - `Comparable` types expose `compareTo()` methods in Swift
    - Kotlin Flows become Swift AsyncSequences
    - Sealed classes get proper Swift enum-like handling
    - Check SKIE documentation (https://skie.touchlab.co) when unsure

3. **Test before implementing** - If you think a Kotlin method/property isn't available in Swift, try using it first before writing a workaround. The compilation error (or success) will confirm your assumption.

4. **Ask** - Before writing a helper/extension, ask: "Does something like this already exist?" Common operations (comparison, formatting, conversion) often have built-in solutions.

5. **Question platform-specific code** - If you find yourself writing iOS-only or Android-only code for something that seems like it should be shared, pause and investigate whether a shared solution already exists.
