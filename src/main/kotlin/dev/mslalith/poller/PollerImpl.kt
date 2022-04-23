package dev.mslalith.poller

import dev.mslalith.poller.strategy.PollStrategy
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
 * A concrete implementation of [Poller]. Handles the execution of a poll
 *
 * For example, let's take `pollInterval = 4_000` and `pollRepeatCount = 5`
 *
 * Then the total time of this poll would be `20_000 (4_000 * 5)`
 *
 * @param coroutineScope The scope in which the poll should execute
 * @param pollInterval Time in millis for a poll to every after
 * @param pollStrategy The strategy to use for the poll to continue
 */
internal class PollerImpl<T>(
    private val coroutineScope: CoroutineScope,
    private val pollInterval: Long,
    private val pollStrategy: PollStrategy
) : Poller<T> {

    private val _pollerStateFlow: MutableStateFlow<PollerState<T>> = MutableStateFlow(PollerState.Initial)

    override val pollerStateFlow: StateFlow<PollerState<T>>
        get() = _pollerStateFlow.asStateFlow()

    private var pollerJob: Job? = null
    private var isPolling: Boolean = false
    private var elapsedPollTime: Long = 0

    override fun poll(pollBlock: suspend () -> T): Job {
        isPolling = true
        elapsedPollTime = 0

        return coroutineScope.launch {
            while (isActive && canPoll()) {
                val pollResult = pollBlock()
                _pollerStateFlow.value = PollerState.InProgress(pollResult)

                elapsedPollTime += pollInterval
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

    override fun isPolling(): Boolean = isPolling

    override fun canPoll(): Boolean = isPolling && pollStrategy.canPoll(pollInterval, elapsedPollTime)

    override fun stop() {
        isPolling = false
        pollerJob?.cancel()
        pollerJob = null
    }
}
