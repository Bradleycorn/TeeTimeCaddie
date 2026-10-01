package net.bradball.teetimecaddie.android.feature.auth.createAccount

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.extensions.PHONE_NUMBER_LENGTH
import net.bradball.teetimecaddie.core.extensions.isValidPhoneNumber
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.players.PlayerErrors
import net.bradball.teetimecaddie.features.players.PlayerException
import net.bradball.teetimecaddie.session.SessionManager
import javax.inject.Inject

/**
 * State of the profile step of creating an account.
 *
 * A data class rather than a sealed hierarchy because its dimensions are **orthogonal**: a phone
 * number already in use produces a message block *and* an inline field error at the same time, and
 * exclusive states cannot express that.
 */
data class CreateAccountUiState(
    val email: String = "",
    val name: String = "",
    val phoneDigits: String = "",
    val photo: Uri? = null,
    val phoneError: StringResource? = null,
    val message: AuthMessage? = null,
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && phoneDigits.isValidPhoneNumber && !isSubmitting
}

@HiltViewModel(assistedFactory = CreateAccountViewModelFactory::class)
class CreateAccountViewModel @AssistedInject constructor(
    @Assisted email: String,
    private val sessionManager: SessionManager,
    private val photoReader: ProfilePhotoReader,
    private val messenger: TtcMessenger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState(email = email))
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, message = null) }
    }

    /**
     * Accepts digits only, capped at a full number.
     *
     * The cap is load-bearing, not just tidy: `PhoneVisualTransformation` formats at most ten
     * digits, so an eleventh held in state would be invisible on screen yet submitted.
     *
     * Clears the inline error and the message block in the same update, so the two treatments of a
     * phone problem can never drift out of step with each other.
     */
    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(
                phoneDigits = phone.toPhoneDigits().take(PHONE_NUMBER_LENGTH),
                phoneError = null,
                message = null,
            )
        }
    }

    fun onPhotoPicked(uri: Uri?) {
        if (uri != null) _uiState.update { it.copy(photo = uri) }
    }

    /**
     * Saves the profile, completing sign-up.
     *
     * Success navigates nowhere: the resulting `SessionState.SignedIn` swaps the whole tree. The
     * greeting goes through the messenger for the same reason — this ViewModel does not survive it.
     */
    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null, phoneError = null) }

            val photoBytes = state.photo?.let { photoReader.read(it) }
            if (state.photo != null && photoBytes == null) {
                // The photo is optional, so a picture we can't read must not block the sign-up.
                messenger.show(PlayerException(PlayerErrors.PHOTO_UPLOAD_FAILED))
            }

            when (
                val result = sessionManager.completeSignUp(
                    name = state.name.trim(),
                    phone = state.phoneDigits,
                    photo = photoBytes,
                )
            ) {
                is TtcResult.Success -> {
                    messenger.show(AR.strings.auth_toast_account_created, result.data.firstName)
                    _uiState.update { it.copy(isSubmitting = false) }
                }

                is TtcResult.Failure -> report(result.error)
            }
        }
    }

    /**
     * The "Sign in instead" action on a phone-in-use block.
     *
     * Abandons the half-made account and returns to the credentials screen — see [abandonSignUp].
     */
    fun onMessageAction() = abandonSignUp(reason = ABANDON_PHONE_IN_USE)

    /**
     * Abandons sign-up, deleting the account that has no profile.
     *
     * Not launched here, and not `suspend`: [SessionManager.abandonSignUp] owns the scope this runs
     * in, precisely because the back press that calls it is the same event that clears this
     * ViewModel. Returns immediately; the screen is free to pop.
     */
    fun abandonSignUp(reason: String = ABANDON_BACK) {
        sessionManager.abandonSignUp(reason)
    }

    /**
     * Ends a submission and says what went wrong.
     *
     * A phone number already in use is the one failure with a designed treatment, and it gets
     * both: the block explains and offers a way out, the field marks what to change. Everything
     * else goes to the snackbar in its own words.
     */
    private fun report(error: TeeTimeCaddieException) {
        val phoneInUse = (error as? PlayerException)?.error == PlayerErrors.PHONE_IN_USE
        if (!phoneInUse) messenger.show(error)

        _uiState.update {
            it.copy(
                isSubmitting = false,
                message = if (phoneInUse) AuthMessage.PhoneInUse else null,
                phoneError = (error as? PlayerException)?.inlineMessage,
            )
        }
    }

    private companion object {
        const val ABANDON_BACK = "back"
        const val ABANDON_PHONE_IN_USE = "phone_in_use"
    }
}

@AssistedFactory
interface CreateAccountViewModelFactory {
    fun create(email: String): CreateAccountViewModel
}
