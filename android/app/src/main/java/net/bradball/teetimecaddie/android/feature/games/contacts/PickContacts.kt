package net.bradball.teetimecaddie.android.feature.games.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.activity.result.contract.ActivityResultContract

/**
 * Opens the system's contact picker for phone numbers, and returns the picked numbers' content
 * URI, or null when the organizer backs out.
 *
 * The app holds no contacts permission: the URI itself grants read access to just what was picked,
 * for a short while. Read it with [PickedContactsReader].
 *
 * - **Android 17 and later:** the system contact picker, listing only contacts with a number, with
 *   multi-select.
 * - **Earlier versions:** the long-standing system picker for a single number. The organizer adds
 *   more people by picking again.
 */
class PickContacts : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        if (Build.VERSION.SDK_INT >= ANDROID_17) {
            Intent(ACTION_PICK_CONTACTS)
                .putExtra(EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .putStringArrayListExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS, arrayListOf(Phone.CONTENT_ITEM_TYPE))
        } else {
            Intent(Intent.ACTION_PICK).setType(Phone.CONTENT_TYPE)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == Activity.RESULT_OK) intent?.data else null

    // The app compiles against API 36, which doesn't define these yet. They're the published values
    // of `ContactsPickerSessionContract` and `Intent` in API 37.
    private companion object {
        const val ANDROID_17 = 37
        const val ACTION_PICK_CONTACTS = "android.provider.action.PICK_CONTACTS"
        const val EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS = "android.provider.extra.PICK_CONTACTS_REQUESTED_DATA_FIELDS"
        const val EXTRA_USE_SYSTEM_CONTACTS_PICKER = "android.intent.extra.USE_SYSTEM_CONTACTS_PICKER"
    }
}
