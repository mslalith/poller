package dev.mslalith.poller.strategy

/**
 * Run the poller indefinitely
 */
class IndefiniteStrategy : PollStrategy {
    override fun canPoll(pollInterval: Long, elapsedPollTime: Long): Boolean = true
}
