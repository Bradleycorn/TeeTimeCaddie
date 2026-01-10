# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TeeTimeCaddie is a Kotlin Multiplatform mobile application (Android + iOS) for managing golf tee times (games). 
The application is primarily aimed at groups of golfers who play together, and need organization to keep
track of who can and cannot play in a game. All players in the group can see the tee times, opt in or out, 
get notifications of upcoming games, etc.

The project uses a modular architecture with shared business logic (80-90% code sharing) and platform-specific UI implementations.

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

## Android-Specific

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

## iOS App

### Using Xcode

Open the project in Xcode:
```bash
open ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj
```

Then use Xcode's build and run commands (⌘R to build and run).

### Using xcodebuild (Command Line)

**Build the iOS app:**
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

All business logic is shared between Android and iOS through Kotlin Multiplatform. The shared code provides repositories, data management, analytics, and domain models.

## TeeTimeCaddieSdk - Entry Point

The SDK is a **singleton** that provides access to all shared functionality:

```kotlin
class TeeTimeCaddieSdk private constructor(
    useLocalResources: Boolean,  // Use Firebase emulators
    private val storageModule: StorageModule
) {
    val eventManager: EventManager
    fun provideAuthRepository(): AuthRepository
    fun provideTeeTimesRepository(): TeeTimesRepository
}
```

**Initialization:**
- Android: `TeeTimeCaddieSdk.initialize(context, BuildConfig.DEBUG)`
- iOS: `TeeTimeCaddieSdk.companion.getInstance().initialize(useLocalResources: false)`

**Platform-Specific Files:**
- `businessLogic/src/androidMain/kotlin/.../TeeTimeCaddieSdk.kt` - Android extension
- `businessLogic/src/iosMain/kotlin/.../TeeTimeCaddieSdk.kt` - iOS extension (includes CrashKiOS setup)

## Repository Pattern

### AuthRepository

Location: `features/auth/src/commonMain/kotlin/.../AuthRepository.kt`

```kotlin
class AuthRepository(
    private val eventManager: EventManager,
    private val appSettings: TeeTimeCaddieSettings,
    private val playerStorage: PlayerStorage
)
```

**Key APIs:**
- `val currentUser: User` - Current logged-in user
- `val isLoggedIn: Boolean` - Auth status
- `val loginState: Flow<Boolean>` - Reactive auth state changes
- `val hasLoggedInOnce: Boolean` - Has user ever logged in
- `suspend fun login(email: String, password: String)` - Login user
- `suspend fun registerUser(email: String, password: String, name: String)` - Register new user
- `suspend fun refreshAuthentication()` - Refresh auth token

### TeeTimesRepository

Location: `features/teetimes/src/commonMain/kotlin/.../TeeTimesRepository.kt`

```kotlin
class TeeTimesRepository(
    private val eventManager: EventManager,
    private val teeTimeStorage: TeeTimeStorage
)
```

**Key APIs:**
- `suspend fun createTeeTime(createdBy: String, course: String, dateTime: LocalDate): TeeTime`
- `fun getTeeTimes(player: String): Flow<List<TeeTime>>` - Reactive tee times list

## Storage Layer

### Firestore Storage

**PlayerStorage** (`core/storage/src/commonMain/kotlin/.../PlayerStorage.kt`):
- `suspend fun addPlayer(id: String, document: PlayerDocument)` - Add player to Firestore

**TeeTimeStorage** (`core/storage/src/commonMain/kotlin/.../TeeTimeStorage.kt`):
- `suspend fun addTeeTime(document: TeeTimeDocument): String` - Add tee time, returns ID
- `suspend fun getTeeTimes(playerId: String): List<TeeTimeDocument>` - Get all tee times
- `fun teeTimesFlow(playerId: String): Flow<List<TeeTimeDocument>>` - Reactive tee times

### Local Settings Storage

**StorageModule** - Platform-specific (expect/actual pattern):
- Common: `core/storage/src/commonMain/kotlin/.../StorageModule.kt` (expect)
- Android: `core/storage/src/androidMain/kotlin/.../StorageModule.kt` (requires Context)
- iOS: `core/storage/src/iosMain/kotlin/.../StorageModule.kt` (no parameters)

