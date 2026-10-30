package net.bradball.teetimecaddie.android.feature.games.contacts

import net.bradball.teetimecaddie.features.games.ContactsRepository
import net.bradball.teetimecaddie.features.games.DeviceContactsReader

/**
 * Builds a [ContactsRepository] over a reader: in practice, a [PickedContactsReader] for each set
 * of contacts the system picker returns. A seam so ViewModel tests can build one over a fake.
 */
fun interface ContactsRepositoryFactory {
    fun create(reader: DeviceContactsReader): ContactsRepository
}
