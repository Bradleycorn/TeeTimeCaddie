# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with android application
code in this repository.

## Project Overview

Native Jetpack Compose application using **Hilt** for DI, **custom multi-backstack Navigator**, and **MVVM** architecture. 
The app is a thin UI layer over the shared KMP SDK provided by the Kotlin Multiplatform parent project.

## Key Technologies

- **TeeTimeCaddieSdk** - KMP SDK that provides data and business logic.
- **Jetpack Compose** - Declarative UI
- **Material3** - Design system
- **Hilt** - Dependency injection
- **androidx.navigation3** - Navigation
- **Kotlin Coroutines** - Async operations
- **StateFlow** - Reactive state
- **ViewModel** - UI state management
- **ProcessLifecycleOwner** - App lifecycle
- **SavedState API** - State preservation
- **Accompanist** - System UI controller
- **Core Splashscreen** - Android 12+ splash screen API

## Module Structure

```
/app/src/main/java/net/bradball/teetimecaddie/android/
├── di/                          - Hilt modules
├── initializers/                - App initialization system
├── analytics/                   - Firebase analytics plugin
├── feature/                     - Features container. Each feature should get it's own folder.
│   ├── auth/                    - Auth screens, VMs, navigation, Hilt module
│   └── teeTimes/                - Tee times screens, VMs, navigation, Hilt module
├── ui/
│   ├── app/                     - Root app composable, app state
│   ├── common/                  - Shared UI components
│   └── navigation/              - Custom Navigator, TopLevelDestination
└── theme/                       - Material3 theme
```
Note: all future file references in this file leave off the root path to the application source code
(`/app/src/main/java/net/bradball/teetimecaddie/android/`), and start from there.

Within a feature, each "screen" should get it's own folder that contains:
- A `**Screen.kt` file that contains the high level composables that make up that screen.
  - The Screen file should be broken up into 2 composables:
    - The "*Screen" composable serves as a "wrapper" around the actual screen content. It is where
      any screen level logic/initialization is done. It obtains a ViewModel instance, does any
      initialization, etc. It should call the `Screen` composable (`ui/common/screen`)
      to log the screen view with the EventManager. 
    - the `*Content` composable is stateless and contains the screen content. It should NOT be passed
      the ViewModel instance, and instead it should be passed State that is exposed by the ViewModel.
  - This setup allows for easier creation of previews and testing of screens.   
- A `*ViewModel.kt` file that contains the ViewModel for the screen.
- A feature will likely also have a `navigation/` folder with a file that contains Navigation
  extensions and methods for that feature.
- A feature may or may not contain a `common/` folder with additional components and widgets 
  used throughout that feature.

## Architecture

The app uses an MVVM architecture and Unidirectional Data Flow (UDF) based on the standard android
application architecture recommended by Google. Views (Jetpack Compose) are "dumb" and merely
render state provided to them by ViewModels. ViewModels are thin orchestration layers, and do not
implement business logic themselves (unless it is  specific to the Android platform). Instead,
they use Repository classes provided by the TeeTimeCaddieSdk shared KMP SDK, letting those
repositories execute business logic and provide data. ViewModels then prepare that data as state
for the UI layer to render.

### State Management

Views are "dumb" and render state provided by ViewModels. However, **ephemeral UI-local state**
may remain in the View/Composable when:
- It's purely presentational with no business logic implications
- The ViewModel only needs the final value (e.g., form fields passed on submit)
- No validation or side effects depend on intermediate changes

Examples of acceptable View-level state: form field bindings, animation toggles, local focus state.

State should move to the ViewModel when business logic, validation, persistence, or
cross-view coordination depends on it. 


## Application Lifecycle

```
TeeTimeCaddieApplication (@HiltAndroidApp)
    ↓ (initializes TeeTimeCaddieSdk)
TeeTimeCaddieActivity (@AndroidEntryPoint)
    ↓ (Hilt provides dependencies)
TeeTimeCaddieActivityViewModel (@HiltViewModel)
    ↓ (exposes initialization state, repositories)
TeeTimeCaddieApp (Composable)
    ↓ (manages app-level state, navigation)
NavDisplay (androidx.navigation3)
    ↓ (renders screens based on Navigator backStack)
Feature Screens (Auth, TeeTimes)
```

## TeeTimeCaddieSdk 
This app uses the TeeTimeCaddieSdk which is a Kotlin Multiplatform library that contains the shared 
business logic and resources for this application, as well as a companion iOS application. Business
logic that is not specific to Android should go in the TeeTimeCaddieSdk, and not be built in this
application directly. 

### String Resources
The TeeTimeCaddieSdk also provides string resources to this application as well as the companion
iOS application. Static text strings for things like titles, labels, content descriptions etc
should use the appropriate string resource from the TeeTimeCaddieSdk and should not be hard coded in this
app. If there are static strings that are specific to Android, they should be defined as string resources
in the app's strings.xml file, and not hard coded in the app.

## Dependency Injection