Uses **multiplatform-settings** library for SharedPreferences/NSUserDefaults abstraction.

## EventManager - Analytics System

Location: `core/analytics/src/commonMain/kotlin/.../EventManager.kt`

**Plugin-based architecture** for analytics and logging:

```kotlin
class EventManager(
    private val errorLogger: ErrorLogger = FirebaseErrorLogger(),
    private val transactionLogger: TransactionLogger = FirebaseTransactionLogger()
)
```

**Key APIs:**
- `fun logEvent(event: AnalyticsEvent)` - Log analytics event
- `fun logScreenView(screen: AnalyticsScreen, type: ScreenType)` - Log screen view
- `fun logException(throwable: Throwable, errorType: LoggableExceptionTypes, data: HashMap<String, Any?>?)` - Log errors
- `fun startTransaction(name: String)` / `fun stopTransaction(name: String)` - Track performance
- `fun setUserId(userId: String)` - Associate events with user
- `fun registerPlugin(eventPlugin: EventPlugin)` - Add analytics provider

**Platform Integration:**
- Android: `FirebaseEventPlugin` registered in `AppModule` (Hilt)
- iOS: `FirebaseEventPlugin` registered in `AppModule` (Factory)

## Data Layer Architecture

**Documents vs Models:**
- **Documents** - Firestore entities with `@Serializable`, `@Transient var id: String?`
- **Models** - Domain objects consumed by UI
- **Mapping** - `fun Document.asModel(): Model` extension functions

**Example:**
```kotlin
// Document (persistence)
@Serializable
data class TeeTimeDocument(
    val createdBy: String,
    val course: String,
    val dateTime: LocalDate
) {
    @Transient var id: String? = null
}

// Model (domain)
data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val dateTime: LocalDate
)

// Mapping
fun TeeTimeDocument.asModel(): TeeTime = TeeTime(...)
```

## Firebase Integration

Uses **gitlive/firebase-kotlin-sdk** for multiplatform Firebase:

**Services:**
- `Firebase.auth` - Authentication
- `Firebase.firestore` - NoSQL database

**Emulator Support:**
```kotlin
// Configured in TeeTimeCaddieSdk.init() when useLocalResources = true
Firebase.auth.useEmulator(host, port)
Firebase.firestore.useEmulator(host, port)
```

Emulator configuration in `FirebaseConfig.kt` (platform-specific).

## Resource Management

**Moko Resources** provides shared strings across platforms:

```kotlin
// Generated per-feature module (e.g., features/auth)
object AR {  // Auth Resources
    object strings {
        val reg_error_default_title: StringResource
    }
}

// Usage in KMP
AR.strings.reg_error_default_title

// Usage in Swift (via SKIE)
AR.shared.strings.reg_error_default_title
```

## Key Technologies

- **Kotlin Multiplatform** - Cross-platform code sharing
- **Firebase Multiplatform SDK** (gitlive) - Auth, Firestore
- **Kotlinx Coroutines** - Async operations
- **Kotlinx Serialization** - JSON serialization
- **Kotlinx DateTime** - Cross-platform dates
- **Kermit** - Multiplatform logging
- **Multiplatform Settings** - Key-value storage (SharedPreferences/NSUserDefaults)
- **CrashKiOS** - iOS crash reporting
- **Moko Resources** - Shared resources (strings)
- **SKIE** - Swift/Kotlin interop enhancements

---

# Android App Architecture

## Overview

Native Jetpack Compose application using **Hilt** for DI, **custom multi-backstack Navigator**, and **MVVM** architecture. The app is a thin UI layer over the shared KMP business logic.

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

## Dependency Injection - Hilt

**Application:**
```kotlin
@HiltAndroidApp
class TeeTimeCaddieApplication: Application() {
    override fun onCreate() {
        TeeTimeCaddieSdk.initialize(this, BuildConfig.DEBUG)
    }
}
```

**Hilt Modules:**

**AppModule** (`di/AppModule.kt`):
```kotlin
@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    @Provides
    fun provideEventManager(): EventManager =
        TeeTimeCaddieSdk.getInstance().eventManager.apply {
            registerPlugin(FirebaseEventPlugin())
        }
}
```

