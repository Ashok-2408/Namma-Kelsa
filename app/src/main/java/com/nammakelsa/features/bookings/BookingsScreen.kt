package com.nammakelsa.features.bookings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.nammakelsa.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.nammakelsa.core.ui.theme.*
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.domain.models.Booking
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onWriteReview: (String, String) -> Unit = { _, _ -> },
    viewModel: BookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBookings() }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.my_bookings), fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            ) 
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator(color = Orange500) 
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), Color.Red.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.no_bookings), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(uiState.error ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.loadBookings() }, shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.retry)) }
                }
            }
        } else if (uiState.bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventNote, null, Modifier.size(80.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.no_bookings), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(uiState.bookings) { booking ->
                    BookingCard(
                        booking = booking,
                        isCustomer = uiState.userRole != Constants.ROLE_WORKER,
                        onAccept = { viewModel.updateStatus(booking.id, Constants.BOOKING_ACCEPTED) },
                        onReject = { viewModel.updateStatus(booking.id, Constants.BOOKING_REJECTED) },
                        onComplete = { viewModel.updateStatus(booking.id, Constants.BOOKING_COMPLETED) },
                        onWriteReview = { onWriteReview(booking.workerId, booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, isCustomer: Boolean, onAccept: () -> Unit, onReject: () -> Unit, onComplete: () -> Unit, onWriteReview: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val statusColor = when (booking.status) {
        Constants.BOOKING_PENDING -> Yellow500
        Constants.BOOKING_ACCEPTED -> Green500
        Constants.BOOKING_COMPLETED -> Blue500
        Constants.BOOKING_REJECTED -> Red500
        Constants.BOOKING_CANCELLED -> Red500
        else -> Color.Gray
    }
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = statusColor.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isCustomer) booking.workerName else booking.customerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            booking.skillType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Orange500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            when(booking.status) {
                                Constants.BOOKING_PENDING -> stringResource(R.string.status_pending)
                                Constants.BOOKING_ACCEPTED -> stringResource(R.string.status_accepted)
                                Constants.BOOKING_COMPLETED -> stringResource(R.string.status_completed)
                                Constants.BOOKING_REJECTED -> stringResource(R.string.status_rejected)
                                Constants.BOOKING_CANCELLED -> stringResource(R.string.status_cancelled)
                                else -> booking.status.uppercase()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.scheduled_date), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Text(
                            if (booking.scheduledDate > 0) dateFormat.format(Date(booking.scheduledDate)) else stringResource(R.string.not_set),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, null, Modifier.size(14.dp), Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.total_amount), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Text("₹${booking.agreedRate}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Orange500)
                    }
                }
                
                if (booking.address.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(booking.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // Actions
                Spacer(Modifier.height(20.dp))
                
                if (booking.status == Constants.BOOKING_PENDING && !isCustomer) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(45.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)
                        ) { Text(stringResource(R.string.reject)) }
                        
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(45.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) { Text(stringResource(R.string.accept)) }
                    }
                } else if (booking.status == Constants.BOOKING_ACCEPTED) {
                    // This is where "Start Now" or "Work in Progress" comes
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { /* Could be used for navigation to maps or tracking */ },
                            modifier = Modifier.weight(1f).height(45.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.start_now), fontWeight = FontWeight.Bold)
                        }
                        
                        if (!isCustomer) {
                            Button(
                                onClick = onComplete,
                                modifier = Modifier.weight(1f).height(45.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                            ) { Text(stringResource(R.string.complete_btn)) }
                        }
                    }
                } else if (booking.status == Constants.BOOKING_COMPLETED && isCustomer) {
                    Button(
                        onClick = onWriteReview,
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.rate_experience), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
