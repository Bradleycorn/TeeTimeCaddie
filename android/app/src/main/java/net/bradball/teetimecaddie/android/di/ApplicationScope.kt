package net.bradball.teetimecaddie.android.di

import javax.inject.Qualifier

/**
 * Marks the [kotlinx.coroutines.CoroutineScope] that lives as long as the process.
 *
 * For the rare piece of work that **must not** be cancelled when the screen that started it goes
 * away. Abandoning a sign-up is the motivating case: the back press that triggers it is the same
 * event that destroys the ViewModel, so running it in `viewModelScope` would cancel the cleanup
 * mid-flight and strand a half-made account.
 *
 * Reach for this sparingly. Work in this scope has no lifecycle to stop it, so anything that should
 * follow the UI belongs in `viewModelScope`.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