**AuthModule** (`feature/auth/AuthModule.kt`):
```kotlin
@InstallIn(SingletonComponent::class)
@Module
class AuthModule {
    @Provides @Singleton
    fun provideAuthRepository(): AuthRepository =
        TeeTimeCaddieSdk.getInstance().provideAuthRepository()
}
```

**Pattern:** All KMP repositories wrapped in Hilt modules for injection.

## MVVM Architecture

**ViewModels:**
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository  // From KMP
): ViewModel() {
    var errorMessage: Int? by mutableStateOf(null)
    var showLoadingProgress: Boolean by mutableStateOf(false)
    var loginSuccess: Boolean by mutableStateOf(false)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Delegate to KMP repository
        }
    }
}
```

**Views:**
```kotlin
@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val viewModel = hiltViewModel<LoginViewModel>()
    // Observe state and render UI
}
```

**Pattern:** ViewModels are thin orchestration layers that delegate to KMP repositories.

## App Initialization System

**Sophisticated priority-based initializer pattern** for startup tasks.

**AppInitializer Interface:**
```kotlin
interface AppInitializer {
    val priority: InitializerPriority  // APP_LAUNCH, ON_CREATE, ON_START
    val dependencies: List<KClass<out AppInitializer>>
    suspend fun init(application: Application)
}
```

**AppInitializers Manager** (`initializers/AppInitializers.kt`):
- Coordinates all initializers based on priority and dependencies
- Three lifecycle-based phases:
  - `APP_LAUNCH` - During `ProcessLifecycleOwner` init
  - `ON_CREATE` - During `ProcessLifecycleOwner.onCreate`
  - `ON_START` - During `ProcessLifecycleOwner.onStart`
- Automatic dependency resolution
- Circular dependency detection
- Exposes `StateFlow<InitializationState>` for UI

**Splash Screen Integration:**
```kotlin
val showSplashScreen: Flow<Boolean> =
    appInitializers.state.map { it == InitializationState.Pending }
```

**Creating Initializers:**
1. Implement `AppInitializer` interface
2. Provide via Hilt in `InitializersModule`
3. Set priority and dependencies
4. AppInitializers automatically runs them

## Navigation System - Custom Multi-Backstack Navigator

Uses **androidx.navigation3** (new Navigation library) with custom `Navigator` class.

**Key Components:**

**Navigator Class** (`ui/navigation/Navigator.kt`):
- Manages separate back stacks for each top-level destination
- State preservation using custom `KSerializer` with SavedState API
- Type-safe navigation with `@Serializable` destinations

**TopLevelDestination Enum:**
```kotlin
enum class TopLevelDestination(
    val icon: Icons,
    val iconTextId: Int,
    val destination: TtcNavKey
): TtcNavKey {
    TEE_TIMES(...)
}
```

**Navigation Destinations:**
```kotlin
@Serializable
data object LoginDestination: TtcNavKey

@Serializable
data object RegistrationDestination: TtcNavKey
```

**Navigation Extensions:**
```kotlin
fun Navigator.navigateToLogin() {
    navigate(LoginDestination, clearBackStack = true)
}
```

**Entry Providers** (define screen → composable mappings):
```kotlin
fun EntryProviderScope<NavKey>.authEntries(
    onLoginClick: () -> Unit,
    onLoggedIn: () -> Unit
) {
    entry<LoginDestination> {
        LoginScreen(onLoggedIn = onLoggedIn)
    }
}
```

**Usage:**
```kotlin
val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)

