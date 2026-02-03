# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TeeTimeCaddie is a Kotlin Multiplatform mobile application (Android + iOS) for managing golf tee times (games). 
The application is primarily aimed at groups of golfers who play together, and need organization to keep
track of who can and cannot play in a game. All players in the group can see the tee times, opt in or out, 
get notifications of upcoming games, etc.

The project uses a modular architecture with shared business logic (80-90% code sharing) and 
platform-specific UI implementations.

**IMPORTANT:** EVERY time I ask you to implement a Jira Issue (Story, Defect, etc), you must
follow all steps and guidelines in the **Jira Issue Workflow** section below, as well as the
guidelines in the **Development Workflow** section.


## Project Structure

```
/businessLogic/          - KMP SDK entry point, exports TeeTimeCaddieKit framework for iOS
/core/                   - Shared core modules
  ├── network/           - Ktor networking abstractions
  ├── analytics/         - EventManager system (plugin-based analytics)
  ├── extensions/        - Kotlin utility extensions
  ├── storage/           - Data persistence (Firestore + local settings)
  └── models/            - Shared domain models
/features/               - Feature modules
  ├── auth/              - Authentication (Firebase Auth)
  └── teetimes/          - Tee time management (Firestore)
/android/app/            - Android app with Jetpack Compose UI
/ios/TeeTimeCaddie/      - iOS app with SwiftUI
```

---

# Build Commands

## Building the Project

```bash
./gradlew build                    # Build and test entire project
./gradlew assemble                 # Build without running tests
./gradlew clean                    # Clean all build artifacts
```

## Android App

```bash
./gradlew :android:app:assembleDebug      # Build debug APK
./gradlew :android:app:assembleRelease    # Build release APK
./gradlew :android:app:installDebug       # Build and install debug APK
```

## iOS Framework

Build the KMP framework that the iOS app depends on:

```bash
./gradlew linkDebugFrameworkIosX64                # Build iOS framework for x64 simulator
./gradlew linkDebugFrameworkIosArm64              # Build iOS framework for device
./gradlew linkDebugFrameworkIosSimulatorArm64     # Build iOS framework for ARM simulator
```

## iOS App (Command line build using xcodebuild)

** Build the app:**
```bash
# Build for simulator (ARM - Apple Silicon Macs)
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 15'

# Build for simulator (x64 - Intel Macs)
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  -arch x86_64

# Build for device
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphoneos
```
**Run on simulator:**
```bash
# List available simulators
xcrun simctl list devices available

# Boot a simulator (if not already running)
xcrun simctl boot "iPhone 15"

# Install and run the app
xcrun simctl install booted path/to/TeeTimeCaddie.app
xcrun simctl launch booted net.bradball.teetimecaddie
```

**Clean build:**
```bash
xcodebuild clean -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj -scheme TeeTimeCaddie
```

**Note:** The iOS app requires the KMP framework to be built first. Always run the appropriate `./gradlew linkDebugFramework...` command before building the iOS app.

## Testing

### Run All Tests
```bash
./gradlew test                     # Run all unit tests
./gradlew allTests                 # Run tests for all targets with aggregated report
./gradlew check                    # Run all checks (tests + lint)
```

### Platform-Specific Tests
```bash
./gradlew testDebugUnitTest              # Android unit tests (debug)
./gradlew iosSimulatorArm64Test          # iOS simulator tests (ARM)
./gradlew iosX64Test                     # iOS simulator tests (x64)
./gradlew connectedDebugAndroidTest      # Android instrumentation tests on connected device
```

### Module-Specific Tests
```bash
./gradlew :core:analytics:test           # Test specific module
./gradlew :features:auth:allTests        # Test specific feature module
```

### Linting
```bash
./gradlew lint                     # Run lint on default variant
./gradlew lintDebug                # Lint debug build
./gradlew lintFix                  # Run lint and apply safe fixes
```
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

## Key Technologies

