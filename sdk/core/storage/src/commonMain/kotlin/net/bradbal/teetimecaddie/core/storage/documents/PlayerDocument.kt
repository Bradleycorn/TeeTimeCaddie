package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDocument(
    val name: String
)
