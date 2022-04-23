package dev.mslalith.poller.strategy

import com.google.common.truth.Truth
import dev.mslalith.poller.Poller
import dev.mslalith.poller.PollerState
import dev.mslalith.poller.new
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndefiniteStrategyTest {

    private fun TestScope.createIndefinitePoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = IndefiniteStrategy()
    )

    @Test
    fun `run indefinite test`() {
        val testScope = TestScope()
        val poller = testScope.createIndefinitePoller()
        var count = 1
        Truth.assertThat(poller.canPoll()).isFalse()
        Truth.assertThat(poller.isPolling()).isFalse()
        poller.poll { count++ }
        Truth.assertThat(poller.canPoll()).isTrue()
        Truth.assertThat(poller.isPolling()).isTrue()
        testScope.advanceTimeBy(10_000)
        Truth.assertThat(poller.pollerStateFlow.value).isEqualTo(PollerState.InProgress(10))
        testScope.advanceTimeBy(10_000)
        Truth.assertThat(poller.pollerStateFlow.value).isEqualTo(PollerState.InProgress(20))
        testScope.advanceTimeBy(10_000)
        Truth.assertThat(poller.pollerStateFlow.value).isEqualTo(PollerState.InProgress(30))

        Truth.assertThat(poller.isPolling()).isTrue()
        Truth.assertThat(poller.canPoll()).isTrue()
        poller.stop()
        testScope.advanceTimeBy(1_000)
        Truth.assertThat(poller.pollerStateFlow.value).isEqualTo(PollerState.Cancelled)
        Truth.assertThat(poller.isPolling()).isFalse()
        Truth.assertThat(poller.canPoll()).isFalse()
    }
}
