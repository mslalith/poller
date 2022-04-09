package dev.mslalith.poller

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
     * Tells whether the poll is running or not
     */
    fun isPolling(): Boolean

    /**
     * Determines whether the poll can continue or not
     */
    fun canPoll(): Boolean

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
 * Create a Poller which runs indefinitely until
 * - stopped explicitly
 * - [coroutineScope] gets cancelled
 *
 * @param coroutineScope The scope in which the poll should execute
 * @param pollInterval Time in millis for the poll to run after
 */
fun <T> Poller.Companion.indefinite(
    coroutineScope: CoroutineScope,
    pollInterval: Long
): Poller<T> = PollerImpl(
    coroutineScope = coroutineScope,
    pollInterval = pollInterval,
    pollRepeatCount = PollerImpl.NO_REPEAT,
    maxRetries = PollerImpl.NO_RETRIES
)


/**
 * A concrete implementation of [PollerImpl]. Handles the execution of a poll
 *
 * For example, let's take `pollInterval = 4_000` and `pollRepeatCount = 5`
 *
 * Then the total lifetime of this poll would be `20_000 (4_000 * 5)`
 *
 * Poll will be considered complete if either
 * - poll completes successfully
 * - exhausts number of retries before its lifetime
 * - exhausts its time
 *
 * @param coroutineScope The scope in which the poll should execute
 * @param pollInterval Time in millis for the poll to run after
 * @param pollRepeatCount Number of times the poll should happen
 * @param maxRetries Maximum number of times the poll can retry within the poll lifecycle
 */
fun <T> Poller.Companion.finite(
    coroutineScope: CoroutineScope,
    pollInterval: Long,
    pollRepeatCount: Int,
    maxRetries: Int = PollerImpl.NO_RETRIES
): Poller<T> = PollerImpl(
    coroutineScope = coroutineScope,
    pollInterval = pollInterval,
    pollRepeatCount = pollRepeatCount,
    maxRetries = maxRetries
)
