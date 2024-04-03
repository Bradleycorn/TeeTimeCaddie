package net.bradball.teetimecaddie.android.ui.common.forms

import android.util.Log
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import net.bradball.teetimecaddie.android.ui.common.forms.filters.numericTextFilter
import java.util.*

/**
 * Format used by the date formatter when parsing date input.
 * This is also used when masking the input field itself
 * to inform the user how the date should be entered.
 **/
const val DATE_FORMAT = "MMddyyyy"

const val DATE_LENGTH = 8
/**
 * Single Character used to delineate
 * the month, day, and years fields when
 * the user is inputting a date.
 */
const val DATE_SEPARATOR: Char = '/'

/**
 * An error to display when the user does not enter
 * a full date value.
 */
const val ERROR_PARTIAL_DATE = "Please enter a full date"

/**
 * An error too display when the user enters a date
 * that cannot be properly parsed into a Date object.
 */
const val ERROR_INVALID_DATE = "Please enter a valid date"

private val String.asIsoDate: String
    get() {
        if (length != 8) return ""

        return try {
            val month = slice(0..1).toInt()
            val day = slice(2..3).toInt()
            val year = slice(4..7).toInt()

            "$year-$month-$day"
        } catch (ex: Exception) {
            ""
        }
    }

private fun Int.padStart(length: Int): String = this.toString().padStart(length, '0')

/**
 * Composable function that renders an [OutlinedTextField]
 * suitable for date input. The field allows the user
 * to input month day and years values to build a date.
 * The field uses masking so that the user only
 * enters the numeric input, and it is displayed as a
 * standard mm/dd/yyyy date.
 *
 * The field will show placeholder prompts to inform
 * the user how to enter the date.
 *
 * @param label The text label to display on the field
 * @param modifier Any Modifiers that should be applied to the field.
 * @param value A LocalDate representing the date to display (or null if there is no date to display)
 * @param onValueChange An event handler to call when the date value changes.
 *   The handler is passed a [LocalDate]? argument. The handler will be called with a null argument
 *   value when the text field is empty, and with a Local date when the text field contains a
 *   valid date value. The handler is not called when the field contains a partial date value.
 * @param modifier A modifier to apply to the OutlinedTextField.
 * @param imeAction an ImeAction to apply to the keyboard. Defaults to ImeAction.Default.
 * @param keyboardActions a KeyboardActions to apply to the keyboard. Defaults to KeyboardActions.Default.
 * @trailingContent A composable function that is passed through to the OutlinedTextField's trailingContent parameter.
 */
@Composable
fun DateTextField(
    label: String,
    date: LocalDate?,
    onValueChange: (LocalDate?)->Unit = {},
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: @Composable (()->Unit)? = null
) {

    var value by remember(date) {

        val dateString = date?.let {
            "${it.monthNumber.padStart(2)}${it.dayOfMonth.padStart(2)}${it.year}"
        } ?: ""

        mutableStateOf(dateString)
    }

    var hasFocus: Boolean by remember { mutableStateOf(false) }
    val transform = rememberDateTransform(value, hasFocus)

    OutlinedTextField(
        label = { Text(label) },
        value = value,
        onValueChange = { newValue ->
            value = dateInputFilter(value, newValue)

            val newDate = try { LocalDate.parse(value.asIsoDate) } catch (ex: Exception) { null }
            when {
                value.isEmpty() -> onValueChange(null)
                newDate != null -> onValueChange(newDate)
            }
        },
        modifier = modifier
            .onFocusChanged { focusState -> hasFocus = focusState.hasFocus },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = transform,
        trailingIcon = trailingContent
    )
}

/**
 * A method for doing basic validation of [CdiDateField] inputs. You can use this
 * in conjunction with the with the `validator` property on [InputState]
 * objects for [CdiDateField]s. This method does basic date validation,
 * making sure that the date entered is valid can can properly
 * be parsed into an actual date.
 */
//val simpleDateValidator: (date: String) -> InputValidation = { input ->
//    dateValidator(input) { InputValidation.Valid }
//}


/**
 * A method for doing extensive validation of [CdiDateField] inputs. You can use this
 * in conjunction with the with the `validator` property on [InputState]
 * objects for Date Fields. This method does basic date validation
 * (makes sure a full date is entered, and that it can be properly
 * parsed as an actual Date), and takes a lambda that can be used
 * for additional validation for a specific use case. If you just
 * need the basic date validation, consider using [simpleDateValidator]
 * instead. This method is intended for doing more complex validation of dates.
 *
 * @param date A string containing the date input to be validated.
 *   The string should be the raw input entered by the user in a [CdiDateField]
 *   (ie. only digits in the form mmddyyyy).
 *
 * @param invalidLengthErrorMessage A custom error message string to provide context-specific error messaging for incomplete input
 *
 * @param invalidDateErrorMessage A custom error message string to provide context-specific error messaging for invalid input
 *
 * @param additionalValidation A lambda that takes a java Date and returns an [InputValidation]
 *  indicating if the Date is valid. This lambda should do any extra date validation that
 *  might be required for a specific use case, for example, making sure the date falls in a specific range.
 *
 * @return An [InputValidation] object indicating if the date was validated successfully.
 */
//fun dateValidator(
//    date: String,
//    invalidLengthErrorMessage: String = ERROR_PARTIAL_DATE,
//    invalidDateErrorMessage: String = ERROR_INVALID_DATE,
//    additionalValidation: (Date) -> InputValidation
//): InputValidation {
//    return if (date.length != DATE_LENGTH) {
//        InputValidation.Invalid(invalidLengthErrorMessage)
//    } else {
//        try {
//            SimpleDateFormat(DATE_FORMAT, Locale.US).apply { isLenient = false }.parse(date)?.let { dob ->
//                additionalValidation(dob)
//            } ?: InputValidation.Invalid(invalidDateErrorMessage)
//        } catch (ex: ParseException) {
//            InputValidation.Invalid(invalidDateErrorMessage)
//        }
//    }
//}

