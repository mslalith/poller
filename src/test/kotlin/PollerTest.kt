import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class PollerTest {

    private fun TestScope.createPoller(): IPoller<Int> = Poller.create(
        coroutineScope = this,
        pollInterval = 1_000,
        pollRepeatCount = 4
    )

    @Test
    fun `cancel coroutine scope before completion of poll & verify poll state`() = runTest {
        val testScope = TestScope()
        val poller = testScope.createPoller()
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
        val poller = createPoller()
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
        val poller = createPoller()
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
    fun `exceed the poll time & verify poll state`() = runTest {
        val poller = createPoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(5_000)
        poller.pollerStateFlow.test {
            assertThat(expectMostRecentItem()).isEqualTo(PollerState.Complete)
        }
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `poll every second & verify that it can still be polled in between`() = runTest {
        val poller = createPoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(2_000)
        assertThat(poller.canPoll()).isTrue()
        advanceUntilIdle()
        poller.stop()
    }

    @Test
    fun `wait until poll completes & verify that it cannot be polled`() = runTest {
        val poller = createPoller()
        var count = 1
        poller.poll { count++ }
        advanceTimeBy(5_000)
        assertThat(poller.canPoll()).isFalse()
        advanceUntilIdle()
        poller.stop()
    }
}