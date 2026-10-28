package net.bradball.teetimecaddie.features.games

/**
 * Reads the contacts stored on the device.
 *
 * Each app implements this over its platform's contacts API and hands it to
 * `TeeTimeCaddieSdk.contactsRepository`. Return the contacts as the device stores them:
 * [ContactsRepository] does all the filtering, splitting and sorting, so the two apps behave the
 * same. Checking and asking for contacts access stays with the app, which calls this only once
 * access is granted.
 */
interface DeviceContactsReader {

    /**
     * Every contact the app can see, in any order. Do the reading off the main thread.
     *
     * Throwing is reported as [GameErrors.CONTACTS_LOAD_FAILED].
     */
    @Throws(Exception::class)
    suspend fun readContacts(): List<DeviceContact>
}

/**
 * A contact as the device stores it.
 *
 * @property id The device's own identifier for the contact.
 * @property name The contact's display name, which may be blank.
 * @property phones Every number on the contact, in the device's order. May be empty.
 */
data class DeviceContact(
    val id: String,
    val name: String,
    val phones: List<DevicePhone>
)

/**
 * One of a [DeviceContact]'s numbers.
 *
 * @property number The number as stored, in any format.
 * @property label The number's type, such as "mobile", already in the device's language. Null or
 *   blank when the device gives none.
 */
data class DevicePhone(
    val number: String,
    val label: String?
)