- **Kotlin Multiplatform** - Cross-platform code sharing
- **Firebase Multiplatform SDK** (https://github.com/GitLiveApp/firebase-kotlin-sdk) - Multiplatform Firebase implementation (Firebase Auth, FireStore Storage, etc)
- **Kotlinx Coroutines** - Async operations
- **Kotlinx Serialization** - JSON serialization
- **Kotlinx DateTime** - Cross-platform dates
- **Kermit** - Multiplatform logging
- **Multiplatform Settings** - Key-value storage (SharedPreferences/NSUserDefaults)
- **CrashKiOS** - iOS crash reporting
- **Moko Resources** (https://github.com/icerockdev/moko-resources) - Shared resources (strings)
- **SKIE** (https://github.com/touchlab/SKIE) - Swift/Kotlin interop enhancements (https://skie.touchlab.co/intro)

## Structure

### `:businessLogic` Module
Found in the `businessLogic/` folder, this module provides the `TeeTimeCaddieSdk` class, which
operates as a singleton and serves as the entry point to the shared KMP business logic. It
provides instances of the classes that make up the public API of the shared KMP SDK. 


### Public (Exported) Modules
The publicly exported modules make up the public API of the Shared KMP SDK. They can be divided
into two categories.

#### Core Modules
These modules provide core and/or shared functionality and data structures that are used 
across multiple features. They include:

- **`:core:models`** - Found in the `core/models` folder this module contains all of the Data
models that are used by the applications. Prefer to put data models here instead of in feature
modules, as data models are often used across features. 

When you define a new model, also define a corresponding `preview*` object that applications
can use in tests and previews. If the model is often used in a list, also create a `preview*List`
object. 

For example:
```kotlin
data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val date: LocalDate,
    val time: LocalTime,
    val numberOfPlayers: Int
)

val previewTeeTime = TeeTime(
    id = "previewTime",
    createdBy = "Brad",
    course = "Persimmon Ridge",
    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    time = LocalTime(9, 0),
    numberOfPlayers = 4
)

val previewTeeTimeList = listOf(
    previewTeeTime.copy(time = LocalTime(10, 0)),
    previewTeeTime.copy(time = LocalTime(11, 0)),
    previewTeeTime.copy(time = LocalTime(12, 0)),
    previewTeeTime.copy(time = LocalTime(13, 0))
)
```

In addition, if a model contains properties whose types are other models, you can leverage existing
preview variables. For example:

```kotlin

data class SomeContainer(
    val id: String,
    val teeTime: TeeTime
)

val previewSomeContainer = SomeContainer(
    id = "previewcontainer",
    teeTime = previewTeeTime
)
```

- **:`core:extensions`** - Found in the `core/extensions` folder, this module provides convenience
utility extension methods and properties on common and primitive types used across both the shared KMP SDK as well 
as the apps. Check here when writing code for methods and properties that can be used to make code
shorter and easier to read and maintain.
- **:`core:analytics`** - Provides a single, shared abstraction layer for logging Analytics Events,
Performance Traces, and Errors to any provider. 

#### Feature Modules
These modules use a Repository Pattern to abstract data access and contain most business logic. 
Modules are defined for each major app feature/section. These modules are defined as sub-modules
under the `:features` modules, for example, an authentication module `:features:auth` 
(located at `features/auth`), or a module for tee time CRUD operations `:features:teetimes`
(located at `features/teetimes`). These modules consume the other public and private core modules
and use them to execute business logic and provide data to the applications.

Feature modules should also define extension methods for converting "private" data model instances
(such as `*Document` storage models and `*Network` response models) to instance of public 
data models that are defined in the `:core/models` module, as well as converting between private 
data model instances. These methods should follow a standardized pattern:

- `.toModel(): SomeDataModel` - `.toModel()` methods should be defined as extensions on storage
and network models to convert them to data model instances.
- `.toDocument(): SomeDocument` - `.toDocument()` methods should be defined as extensions on 
data models and network response models to convert them to documents for storage.

### Private Modules
The KMP SDK also includes a set of "private" modules, which are modules that are consumed by
other modules in the KMP SDK but who's classes, methods, and properties are NOT exposed to client
applications. They include:

#### Storage 
The `:core:storage` module (found in the `core/storage` folder) contains classes, objects,
and data structures for persisting application data, both locally and in the cloud. 

**Local Settings** - Within the `:core:storage` module, the **multiplatform-settings** library is used
to provide a common API abstraction for storing key value data such as user settings via
SharedPreferences on Android and NSUserDefaults on iOS.

**Firestore Storage** - Firebase Firestore is used to persist structured data in the cloud.
`Document` classes (for example `PlayerDocument` and `TeeTimeDocument`) model firestore documents
and `Storage` classes (for example `PlayerStorage` and `TeeTimeStorage`) provide an api for
CRUD operations on documents.

#### Network
The `:core:network` module (found in the `core/network` folder) contains classes, objects, and
data structures for making http network requests, using the `ktor` networking library.

## Dependency Injection

The modules that make up the shared KMP SDK use a "do-it-yourself" dependency injection system. 
Gradle modules contain a `*Module` class (for example, `StorageModule` in the
`:core:storage` module) that is responsible fore creating and providing instances of classes
defined in the module. The `TeeTimeCaddieSdk` creates and uses instances of the `*Module` classes
to obtain instances and expose them to the consuming android and ios applications.

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
- ❌ ViewModels/ObservableObjects
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
- Same shape definitions (small, medium, large)
- Centralized theme definitions

**When updating themes:**
1. Changes must be applied to both platforms
2. Use identical naming conventions
3. Maintain the same semantic color meanings
4. Android: Update Material3 theme files
5. iOS: Update ThemeUI configuration

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
- ViewModels/ObservableObjects delegate to KMP repositories
- Reactive state updates (StateFlow/Published properties)
- Same state properties and methods across platforms
- Identical business logic flow

**When adding features:**
1. Keep ViewModel/ObservableObject APIs parallel
2. Use same state property names
3. Maintain identical user interaction flows
4. Business logic stays in KMP, UI orchestration in platform layer

---


# Jira Issue Workflow
A Jira "Issue" is any Story, Defect, Epic (feature) defined in Jira.
"Acceptance Criteria" is all of the details and description in a Jira Issue.

Follow all of the Steps in the sections below for EVERY Jira Issue that you implement. 

## Before Writing Any Code:

1. Plan the work to be done:
   1. Read the full Issue, including Acceptance Criteria and Notes. 
   2. Make sure the Issue has proper sub-tasks:
      a. If the Issue already has sub-tasks, read them to understand what to do and how to complete the implementation.
         - If the sub-tasks are not sufficient to complete the story, follow the rest of these instructions to complete the task list.
     b. If the Issue does not have sub-tasks (or if the tasks aren't enough to fully implement the story):
        - Add appropriate sub-tasks to the Issue so that you or others can complete the Issue.
          - Keep sub-tasks fairly high level. Prefer defining 5-10 broad tasks to complete an Issue, instead of 20+ detailed tasks.
          - Tasks can have a list of steps in the task list if you want to provide detailed instructions for a task. However, this is not optional, not required.
     c. Ask me to check and verify the sub-tasks before continuing.
   3. Assign the Issue to yourself, and move it to the IN-PROGRESS step/column in Jira.
   4. Ensure that the necessary git branches are setup, according to the "Branch Strategy" and "Workflow for Jira Issue Development" guidelines in the "Working with Github" section of this document.
      a. If there are uncommited changes on the current branch, ask me what to do before continuing.

## Writing Code to Implement the Issue

1. Implement the Issue following all of the guidelines in this file, as well as context provided by other Claude.md files in this project.
   - Make sure all Acceptance Criteria of the Issue are met. 
   - Commit somewhat frequently to the Issue branch A decent guideline might be to commit the work for each sub-task in the Issue. 
2. Write Unit tests for non-UI code, including all shared KMP code, and View Models in platform code.
3. Write UI tests for all views and UI code in each platform. 
4. Build and test both platforms.
   - Note that is is not enough to just build the iOS framework with gradle. Use xcodebuild to build the ios app.
   - Run unit tests for both platforms and ensure all tests pass. 
   - Run UI tests for both platforms and ensure all tests pass. 

## Finishing the Implementation
1. Once all code is written to meet the Acceptance Criteria of the Issue, and tests are passing, make sure all code is commited to the Issue branch.
2. Push the Issue branch to the git origin repository.
3. Create a PR targeting the parent branch (which will usually be the epic/feature branch).
4. Include a link to the Jira Issue. 
5. In Jira, Transition the Issue to to the IN-REVIEW step/column.
   - Link the PR to the Issue.

## Important Notes
- Never commit directly to the `main` or Epic branches unless I explicitly tell you to do so. 
  - If I do tell you to do so, ask me one more time to confirm.
- Always target PR's back to the branch that the head branch was created from. For a story/defect, this is usually the Epic branch. 

# Working with GitHub

The project repository is at `https://github.com/Bradleycorn/TeeTimeCaddie.git` and the GitHub CLI (`gh`) is configured and authenticated.

## Branch Strategy

This project uses a **hierarchical branching strategy** aligned with Jira Epics and Stories, loosely based on Git Flow:

```
main
 └── epic/TTC-100-user-management (Epic branch)
      ├── story/TTC-101-login-screen (Story branch)
      ├── story/TTC-102-profile-screen (Story branch)
      └── story/TTC-103-settings-screen (Story branch)
```

**Branch Types:**

1. **Epic Branches** (correspond to Jira Epics):
    - Created from `main`
    - Naming: `epic/TTC-XXX-short-description`
    - Long-lived branches that accumulate story work
    - Merged back to `main` when the entire Epic is complete (usually manual PR)

2. **Story Branches** (correspond to Jira Stories and Defects):
    - Created from the Epic/feature branch
    - Naming: `story/TTC-XXX-short-description`
    - Short-lived branches for individual stories
    - Merged back to the Epic/feature branch via PR
    - Should be focused on a single story's scope

3. **Hotfix Branches** (for urgent fixes):
    - Created from `main`
    - Naming: `hotfix/TTC-XXX-short-description`
    - Merged directly back to `main` via PR

## Workflow for Jira Issue Development

When working on a Jira Story within an Epic:

1. **Before Starting - Check for Epic Branch:**
    - If there are uncommitted changes on the current branch, ask the user what to do with them before continuing.
    - Fetch latest branches: `git fetch origin`
    - Check if the parent Epic branch exists.
    - **If Epic branch does NOT exist:**
        - **ALWAYS ask the user before creating it**
        - Ask which branch should be the base (usually `main`, but may be different)
        - Example: "This story belongs to Epic TTC-100. Should I create epic/TTC-100-user-management from main?"
    - **If Epic branch exists:** Proceed with story branch creation from epic branch

2. **Story Branch Creation:**
    - Check if story branch exists.
    - **If Story Branch Exists**, switch to it.
    - **If Story Branch does NOT Exist**, Create story branch from Epic branch: `git checkout -b story/TTC-100-user-management`
    - Use descriptive branch names that include the Jira key

3. **Making Changes:**
    - Make code changes following the architecture patterns
    - Commit frequently with clear, descriptive messages

4. **Creating Story PR (Story → Epic branch):**
    - Push the story branch to GitHub: `git push -u origin story/TTC-123-login-screen`
    - Create a pull request targeting the **Epic branch** (not main!)
    - Use `gh pr create --base epic/TTC-100-user-management`
    - Include in the PR description:
        - Link to the Jira story
        - Summary of changes (what and why)
        - Test plan or testing notes
        - Any breaking changes or migration notes
        - Screenshots for UI changes

5. **After Story PR Approval:**
    - Merge the story branch into the Epic branch
    - Delete the story branch after merge
    - The Epic branch now contains your story's work

**Example Story PR Creation:**
```bash
gh pr create \
  --base epic/TTC-100-user-management \
  --title "TTC-123: Add login screen" \
  --body "$(cat <<'EOF'
## Summary
Implements login screen with email/password fields and validation.

## Changes
- Add LoginScreen composable (Android)
- Add LoginView SwiftUI view (iOS)
- Add form validation logic
- Wire up to AuthRepository

## Test Plan
- [x] Verify validation shows errors for invalid input
- [x] Test successful login flow
- [x] Test failed login handling
- [x] Test on both Android and iOS

## Related Story
[TTC-123: Add login screen](https://your-jira-instance.atlassian.net/browse/TTC-123)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```
## Handling PR Comments and Reviews

When asked to address PR feedback, follow this system:

**Default Behavior:**
- If there are no `@claude` mentions in any comments, then address ALL comments in "Request Changes" reviews by default
- If one or more comments mention `@claude` in "Request Changes" reviews, only address those comments.
- "Request Changes" reviews = changes that must be made
- "Comment" reviews = discussion/suggestions that may or may not need action

**Explicit Mentions:**
- `@claude address these items` - Handle these specific comments
- `@claude don't fix this` (or similar) - Skip this comment even if in "Request Changes"

**Inferring Comment Intent:**
- Can generally distinguish between definite changes vs. questions/suggestions based on comment content and tone
- **Definite changes**: Bug reports, architecture violations, missing error handling, null checks
- **Questions/Discussion**: "Why...", "Have you considered...", "What happens if..."
- **Suggestions**: "Could this be simplified...", "Might be clearer if...", "Nit: ..."
- For "Comment" reviews, use judgment about what clearly needs fixing vs. what's discussion
- **When uncertain, ask for clarification** - collaboration is key and git makes rollback easy

**Discussion Comments:**
- When a comment is marked "For discussion:", "Let's discuss:", "Question:", or uses similar exploratory language, do NOT immediately implement changes
- Instead, reply to the PR comment directly (using `gh api` or `gh pr comment`) with analysis and thoughts
- Wait for the user's response before making any code changes
- This creates a documented record of architectural decisions in the PR for future reference

**Process for Addressing PR Feedback:**
1. Fetch PR details: `gh pr view <pr-number>`
2. Read all "Request Changes" reviews and their comments
3. Address all comments UNLESS marked with `@claude don't fix` or similar
4. For "Comment" reviews, use judgment to determine what needs action
5. **Ask for clarification if unsure** - better to ask than guess wrong
6. After making changes, commit and push updates
7. Respond to PR comments indicating what was fixed

## Git Workflow Notes

- **Main branch:** `main` - always stable, production-ready code
- **Epic branches:** Long-lived feature branches aligned with Jira Epics
- **Story branches:** Short-lived branches for individual stories, merged to Epic branch
- **Hotfix branches:** Emergency fixes merged directly to `main`
- **Commit messages:** Use conventional commit format when possible (feat:, fix:, docs:, etc.)
- **Force push:** Avoid unless absolutely necessary and coordinate with team
- **Always verify target branch:** Story PRs target Epic branch, not `main`

# Development Guidelines

## Adding a New Feature

1. **Create KMP module** under `/features/`
2. **Add to `settings.gradle.kts`**
3. **Create Repository** in `commonMain`
   - Repository should be defined as an interface (`interface ExampleRepository`) , with a corresponding implementation class named with an "Impl" suffix (`class ExampleRespositoryImpl`).
   - The Implementation class should have an internal constructor (`class ExampleRepositoryImpl internal constructor(..): ExampleRepository`), so that it cannot be instantiated from other modules.
4. **Create a `Module` class** in `commonMain`
   - It should contain provider methods to obtain an instance of the Repositories defined in the module.
   - provider methods should return the interface type, and the method should create instances of the implementation class. 
     For example:
     ```kotlin
     class ExampleFeatureModule() {
        fun providesExampleRepository(): ExampleRepository {
           return ExampleRepositoryImpl()
        }
     }
     ```
4. **Add Storage classes and Storage Models** if needed in `core/storage`
5. **Define Data Models** in `core/models`
6. **Export module from businessLogic**, and expose repositories via `TeeTimeCaddieSdk` class.
7. **Create Hilt module** in Android app to provide repository
8. **Create Factory module** in iOS app to provide repository
9. **Create ViewModels** (Android) and ObservableObjects (iOS)
10. **Create Compose screens** (Android) and SwiftUI views (iOS)
11. **Add navigation destinations** and entry providers

## Working with Shared Code

**Repository Pattern:**
- Repositories in `features/*/src/commonMain`
- Constructor injection (dependencies provided by platform DI)
- Suspend functions for async operations
- Kotlin Flows for reactive data
- Integrate EventManager for analytics

**Storage Layer:**
- Firestore: `core/storage/.../PlayerStorage.kt`, `TeeTimeStorage.kt`
- Local: `StorageModule` (expect/actual for platform-specific)
- Documents (Firestore) vs Models (domain)
- Mapping functions: `fun Document.asModel(): Model`

**Testing:**
- Common tests in `commonTest`
- Platform-specific tests in `androidTest`/`iosTest`
- Use `./gradlew :module:allTests` for full test suite

## Gradle Properties Notes

- JBR (JetBrains Runtime) from Android Studio required
- Native caching disabled for iOS targets (required for CrashKiOS)
- Android sourceSet layout version 2
- Moko resources static framework warning disabled

## Common Issues

**iOS Build Issues:**
- Clean derived data: `rm -rf ~/Library/Developer/Xcode/DerivedData`
- Rebuild framework: `./gradlew clean linkDebugFrameworkIosSimulatorArm64`
- Check Xcode is using correct framework path

**Android Build Issues:**
- Invalidate caches and restart Android Studio
- `./gradlew clean build --refresh-dependencies`

**KMP Issues:**
- Check expect/actual implementations match
- Verify platform-specific dependencies in build.gradle.kts
- Ensure SKIE plugin is applied for iOS interop

## Verifying Assumptions Before Writing Workarounds

Before writing custom code to work around a perceived limitation (especially in KMP/Swift interop), verify the assumption first:

1. **Check what already exists** - Before writing a helper/extension, ask: "Does something like this already exist?" Common operations (comparison, formatting, conversion) often have built-in solutions.

2. **Verify SKIE behavior** - SKIE exposes many Kotlin features to Swift that might not be obvious:
   - `Comparable` types expose `compareTo()` methods in Swift
   - Kotlin Flows become Swift AsyncSequences
   - Sealed classes get proper Swift enum-like handling
   - Check SKIE documentation (https://skie.touchlab.co) when unsure

3. **Test before implementing** - If you think a Kotlin method/property isn't available in Swift, try using it first before writing a workaround. The compilation error (or success) will confirm your assumption.

4. **Question platform-specific code** - If you find yourself writing iOS-only or Android-only code for something that seems like it should be shared, pause and investigate whether a shared solution already exists.

---

# Key Architectural Concepts

## Expect/Actual Pattern

Used for platform-specific implementations in KMP:

```kotlin
// commonMain
expect class StorageModule {
    fun provideSettings(): TeeTimeCaddieSettings
}

// androidMain
actual class StorageModule(private val appContext: Context) {
    actual fun provideSettings(): TeeTimeCaddieSettings =
        TeeTimeCaddieSettings(appContext)
}

// iosMain
actual class StorageModule {
    actual fun provideSettings(): TeeTimeCaddieSettings =
        TeeTimeCaddieSettings()
}
```

Use this pattern sparingly - prefer shared code when possible.

## Repository Constructor Injection

Repositories use constructor injection to receive dependencies:

```kotlin
class AuthRepositoryImpl(
    private val eventManager: EventManager,
    private val appSettings: TeeTimeCaddieSettings,
    private val playerStorage: PlayerStorage
): AuthRepository
```