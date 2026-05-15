package com.nammakelsa.features.worker_profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nammakelsa.R
import com.nammakelsa.core.ui.components.LoadingScreen
import com.nammakelsa.core.ui.theme.*
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.domain.models.Worker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(viewModel: WorkerProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = {
                    fadeIn(tween(400)) with fadeOut(tween(400))
                },
                label = "ProfileTransition"
            ) { editing ->
                if (editing) {
                    if (uiState.userRole == Constants.ROLE_WORKER && uiState.worker != null) {
                        WorkerEditContent(worker = uiState.worker!!, viewModel = viewModel, onCancel = { isEditing = false })
                    } else {
                        UserEditContent(uiState = uiState, viewModel = viewModel, onCancel = { isEditing = false })
                    }
                } else {
                    if (uiState.userRole == Constants.ROLE_WORKER && uiState.worker != null) {
                        WorkerProfileContent(worker = uiState.worker!!, viewModel = viewModel, onEdit = { isEditing = true })
                    } else {
                        UserProfileContent(uiState = uiState, viewModel = viewModel, onEdit = { isEditing = true })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerEditContent(worker: Worker, viewModel: WorkerProfileViewModel, onCancel: () -> Unit) {
    WorkerForm(
        initialName = worker.name,
        initialPhone = worker.phone,
        initialSkillType = worker.skillType,
        initialDailyRate = worker.dailyRate.toString(),
        initialCity = worker.locationCity,
        initialExperience = worker.experience.toString(),
        initialBio = worker.bio,
        saveProfile = { n, p, s, r, c, e, b, g ->
            viewModel.saveProfile(n, p, s, r, c, e, b, g)
            onCancel()
        },
        isEditing = true,
        onCancel = onCancel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerForm(
    initialName: String, initialPhone: String, initialSkillType: String, initialDailyRate: String,
    initialCity: String, initialExperience: String, initialBio: String,
    saveProfile: (String, String, String, Int, String, Int, String, List<Uri>) -> Unit,
    isEditing: Boolean, onCancel: () -> Unit = {}
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var skillType by remember { mutableStateOf(initialSkillType) }
    var dailyRate by remember { mutableStateOf(initialDailyRate) }
    var city by remember { mutableStateOf(initialCity) }
    var experience by remember { mutableStateOf(initialExperience) }
    var bio by remember { mutableStateOf(initialBio) }
    var skillExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var galleryUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && galleryUris.size < 3) {
            galleryUris = galleryUris + uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Professional Profile" else "Expert Registration", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = onCancel) { Icon(Icons.Default.ArrowBack, null) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Orange500.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Engineering, null, Modifier.size(40.dp), Orange500)
            }
            
            Spacer(Modifier.height(16.dp))
            Text(
                if (isEditing) "Professional Details" else "Join as an Expert",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Showcase your skills to clients nearby",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))

            ProfileTextField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = phone, onValueChange = { phone = it }, label = "Contact Number", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = skillExpanded, onExpandedChange = { skillExpanded = !skillExpanded }) {
                OutlinedTextField(
                    value = skillType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Primary Service Skill") },
                    leadingIcon = { Icon(Icons.Default.Handyman, null, tint = Orange500) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(expanded = skillExpanded, onDismissRequest = { skillExpanded = false }) {
                    Constants.SKILL_TYPES.forEach { skill ->
                        DropdownMenuItem(text = { Text(skill) }, onClick = { skillType = skill; skillExpanded = false })
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileTextField(value = dailyRate, onValueChange = { dailyRate = it }, label = "Daily Rate (₹)", icon = Icons.Default.Payments, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                ProfileTextField(value = experience, onValueChange = { experience = it }, label = "Exp (Yrs)", icon = Icons.Default.Timer, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }
            
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = city, onValueChange = { city = it }, label = "Service Location / City", icon = Icons.Default.LocationCity)
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Professional Bio / Portfolio Summary") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Service Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Upload photos of your past work to build trust", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(galleryUris) { uri ->
                            Box(modifier = Modifier.size(90.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { galleryUris = galleryUris - uri },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.White, CircleShape)
                                ) { Icon(Icons.Default.Close, null, Modifier.size(14.dp), Color.Red) }
                            }
                        }
                        if (galleryUris.size < 3) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Orange500.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .background(Orange500.copy(alpha = 0.05f))
                                        .clickable { photoPicker.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, null, Modifier.size(24.dp), Orange500)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Add Photo", style = MaterialTheme.typography.labelSmall, color = Orange500)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && skillType.isNotBlank()) {
                        isSaving = true
                        saveProfile(name.trim(), phone.trim(), skillType, dailyRate.toIntOrNull() ?: 0, city.trim(), experience.toIntOrNull() ?: 0, bio.trim(), galleryUris)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && phone.isNotBlank() && skillType.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Icon(if (isEditing) Icons.Default.CheckCircle else Icons.Default.AssignmentTurnedIn, null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (isEditing) "Confirm Changes" else "Start Working Now", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Orange500) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerProfileContent(worker: Worker, viewModel: WorkerProfileViewModel, onEdit: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Premium Header
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Brush.verticalGradient(listOf(Orange500, Orange700)))
                )
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
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
                
                Surface(
                    color = Orange500.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        worker.skillType,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Orange500,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStat(label = stringResource(R.string.rating), value = "★ ${worker.rating}")
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    ProfileStat(label = stringResource(R.string.projects), value = "${worker.reviewCount}+")
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    ProfileStat(label = stringResource(R.string.experience), value = "${worker.experience}y")
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Professional Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Orange500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.about_professional), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            worker.bio.ifEmpty { "Expert service provider specialized in ${worker.skillType}. Highly reliable and professional." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            lineHeight = 22.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Details Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailItem(label = "Daily Rate", value = "₹${worker.dailyRate}", icon = Icons.Default.Payments, modifier = Modifier.weight(1f))
                    DetailItem(label = stringResource(R.string.location), value = worker.locationCity, icon = Icons.Default.LocationOn, modifier = Modifier.weight(1f))
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Availability Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.active_status), fontWeight = FontWeight.Bold)
                            Text(if (worker.isAvailable) stringResource(R.string.ready_for_work) else stringResource(R.string.currently_busy), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Switch(
                            checked = worker.isAvailable,
                            onCheckedChange = { viewModel.updateAvailability(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Green500)
                        )
                    }
                }
                
                if (worker.galleryPhotos.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.recent_gallery), modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(worker.galleryPhotos) { photo ->
                            AsyncImage(
                                model = photo,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp, 160.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                SettingsSection(uiState = viewModel.uiState.collectAsState().value, viewModel = viewModel)
                Spacer(Modifier.height(40.dp))
            }
        }
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
private fun DetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = Orange500)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(Orange500.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = Orange500)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value.ifEmpty { "Not provided" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileContent(uiState: WorkerProfileUiState, viewModel: WorkerProfileViewModel, onEdit: () -> Unit) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(listOf(Orange500, Orange700))))
                
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(CircleShape).background(Orange500.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(50.dp), Orange500)
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Text(uiState.userName.ifEmpty { "Guest User" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(uiState.userEmail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Account Details", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(20.dp))
                        ProfileInfoRow(label = "Phone Number", value = uiState.userPhone, icon = Icons.Default.Phone)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
                        ProfileInfoRow(label = "Address", value = uiState.userAddress, icon = Icons.Default.Home)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
                        ProfileInfoRow(label = "Current City", value = uiState.userCity, icon = Icons.Default.LocationCity)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                SettingsSection(uiState = uiState, viewModel = viewModel)
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange500),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Orange500)
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.edit_information), fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = { /* TODO */ }) {
                    Text(stringResource(R.string.switch_professional), color = Color.Gray)
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(uiState: WorkerProfileUiState, viewModel: WorkerProfileViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(stringResource(R.string.settings), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(20.dp))
            
            // Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DarkMode, null, tint = Orange500, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.dark_mode), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(
                    checked = uiState.isDarkMode,
                    onCheckedChange = { checked ->
                        scope.launch { viewModel.toggleDarkMode(checked) }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Orange500, checkedTrackColor = Orange500.copy(alpha = 0.4f))
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))

            // Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Language, null, tint = Orange500, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.language), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)
                ) {
                    // EN button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.currentLanguage != "kn") Orange500 else Color.Transparent)
                            .clickable {
                                if (uiState.currentLanguage != "en") {
                                    scope.launch {
                                        viewModel.changeLanguage("en")
                                        kotlinx.coroutines.delay(150)
                                        context.findActivity()?.recreate()
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            stringResource(R.string.en),
                            color = if (uiState.currentLanguage != "kn") Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    // KN button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.currentLanguage == "kn") Orange500 else Color.Transparent)
                            .clickable {
                                if (uiState.currentLanguage != "kn") {
                                    scope.launch {
                                        viewModel.changeLanguage("kn")
                                        kotlinx.coroutines.delay(150)
                                        context.findActivity()?.recreate()
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            stringResource(R.string.kn),
                            color = if (uiState.currentLanguage == "kn") Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserEditContent(uiState: WorkerProfileUiState, viewModel: WorkerProfileViewModel, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(uiState.userName) }
    var phone by remember { mutableStateOf(uiState.userPhone) }
    var email by remember { mutableStateOf(uiState.userEmail) }
    var address by remember { mutableStateOf(uiState.userAddress) }
    var city by remember { mutableStateOf(uiState.userCity) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            ProfileTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.full_name), icon = Icons.Default.Person)
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = phone, onValueChange = { phone = it }, label = stringResource(R.string.phone_number), icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = email, onValueChange = { email = it }, label = stringResource(R.string.email), icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = address, onValueChange = { address = it }, label = stringResource(R.string.address), icon = Icons.Default.Home)
            Spacer(Modifier.height(16.dp))
            ProfileTextField(value = city, onValueChange = { city = it }, label = stringResource(R.string.city), icon = Icons.Default.LocationCity)
            
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.updateUserInfo(name.trim(), phone.trim(), email.trim(), address.trim(), city.trim())
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.save_profile), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
