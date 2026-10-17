package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

/**
 * Document model for a course, stored at `courses/{nameKey}`.
 *
 * The document id *is* [nameKey], so Firestore itself keeps course names unique: two organizers
 * typing "Pebble Beach" and "pebble beach" address the same document.
 *
 * @property name The name as the first organizer typed it. Everyone after them sees this spelling.
 * @property nameKey [name] normalised for matching. The security rules check it equals the id.
 */
@Serializable
data class CourseDocument(
    val name: String,
    val nameKey: String
)
