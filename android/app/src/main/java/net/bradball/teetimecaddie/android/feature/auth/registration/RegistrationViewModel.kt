package net.bradball.teetimecaddie.android.feature.auth.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val eventManager: EventManager
): ViewModel() {

}