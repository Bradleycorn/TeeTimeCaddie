# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with iOS application
code in this repository.

## Project Overview

Native SwiftUI application using **Factory** for DI, **custom multi-backstack Navigator**, and **MVVM** architecture.
The app is a thin UI layer over the shared KMP SDK provided by the Kotlin Multiplatform parent project.

## Key Technologies

- **TeeTimeCaddieKit** - KMP framework
- **SwiftUI** - Declarative UI
- **Factory** - Dependency injection
- **NavigationStack** - SwiftUI navigation
- **Combine** - Reactive programming (via @Published)
- **Swift Concurrency** - async/await
- **SKIE** - Swift/Kotlin interop
- **ThemeUI** - Material3-inspired theming
- **SwiftAppInitializers** - App initialization
- **Firebase iOS SDK** - Firebase services

## Module Structure

```
/TeeTimeCaddie/
├── TeeTimeCaddie/              - Main iOS app
│   ├── Source/
│   │   ├── TeeTimeCaddieApp.swift        - App entry point
│   │   ├── TeeTimeCaddieAppState.swift   - Root app state
│   │   ├── inject/                       - Factory DI modules
│   │   ├── features/                     - Feature modules
│   │   │   ├── auth/                     - Auth screens & ViewModels
│   │   │   └── teetimes/                 - Tee times screens, ViewModels, navigation
│   │   ├── ui/                           - UI components
│   │   │   ├── common/                   - Shared UI components
│   │   │   ├── theme/                    - App theme
│   │   │   └── navigation/               - Custom Navigator, AppTabs, TtcNavKey
│   │   ├── util/                         - Utilities (UiState, extensions)
│   │   └── analytics/                    - Analytics plugins
├── TeeTimeCaddieTests/         - Unit tests
└── TeeTimeCaddieUITests/       - UI tests
```

Note: all future file references in this file leave off the root path to the application source code
(`/TeeTimeCaddie/TeeTimeCaddie/Source`), and start from there.

Within a feature, each "screen" should get it's own folder that contains:
- A `**Screen.swift` file that contains the high level views that make up that screen.
    - The Screen file should be broken up into 2 views:
        - The "*Screen" view serves as a "wrapper" around the actual screen content. It is where
          any screen level logic/initialization is done. It obtains a View Model instance, does any
          initialization, etc. It should call the `Screen` view (`ui/common/screen`)
          to log the screen view with the EventManager.
        - The `*Content` view is stateless and contains the screen content. It should NOT be passed
          the View Model instance, and instead it should be passed State that is exposed by the View Model.
    - This setup allows for easier creation of previews and testing of screens.
- A `*ViewModel.kt` file that contains the View Model for the screen.
- A feature will likely also have a `navigation/` folder with a file that contains Navigation
  extensions and methods for that feature.
- A feature may or may not contain a `common/` folder with additional components and widgets
  used throughout that feature.

## Architecture

The app uses an MVVM architecture inspired by the standard android application architecture
used in Android apps. The app also aspires to use Unidirectional Data Flow (UDF) principles when
possibile. However, SwiftUI in some places uses BiDirectional Data Flow (for example, `@Binding`s
are two-way data flows). While we want to use Unidirectional Data Flows where it makes sense, we
don't want to "fight the system" and force it. Use standard SwiftUI mechanisms when it makes sense
to do so. Views are "dumb" and merely render state provided to them by ViewModels.
ViewModels are thin orchestration layers, and do not implement business logic themselves
(unless it is  specific to the iOS platform). Instead, they use Repository classes provided by
the TeeTimeCaddieKit shared KMP SDK, letting those repositories execute business logic and provide
data. ViewModels then prepare that data as state for the UI layer to render.

### State Management

Views are "dumb" and render state provided by ViewModels. However, **ephemeral UI-local state**
may remain in the View when:
- It's purely presentational with no business logic implications
- The ViewModel only needs the final value (e.g., form fields passed on submit)
- No validation or side effects depend on intermediate changes

Examples of acceptable View-level state: form field bindings, animation toggles, local focus state.

State should move to the ViewModel when business logic, validation, persistence, or
cross-view coordination depends on it.

### Swift/Kotlin Interop

