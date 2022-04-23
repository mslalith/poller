package dev.mslalith.poller.strategy

import dev.mslalith.poller.Poller
import dev.mslalith.poller.PollerState
import dev.mslalith.poller.new
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class BeforeTimeoutStrategyTest : BaseStrategyTest() {
    override fun TestScope.createPoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = TimeoutStrategy(timeOutInMillis = 5_000)
    )

    override fun pollCompletionDelay(): Long = 3_000

    override fun endPollerState(): PollerState<Int> = PollerState.InProgress(result = 3)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AfterTimeoutStrategyTest : BaseStrategyTest() {
    override fun TestScope.createPoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = TimeoutStrategy(timeOutInMillis = 5_000)
    )

    override fun pollCompletionDelay(): Long = 10_000

    override fun endPollerState(): PollerState<Int> = PollerState.Complete
}
