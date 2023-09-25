package net.bradball.teetimecaddie.android

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class TeeTimeCaddieActivityViewModel @Inject constructor(
    val appInitializers: AppInitializers,
    val authRepo: AuthRepository,
    val eventManager: EventManager
): ViewModel() {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appInitializers)
    }

    val initState: StateFlow<InitializationState> = appInitializers.state.stateIn(
        scope = viewModelScope,
        initialValue = InitializationState.Pending,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    val isLoggedIn: Boolean
        get() = authRepo.isLoggedIn
}