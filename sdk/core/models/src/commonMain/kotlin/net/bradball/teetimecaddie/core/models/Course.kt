package net.bradball.teetimecaddie.core.models

/**
 * A golf course that games are played at.
 *
 * Courses are shared: every organizer who types the same name, in any capitalisation, plays at the
 * same course. The first person to use a name decides how it is spelled, and everyone after them
 * sees that spelling.
 *
 * @property id The course's key — its name normalised for matching. Stable, and the same for every
 *   spelling of the name, so it is safe to compare courses by it.
 * @property name The course's name as it was first entered, e.g. "Pebble Beach".
 */
data class Course(
    val id: String,
    val name: String
)

val previewCourse = Course(
    id = "persimmon ridge",
    name = "Persimmon Ridge"
)

val previewCourseList = listOf(
    previewCourse,
    Course(id = "pebble beach", name = "Pebble Beach"),
    Course(id = "bethpage black", name = "Bethpage Black"),
    Course(id = "valhalla", name = "Valhalla")
)