The application uses Hilt for Dependency injection, leveraging typical Hilt Android components,
including `@HiltAndroidApp`, `@HiltViewModel`, `@AndroidEntryPoint`, etc. 

ViewModel instances are creating using the `hiltViewModel()` method to scope view models to
a navigation destination. 

- **AppModule** (`di/AppModule.kt`) - The AppModule provides global singleton instances used
across the application, such as instances of `EventManager`, `FirebaseCrashlytics`, etc.
- **InitializersModule** (`di/InitializersModule.kt`) - Provides the set of `AppInitializer`
instances that are used by the `AppInitializers` class to perform app initialization.
- **FeatureModules** - Feature Modules (for example, the `AuthModule` (`feature/auth/AuthModule.kt`))
provide instances of Repositories and other classes related to a specific high level feature.

## AppInitializers

AppInitializers is a sophisticated priority-based initializer pattern for startup tasks.
Find documentation in the KDoc comments in the files in the `initializers/` folder, as well
as the `di/InitializersModule.kt` file.

## Navigation

This app uses the compose Navigation3 library for navigation, and navigation is orchestrated
by the `Navigator` class (`ui/navigation/Navigator.kt`) and the `TtcNavDisplay` composable
(`ui/app/TeeTimeCaddieApp.kt`).

### Passing Navigation Parameters to ViewModels

When a screen needs navigation parameters (e.g., an item ID from a destination), use **Hilt Assisted
Injection** to inject those parameters into the ViewModel. This is necessary because Navigation3's
type-safe destinations don't automatically populate the `SavedStateHandle` like the older
navigation-compose library did.

**Step 1: Define an AssistedFactory in the ViewModel file**

```kotlin
@AssistedFactory
interface MyViewModelFactory {
    fun create(itemId: String): MyViewModel
}
```

**Step 2: Use @AssistedInject and @Assisted in the ViewModel**

```kotlin
@HiltViewModel(assistedFactory = MyViewModelFactory::class)
class MyViewModel @AssistedInject constructor(
    @Assisted private val itemId: String,
    private val someRepo: SomeRepository,
    // ... other Hilt-provided dependencies
): ViewModel() {
    // Use itemId as needed
}
```

**Step 3: Create the ViewModel in the navigation entry using the factory**

```kotlin
entry<MyDestination> { destination ->
    val viewModel = hiltViewModel<MyViewModel, MyViewModelFactory> { factory ->
        factory.create(destination.itemId)
    }
    MyScreen(
        viewModel = viewModel,
        onBack = { navigator.goBack() }
    )
}
```

**Step 4: Update the Screen composable to accept the ViewModel as a parameter**

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel,
    onBack: () -> Unit
) {
    // Screen implementation
}
```

**Key Points:**
- The ViewModel should NOT use `SavedStateHandle` to get navigation parameters with Navigation3
- Pass only primitive types or simple data (String, Int, etc.) to the factory, not the whole destination object
- The Screen composable should receive the ViewModel as a parameter (not create it internally)
- This keeps the ViewModel decoupled from navigation concerns while allowing runtime parameters

## Theme

The app uses Material3 design components, and the Material3 theming system. 
The theme is defined in the files in the `theme/` folder and the `MyApplicationTheme`
composable is called at the root of the Composable view tree, providing the theme
for the entire application. 

## Common UI Components

UI components that are generic and usable in all areas of the app can be found in the
`ui/common` folder. Check this folder (and it's sub-folders) for reusable components and
widgets when building new screens, views, and interface, and use them when appropriate. 

In addition, some features may include a `common/` folder that includes UI components
and widgets that are used across several screens within that feature. For example, 
the `feature/auth/common` folder contains several composables that are used on many
screens within the Auth feature.

### Icons
The `ui/common/icons/TtcIcons.kt` file contains an enum that defines all icons used in
the app. The app uses material icons, however, we do NOT want directly import and use
the Material Icons library, because it will automatically include ALL icons. Instead, 
we can add the specific Material icons we need as drawable resources, and then add a 
reference to them in the `TtcIcons` enum. Android Studio provides a Vector Asset tool
that we can use to automatically select a Material Icon and import it as a vector drawable. 
If you need a new icon and cannot do this yourself, ask me and I will add the drawable resource
and update the `TtcIcons` enum. 

To display an icon, the enum provides a `painter` property to expose the icon resource as Painter
object for use with Image/Icon composables. You can render a TtcIcon like this:

```kotlin
Icon(Icons.ADD.painter, contentDescription = "Add Tee Time")
```
# Development Instructions

Whenever you create a composable, create a corresponding preview to preview that composable. 
If the composable can be rendered in several different states, create previews for each of the
major states. However, you do not need to create previews for every minor state. For most views, 
you should not need more than 2 or 3 previews. When creating previews, you can use available 
`preview*` instances to pass preview model data into views that need it.

All "Screen" level composables in the app should use the `Screen` composable (`ui/common/Screen.kt`)
at the top level to log the screen view. Create an appropriate `AnalyticsScreen` entry in the 
shared KMP SDK as needed to log new screens. 