NavDisplay(
    backStack = navigator.backStack,
    onBack = { navigator.goBack() },
    entryProvider = entryProvider {
        teeTimesEntries()
        authEntries(...)
    }
)
```

## State Management

**App-Level State** (`TeeTimeCaddieAppState`):
```kotlin
class TeeTimeCaddieAppState(
    coroutineScope: CoroutineScope,
    appInitializers: AppInitializers,
    authRepository: AuthRepository
) {
    val hasLoggedInOnce: Boolean
    val isLoggedIn: StateFlow<Boolean>
    val appInitStatus: StateFlow<InitializationState>
}
```

**Activity-Level State** (`TeeTimeCaddieActivityViewModel`):
```kotlin
@HiltViewModel
class TeeTimeCaddieActivityViewModel @Inject constructor(
    val appInitializers: AppInitializers,
    val authRepo: AuthRepository,
    val eventManager: EventManager
): ViewModel() {
    val showSplashScreen: Flow<Boolean>
    val showApp: StateFlow<Boolean>
}
```

## Module Structure

```
/android/app/src/main/java/net/bradball/teetimecaddie/android/
├── di/                          - Hilt modules
├── initializers/                - App initialization system
├── analytics/                   - Firebase analytics plugin
├── feature/                     - Feature modules
│   ├── auth/                    - Auth screens, VMs, navigation, Hilt module
│   └── teeTimes/                - Tee times screens, VMs, navigation, Hilt module
├── ui/
│   ├── app/                     - Root app composable, app state
│   ├── common/                  - Shared UI components
│   └── navigation/              - Custom Navigator, TopLevelDestination
└── theme/                       - Material3 theme
```

## Key Technologies

- **Jetpack Compose** - Declarative UI
- **Material3** - Design system
- **Hilt** - Dependency injection
- **androidx.navigation3** - Navigation (new library)
- **Kotlin Coroutines** - Async operations
- **StateFlow** - Reactive state
- **ViewModel** - UI state management
- **ProcessLifecycleOwner** - App lifecycle
- **SavedState API** - State preservation
- **Accompanist** - System UI controller
- **Core Splashscreen** - Android 12+ splash screen API

## Firebase Configuration

**Debug builds:**
- Analytics, Crashlytics, Performance disabled via `manifestPlaceholders` in `build.gradle.kts`

**Release builds:**
- All Firebase services enabled

---

# iOS App Architecture

## Overview

Native SwiftUI application using **Factory** for DI, **SwiftUI NavigationStack**, and **MVVM** architecture. Like Android, it's a thin UI layer over shared KMP logic.

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

## Dependency Injection - Factory

Uses **Factory** library (https://github.com/hmlongco/Factory) for service location.

**Module Pattern:**
```swift
final class AuthModule: SharedContainer {
    static let shared = AuthModule()
    var manager = ContainerManager()

    var authRepository: Factory<AuthRepository> {
        self { TeeTimeCaddieSdk.companion.getInstance().provideAuthRepository() }
            .singleton
    }
}
```

**Modules:**
- `AppModule` - EventManager, Firebase services
- `AuthModule` - AuthRepository from KMP
- `TeeTimesModule` - TeeTimesRepository from KMP
- `AppInitModule` - App initializers (currently unused)

**Usage:**
```swift
let authRepo = AuthModule.shared.authRepository()
```

## MVVM Architecture

**ViewModels:**
```swift
@MainActor
class LoginViewModel: ObservableObject {
    private let authRepo: AuthRepository  // From KMP

    @Published private(set) var processingLogin: Bool = false
    @Published var loginError: TeeTimeCaddieError? = nil

    func loginUser(email: String, password: String) {
        Task {
            try await authRepo.login(email: email, password: password)
        }
    }
}
```

**Views:**
```swift
struct LoginScreen: View {
    @StateObject private var viewModel = LoginViewModel(
        authRepo: AuthModule.shared.authRepository(),
        eventManager: AppModule.shared.eventManager()
    )

    var body: some View {
        // UI
    }
}
```

## State Management

**App-Level State** (`TeeTimeCaddieAppState`):
```swift
enum AppUiState {
    case REGISTRATION
    case LOGIN
    case APP
}

@MainActor
class TeeTimeCaddieAppState: ObservableObject {
    @Published private(set) var uiState: AppUiState
    private let authRepo: AuthRepository

    func observeAuthState() async {
        for await isLoggedIn in authRepo.loginState {
            uiState = AppUiState.fromLoginState(isLoggedIn.boolValue, hasLoggedInOnce: authRepo.hasLoggedInOnce)
        }
    }
}
```

**Navigation:**
```swift
switch(state) {
    case .APP:
        TeeTimesNavStack()
    case .LOGIN:
        LoginScreen(onRegisterClick: onRegisterClick)
    case .REGISTRATION:
        RegistrationScreen(onLoginClick: onLoginClick)
}
```

**Pattern:** Simple state-based navigation for auth flow, then delegates to custom Navigator for in-app navigation.

## Navigation System - Custom Multi-Backstack Navigator

Uses **SwiftUI's NavigationStack** with a custom `Navigator` class for per-tab navigation management.

**Key Components:**

**Navigator Class** (`ui/navigation/Navigator.swift`):
- Manages separate back stacks for each top-level tab
- Type-safe navigation with `TtcNavKey` protocol
- `@Observable` for SwiftUI reactivity
- Methods: `navigate(to:)`, `pop()`, `popUpTo(to:inclusive:)`, `clearbackstack()`

**TtcNavKey Protocol** (`ui/navigation/TtcNavKey.swift`):
```swift
protocol TtcNavKey: Hashable, Equatable, Identifiable {
    associatedtype Screen: View

