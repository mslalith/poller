package dev.mslalith.poller

import dev.mslalith.poller.strategy.PollStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * An abstraction for Polling.
 */
interface Poller<T> {

    companion object

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
     * @see Poller.stopIfPolling
     */
    fun stop()
}

/**
 * Will stop only if the poll is running
 */
fun <T> Poller<T>.stopIfPolling() {
    if (isPolling()) stop()
}

/**
 * Create a new Poller instance
 *
 * @param coroutineScope The scope in which the poll should execute
 * @param pollInterval Time in millis for the poll to run after
 * @param pollStrategy The strategy to continue the poll
 */
fun <T> Poller.Companion.new(
    coroutineScope: CoroutineScope,
    pollInterval: Long,
    pollStrategy: PollStrategy
): Poller<T> = PollerImpl(
    coroutineScope = coroutineScope,
    pollInterval = pollInterval,
    pollStrategy = pollStrategy
)
