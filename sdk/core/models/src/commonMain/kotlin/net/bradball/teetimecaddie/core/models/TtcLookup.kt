package net.bradball.teetimecaddie.core.models

import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

/**
 * The outcome of a read that may legitimately find nothing.
 *
 * Same shape as [TtcResult] — succeeded or failed — differing only in that the payload is
 * optional. That is the whole distinction, and it is deliberate: there is one mental model for
 * every SDK outcome, and a method picks the variant that matches its contract.
 *
 * - [TtcResult] for operations that produce a value or fail. Its `T : Any` bound keeps the payload
 *   non-optional, so Swift never unwraps something that cannot be null.
 * - [TtcLookup] for reads where "nothing there" is a normal answer. `T` is unbounded, so the
 *   payload is optional — and here that optional is *meaningful* rather than ceremony.
 *
 * Seeing which type a method returns tells you, at the call site, whether absence is expected.
 *
 * Note that "nothing found" and "the read failed" stay distinct: `Success(null)` versus [Failure].
 * Collapsing them is how you end up deleting an account because the network blipped.
 *
 * **When absence is genuinely an error** — fetching a game by an id you were handed, say — the
 * method should return [TtcResult] instead and map the absence to a domain error itself. That is a
 * semantic call the repository can make once, rather than every caller re-deciding.
 */
sealed class TtcLookup<out T> {

    data class Success<out T>(val data: T?) : TtcLookup<T>()

    data class Failure(val error: TeeTimeCaddieException) : TtcLookup<Nothing>()

    /** The value if the read succeeded and found something, null otherwise. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** The error if the read failed, null otherwise. */
    fun errorOrNull(): TeeTimeCaddieException? = (this as? Failure)?.error
}

/** Transform a found value. Absence and failure both pass through untouched. */
inline fun <T, R> TtcLookup<T>.map(transform: (T) -> R): TtcLookup<R> = when (this) {
    is TtcLookup.Success -> TtcLookup.Success(data?.let(transform))
    is TtcLookup.Failure -> this
}
