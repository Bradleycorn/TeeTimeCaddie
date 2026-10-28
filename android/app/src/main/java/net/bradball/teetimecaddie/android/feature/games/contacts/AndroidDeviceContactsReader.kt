package net.bradball.teetimecaddie.android.feature.games.contacts

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bradball.teetimecaddie.features.games.DeviceContact
import net.bradball.teetimecaddie.features.games.DeviceContactsReader
import net.bradball.teetimecaddie.features.games.DevicePhone
import javax.inject.Inject

/**
 * Reads the device's contacts through [android.provider.ContactsContract], for the SDK's
 * `ContactsRepository` to shape.
 *
 * Only contacts with at least one number are read, since the phone table lists numbers rather than
 * contacts. Each number's label is the system's own wording for its type, in the device's language.
 * Needs `READ_CONTACTS`; without it the query throws, which the repository reports as a failure.
 */
class AndroidDeviceContactsReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceContactsReader {

    override suspend fun readContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = LinkedHashMap<Long, Pair<String, MutableList<DevicePhone>>>()
        context.contentResolver.query(
            Phone.CONTENT_URI,
            arrayOf(Phone.CONTACT_ID, Phone.DISPLAY_NAME_PRIMARY, Phone.NUMBER, Phone.TYPE, Phone.LABEL),
            null,
            null,
            // A contact's primary number first, then the rest in the order they were added.
            "${Phone.CONTACT_ID}, ${Phone.IS_PRIMARY} DESC, ${Phone._ID}",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME_PRIMARY)
            val numberColumn = cursor.getColumnIndexOrThrow(Phone.NUMBER)
            val typeColumn = cursor.getColumnIndexOrThrow(Phone.TYPE)
            val labelColumn = cursor.getColumnIndexOrThrow(Phone.LABEL)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberColumn) ?: continue
                val label = Phone.getTypeLabel(
                    context.resources,
                    cursor.getInt(typeColumn),
                    cursor.getString(labelColumn),
                ).toString()
                val (_, phones) = contacts.getOrPut(cursor.getLong(idColumn)) {
                    cursor.getString(nameColumn).orEmpty() to mutableListOf()
                }
                phones += DevicePhone(number = number, label = label)
            }
        }
        contacts.map { (id, contact) -> DeviceContact(id = id.toString(), name = contact.first, phones = contact.second) }
    }
}
