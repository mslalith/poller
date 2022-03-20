import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * An abstraction for Polling.
 */
interface IPoller<T> {
    /**
     * Holds the current [PollerState] of the Poll
     */
    val pollerStateFlow: StateFlow<PollerState<T>>

    /**
     * Starts the poll
     * @param pollBlock will execute on every poll
     * @return a [Job] for the running poll
     */
    fun poll(pollBlock: suspend () -> T): Job

    /**
     * Determines whether the poll can continue or not
     */
    fun canPoll(): Boolean

    /**
     * Tells whether the poll is running or not
     */
    fun isPolling(): Boolean

    /**
     * Stops the poll
     * @see IPoller.stopIfPolling
     */
    fun stop()
}

/**
 * Will stop only if the poll is running
 */
fun <T> IPoller<T>.stopIfPolling() {
    if (isPolling()) stop()
}
