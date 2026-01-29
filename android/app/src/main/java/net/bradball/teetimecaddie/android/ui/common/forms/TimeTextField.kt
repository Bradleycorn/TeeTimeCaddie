package net.bradball.teetimecaddie.android.ui.common.forms

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalTime

/**
 * Enum representing AM or PM in 12-hour time format
 */
enum class AmPm {
    AM, PM;

    override fun toString(): String = name

    companion object {
        fun from12HourFormat(hour: Int): AmPm = if (hour < 12) AM else PM
    }
}

/**
 * Data class representing a 12-hour time with AM/PM
 */
private data class Time12Hour(
    val digits: String,  // Just the numeric digits (e.g., "130" or "1130")
    val hasExplicitColon: Boolean,  // Whether user typed a colon
    val amPm: AmPm?  // AM or PM, null if not yet specified
)

/**
 * Converts a LocalTime (24-hour) to 12-hour format with AM/PM
 */
private fun LocalTime.to12Hour(): Pair<Int, AmPm> {
    val hour12 = when (hour) {
        0 -> 12  // Midnight: 00:XX → 12:XX AM
        in 1..12 -> hour  // Morning/Noon: keep as-is
        else -> hour - 12  // Afternoon/Evening: subtract 12
    }
    val amPm = AmPm.from12HourFormat(hour)
    return hour12 to amPm
}

/**
 * Converts 12-hour format (hour, minute, AM/PM) to LocalTime (24-hour)
 */
private fun to24Hour(hour12: Int, minute: Int, amPm: AmPm): LocalTime {
    val hour24 = when {
        hour12 == 12 && amPm == AmPm.AM -> 0  // 12:XX AM → 00:XX
        hour12 == 12 && amPm == AmPm.PM -> 12  // 12:XX PM → 12:XX
        amPm == AmPm.PM -> hour12 + 12  // 1-11 PM → 13-23
        else -> hour12  // 1-11 AM → 1-11
    }
    return LocalTime(hour24, minute)
}

/**
 * Parses time digits to extract hour and minute based on format
 */
