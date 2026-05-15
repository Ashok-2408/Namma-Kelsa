package com.nammakelsa.features.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.nammakelsa.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.ui.components.LoadingScreen
import com.nammakelsa.core.ui.components.WorkerCard
import com.nammakelsa.core.ui.theme.*
import com.nammakelsa.core.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prefs: PreferencesManager,
    currentUserId: String = "",
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onViewClick: (String) -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Orange500, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, null, Modifier.size(20.dp), Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.app_name), 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Orange500,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, null, tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.workers.isEmpty()) {
            LoadingScreen()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // User Info Card
                if (uiState.userName.isNotEmpty()) {
                    item {
                        AnimatedGreeting(uiState.userName, uiState.userCity)
                    }
                }

                // Search Bar
                item {
                    SearchBar(searchQuery) {
                        searchQuery = it
                        viewModel.searchWorkers(it)
                    }
                }

                // Hero Banner
                item {
                    HeroBanner { viewModel.searchWorkers(it) }
                }

                // Popular Skills
                item {
                    SkillsSection { viewModel.searchWorkers(it) }
                }

                // Workers List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.available_workers_count, uiState.workers.size), 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Workers List
                if (uiState.workers.isEmpty()) {
                    item {
                        EmptyWorkersState()
                    }
                } else {
                    items(uiState.workers) { worker ->
                        WorkerCard(
                            worker = worker, 
                            onViewClick = { onViewClick(worker.uid) }, 
                            onCallClick = {
                                val phone = worker.phone.ifEmpty { "9876543210" }
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }, 
                            onChatClick = { onChatClick(worker.uid, worker.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedGreeting(name: String, city: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(com.nammakelsa.R.string.hello_with_name, name), 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    stringResource(com.nammakelsa.R.string.find_workers_in_city, city), 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_service_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Orange500) },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun HeroBanner(onSkillSelect: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Orange500, Orange700)))
                .padding(20.dp)
        ) {
            Column {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        " ${stringResource(R.string.new_service)} ", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.home_maintenance_title), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.White,
                    lineHeight = 28.sp
                )
                Text(
                    stringResource(R.string.book_expert_desc), 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onSkillSelect("Plumber") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Orange500),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Plumbers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSkillSelect("Electrician") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Electricians", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(
                Icons.Default.Engineering, 
                null, 
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp),
                tint = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun SkillsSection(onSkillSelect: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = { /* See all */ }) {
                Text(stringResource(R.string.see_all), color = Orange500, fontWeight = FontWeight.Bold)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(Constants.SKILL_TYPES.toList()) { skill ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSkillSelect(skill) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Orange500.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when(skill) {
                                "Plumber" -> Icons.Default.WaterDrop
                                "Electrician" -> Icons.Default.Lightbulb
                                "Carpenter" -> Icons.Default.Construction
                                "Painter" -> Icons.Default.FormatPaint
                                "Cleaner" -> Icons.Default.CleaningServices
                                else -> Icons.Default.Handyman
                            },
                            null,
                            tint = Orange500
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(skill, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun EmptyWorkersState() {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(40.dp).fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.SearchOff, null, Modifier.size(64.dp), Color.Gray.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.no_workers_found), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.try_another_skill), textAlign = TextAlign.Center, color = Color.Gray)
        }
    }
}
