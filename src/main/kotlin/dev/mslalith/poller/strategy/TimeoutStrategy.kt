package dev.mslalith.poller.strategy

/**
 * Run the poll for maximum of [timeOutInMillis] milliseconds
 */
class TimeoutStrategy(
    private val timeOutInMillis: Long
) : PollStrategy {
    override fun canPoll(pollInterval: Long, elapsedPollTime: Long): Boolean {
        return elapsedPollTime < timeOutInMillis
    }
}
