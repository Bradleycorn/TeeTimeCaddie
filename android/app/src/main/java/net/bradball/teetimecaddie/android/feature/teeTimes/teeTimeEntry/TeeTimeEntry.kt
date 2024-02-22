package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeEntry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.datetime.LocalDate
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.TtcDatePicker
import net.bradball.teetimecaddie.android.ui.common.buttons.LoadingButton
import net.bradball.teetimecaddie.android.ui.common.forms.DateTextField
import net.bradball.teetimecaddie.android.ui.common.forms.InputSpacer
import net.bradball.teetimecaddie.android.ui.common.forms.OutlinedPasswordField
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.selectedDate
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun TeeTimeEntryRoute(
    onTeeTimeAdded: ()->Unit = {},
    viewModel: TeeTimeEntryViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    LaunchedEffect(viewModel.teeTimeAdded) {
        if (viewModel.teeTimeAdded) {
            onTeeTimeAdded()
        }
    }

    TeeTimeEntryScreen(
        showLoadingSpinner = viewModel.showLoadingProgress,
        onSubmitClicked = viewModel::createTeeTime
    )
}

@Composable
fun TeeTimeEntryScreen(showLoadingSpinner: Boolean = false, onSubmitClicked: (String, LocalDate)->Unit = { _, _->}, onInputChanged: ()->Unit = {}) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var courseName by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val date = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(stringResource(TTR.strings.add_tee_time.resourceId), style = MaterialTheme.typography.titleLarge)
        
        OutlinedTextField(
            label = { Text(stringResource(TTR.strings.field_label_course_name.resourceId)) },
            value = courseName,
            onValueChange = { value ->
                courseName = value
                onInputChanged()
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        InputSpacer()

        TtcDatePicker(pickerState = date, pickerDialogTitle = "Tee Time Date")

//        DateTextField(
//            label = stringResource(TTR.strings.field_label_Date.resourceId),
//            value = date.text,
//            onValueChange = { value ->
//                date = TextFieldValue(value)
//                onInputChanged()
//            },
//            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
//            trailingContent = { IconButton(onClick = { /*TODO*/ }) {
//                Icon(imageVector = TtcIcons.Calendar, contentDescription = "Select Date")
//            } },
//            modifier = Modifier.fillMaxWidth()
//        )

        InputSpacer()

        LoadingButton(
            text = stringResource(TTR.strings.button_create.resourceId),
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                // selectedDate should always have a value, because the button
                // is disabled if selectedDate is null.
                date.selectedDate?.let { selectedDate ->
                    onSubmitClicked(courseName.text, selectedDate)
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = courseName.text.isNotBlank() && date.selectedDate != null,
            isLoading = showLoadingSpinner
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTeeTimeEntryScreen() {
    MyApplicationTheme {
        TeeTimeEntryScreen()
    }
}