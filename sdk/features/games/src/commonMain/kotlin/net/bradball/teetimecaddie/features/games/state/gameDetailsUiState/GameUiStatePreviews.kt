package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameInviteeList
import net.bradball.teetimecaddie.core.models.previewGamePlayerList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.core.models.previewPlayerList

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameUiState = previewGame.toUiState(viewer = previewPlayer)

/** [previewGame] with three invitees, as its organizer sees it: no open slots left to show. */
val previewGameWithInviteesUiState = previewGame
    .copy(invitees = previewGameInviteeList)
    .toUiState(viewer = previewPlayer)

// The invitee the invitee-view previews are seen by: invited by the number on their profile.
private val previewInviteeViewer = previewPlayerList.last()
private val previewInvitation = previewGameInvitee.copy(name = previewInviteeViewer.name, phone = previewInviteeViewer.phone)

/** [previewGame] as an invitee sees it before answering: the RSVP card asks. */
val previewInvitedGameUiState = previewGame
    .copy(invitees = listOf(previewInvitation) + previewGameInviteeList.drop(1))
    .toUiState(viewer = previewInviteeViewer)

/** [previewGame] as an invitee sees it after saying yes: they hold a slot, labelled "Accepted". */
val previewAcceptedGameUiState = previewGame
    .copy(
        teeTimes = listOf(
            previewGame.teeTimes.single().let {
                it.copy(players = it.players + GamePlayer(previewInviteeViewer.id, previewInviteeViewer.name))
            }
        ),
        invitees = listOf(previewInvitation.copy(playerId = previewInviteeViewer.id, status = InviteStatus.ACCEPTED))
    )
    .toUiState(viewer = previewInviteeViewer)

/** [previewGame] as an invitee sees it once confirmed players fill every slot. */
val previewFullGameUiState = previewGame
    .copy(
        teeTimes = listOf(previewGame.teeTimes.single().copy(slots = 2, players = previewGamePlayerList.take(2))),
        invitees = listOf(previewInvitation)
    )
    .toUiState(viewer = previewInviteeViewer)
