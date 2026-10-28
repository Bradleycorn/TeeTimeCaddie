package net.bradball.teetimecaddie.android.feature.games.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Where the app stands on reading the device's contacts. */
enum class ContactsAccess {
    /** Contacts can be read. */
    GRANTED,

    /** The system has never been asked, so the explanation sheet comes first. */
    NOT_ASKED,

    /** The system was asked and access isn't on. Only Settings can turn it on now. */
    DENIED,
}

/**
 * The app's contacts access, as the New game form needs it.
 *
 * An interface so ViewModel tests can set the answer.
 */
interface ContactsPermission {
    val access: ContactsAccess

    /** The system prompt has been answered, either way. */
    fun markRequested()
}

/**
 * [ContactsPermission] over `READ_CONTACTS`.
 *
 * Android reports a permission only as granted or not, and "not" covers both "never asked" and
 * "turned down". So this remembers, across launches, that the system prompt has been shown.
 */
class AndroidContactsPermission @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContactsPermission {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val access: ContactsAccess
        get() = when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED -> ContactsAccess.GRANTED
            preferences.getBoolean(KEY_REQUESTED, false) -> ContactsAccess.DENIED
            else -> ContactsAccess.NOT_ASKED
        }

    override fun markRequested() {
        preferences.edit { putBoolean(KEY_REQUESTED, true) }
    }

    private companion object {
        const val PREFERENCES_NAME = "contacts_access"
        const val KEY_REQUESTED = "requested"
    }
}