    @MainActor
    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> Screen
}
```

**Important:** The Navigator enforces separation of concerns - it should **NOT** be passed into views or added to the environment. Views receive navigation callbacks instead:

```swift
// ✅ Correct: Views receive navigation callbacks
HomeScreen(
    onLoginClicked: { navigator.navigateToLogin() },
    onDetailClicked: { item in navigator.navigateToDetail(item) }
)

// ❌ Incorrect: Don't pass Navigator to views
HomeScreen(navigator: navigator)
```

**AppTabs Enum** (`ui/navigation/AppTabs.swift`):
```swift
enum AppTabs: TtcNavKey {
    case teeTimes

    var icon: ImageResource { ... }
    var iconText: String { ... }

    func destinationView(_ navigator: Navigator) -> some View {
        // Returns tab's root view
    }
}
```

**Feature Destinations** (`features/teetimes/TeetimesNavigation.swift`):
```swift
enum TeeTimesDestinations: TtcNavKey {
    case teeTimesList

    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> some View {
        switch self {
        case .teeTimesList:
            TeeTimesListScreen()
        }
    }
}

// Extension methods for type-safe navigation
extension Navigator {
    func navigateToTeeTimesTab(clearBackStack: Bool = false) {
        navigate(to: AppTabs.teeTimes, clearBackStack: clearBackStack)
    }

    func navigateToTeeTimes() {
        navigate(to: TeeTimesDestinations.teeTimesList)
    }
}
```

**AnyTtcNavKey** - Type-erased wrapper that enables storing different destination types in the same collection (navigation stack).

**View Components:**
- **AppTabView** - Root TabView using Navigator's `currentTab` binding
- **TabNavStack** - Wraps each tab in a NavigationStack with destination routing

**Usage:**
```swift
@State private var navigator = Navigator()

AppTabView(navigator)
```

## App Initialization - SwiftAppInitializers

**Package:** https://github.com/Bradleycorn/SwiftAppInitializers

**Setup in AppInitModule:**
```swift
import AppInitializers

final class AppInitModule: SharedContainer {
    static let shared = AppInitModule()

    private var allInitializers: Array<AppInitializer> {
        return []  // Currently empty
    }

    var initManager: Factory<InitManager> {
        self { InitManager(self.allInitializers) }
            .singleton
    }
}
```

**Note:** Infrastructure is in place but not currently used. Android has active initializers (Firebase), iOS handles initialization in AppDelegate.

## Theme System - ThemeUI

**Package:** https://github.com/bradleycorn/ThemeUI

**Material3-inspired theming for SwiftUI:**

```swift
public class AppTheme: ObservableObject {
    public let colorScheme: Colors
    public let typography: Typography
    public let shapes: Shapes
}
```

**Colors** - Material3 color roles:
- `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`
- `secondary`, `tertiary` (with variants)
- `surface`, `background`, `error` (with variants)
- `outline`, `scrim`, `inversePrimary`

**Typography** - Material3 type scale:
- `displayLarge/Medium/Small`
- `headlineLarge/Medium/Small`
- `titleLarge/Medium/Small`
- `bodyLarge/Medium/Small`
- `labelLarge/Medium/Small`

**Shapes:**
- `small`, `medium`, `large` (rounded rectangles with different radii)

**Usage:**
```swift
ThemedView(colors: colors, typography: Typography(), shapes: Shapes()) {
    // Your content
}
.environmentObject(theme)

