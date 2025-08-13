package net.bradball.teetimecaddie.android.ui.common.forms

import androidx.compose.ui.text.input.OffsetMapping

/**
 * An extension of the OffsetMapping interface
 * that adds a property to track the original
 * text length, and uses it to make sure a
 * transform an offset to the original text
 * falls within the bounds of that original text length.
 */
interface TtcOffsetMapping: OffsetMapping {
    /**
     * The length of the original text that was transformed.
     */
    val originalTextLength: Int

    /**
     * Ensures that an offset value falls within the bounds
     * of the original text length.
     * Implementors should override this method to do their
     * offset calculation, and then call through to this
     * method, passing in their calculated offset.
     */
    override fun transformedToOriginal(offset: Int): Int {
        return offset.coerceAtMost(originalTextLength)
    }
}