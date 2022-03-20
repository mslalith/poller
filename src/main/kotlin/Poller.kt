import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A concrete implementation of [IPoller]. Handles the execution of a poll
 *
 * For example, let's take `pollInterval = 4_000` and `pollRepeatCount = 5`
 *
 * Then the total time of this poll would be `20_000 (4_000 * 5)`
 *
 * @param coroutineScope The scope in which the poll should execute
 * @param pollInterval Time in millis for a poll to every after
 * @param pollRepeatCount Number of times the poll should happen
 */
internal class Poller<T>(
    private val coroutineScope: CoroutineScope,
    private val pollInterval: Long,
    private val pollRepeatCount: Int
) : IPoller<T> {

    companion object {
        /**
         * Creates a [Poller] instance
         */
        fun <T> create(
            coroutineScope: CoroutineScope,
            pollInterval: Long,
            pollRepeatCount: Int
        ): IPoller<T> = Poller(coroutineScope, pollInterval, pollRepeatCount)
    }

    private val _pollerStateFlow: MutableStateFlow<PollerState<T>> = MutableStateFlow(PollerState.Initial)

    override val pollerStateFlow: StateFlow<PollerState<T>>
        get() = _pollerStateFlow.asStateFlow()

    private var pollerJob: Job? = null
    private var isPolling: Boolean = false
    private var currentPollCount: Int = 1

    override fun poll(pollBlock: suspend () -> T): Job {
        isPolling = true
        currentPollCount = 1

        return coroutineScope.launch {
            while (isActive && canPoll()) {
                val pollResult = pollBlock()
                _pollerStateFlow.value = PollerState.InProgress(pollResult)

                currentPollCount++
                delay(pollInterval)
            }

            _pollerStateFlow.value = PollerState.Complete
        }.also { job ->
            job.invokeOnCompletion { throwable ->
                _pollerStateFlow.value = when (throwable) {
                    is CancellationException -> PollerState.Cancelled
                    else -> PollerState.Complete
                }
                stop()
            }
            pollerJob = job
        }
    }

    override fun canPoll(): Boolean = isPolling && currentPollCount <= pollRepeatCount

    override fun isPolling(): Boolean = isPolling

    override fun stop() {
        isPolling = false
        pollerJob?.cancel()
        pollerJob = null
    }
}
