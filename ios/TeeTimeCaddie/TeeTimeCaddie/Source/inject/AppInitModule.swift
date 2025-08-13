
import Foundation
import Factory
import AppInitializers

/// A  [``Factory``](https://github.com/hmlongco/Factory)  Container that provides
/// dependencies for doing App Initialization.
///
/// This module (like all Factory Containers) is a singleton, accessed via it's (static) ``shared`` property.
final class AppInitModule: SharedContainer {
    /// The singlton instance of this module.
    static let shared = AppInitModule()

    var manager = ContainerManager()

    /// A list of all intializers that are defined. When adding new initializers, they should be included in the array that this
    /// property returns.
    private var allInitializers: Array<AppInitializer> {
        return []
    }
    
    /// The InitManager that contains all defined App Initializers, and manages their execution at the appropriate time.
    var initManager: Factory<InitManager> {
        self {
            return InitManager(self.allInitializers)
        }.singleton
    }
    
//    private var networkAuthInitializer: Factory<NetworkAuthInitializer> {
//        self {
//            NetworkAuthInitializer()
//        }
//    }
}