private fun parseTimeDigits(digits: String, hasExplicitColon: Boolean): Pair<Int, Int>? {
    if (digits.length < 3) return null  // Need at least H:MM or HMM

    return try {
        val (hour, minute) = when {
            hasExplicitColon -> {
                // With explicit colon: first 1-2 digits are hour, rest are minutes
                when (digits.length) {
                    3 -> digits[0].digitToInt() to digits.substring(1).toInt()  // H:MM
                    4 -> digits.substring(0, 2).toInt() to digits.substring(2).toInt()  // HH:MM
                    else -> return null
                }
            }
            else -> {
                // Without colon: infer format
                when (digits.length) {
                    3 -> {
                        // Check if first two digits form valid hour (10, 11, 12)
                        val first = digits[0].digitToInt()
                        if (first == 1) {
                            val second = digits[1].digitToInt()
                            if (second in 0..2) {
                                // 10X, 11X, 12X -> HH:M
                                digits.substring(0, 2).toInt() to digits[2].digitToInt()
                            } else {
                                // 13X, 14X, 15X -> 1:3X, 1:4X, 1:5X (H:MM)
                                digits[0].digitToInt() to digits.substring(1).toInt()
                            }
                        } else {
                            // 2-9 as first digit -> H:MM
                            digits[0].digitToInt() to digits.substring(1).toInt()
                        }
                    }
                    4 -> digits.substring(0, 2).toInt() to digits.substring(2).toInt()  // HHMM
                    else -> return null
                }
            }
        }

        // Validate hour (1-12) and minute (0-59)
        if (hour in 1..12 && minute in 0..59) {
            hour to minute
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Formats time digits for display with colon
 */
private fun formatTimeDisplay(digits: String, hasExplicitColon: Boolean): Pair<String, String> {
    if (digits.isEmpty()) return "" to ""

    return when {
        hasExplicitColon -> {
            // With colon: split based on explicit boundary
            when (digits.length) {
                1 -> digits to ""  // H:
                2 -> digits[0].toString() to digits[1].toString()  // H:M
                3 -> digits[0].toString() to digits.substring(1)  // H:MM
                4 -> digits.substring(0, 2) to digits.substring(2)  // HH:MM
                else -> "" to ""
            }
        }
        else -> {
            // Without colon: infer split
            when (digits.length) {
                1 -> digits to ""  // H:
                2 -> {
                    // Check if it's HH: or H:M
                    val first = digits[0].digitToInt()
                    if (first == 1) {
                        val second = digits[1].digitToInt()
                        if (second in 0..2) {
                            digits to ""  // 10, 11, 12 - two-digit hour
                        } else {
                            digits[0].toString() to digits[1].toString()  // 1:3-5
                        }
                    } else {
                        digits[0].toString() to digits[1].toString()  // 2-9:X
                    }
                }
                3 -> {
                    // Check if first two digits form valid hour (10, 11, 12)
                    val first = digits[0].digitToInt()
                    if (first == 1) {
                        val second = digits[1].digitToInt()
                        if (second in 0..2) {
                            // 10X, 11X, 12X -> HH:M
                            digits.substring(0, 2) to digits[2].toString()
                        } else {
                            // 13X, 14X, 15X -> 1:3X, 1:4X, 1:5X (H:MM)
                            digits[0].toString() to digits.substring(1)
                        }
                    } else {
                        // 2-9 as first digit -> H:MM
                        digits[0].toString() to digits.substring(1)
                    }
                }
                4 -> digits.substring(0, 2) to digits.substring(2)  // HH:MM
                else -> "" to ""
            }
        }
    }
}

/**
 * Input filter for 12-hour time with AM/PM
 */
private fun filterTimeInput(old: Time12Hour, newInput: String): Time12Hour {
    // Separate digits and letters
    val newDigits = newInput.filter { it.isDigit() }
    val newLetters = newInput.filter { it.isLetter() }.uppercase()

    // Check for colon
    val hasColon = old.hasExplicitColon || (newInput.contains(':') && !old.digits.isEmpty())

    // Filter digits
    var filteredDigits = newDigits.take(4)

    // Validate digit input
    if (filteredDigits.isNotEmpty() && filteredDigits != old.digits) {
        val firstDigit = filteredDigits[0].digitToInt()

        // First digit must be 1-9 (no leading zero)
        if (firstDigit == 0) {
            filteredDigits = old.digits
        }
        // Special validation for first digit = 1
        else if (firstDigit == 1 && filteredDigits.length == 2 && !hasColon) {
            val secondDigit = filteredDigits[1].digitToInt()
            // 10, 11, 12 are valid hours
            // 13, 14, 15 would be interpreted as 1:3X, 1:4X, 1:5X (valid)
            // 16-19 are invalid in all interpretations
            if (secondDigit > 5) {
                filteredDigits = old.digits
            }
        }

        // Validate first minute digit (must be 0-5)
        if (filteredDigits.length >= 2 && filteredDigits != old.digits) {
            val (hourPart, minutePart) = formatTimeDisplay(filteredDigits, hasColon)
            if (minutePart.isNotEmpty()) {
                val firstMinuteDigit = minutePart[0].digitToInt()
                if (firstMinuteDigit > 5) {
                    filteredDigits = old.digits
                }
            }
        }
    }

    // Parse AM/PM from letters
    val newAmPm = when {
        newLetters.isEmpty() -> old.amPm
        newLetters.startsWith("A") -> AmPm.AM
        newLetters.startsWith("P") -> AmPm.PM
        else -> old.amPm
    }

    return Time12Hour(filteredDigits, hasColon, newAmPm)
}

/**
 * Visual transformation for 12-hour time display
 */
private fun buildTimeVisualTransformation(time: Time12Hour, hasFocus: Boolean): VisualTransformation {
    if (!hasFocus && time.digits.isEmpty()) {
        return VisualTransformation.None
    }

    return VisualTransformation { text ->
        val (hourPart, minutePart) = formatTimeDisplay(time.digits, time.hasExplicitColon)

        val displayText = buildAnnotatedString {
            // Hour
            append(hourPart)
            if (hourPart.isEmpty()) {
                withStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.35f))) {
                    append("hh")
                }
            } else if (hourPart.length == 1 && !time.hasExplicitColon && time.digits.length < 2) {
                withStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.35f))) {
                    append("h")
                }
            }

            // Colon
            append(":")

            // Minutes
            append(minutePart)
            if (minutePart.length < 2) {
                withStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.35f))) {
                    append("m".repeat(2 - minutePart.length))
                }
            }

            // Space before AM/PM
            append(" ")

            // AM/PM
            if (time.amPm != null) {
                append(time.amPm.toString())
            } else {
                withStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.35f))) {
                    append("am")
                }
            }
        }

        TransformedText(displayText, TimeOffsetMapping(time))
    }
}

