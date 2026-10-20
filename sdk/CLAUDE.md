# CLAUDE.md

Guidance for the KMP SDK (`sdk/`). Project-wide rules, including platform parity and string
resources, live in the root `CLAUDE.md`.

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