// Access in views
@EnvironmentObject var theme: AppTheme
theme.colorScheme.primary
```

Provides design consistency with Android Material3.

## KMP Integration

**SDK Access:**
```swift
TeeTimeCaddieSdk.companion.getInstance()
```

**Framework Import:**
```swift
import TeeTimeCaddieKit  // Static framework from businessLogic module
```

**Swift/Kotlin Interop:**
- SKIE plugin enhances interop (better Flow support, sealed classes)
- Kotlin Flows accessible as Swift AsyncSequence

**Example:**
```swift
// Kotlin: val loginState: Flow<Boolean>
// Swift:
for await isLoggedIn in authRepo.loginState {
    // Handle state change
}
```

## Module Structure

```
/ios/TeeTimeCaddie/
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
├── ThemeUI/                    - Custom theming package (local)
├── TeeTimeCaddieTests/         - Unit tests
└── TeeTimeCaddieUITests/       - UI tests
```

## Key Technologies

- **SwiftUI** - Declarative UI
- **Factory** - Dependency injection
- **NavigationStack** - SwiftUI navigation
- **Combine** - Reactive programming (via @Published)
- **Swift Concurrency** - async/await
- **TeeTimeCaddieKit** - KMP framework
- **SKIE** - Swift/Kotlin interop
- **ThemeUI** - Material3-inspired theming
- **SwiftAppInitializers** - App initialization (available but unused)
- **Firebase iOS SDK** - Firebase services

## Swift Package Dependencies

From `Package.resolved`:
- **Factory** (2.2.0) - DI
- **Firebase iOS SDK** (10.29.0) - Firebase services
- **SwiftAppInitializers** (1.0.0) - App initialization framework
- **ThemeUI** (1.0.0) - Material3-inspired theming

---

# Architecture Comparison

## Android vs iOS

| Aspect | Android | iOS |
|--------|---------|-----|
| **DI** | Hilt (compile-time, comprehensive) | Factory (runtime, service locator) |
| **Navigation** | Custom multi-backstack Navigator with androidx.navigation3 | Custom multi-backstack Navigator with NavigationStack |
| **State** | StateFlow + Compose State | @Published properties + @Observable |
| **Initialization** | Complex 3-phase system with dependencies | Infrastructure present, currently unused |
| **ViewModels** | AAC ViewModel with Hilt injection | ObservableObject with Factory |
| **Theming** | Material3 (built-in) | ThemeUI (custom, Material3-inspired) |
| **Lifecycle** | ProcessLifecycleOwner observers | Scene phase monitoring |
| **Async** | Kotlin Coroutines | Swift async/await |

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

## Platform Parity Guidelines

**IMPORTANT**: While the implementations are platform-specific, the **architectural patterns** and **concepts** must remain parallel between Android and iOS. This ensures consistency in:
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

# Development Guidelines

## Adding a New Feature

1. **Create KMP module** under `/features/`
2. **Add to `settings.gradle.kts`**
3. **Create Repository** in `commonMain`
4. **Add Storage classes** if needed in `core/storage`
5. **Define Models** in `core/models` or feature module
6. **Export from businessLogic** if needed for iOS framework
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

## Working with Jira Issues

When implementing features or fixes from Jira:

1. **Planning Phase:**
   - If asked to plan work for a Jira issue, add appropriate subtasks or checklist items to the issue
   - Break down the work into actionable steps that can be tracked
   - This allows either you or others to pick up the issue and understand the implementation plan

2. **Before Starting Work:**
   - Assign the Jira issue to yourself
   - Transition the issue to "In Progress" status on the Kanban board
   - This ensures team visibility and prevents duplicate work

3. **During Development:**
   - Reference the issue key in commit messages (e.g., "TTC-123: Add user profile feature")
   - Keep the issue updated with progress notes if needed
   - Update subtasks/checklist items as work progresses

4. **After Completion:**
   - Transition the issue appropriately (e.g., to "Done" or "Ready for Review")
   - Link pull requests to the Jira issue
   - Add relevant comments about implementation decisions

**Best Practices:**
- Only assign issues you're actively working on
- Keep issue status current to reflect actual work state
- Use Jira comments for technical notes that help reviewers or future maintainers
- When planning, create clear, actionable subtasks that provide a roadmap for implementation

## Working with GitHub

The project repository is at `https://github.com/Bradleycorn/TeeTimeCaddie.git` and the GitHub CLI (`gh`) is configured and authenticated.