The TeeTimeCaddieKit framework is built from the parent project's shared Kotlin Multiplatform code.
The framework leverages the SKIE library (https://github.com/touchlab/SKIE) to facilitate interop 
between Kotlin and Swift code. While all SKIE features are supported (Flow support, sealed classes, 
etc), in particular it should be noted that Kotlin Flows are exposed as Swift AsyncStreams. 

SKIE documentation can be found at: https://skie.touchlab.co/intro

## Application Lifecycle

```
TeeTimeCaddieApp (@main)
    ↓ (AppDelegate via @UIApplicationDelegateAdaptor)
TeeTimeCaddieAppState (@StateObject)
    ↓ (observes auth state from KMP)
UserInterface (View)
    ↓ (switches between app states)
TeeTimeCaddieTheme
    ↓
Feature Screens (Auth, TeeTimes)
```


## TeeTimeCaddieKit
This app uses the TeeTimeCaddieKit which is a Kotlin Multiplatform library that contains the shared
business logic and resources for this application, as well as a companion Android application. Business
logic that is not specific to iOS should go in the TeeTimeCaddieKit, and not be built in this
application directly.

### String Resources
The TeeTimeCaddieKit also provides string resources to this application as well as the companion
Android application. Static text strings for things like titles, labels, content descriptions etc
should use the appropriate string resource from the TeeTimeCaddieKit and should not be hard coded in this
app.

## Dependency Injection

The application uses the Factory library (https://github.com/hmlongco/Factory) for 
Dependency Injection. 

- **AppModule** (`inject/AppModule.kt`) - The AppModule provides global singleton instances used
  across the application, such as instances of `EventManager`, `FirebaseCrashlytics`, etc.
- **InitializersModule** (`di/AppInitModule.kt`) - Provides the set of `AppInitializer`
  instances that are used by the `InitManager` class to perform app initialization.
- **FeatureModules** - Feature Modules (for example, the `AuthModule` (`features/auth/AuthModule.kt`))
  provide instances of Repositories and other classes related to a specific high level feature.

## SwiftAppInitializers

AppInitializers is a sophisticated priority-based initializer pattern for startup tasks.
Code (with documentation in source comments) is available at https://github.com/Bradleycorn/SwiftAppInitializers/

## Navigation

While the app uses standard SwiftUI navigation components (NavigationStack, TabView, etc),
Navigation state is managed by a sophisticated navigation system orchestrated by the
`Navigator` class (`ui/navigation/Navigator.swift`).

Navigation-related view modifiers (`.navigationTitle()`, `.navigationBarTitleDisplayMode()`,
`.toolbar()`, etc.) should be applied in the navigation layer (in `destinationView()` methods within
`*Destinations` enums), not inside the Screen views themselves. This keeps navigation concerns
separate from view content and allows the navigation system to manage all navigation-related UI.

When navigating and passing parameters, avoid passing entire objects if possible.
Instead, pass a primitive identifier, and let the new destination's view model load the object using the identifier.
For example, when navigating from an item list screen to an item detail screen, don't pass
the `Item` object as a parameter. Instead pass the item id. When the details screen loads, its
view model can use the item id to load the item from it's usual storage location (fetch it from the network, )
load it from a database, etc).

## Theme

The app uses the ThemeUI (https://github.com/Bradleycorn/ThemeUI) framework to provide
semantically named colors and shapes to the application. Colors are defined in code
(see the `ui/theme/Color.swift` file) and NOT in the asset catalog. 
The `TeeTimeCaddieTheme` view (`ui/themeTeeTimeCaddieTheme.kt`) defines an `AppTheme` and 
exposes it as an Environment Variable. `TeeTimeCaddieTheme` is the root view for the application,
allowing all views within the app to obtain the `AppTheme` environment variable to set 
colors and shapes. 

Note: While the `ThemeUI` framework provides typography theming, it is not used in this application.
Instead it uses standard, built in SwiftUI typography elements.

## UI Components
As a general rule, create a file for each view, named the same as the view struct.

If there are multiple views that are always used together, they can be put in the same file,
with the "child" views using the `fileprivate` keyword to indicate that they should only
be called from the "parent" or other vies in this file. In this case, the file should be named the same as the parent.

For example:
```swift
struct SomeView: View {
    let items: [Item]

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                ForEach(items, id: \.id) { item in
                    ItemView(item: item)
                }
            }
        }
    }
}

fileprivate struct ItemView: View {
    let item: Item
    
    var body: some View {
        Text(item.name)
    }
}
```

Whenever you create a view file, create a corresponding preview to preview that view.
Previews should come at the end of the file.
When creating previews, you can use available `preview*` instances to pass preview model data into views that need it.
For files with a main "public" view and one or more fileprivate "child" views, you do not
need to create previews for the child views. They will be handled by previewing the public
"parent" view.

### Screens

All "Screen" level views in the app should use the `Screen` view (`ui/common/Screen.swift`)
at the top level to log the screen view. Create an appropriate `AnalyticsScreen` entry in the
shared KMP SDK as needed to log new screens.

### Common/Shared Components

UI components that are generic and usable in all areas of the app can be found in the
`ui/common` folder. Check this folder (and it's sub-folders) for reusable components and
widgets when building new screens, views, and interface, and use them when appropriate.

In addition, some features may include a `common/` folder that includes UI components
and widgets that are used across several screens within that feature. For example,
the `feature/auth/common` folder contains several views that are used on many
screens within the Auth feature.

## Utilities

The `util` folder contains several files that define extension and other utility methods.
When doing development, check for components defined in these files that can be used.