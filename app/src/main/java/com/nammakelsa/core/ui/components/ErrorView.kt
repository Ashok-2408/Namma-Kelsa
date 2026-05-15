package com.nammakelsa.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ErrorType {
    NETWORK,
    SERVER,
    UNKNOWN
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.UNKNOWN,
    title: String? = null
) {
    val (icon, defaultTitle) = when (errorType) {
        ErrorType.NETWORK -> Icons.Default.WifiOff to "No Internet Connection"
        ErrorType.SERVER -> Icons.Default.CloudOff to "Server Error"
        ErrorType.UNKNOWN -> Icons.Default.Error to "Something Went Wrong"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = defaultIcon(icon),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title ?: defaultTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun defaultIcon(icon: ImageVector): ImageVector = icon

@Composable
fun NetworkErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorView(
        message = "Please check your internet connection and try again.",
        onRetry = onRetry,
        modifier = modifier,
        errorType = ErrorType.NETWORK
    )
}

@Composable
fun ServerErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorView(
        message = "We're having trouble connecting to our servers. Please try again later.",
        onRetry = onRetry,
        modifier = modifier,
        errorType = ErrorType.SERVER
    )
}
