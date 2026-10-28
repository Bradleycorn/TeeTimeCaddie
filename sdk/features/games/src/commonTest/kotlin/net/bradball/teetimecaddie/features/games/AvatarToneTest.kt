package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.NewInvitee
import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarToneTest {

    // Pinned, so a change to the hash or the tone list, which would recolor everyone's avatar,
    // can't slip through unnoticed.
    @Test
    fun aNameAlwaysGetsTheSameTone() {
        assertEquals(AvatarTone.PRIMARY, AvatarTone.forName("Matt Chen"))
        assertEquals(AvatarTone.SECONDARY, AvatarTone.forName("Jeff Morrison"))
        assertEquals(AvatarTone.TERTIARY, AvatarTone.forName("brad Ball"))
    }

    @Test
    fun ignoresSpacesAroundTheName() {
        assertEquals(AvatarTone.forName("Matt Chen"), AvatarTone.forName("  Matt Chen "))
    }

    @Test
    fun anInviteeKeepsTheirToneFromNewGameToDetails() {
        val picked = NewInvitee(name = "Jeff Morrison", phone = "4155550142")
        val published = GameInvitee(name = "Jeff Morrison", phone = "4155550142", playerId = null, status = InviteStatus.PENDING)

        assertEquals(picked.avatarTone, published.avatarTone)
    }
}