/**
 * Offset mapping for cursor positioning in transformed time text
 */
private class TimeOffsetMapping(private val time: Time12Hour) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        // Map position in digit string to position in display string "H:MM AM"
        val digits = time.digits
        val (hourPart, minutePart) = formatTimeDisplay(digits, time.hasExplicitColon)

        return when (offset) {
            0 -> 0
            in 1..hourPart.length -> offset
            in (hourPart.length + 1)..digits.length -> {
                // After hour, account for colon
                hourPart.length + 1 + (offset - hourPart.length)
            }
            else -> hourPart.length + 1 + minutePart.length
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        // Map position in display string back to digit string
        val (hourPart, minutePart) = formatTimeDisplay(time.digits, time.hasExplicitColon)
        val colonPos = hourPart.length

        return when {
            offset <= colonPos -> offset
            offset == colonPos + 1 -> hourPart.length  // On colon, map to end of hour
            offset <= colonPos + 1 + minutePart.length -> {
                // In minutes section
                hourPart.length + (offset - colonPos - 1)
            }
            else -> time.digits.length  // After minutes or in AM/PM
        }
    }
}

/**
 * Composable text field for 12-hour time input with AM/PM
 */
@Composable
fun TimeTextField(
    label: String,
    time: LocalTime?,
    onValueChange: (LocalTime?) -> Unit = {},
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: @Composable (() -> Unit)? = null
) {
    // Initialize time state from LocalTime
    var timeState by remember(time) {
        val (hour12, amPm) = time?.to12Hour() ?: (0 to null)
        val digits = if (time != null) {
            val h = hour12.toString().padStart(2, '0')
            val m = time.minute.toString().padStart(2, '0')
            h + m
        } else {
            ""
        }
        mutableStateOf(Time12Hour(digits, false, amPm))
    }

    var hasFocus by remember { mutableStateOf(false) }

    // Build input value: digits + optional AM/PM letter(s)
    val inputValue = buildString {
        append(timeState.digits)
        when (timeState.amPm) {
            AmPm.AM -> append("a")
            AmPm.PM -> append("p")
            null -> {}  // No suffix
        }
    }

    OutlinedTextField(
        label = { Text(label) },
        value = inputValue,
        onValueChange = { newValue ->
            timeState = filterTimeInput(timeState, newValue)

            // Try to parse and emit LocalTime if complete
            val parsed = parseTimeDigits(timeState.digits, timeState.hasExplicitColon)
            if (parsed != null && timeState.amPm != null) {
                val (hour, minute) = parsed
                val localTime = to24Hour(hour, minute, timeState.amPm!!)
                onValueChange(localTime)
            } else if (timeState.digits.isEmpty()) {
                onValueChange(null)
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            hasFocus = focusState.hasFocus
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,  // Allows both numbers and letters
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        visualTransformation = buildTimeVisualTransformation(timeState, hasFocus),
        trailingIcon = trailingContent
    )
}