### Branch Strategy

This project uses a **hierarchical branching strategy** aligned with Jira Epics and Stories:

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
   - Naming: `epic/TTC-XXX-short-description` or `feature/TTC-XXX-short-description`
   - Long-lived branches that accumulate story work
   - Merged back to `main` when the entire Epic is complete (usually manual PR)

2. **Story Branches** (correspond to Jira Stories):
   - Created from the Epic/feature branch
   - Naming: `story/TTC-XXX-short-description`
   - Short-lived branches for individual stories
   - Merged back to the Epic/feature branch via PR
   - Should be focused on a single story's scope

3. **Hotfix Branches** (for urgent fixes):
   - Created from `main`
   - Naming: `hotfix/TTC-XXX-short-description`
   - Merged directly back to `main` via PR

### Workflow for Story Development

When working on a Jira Story within an Epic:

1. **Branch Creation:**
   - Ensure the Epic branch exists: `git fetch origin`
   - Create story branch from Epic branch: `git checkout -b story/TTC-123-login-screen epic/TTC-100-user-management`
   - Use descriptive branch names that include the Jira key

2. **Making Changes:**
   - Make code changes following the architecture patterns
   - Commit frequently with clear, descriptive messages
   - Reference Jira story key in commit messages (e.g., "TTC-123: Add login screen layout")

3. **Creating Story PR (Story → Epic branch):**
   - Push the story branch to GitHub: `git push -u origin story/TTC-123-login-screen`
   - Create a pull request targeting the **Epic branch** (not main!)
   - Use `gh pr create --base epic/TTC-100-user-management`
   - Include in the PR description:
     - Link to the Jira story
     - Summary of changes (what and why)
     - Test plan or testing notes
     - Any breaking changes or migration notes
     - Screenshots for UI changes

4. **After Story PR Approval:**
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

### Workflow for Epic Completion

When an Epic is complete (usually done manually by team lead):

1. All story branches have been merged into the Epic branch
2. Epic branch is tested as a complete feature set
3. Create PR from Epic branch → `main` (usually manual process)
4. After Epic PR is merged, delete the Epic branch

### Git Workflow Notes

- **Main branch:** `main` - always stable, production-ready code
- **Epic branches:** Long-lived feature branches aligned with Jira Epics
- **Story branches:** Short-lived branches for individual stories, merged to Epic branch
- **Hotfix branches:** Emergency fixes merged directly to `main`
- **Commit messages:** Use conventional commit format when possible (feat:, fix:, docs:, etc.)
- **Force push:** Avoid unless absolutely necessary and coordinate with team
- **Always verify target branch:** Story PRs target Epic branch, not `main`

## Firebase Emulator Setup

**Configuration:**
- Update `FirebaseConfig.kt` in platform-specific source sets
- Set `useLocalResources = true` in SDK initialization

**Running Emulators:**
```bash
firebase emulators:start
```

**Ports:**
- Auth: Usually 9099
- Firestore: Usually 8080

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

---

# Key Architectural Concepts

## EventManager Plugin System

The EventManager uses a **plugin architecture** for extensible analytics:

1. Create an `EventPlugin` implementation
2. Register with `eventManager.registerPlugin(plugin)`
3. Plugin receives all events via `logEvent()` and decides which to track
4. Multiple plugins can track the same event

**Example:**
```kotlin
class FirebaseEventPlugin : EventPlugin {
    override fun logEvent(event: AnalyticsEvent): Boolean {
        return when (event) {
            is AnalyticsEvent.Login -> {
                Firebase.analytics.logEvent("login", ...)
                true
            }
            else -> false
        }
    }
}
```

## Navigator State Preservation (Android)

The custom Navigator uses `KSerializer` with SavedState API to preserve state across process death:

- Each `TopLevelDestination` maintains its own back stack
- Navigator state serialized using kotlinx.serialization
- Restored automatically via `rememberSaveable`
- Custom serializers handle navigation keys

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
class AuthRepository(
    private val eventManager: EventManager,
    private val appSettings: TeeTimeCaddieSettings,
    private val playerStorage: PlayerStorage
)
```

Platform DI (Hilt/Factory) provides these dependencies when obtaining repositories from the SDK.
