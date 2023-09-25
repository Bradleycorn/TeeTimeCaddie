package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
): ViewModel() {

    var errorMessage: Int? by mutableStateOf(null)
        private set

    var showLoadingProgress: Boolean by mutableStateOf(false)
        private set

    var loginSuccess: Boolean by mutableStateOf(false)
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                errorMessage = null
                showLoadingProgress = true
                authRepo.login(email, password)
                loginSuccess = true
            } catch (ex: AuthException) {
                errorMessage = ex.displayMessage.resourceId
            } finally {
                showLoadingProgress = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}