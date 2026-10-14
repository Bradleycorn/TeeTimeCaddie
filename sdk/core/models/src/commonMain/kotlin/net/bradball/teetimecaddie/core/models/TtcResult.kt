package net.bradball.teetimecaddie.core.models

import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * The outcome of an SDK operation that can fail.
 *
 * **Public SDK methods return this instead of throwing.** Kotlin exceptions are unchecked, and
 * Kotlin/Native only surfaces them to Swift when a method is annotated `@Throws` — where they
 * arrive as a lossy `NSError`, and anything *not* listed in the annotation terminates the app
 * rather than being catchable. Handing the failure back as a value sidesteps all of that: Swift
 * switches on it with SKIE's `onEnum(of:)`, exhaustively, with the error object intact.
 *
 * [Failure] carries a [TeeTimeCaddieException] rather than a feature-specific enum so that one
 * type covers every operation, including the ones whose failures span domains — completing a
 * sign-up can fail for an authentication reason *or* a profile reason. The interface already
 * exposes `title`, `displayMessage` and `recoverySuggestion`, which is everything a generic error
 * surface needs. A screen that must tell specific cases apart — "that email is already in use"
 * renders differently from a failed sign-in — casts once to the feature's exception type and
 * switches on its `error` enum.
 *
 * Note the exception is only ever *carried*, never thrown.
 *
 * ### Why `T : Any`
 *
 * The bound is load-bearing for Swift. Kotlin/Native exports an *unbounded* generic type parameter
 * as `_Nullable`, so `Success.data` would arrive optional even for values that can never be null,
 * forcing an unwrap at every call site. With a non-null bound it exports as plain `T`. (Verified
 * against the generated header, not assumed.)
 *
 * The cost is that an operation cannot express "succeeded, and there is nothing there" as a null
 * payload — which is exactly what [TtcLookup] is for. A method returning this type is promising a
 * value on success; one that may legitimately find nothing returns [TtcLookup] instead.
 */
sealed class TtcResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : TtcResult<T>()

    data class Failure(val error: TeeTimeCaddieException) : TtcResult<Nothing>()

    /** True when this is a [Success]. */
    val isSuccess: Boolean
        get() = this is Success

    /** The value on success, or null on failure. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** The error on failure, or null on success. */
    fun errorOrNull(): TeeTimeCaddieException? = (this as? Failure)?.error
}

/** Transform a successful value, passing any failure through untouched. */
inline fun <T : Any, R : Any> TtcResult<T>.map(transform: (T) -> R): TtcResult<R> = when (this) {
    is TtcResult.Success -> TtcResult.Success(transform(data))
    is TtcResult.Failure -> this
}