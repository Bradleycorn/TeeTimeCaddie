package net.bradball.teetimecaddie.android.ui.common.forms.filters

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import net.bradball.teetimecaddie.core.extensions.formattedPhoneNumber

/**
 * Draws a run of digits as "(502) 555-1234" while the field's value stays the digits alone.
 *
 * A [VisualTransformation] rather than formatting the value itself. Rewriting a `String`-valued
 * `TextField` on every keystroke moves the text under the caret without moving the caret, so digits
 * typed after the first separator appear get inserted in the middle — "5025551234" typed in order
 * comes out as "(502) 123-4555". Transforming only the *display* leaves the caret indexed against
 * the digits, and [OffsetMapping] translates between the two.
 *
 * It also keeps the formatting out of state: the ViewModel holds and submits digits, which is what
 * storage wants.
 *
 * The iOS twin is the `Binding` in `util/PhoneNumber.swift`.
 */
object PhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = digits.formattedPhoneNumber

        // Where each digit landed in the formatted string. Built by walking the result rather than
        // assuming a fixed layout, so a partial number — which formats without trailing separators
        // — maps just as correctly as a complete one.
        val digitPositions = buildList {
            formatted.forEachIndexed { index, char -> if (char.isDigit()) add(index) }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 0 -> 0
                offset >= digitPositions.size -> formatted.length
                else -> digitPositions[offset]
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset.coerceIn(0, formatted.length)).count { it.isDigit() }
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
