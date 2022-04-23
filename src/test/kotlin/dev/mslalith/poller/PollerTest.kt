package dev.mslalith.poller

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.mslalith.poller.strategy.RetryLimitStrategy
import dev.mslalith.poller.strategy.TimeoutStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class PollerTest {

    private fun TestScope.createFinitePoller(): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = TimeoutStrategy(timeOutInMillis = 4_000)
    )

    private fun TestScope.createRetryLimitPoller(maxRetries: Int): Poller<Int> = Poller.new(
        coroutineScope = this,
        pollInterval = 1_000,
        pollStrategy = RetryLimitStrategy(maxRetries)
    )

    @Test
    fun `cancel coroutine scope before completion of poll & verify poll state`() = runTest {
        val testScope = TestScope()
        val poller = testScope.createFinitePoller()
        var count = 1
        poller.poll { count++ }
        testScope.advanceTimeBy(2_000)
        testScope.cancel()
        testScope.advanceTimeBy(500)
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.Cancelled)
        }
    }

    @Test
    fun `cancel poll before it's completion & verify poll state`() = runTest {
        val poller = createFinitePoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(2_000)
        poller.stop()
        advanceTimeBy(500)
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.Cancelled)
        }
    }

    @Test
    fun `wait for poll to complete & verify poll state`() = runTest {
        val poller = createFinitePoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(4_000)
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.InProgress(4))
        }
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `poll every second & verify that it can still be polled in between`() = runTest {
        val poller = createFinitePoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(2_000)
        assertThat(poller.canPoll()).isTrue()
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `wait until poll completes & verify that it cannot be polled`() = runTest {
        val poller = createFinitePoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(5_000)
        assertThat(poller.canPoll()).isFalse()
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `exhaust retires & verify the behaviour`() = runTest {
        val poller = createRetryLimitPoller(maxRetries = 2)
        var count = 1
        poller.poll { count++ }
        assertThat(poller.canPoll()).isTrue()
        advanceTimeBy(3_000)
        assertThat(poller.canPoll()).isFalse()
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.Complete)
        }
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `exhaust time & verify the behaviour`() = runTest {
        val poller = createFinitePoller()
        var count = 1
        poller.poll { count++ }
        val canPoll = poller.canPoll()
        assertThat(canPoll).isTrue()
        advanceTimeBy(5_000)
        assertThat(poller.canPoll()).isFalse()
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.Complete)
        }
        advanceUntilIdle()
        poller.stop()
    }
}
