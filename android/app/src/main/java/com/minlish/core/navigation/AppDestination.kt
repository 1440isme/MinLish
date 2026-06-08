package com.minlish.core.navigation

object AppDestination {
    const val HOME = "home"
    const val DECKS = "decks"
    const val ANALYTICS = "analytics"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val STUDY = "study"
    const val PRACTICE = "practice_quiz"

    const val DECK_ID_ARG = "deckId"
    const val DECK_DETAIL_BASE = "deck_detail"
    const val DECK_DETAIL = "$DECK_DETAIL_BASE/{$DECK_ID_ARG}"

    fun deckDetail(deckId: String): String = "$DECK_DETAIL_BASE/$deckId"
}
