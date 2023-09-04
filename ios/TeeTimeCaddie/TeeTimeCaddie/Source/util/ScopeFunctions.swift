// Replicating https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/util/Standard.kt in Swift

import Foundation

@inline(__always) func run<R>(block: () -> R) -> R {
    return block()
}

protocol ScopeFunc {}
extension ScopeFunc {

    @inline(__always) func `let`<R>(block: (Self) -> R) -> R {
        return block(self)
    }

    @inline(__always) func also(block: (Self) -> ()) -> Self {
        block(self)
        return self
    }

    @inline(__always) func takeIf(predicate: (Self) -> Bool) -> Self? {
        if (predicate(self)) {
            return self
        } else {
            return nil
        }
    }

    @inline(__always) func takeUnless(predicate: (Self) -> Bool) -> Self? {
        if (!predicate(self)) {
            return self
        } else {
            return nil
        }
    }

    @inline(__always) func `repeat`(times: Int, action: (Int) -> ()) -> () {
        for index in (0...times-1) {
            action(index)
        }
    }
}

extension NSObject: ScopeFunc {}
