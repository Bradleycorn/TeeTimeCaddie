package net.bradball.teetimecaddie.android.initializers

/*** Represents the current state of an initialization task in the AppInitializers system.
 *
 * This sealed class hierarchy tracks the lifecycle of individual [AppInitializer] instances
 * as they progress through execution. The state is exposed via Flow streams in [AppInitializers],
 * allowing UI components and other observers to react to initialization progress.
 *
 * ## State Transitions
 *
 * All initializers begin in [Pending] state and transition to either [Complete] or [Failed]:
 * - **Pending → Complete**: Normal successful initialization
 * - **Pending → Failed**: Initialization threw an exception
 *
 * Once an initializer reaches [Complete] or [Failed], its state does not change (except for
 * ON_START priority initializers which reset to [Pending] on each app start).
 *
 * ## Usage Example
 *
 * ```kotlin
 * appInitializers.stateFlow(InitializerPriority.APP_LAUNCH)
 *     .collect { state ->
 *         when (state) {
 *             is InitializationState.Pending -> showLoadingSpinner()
 *             is InitializationState.Complete -> hideLoadingSpinner()
 *             is InitializationState.Failed -> showError(state.error)
 *         }
 *     }
 * ```
 *
 * @see AppInitializers.stateFlow for observing initialization state
 * @see InitializerPriority for grouping initializers by priority level
 */
sealed class InitializationState {
    /**
     * Indicates that initialization has not yet started or is currently in progress.
     *
     * This is the initial state for all initializers. For ON_START priority initializers,
     * this state may occur multiple times as they re-initialize on each app foreground.
     */
    object Pending: InitializationState()

    /**
     * Indicates that initialization completed successfully.
     *
     * All initializers in this state have executed their [AppInitializer.init] method
     * without throwing exceptions.
     */
    object Complete: InitializationState()

    /**
     * Indicates that initialization failed with an exception.
     *
     * When an initializer reaches this state, subsequent initializers in the same
     * [InitializerPriority] level will not execute. The [error] property contains
     * the wrapped exception for logging and error handling.
     *
     * @property error The exception that caused initialization to fail, wrapped in
     *                 [InitializationException]
     */
    class Failed(val error: Exception): InitializationState()
}