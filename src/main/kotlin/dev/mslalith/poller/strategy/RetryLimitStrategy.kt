package dev.mslalith.poller.strategy

/**
 * Run the poll for [maxRetries] number of times
 */
class RetryLimitStrategy(
    private val maxRetries: Int
) : PollStrategy {
    override fun canPoll(pollInterval: Long, elapsedPollTime: Long): Boolean {
        val elapsedRetries = elapsedPollTime / pollInterval
        return elapsedRetries < maxRetries
    }
}
