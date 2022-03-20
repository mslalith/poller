# Poller

Poller is a simple Kotlin library which runs a certain task at a regular interval

[//]: # (## Download)
[//]: # ()
[//]: # (```kotlin)
[//]: # (testImplementation&#40;"app.cash.turbine:turbine:0.7.0"&#41;)
[//]: # (```)

## Usage

### Create Poller

Use `Poller.create` method to create new Poller. This takes<br>

`coroutineScope`  - The scope in which the poll should execute<br>
`pollInterval`    - Time in millis for the next poll to execute after<br>
`pollRepeatCount` - Number of times for the poll to execute

For example, let's take `pollInterval = 4_000` and `pollRepeatCount = 5`<br>
Then the total time of this poll would be `20_000 (4_000 * 5)`

```kotlin
val poller: IPoller<Int> = Poller.create(
    coroutineScope = coroutineScope,
    pollInterval = 1_000,
    pollRepeatCount = 4
)
```

### Consuming Poll events

Poller exposes a `StateFlow<PollerState>` which holds the current state of your Poll.
`PollerState` can be of 4 states:
- `Initial` - the initial state when the poller is created
- `InProgress` - indicates the poll is in progress. This state holds the current result from the `pollBlock`
- `Complete` - the poll is complete
- `Cancelled` - poll was cancelled manually or the provided `coroutineScope` was cancelled

```kotlin
pollerStateFlow.collect { pollerState ->
    when (pollerState) {
        PollerState.Cancelled -> // handle cancel
        PollerState.Complete -> // handle complete
        is PollerState.InProgress -> // handle in progress
        PollerState.Initial -> // handle initial state
    }
}
```

### Start Polling

Calling `poll` will start the poll.<br>
It takes a `pollBlock` which executes on every poll.<br>
The advantage with this is that your poll logic can be conditional if needed.

This will return a `Job` which gives more control over your poll

```kotlin
val job = poller.poll {
    // your poll logic
}
```

### Stop Polling

Calling `stop` will stop the poll. It takes a `pollBlock` which executes on every poll.<br>
The advantage with this is that your poll logic can be conditional if needed

```kotlin
poller.stop()
```

### Is Poll running?

Calling `isPolling` will return a boolean which tells whether your poll is running or not

```kotlin
poller.isPolling()
```

## License

    Copyright 2022 M S Lalith

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
