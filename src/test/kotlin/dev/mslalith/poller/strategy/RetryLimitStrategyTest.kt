package dev.mslalith.poller.strategy

import dev.mslalith.poller.Poller
import dev.mslalith.poller.PollerState
import dev.mslalith.poller.new
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class BeforeRetryLimitStrategyTest : BaseStrategyTest() {
    override fun TestScope.createPoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = RetryLimitStrategy(maxRetries = 3)
    )

    override fun pollCompletionDelay(): Long = 2_000

    override fun endPollerState(): PollerState<Int> = PollerState.InProgress(result = 2)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AfterRetryLimitStrategyTest : BaseStrategyTest() {
    override fun TestScope.createPoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = RetryLimitStrategy(maxRetries = 3)
    )

    override fun pollCompletionDelay(): Long = 5_000

    override fun endPollerState(): PollerState<Int> = PollerState.Complete
}
