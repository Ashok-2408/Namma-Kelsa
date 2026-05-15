package com.nammakelsa.features.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nammakelsa.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.nammakelsa.core.ui.theme.Orange500
import com.nammakelsa.core.ui.theme.Yellow500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    onBack: () -> Unit,
    viewModel: WriteReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.write_review), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.success) {
                Spacer(Modifier.height(48.dp))
                Icon(Icons.Default.Star, null, Modifier.size(80.dp), Yellow500)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.thank_you), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.review_submitted), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Orange500)) { Text(stringResource(R.string.done_btn)) }
                return@Column
            }

            Text(stringResource(R.string.rate_experience), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { i ->
                    IconButton(onClick = { viewModel.updateRating(i + 1) }) {
                        Icon(
                            Icons.Default.Star, null, Modifier.size(40.dp),
                            tint = if (i < uiState.rating) Yellow500 else Color.LightGray
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = { viewModel.updateComment(it) },
                label = { Text(stringResource(R.string.write_review_hint)) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.submitReview() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSubmitting && uiState.rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.submit_review), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
