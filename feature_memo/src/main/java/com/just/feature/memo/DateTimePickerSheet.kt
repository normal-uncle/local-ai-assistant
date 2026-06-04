package com.just.feature.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerSheet(
    initial: Instant,
    onConfirm: (Instant) -> Unit,
    onDismiss: () -> Unit,
) {
    val zoneId = ZoneId.systemDefault()
    val initialLocal = LocalDateTime.ofInstant(initial, zoneId)
    val initialMillis = initial.toEpochMilli()

    val dateState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
        )
    val timeState =
        rememberTimePickerState(
            initialHour = initialLocal.hour,
            initialMinute = initialLocal.minute,
            is24Hour = true,
        )
    var showingDate by remember { mutableStateOf(true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.memo_datetime_picker_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))

            if (showingDate) {
                DatePicker(state = dateState)
            } else {
                TimePicker(state = timeState)
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showingDate = !showingDate }) {
                    Text(
                        if (showingDate) {
                            stringResource(R.string.memo_datetime_to_time)
                        } else {
                            stringResource(R.string.memo_datetime_to_date)
                        },
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.memo_datetime_cancel))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val dateMillis = dateState.selectedDateMillis ?: initialMillis
                        val pickedDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
                        val pickedDateTime =
                            pickedDate
                                .atTime(timeState.hour, timeState.minute)
                                .atZone(zoneId)
                                .toInstant()
                        onConfirm(pickedDateTime)
                    },
                ) {
                    Text(stringResource(R.string.memo_datetime_confirm))
                }
            }
        }
    }
}
