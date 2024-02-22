import Foundation
import Factory
import TeeTimeCaddieKit

final class TeeTimesModule: SharedContainer {
    static let shared = TeeTimesModule()
    var manager = ContainerManager()
        
    var teeTimesRepository: Factory<TeeTimesRepository> {
        self { TeeTimeCaddieSdk.companion.getInstance().provideTeeTimesRepository() }
            .singleton
    }
     
}
