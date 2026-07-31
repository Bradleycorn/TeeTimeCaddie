package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimesList

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import javax.inject.Inject

sealed interface TeeTimesUiState {
    object Loading: TeeTimesUiState
    object Empty: TeeTimesUiState
    data class Content(val teeTimes: List<TeeTime>): TeeTimesUiState
}


@HiltViewModel
class TeeTimesListViewModel @Inject constructor(
    private val teeTimesRepo: TeeTimesRepository,
    private val authRepository: AuthRepository,
    private val eventManager: EventManager
): ViewModel() {


}