/**
 * Builds a VisualTransformation for the date field based on its current [InputState].
 *
 * The Transformation is remembered based on the current [InputState], and is built
 * to appropriately show masked or unmasked input based on the state, such as focus
 * and current value
 *
 * @param inputState The current [InputState] of the field.
 *
 * @return a remembered VisualTransformation suitable for use with [TextField]
 */
@Composable
private fun rememberDateTransform(value: String, hasFocus: Boolean) = remember(value, hasFocus) {
    when {
        !hasFocus && value.isEmpty() -> VisualTransformation.None
        else -> dateTransformBuilder()
    }
}

/**
 * An input filter for date entry.
 *
 * Limits accepted characters in the date field to only digits, and
 * at most DATE_LENGTH digits. Additionally, the first digit must be a 1 or 0.
 *
 * @param old A string with the previous state value of the date field.
 * @param new A string with the new input (usually a single new character) typed by the user.
 *
 * @return A string that has been scrubbed of invalid characters.
 */
private val dateInputFilter: (String, String)->String = { old, new ->
    var text = numericTextFilter(old, new).take(DATE_LENGTH)

    // Make sure the first digit is a 0 or a 1.
    // This simple check should go a long way toward helping the user
    // to know how they need to format the date, and eliminate many
    // other forms of bad input that they may try.
    if (text.isNotEmpty() && !text.startsWith("0") && !text.startsWith("1") ) {
        text = old
    }

    //TODO: We'd like to do some additional validation here, and only
    // allow digits that would form a correct date, similar to the
    // check on the first digit, above.
    // However, there is a defect in the TextField composable that
    // makes doing such checks nearly impossible. We can revisit
    // this once that is fixed.
    // https://issuetracker.google.com/issues/200577798

    text
}

/**
 * Creates and returns a VisualTransform for date values.
 *
 * This transform converts up to DATE_LENGTH digits to date form.
 * For example, "12132021" will be shown as "12/13/2021".
 * Missing digits will be filled in with the passed in placeholder string.
 */
fun dateTransformBuilder() = VisualTransformation { text ->
    val trimmed = if (text.text.length >= DATE_LENGTH) text.text.substring(0..7) else text.text

    val filled = buildAnnotatedString {
        append(trimmed)
        withStyle(SpanStyle(color = Color.Unspecified.copy(alpha = 0.35f))) {
            append(DATE_FORMAT.lowercase().substring(trimmed.length))
        }
    }

    val res = buildAnnotatedString {
        append(filled.subSequence(0, 2))
        append(DATE_SEPARATOR)
        append(filled.subSequence(2, 4))
        append(DATE_SEPARATOR)
        append(filled.subSequence(4, 8))
    }

    TransformedText(res, DateOffsetMapping(trimmed.length))
}

/**
 * The offset translator for dates
 *
 * @see dateTransformBuilder
 */
class DateOffsetMapping(
    override val originalTextLength: Int
): TtcOffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        return when (offset) {
            0 -> 0
            1 -> 1
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 7
            6 -> 8
            7 -> 9
            else -> 10
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        val newOffset = when (offset) {
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 2
            4 -> 3
            5 -> 4
            6 -> 4
            7 -> 5
            8 -> 6
            9 -> 7
            else -> 8
        }
        return super.transformedToOriginal(newOffset)
    }
}

/* ******** PREVIEWS ******** */

//private val previewInputs = listOf(
//    Input(
//        label = "Date of Birth",
//        inputState = InputState(""),
//        inputType = InputType.DATE
//    ),
//    Input(
//        label = "Date of Birth",
//        inputState = InputState("09231977"),
//        inputType = InputType.DATE
//    ),
//    Input(
//        label = "Date of Birth",
//        inputState = InputState(
//            "0923",
//            validateInitialValue = true,
//            validator = { InputValidation.Invalid(ERROR_PARTIAL_DATE) }
//        ),
//        inputType = InputType.DATE
//    )
//)

//@Preview("Standard Date Fields")
//@Composable
//fun PreviewNormalDateFields() {
//    ThemedPreview {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp)
//        ) {
//
//            previewInputs.forEach { input->
//                Spacer(modifier = Modifier.height(16.dp))
//                CdiDateField(
//                    label = input.label,
//                    inputState = input.inputState,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}
//
//@Preview("Standard Date Fields on Primary Background")
//@Composable
//fun PreviewOnPrimaryDateFields() {
//    ThemedPreview {
//        OnPrimaryTheme {
//            CdiSurface {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 8.dp)
//                ) {
//
//                    previewInputs.forEach { input->
//                        Spacer(modifier = Modifier.height(16.dp))
//                        CdiDateField(
//                            label = input.label,
//                            inputState = input.inputState,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
//        }
//    }
//}
//
//@Preview("Filled Date Fields")
//@Composable
//fun PreviewFilledOnPrimaryDateFields() {
//    ThemedPreview {
//        OnPrimaryTheme {
//            CdiSurface {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 8.dp)
//                ) {
//
//                    previewInputs.forEach { input->
//                        Spacer(modifier = Modifier.height(16.dp))
//                        CdiFilledDateField(
//                            label = input.label,
//                            inputState = input.inputState,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
//        }
//    }
//}