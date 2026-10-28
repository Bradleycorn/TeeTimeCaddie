package net.bradball.teetimecaddie.android.feature.games

import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsAccess
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsPermission

/** A [ContactsPermission] whose answer the test sets. */
class FakeContactsPermission(override var access: ContactsAccess = ContactsAccess.NOT_ASKED) : ContactsPermission {

    var requested = false
        private set

    override fun markRequested() {
        requested = true
    }
}
