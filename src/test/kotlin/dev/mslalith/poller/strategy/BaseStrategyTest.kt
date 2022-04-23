package dev.mslalith.poller.strategy

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.mslalith.poller.Poller
import dev.mslalith.poller.PollerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseStrategyTest {

    abstract fun TestScope.createPoller(): Poller<Int>

    abstract fun pollCompletionDelay(): Long

    abstract fun endPollerState(): PollerState<Int>

    @Test
    fun runTest() = runTest {
        val poller = createPoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(pollCompletionDelay())
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(endPollerState())
        }
        advanceUntilIdle()
        poller.stop()
    }
}
