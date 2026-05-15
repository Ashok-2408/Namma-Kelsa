package com.nammakelsa.features.worker_detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.nammakelsa.R
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nammakelsa.core.ui.components.LoadingScreen
import com.nammakelsa.core.ui.theme.*
import com.nammakelsa.domain.models.Review
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    onBack: () -> Unit,
    viewModel: WorkerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showBookingSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.booking_request_sent))
            viewModel.resetBookingSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.worker != null) {
                Surface(
                    modifier = Modifier.shadow(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.estimated_price), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₹${uiState.worker?.dailyRate}${stringResource(R.string.per_day)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Orange500)
                        }
                        Button(
                            onClick = { showBookingSheet = true },
                            modifier = Modifier.weight(1.5f).height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                        ) {
                            Icon(Icons.Default.Bolt, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.book_now), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else if (uiState.worker == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.worker_not_found), color = Color.Gray)
            }
        } else {
            val worker = uiState.worker!!
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 100.dp)
            ) {
                // Premium Header with Profile Photo
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Brush.verticalGradient(listOf(Orange500, Orange700)))
                    )
                    
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(12.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = worker.profilePhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(worker.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        if (worker.isVerified) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Verified, "Verified", Modifier.size(24.dp), Color(0xFF1E88E5))
                        }
                    }
                    
                    Text(
                        worker.skillType,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Orange500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStat(label = stringResource(R.string.rating), value = "★ ${worker.rating}")
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        ProfileStat(label = stringResource(R.string.experience), value = stringResource(R.string.years_abbr, worker.experience))
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        ProfileStat(label = stringResource(R.string.location), value = worker.locationCity.ifEmpty { "Local" })
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // About Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.about_professional), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            worker.bio.ifEmpty { "Professional service provider specialized in ${worker.skillType}. Known for quality work and punctuality." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            lineHeight = 22.sp
                        )
                    }

                    if (worker.galleryPhotos.isNotEmpty()) {
                        Spacer(Modifier.height(32.dp))
                        Text(stringResource(R.string.work_portfolio), modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(worker.galleryPhotos) { photo ->
                                AsyncImage(
                                    model = photo,
                                    contentDescription = null,
                                    modifier = Modifier.size(140.dp, 100.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    // Reviews Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.reviews_count, uiState.reviews.size), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.see_all), color = Orange500, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    if (uiState.reviews.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Box(modifier = Modifier.padding(40.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_reviews_yet), color = Color.Gray)
                            }
                        }
                    } else {
                        uiState.reviews.forEach { review ->
                            ReviewCard(review = review)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    if (showBookingSheet && uiState.worker != null) {
        BookingDialog(
            worker = uiState.worker!!,
            onDismiss = { showBookingSheet = false },
            onBook = { desc, addr, date, time, rate ->
                viewModel.createBooking(desc, addr, date, time, rate)
                showBookingSheet = false
            }
        )
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(Orange500.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, Modifier.size(18.dp), Orange500)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.fromUserName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(dateFormat.format(Date(review.createdAt)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Row {
                    repeat(5) { i ->
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), if (i < review.rating.toInt()) Yellow500 else Color.LightGray)
                    }
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (review.workerReply != null) {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text(stringResource(R.string.reply_label, review.workerReply), style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDialog(
    worker: com.nammakelsa.domain.models.Worker,
    onDismiss: () -> Unit,
    onBook: (String, String, Long, String, Int) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var agreedRate by remember { mutableStateOf(worker.dailyRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && address.isNotBlank()) {
                        val dateParts = scheduledDate.split("/")
                        val cal = Calendar.getInstance()
                        if (dateParts.size == 3) {
                            cal.set(dateParts[2].toIntOrNull() ?: 2026, (dateParts[1].toIntOrNull() ?: 1) - 1, dateParts[0].toIntOrNull() ?: 1)
                        }
                        onBook(description, address, cal.timeInMillis, scheduledTime, agreedRate.toIntOrNull() ?: worker.dailyRate)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                enabled = description.isNotBlank() && address.isNotBlank()
            ) { Text(stringResource(R.string.confirm_booking), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.go_back), color = Color.Gray) }
        },
        title = { Text(stringResource(R.string.book_service), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text(stringResource(R.string.what_needs_done)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                )
                OutlinedTextField(
                    value = address, 
                    onValueChange = { address = it }, 
                    label = { Text(stringResource(R.string.service_address)) }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = scheduledDate, 
                        onValueChange = { scheduledDate = it }, 
                        label = { Text(stringResource(R.string.date_label)) }, 
                        placeholder = { Text("DD/MM") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
                    )
                    OutlinedTextField(
                        value = agreedRate, 
                        onValueChange = { agreedRate = it }, 
                        label = { Text(stringResource(R.string.rate_label)) }, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
