package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.networking.ErrorType

@Composable
fun ErrorView(
    errorType: ErrorType,
    message: String,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Map properties based on error type
    val icon = when (errorType) {
        ErrorType.Timeout -> Icons.Default.Timer
        ErrorType.Network -> Icons.Default.WifiOff
        else -> Icons.Default.Warning
    }

    val iconColor = when (errorType) {
        ErrorType.Timeout, ErrorType.Network -> Color(0xFFFFA500)
        else -> MaterialTheme.colorScheme.error
    }

    val title = when (errorType) {
        ErrorType.Timeout -> MR.strings.error_timeout_title
        ErrorType.Network -> MR.strings.error_network_title
        else -> MR.strings.error_generic_title
    }

    val detailMessage = if (errorType == ErrorType.Timeout) {
        mokoString(MR.strings.error_timeout_description)
    } else {
        message
    }

    // 2. Build the Layout
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = mokoString(title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = detailMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Button Group
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(top = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorType == ErrorType.Timeout) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.error_timeout_configure_instance))
                }
            }

            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(mokoString(MR.strings.retry))
            }
        }

        if (errorType == ErrorType.Timeout) {
            Text(
                text = mokoString(MR.strings.error_timeout_tip,  mokoString(MR.strings.slow_instance)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}