import Foundation

enum UiState<T: Equatable> {
    case Loading
    case Empty
    case Content(T)
}

extension UiState {
    var hasContent: Bool {
        switch self {
            case .Content(_):
                return true
            default:
                return false
        }
    }
    
    var isEmpty: Bool {
        return self == .Empty
    }
    
    var isLoading: Bool {
        return self == .Loading
    }
}

extension UiState: Equatable {
    static func == (lhs: UiState, rhs: UiState) -> Bool {
        switch (lhs, rhs) {
            case (.Loading, .Loading):
                return true
            case (.Empty, .Empty):
                return true
            case (.Content(let first), .Content(let second)):
                return first == second
            default:
                return false
        }
    }
}
