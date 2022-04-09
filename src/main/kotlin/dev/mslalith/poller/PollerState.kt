package dev.mslalith.poller

/**
 * Used to hold the state of [Poller]
 */
sealed class PollerState<out T> {
    object Initial : PollerState<Nothing>()
    data class InProgress<S>(val result: S) : PollerState<S>()
    object Complete : PollerState<Nothing>()
    object Cancelled : PollerState<Nothing>()
}
