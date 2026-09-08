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
/sdk/                    - KMP SDK entry point, exports TeeTimeCaddieKit framework for iOS
  ├── core/              - Shared core modules
  │   ├── network/       - Ktor networking abstractions
  │   ├── analytics/     - EventManager system (plugin-based analytics)
  │   ├── extensions/    - Kotlin utility extensions
  │   ├── storage/       - Data persistence (Firestore + local settings)
  │   └── models/        - Shared domain models
  └── features/          - Feature modules
      ├── auth/          - Authentication (Firebase Auth)
      └── teetimes/      - Tee time management (Firestore)
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

**Note:** The `-destination` parameter can use any available iPhone simulator with iOS 17.0+.
Use `xcrun simctl list devices available` to see available simulators and choose one that exists on your system.

** Build the app:**
```bash
# Build for simulator (ARM - Apple Silicon Macs)
# Replace the destination with any available iPhone simulator (iOS 17.0+)
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16'

# Build for simulator (x64 - Intel Macs)
xcodebuild -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
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

# Boot a simulator (if not already running) - use any available iPhone simulator
xcrun simctl boot "iPhone 16"

# Install and run the app
xcrun simctl install booted path/to/TeeTimeCaddie.app
xcrun simctl launch booted net.bradball.teetimecaddie
```

**Clean build:**
```bash
xcodebuild clean -project ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj -scheme TeeTimeCaddie
```

**Note:** The iOS app build process automatically builds the KMP framework as a build phase, so you do NOT need to run `./gradlew linkDebugFramework...` separately before building the app.

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
./gradlew :sdk:core:analytics:test       # Test specific module
./gradlew :sdk:features:auth:allTests    # Test specific feature module
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
- **NSExceptionKt** - iOS crash reporting
- **Moko Resources** (https://github.com/icerockdev/moko-resources) - Shared resources (strings)
- **SKIE** (https://github.com/touchlab/SKIE) - Swift/Kotlin interop enhancements (https://skie.touchlab.co/intro)

## Structure

### `:sdk` Module
Found in the `sdk/` folder, this module provides the `TeeTimeCaddieSdk` class, which
operates as a singleton and serves as the entry point to the shared KMP business logic. It
provides instances of the classes that make up the public API of the shared KMP SDK.


### Public (Exported) Modules
The publicly exported modules make up the public API of the Shared KMP SDK. They can be divided
into two categories.

#### Core Modules
These modules provide core and/or shared functionality and data structures that are used
across multiple features. They include:

- **`:sdk:core:models`** - Found in the `sdk/core/models` folder this module contains all of the Data
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

- **:`sdk:core:extensions`** - Found in the `sdk/core/extensions` folder, this module provides convenience
  utility extension methods and properties on common and primitive types used across both the shared KMP SDK as well
  as the apps. Check here when writing code for methods and properties that can be used to make code
  shorter and easier to read and maintain.
- **:`sdk:core:analytics`** - Provides a single, shared abstraction layer for logging Analytics Events,
  Performance Traces, and Errors to any provider.

#### Feature Modules
These modules use a Repository Pattern to abstract data access and contain most business logic.
Modules are defined for each major app feature/section. These modules are defined as sub-modules
under the `:sdk:features` modules, for example, an authentication module `:sdk:features:auth`
(located at `sdk/features/auth`), or a module for tee time CRUD operations `:sdk:features:teetimes`
(located at `sdk/features/teetimes`). These modules consume the other public and private core modules
and use them to execute business logic and provide data to the applications.

Feature modules should also define extension methods for converting "private" data model instances
(such as `*Document` storage models and `*Network` response models) to instance of public
data models that are defined in the `:sdk:core:models` module, as well as converting between private
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
The `:sdk:core:storage` module (found in the `sdk/core/storage` folder) contains classes, objects,
and data structures for persisting application data, both locally and in the cloud.

**Local Settings** - Within the `:sdk:core:storage` module, the **multiplatform-settings** library is used
to provide a common API abstraction for storing key value data such as user settings via
SharedPreferences on Android and NSUserDefaults on iOS.

**Firestore Storage** - Firebase Firestore is used to persist structured data in the cloud.
`Document` classes (for example `PlayerDocument` and `TeeTimeDocument`) model firestore documents
and `Storage` classes (for example `PlayerStorage` and `TeeTimeStorage`) provide an api for
CRUD operations on documents.

#### Network
The `:sdk:core:network` module (found in the `sdk/core/network` folder) contains classes, objects, and
data structures for making http network requests, using the `ktor` networking library.

## Dependency Injection

The modules that make up the shared KMP SDK use a "do-it-yourself" dependency injection system.
Gradle modules contain a `*Module` class (for example, `StorageModule` in the
`:sdk:core:storage` module) that is responsible fore creating and providing instances of classes
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



## Working with Shared Code

**Repository Pattern:**
- Repositories in `sdk/features/*/src/commonMain`
- Constructor injection (dependencies provided by platform DI)
- Suspend functions for async operations
- Kotlin Flows for reactive data
- Integrate EventManager for analytics

**Storage Layer:**
- Firestore: `sdk/core/storage/.../PlayerStorage.kt`, `TeeTimeStorage.kt`
- Local: `StorageModule` (expect/actual for platform-specific)
- Documents (Firestore) vs Models (domain)
- Mapping functions: `fun Document.asModel(): Model`

**Testing:**
- Common tests in `commonTest`
- Platform-specific tests in `androidTest`/`iosTest`
- Use `./gradlew :module:allTests` for full test suite

## Gradle Properties Notes

- JBR (JetBrains Runtime) from Android Studio required
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