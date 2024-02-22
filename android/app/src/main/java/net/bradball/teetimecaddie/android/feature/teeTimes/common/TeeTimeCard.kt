package net.bradball.teetimecaddie.android.feature.teeTimes.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.previewTeeTime

@Composable
fun TeeTimeCard(teeTime: TeeTime, onClick: ()->Unit, modifier: Modifier = Modifier) {

    val month = teeTime.dateTime.month.name.take(3)
    val day = teeTime.dateTime.dayOfMonth

    ElevatedCard {
        Row(
            modifier = modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$month\n$day",
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(teeTime.course, style = MaterialTheme.typography.titleMedium)
//                    Text(teeTime.time, style = MaterialTheme.typography.titleSmall)
            }
//                Column(
//                    modifier = Modifier.padding(horizontal = 8.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("16", style = MaterialTheme.typography.titleMedium)
//                    Text("Players", style = MaterialTheme.typography.labelSmall)
//                }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TeeTimeCardPreview() {
    MyApplicationTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            TeeTimeCard(previewTeeTime, {}, Modifier.fillMaxWidth())
        }
    }
}