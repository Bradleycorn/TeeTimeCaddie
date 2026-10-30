import Foundation
import Factory
import TeeTimeCaddieKit

/// The Swift twin of `GamesModule.kt`.
final class GamesModule: SharedContainer {
    static let shared = GamesModule()
    var manager = ContainerManager()

    var gamesRepository: Factory<GamesRepository> {
        self { AppModule.shared.teeTimeCaddieSdk().gamesRepository }
            .singleton
    }

    /// Builds a `ContactsRepository` over a reader: in practice, a ``PickedContactsReader`` for
    /// each set of numbers the system picker returns. The twin of Android's
    /// `ContactsRepositoryFactory`, and a seam so ViewModel tests can build one over a fake.
    var contactsRepositoryFactory: Factory<(any DeviceContactsReader) -> any ContactsRepository> {
        self {
            let sdk = AppModule.shared.teeTimeCaddieSdk()
            return { sdk.contactsRepository(reader: $0) }
        }
    }

    /// The signed-in player, read when asked.
    ///
    /// A seam rather than a convenience: `SessionManager`'s constructor is internal to the SDK, so
    /// a ViewModel that takes it cannot be unit tested. One that takes this closure can be handed
    /// `{ testPlayer }`. The twin of the `StateFlow<SessionState>` Android's ViewModels take.
    var currentPlayer: Factory<() -> Player?> {
        self {
            let sessionManager = AuthModule.shared.sessionManager()
            return { (sessionManager.sessionState.value as? SessionState.SignedIn)?.player }
        }
    }
}
