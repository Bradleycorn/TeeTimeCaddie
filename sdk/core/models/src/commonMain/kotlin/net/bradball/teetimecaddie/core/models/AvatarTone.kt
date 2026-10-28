package net.bradball.teetimecaddie.core.models

/**
 * The color an initial avatar is drawn in.
 *
 * The apps map each value onto their own color role: [PRIMARY] the brand green, [SECONDARY] the
 * gold, [TERTIARY] the sky blue.
 */
enum class AvatarTone {
    PRIMARY,
    SECONDARY,
    TERTIARY;

    companion object {
        // Five slots, so blue and green each come up about twice as often as gold.
        private val byHash = listOf(TERTIARY, PRIMARY, TERTIARY, PRIMARY, SECONDARY)

        /**
         * The tone for a person named [name], the same every time, so someone keeps their color on
         * every screen and on both platforms.
         *
         * A 31-based rolling hash of the name's characters, kept to 16 bits, picks a slot in
         * [byHash]. Spaces around the name don't change it.
         */
        fun forName(name: String): AvatarTone {
            var hash = 0
            name.trim().forEach { hash = (hash * 31 + it.code) and 0xFFFF }
            return byHash[hash % byHash.size]
        }
    }
}
