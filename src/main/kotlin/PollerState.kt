/**
 * Used to hold the state of [IPoller]
 */
sealed class PollerState<out T> {
    object Initial : PollerState<Nothing>()
    data class InProgress<S>(val result: S) : PollerState<S>()
    object Complete : PollerState<Nothing>()
    object Cancelled : PollerState<Nothing>()
}
