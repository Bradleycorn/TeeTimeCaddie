package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.extensions.isValidEmail
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.session.SessionManager
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

/**
 * State of the credentials screen.
 *
 * One screen serves both signing in and starting an account, so there is no "mode" here — which of
 * the two happens is decided by the button that was tapped, not by state.
 *
 * There is no `emailError`/`passwordError`: after the design update this screen has no inline field
 * errors. Everything it has to say, it says in [message].
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val message: AuthMessage? = null,
    val isSubmitting: Boolean = false,
) {
    /**
     * Whether either action can be taken.
     *
     * The same for both buttons: both need an address that could be real and a password that was
     * typed. Password *rules* are Firebase's to enforce on sign-up, and applying them here would
     * lock out an existing account whose password predates them.
     */
    val canSubmit: Boolean
        get() = email.isValidEmail && password.isNotEmpty() && !isSubmitting
}

/** One-shot things the credentials screen does in response to the ViewModel, rather than renders. */
sealed interface LoginEvent {
    /** The account now exists; move to the profile step with this address. */
    data class AccountCreated(val email: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val messenger: TtcMessenger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)

    /**
     * Navigation that follows a success.
     *
     * A [Channel] rather than a `StateFlow`, because "go to the profile step" must happen once —
     * replaying it on the next configuration change would push a second copy of the screen.
     */
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    /**
     * Editing either field clears the message.
     *
     * Both handlers do it, so "changing what you typed dismisses the block" is a property of the
     * state update rather than a separate rule someone has to remember to apply.
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, message = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, message = null) }
    }

    /**
     * Signs in with what has been typed.
     *
     * On failure this deliberately leaves [LoginUiState.email] alone and fires no event — the
     * address someone just typed survives every failure, and nothing navigates away from a screen
     * still showing an unexplained problem.
     */
    fun signIn() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }

            // NonCancellable: success is what destroys this ViewModel, because TeeTimeCaddieApp
            // swaps the tree the moment SessionState changes. Without this the greeting is emitted
            // from a coroutine that the swap has already cancelled, and whether it survives is a
            // race. See ProfileViewModel.signOut, which lost that race every time.
            withContext(NonCancellable) {
                when (val result = sessionManager.signIn(state.email, state.password)) {
                    is TtcResult.Success -> {
                        // Nothing navigates: TeeTimeCaddieApp swaps the tree when SessionState
                        // changes. The greeting is routed through the messenger because this
                        // ViewModel is about to be destroyed by that swap.
                        (result.data as? SessionState.SignedIn)?.let { user ->
                            messenger.show(AR.strings.auth_toast_welcome_back, user.player.firstName)
                        }
                        _uiState.update { it.copy(isSubmitting = false) }
                    }

                    // Only a rejected credential gets the designed block. A dropped connection or a
                    // disabled account is a different problem, and saying "that email and password
                    // don't match" about it would be a lie.
                    is TtcResult.Failure -> report(
                        error = result.error,
                        message = if (result.error.isInvalidCredentials) AuthMessage.SignInFailed else null,
                    )
                }
            }
        }
    }

    /**
     * Creates the account, then moves to the profile step.
     *
     * An address that already has an account is reported here rather than after the profile form,
     * which is the whole reason the Firebase account is created from this screen.
     */
    fun createAccount() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }

            when (val result = sessionManager.startSignUp(state.email, state.password)) {
                is TtcResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(LoginEvent.AccountCreated(state.email))
                }

                is TtcResult.Failure -> report(
                    error = result.error,
                    message = if (result.error.isEmailInUse) AuthMessage.EmailInUse(state.email) else null,
                )
            }
        }
    }

    /**
     * The "Sign in instead" action on an [AuthMessage.EmailInUse] block.
     *
     * Clears the password and the block but **keeps the email**: the address was right, it is the
     * intent that changes. The person is already on the screen that signs in, so nothing moves.
     */
    fun onMessageAction() {
        _uiState.update { it.copy(password = "", message = null) }
    }

    /**
     * Ends a submission and says what went wrong.
     *
     * A failure the design draws a [message] for is shown on the screen, where it persists until the
     * person edits a field. Everything else goes to the snackbar in its own words — the design has
     * no banner for it, and showing nothing at all would read as the button not working.
     */
    private fun report(error: TeeTimeCaddieException, message: AuthMessage?) {
        if (message == null) messenger.show(error)
        _uiState.update { it.copy(isSubmitting = false, message = message) }
    }

    private val TeeTimeCaddieException.isInvalidCredentials: Boolean
        get() = (this as? AuthException)?.error == AuthErrors.INVALID_CREDENTIALS

    private val TeeTimeCaddieException.isEmailInUse: Boolean
        get() = (this as? AuthException)?.error == AuthErrors.EMAIL_IN_USE
}
