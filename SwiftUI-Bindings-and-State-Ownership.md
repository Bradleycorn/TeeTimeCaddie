# SwiftUI Bindings, State Ownership, and Encapsulation

## The Problem This Document Addresses

SwiftUI's `@Bindable` property wrapper makes it very easy to pass a writable binding from a
ViewModel directly into a view. Apple's own documentation demonstrates this pattern, and it has
become common practice in the SwiftUI community. But this convenience comes with a real cost to
encapsulation that is worth understanding before adopting it.

---

## Background: What is a Binding?

A `Binding<T>` in SwiftUI is a two-way reference to a value owned elsewhere. It lets a child view
read *and write* a value without owning it. SwiftUI's built-in controls — `TextField`, `DatePicker`,
`Toggle` — all require a `Binding` because they need to both display and modify a value.

The canonical example is `@State`:

```swift
struct MyView: View {
    @State private var name: String = ""

    var body: some View {
        TextField("Name", text: $name) // $name is a Binding<String>
    }
}
```

Here, `MyView` owns the state. `$name` is a binding to that state. `TextField` reads and writes
through it. This is clean because the owner and the writer are the same entity.

---

## The Common Practice: Public `var` + `@Bindable`

When a ViewModel enters the picture, Apple's guidance suggests making ViewModel properties plain
`var`s and using the `@Bindable` property wrapper to derive bindings from them:

```swift
@Observable
class ProfileViewModel {
    var name: String = ""
    var email: String = ""
}

struct ProfileScreen: View {
    @State private var viewModel = ProfileViewModel()

    var body: some View {
        ProfileContent(viewModel: viewModel)
    }
}

struct ProfileContent: View {
    @Bindable var viewModel: ProfileViewModel

    var body: some View {
        TextField("Name", text: $viewModel.name)   // directly binds to viewModel.name
        TextField("Email", text: $viewModel.email) // directly binds to viewModel.email
    }
}
```

This is concise, reads naturally, and requires minimal boilerplate. It's why the community has
widely adopted it.

---

## The Encapsulation Problem

The issue is that making `name` and `email` plain `var`s means **any code anywhere can write to
them directly**:

```swift
// In a view:
viewModel.name = "hacked"

// In another service:
someService.viewModel.name = "also hacked"

// In a test:
viewModel.name = ""  // bypass any logic that should run on change
```

The ViewModel is supposed to be the owner and guardian of its own state. It should be the single
entity that decides when and how its properties change. A public `var` removes that guarantee
entirely — it relies on convention ("nobody should write to this directly") rather than enforcement
("the compiler won't let you write to this directly").

This matters because:

1. **Side effects can be bypassed.** If updating `name` should also trigger validation, log an
   analytics event, or update derived state, a direct assignment skips all of that.

2. **The source of truth becomes ambiguous.** If multiple callers can write to the ViewModel
   directly, tracing where a state change originated becomes harder.

3. **It doesn't scale.** A simple string assignment today may grow into something with real logic
   tomorrow. If the property is already public, adding that logic to a method doesn't prevent
   callers from continuing to bypass it via direct assignment.

4. **It violates the principle of least privilege.** Views are supposed to be dumb renderers. They
   shouldn't have unrestricted write access to the ViewModel's internals.

---

## A Better Approach: `private(set)` + Explicit Mutation Methods

The solution is to make ViewModel properties readable but not directly writable from outside the
class, and expose explicit methods for mutation:

```swift
@Observable
class ProfileViewModel {
    private(set) var name: String = ""
    private(set) var email: String = ""

    func updateName(_ newName: String) {
        name = newName
        // room for validation, analytics, derived state, etc.
    }

    func updateEmail(_ newEmail: String) {
        email = newEmail
    }
}
```

Now nothing outside `ProfileViewModel` can write to `name` directly. All mutations go through
`updateName()`, which is the single authorized path.

---

## Bridging to SwiftUI: The `Binding(get:set:)` Pattern

The tradeoff is that `@Bindable` no longer works for deriving bindings, because `name` is
`private(set)` — SwiftUI can't generate a writable binding to a read-only property.

The solution is to construct the `Binding` explicitly in the Screen, which acts as the authorized
intermediary between the ViewModel and the Content view:

```swift
struct ProfileScreen: View {
    @State private var viewModel = ProfileViewModel()

    var body: some View {
        ProfileContent(
            name: Binding(
                get: { viewModel.name },
                set: { viewModel.updateName($0) }
            ),
            email: Binding(
                get: { viewModel.email },
                set: { viewModel.updateEmail($0) }
            )
        )
    }
}

struct ProfileContent: View {
    @Binding var name: String
    @Binding var email: String

    var body: some View {
        TextField("Name", text: $name)
        TextField("Email", text: $email)
    }
}
```

`ProfileContent` is now fully stateless. It holds bindings to values it doesn't own, and those
bindings route mutations back through the ViewModel's explicit methods. The compiler still prevents
anything from writing to `viewModel.name` directly.

---

## Comparing the Two Approaches

| | Public `var` + `@Bindable` | `private(set)` + `Binding(get:set:)` |
|---|---|---|
| Boilerplate | Minimal | Slightly more |
| Encapsulation | Broken — anyone can write | Enforced — compiler prevents direct writes |
| Mutation control | Convention only | Guaranteed by language |
| Side effects on change | Easy to bypass | Always run through the method |
| Scalability | Fragile as logic grows | Stable — add logic to the method freely |
| SwiftUI idiom | Common practice | Less common, but fully supported |

---

## Addressing the "It Works in Practice" Argument

A common response to these concerns is: "Views are the only things that hold ViewModels, and views
don't abuse direct access. So it works fine in practice."

This is true — until it isn't. It works as long as:
- The codebase stays small enough that everyone knows the conventions
- Nobody adds a service, coordinator, or test that holds a ViewModel reference
- The properties never grow to need logic on mutation

"Works because everyone follows the convention" is a weaker guarantee than "the compiler prevents
it." The extra few lines of code to use `private(set)` and `Binding(get:set:)` buys a guarantee
that no amount of convention can provide.

---

## Summary

SwiftUI's `@Bindable` pattern is convenient and Apple-endorsed, but it sacrifices encapsulation
by requiring public mutable properties on the ViewModel. The `private(set)` + `Binding(get:set:)`
approach requires slightly more code but enforces state ownership at the language level — the
ViewModel remains the single authorized writer of its own state, and all mutations are explicit
and traceable.

The content view remains stateless in both approaches. The difference is whether the mutation path
is enforced by the compiler or merely by convention.