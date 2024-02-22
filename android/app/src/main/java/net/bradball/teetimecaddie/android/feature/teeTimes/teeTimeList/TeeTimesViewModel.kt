package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
class TeeTimesViewModel @Inject constructor(
    private val teeTimesRepo: TeeTimesRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    val uiState: StateFlow<TeeTimesUiState> = teeTimesRepo.getTeeTimes(authRepository.currentUser.id)
        .map {
            when {
                it.isEmpty() -> TeeTimesUiState.Empty
                else -> TeeTimesUiState.Content(it)
            }
        }
        .onStart { emit(TeeTimesUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1_000),
            initialValue = TeeTimesUiState.Loading
        )
}