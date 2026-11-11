package net.bradball.teetimecaddie.features.games.state.gamesListUiState

/** The circle after a stack of player avatars, if any. */
sealed interface PlayerStackOverflow {
    /** No extra circle. */
    data object None : PlayerStackOverflow

    /** A "+" circle: there are open slots to claim. */
    data object Add : PlayerStackOverflow

    /**
     * A "+N" circle: [count] invitees still haven't answered.
     *
     * @property label The circle's text, e.g. "+2".
     */
    data class More(val count: Int) : PlayerStackOverflow {
        val label: String
            get() = "+$count"
    }
}
