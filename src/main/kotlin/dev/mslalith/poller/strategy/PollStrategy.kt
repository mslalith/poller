package dev.mslalith.poller.strategy

/**
 * An abstraction for PollStrategy. Supported strategies are:
 * - [IndefiniteStrategy] - runs indefinitely
 * - [RetryLimitStrategy] - stop when retires are exhausted
 * - [TimeoutStrategy] - stop when time is exhausted
 */
interface PollStrategy {
    /**
     * Determines whether poll can continue or not
     */
    fun canPoll(pollInterval: Long, elapsedPollTime: Long): Boolean
}
