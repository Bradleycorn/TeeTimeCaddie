package net.bradball.teetimecaddie.android.feature.games.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bradball.teetimecaddie.features.games.DeviceContact
import net.bradball.teetimecaddie.features.games.DeviceContactsReader
import net.bradball.teetimecaddie.features.games.DevicePhone

/**
 * Reads the numbers the organizer picked in [PickContacts], from the URI the picker returned.
 *
 * The rows follow the `ContactsContract.Data` schema whichever picker ran, so one query covers
 * both. Numbers are grouped into contacts by lookup key, and each number's label is the system's
 * own wording for its type, in the device's language; mobile numbers are marked, so the contact is
 * invited at one. Read it promptly: the picker's grant expires.
 */
class PickedContactsReader(
    private val context: Context,
    private val uri: Uri,
) : DeviceContactsReader {

    override suspend fun readContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = LinkedHashMap<String, Pair<String, MutableList<DevicePhone>>>()
        // The picker's URI rejects a selection, so every row comes back and non-phone rows are
        // skipped here.
        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.Data.LOOKUP_KEY,
                ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                ContactsContract.Data.MIMETYPE,
                Phone.NUMBER,
                Phone.TYPE,
                Phone.LABEL,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            val keyColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME_PRIMARY)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val numberColumn = cursor.getColumnIndexOrThrow(Phone.NUMBER)
            val typeColumn = cursor.getColumnIndexOrThrow(Phone.TYPE)
            val labelColumn = cursor.getColumnIndexOrThrow(Phone.LABEL)

            while (cursor.moveToNext()) {
                if (cursor.getString(mimeTypeColumn) != Phone.CONTENT_ITEM_TYPE) continue
                val number = cursor.getString(numberColumn) ?: continue
                val type = cursor.getInt(typeColumn)
                val label = Phone.getTypeLabel(context.resources, type, cursor.getString(labelColumn)).toString()
                val key = cursor.getString(keyColumn) ?: number
                val (_, phones) = contacts.getOrPut(key) {
                    cursor.getString(nameColumn).orEmpty() to mutableListOf()
                }
                phones += DevicePhone(number = number, label = label, isMobile = type in MOBILE_TYPES)
            }
        }
        contacts.map { (key, contact) -> DeviceContact(id = key, name = contact.first, phones = contact.second) }
    }
}

/** The phone types that count as a mobile number. */
private val MOBILE_TYPES = setOf(Phone.TYPE_MOBILE, Phone.TYPE_WORK_MOBILE)
