package com.nammakelsa.features.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nammakelsa.core.ui.theme.Orange500
import com.nammakelsa.core.ui.theme.Orange700
import com.nammakelsa.core.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    currentLanguage: String,
    onSaveAndContinue: (name: String, phone: String, email: String, address: String, city: String, isWorker: Boolean, skillType: String, dailyRate: Int, experience: Int, bio: String, galleryUris: List<Uri>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var isWorker by remember { mutableStateOf(false) }
    var skillType by remember { mutableStateOf("") }
    var skillExpanded by remember { mutableStateOf(false) }
    var dailyRate by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var galleryUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && galleryUris.size < 3) {
            galleryUris = galleryUris + uri
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Orange500, Orange700)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountCircle, null, Modifier.size(50.dp), tint = Color.White)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Create Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Help us personalize your experience",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PremiumOnboardingTextField(value = name, onValueChange = { name = it }, label = "Full Name *", icon = Icons.Default.Person)
                    PremiumOnboardingTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number *", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                    PremiumOnboardingTextField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                    PremiumOnboardingTextField(value = address, onValueChange = { address = it }, label = "Full Address", icon = Icons.Default.Home)
                    PremiumOnboardingTextField(value = city, onValueChange = { city = it }, label = "City", icon = Icons.Default.LocationCity)

                    Spacer(Modifier.height(8.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.1f))
                    Spacer(Modifier.height(8.dp))

                    Text("Register as a Professional?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WorkerTypeChip(
                            label = "User",
                            isSelected = !isWorker,
                            onClick = { isWorker = false },
                            modifier = Modifier.weight(1f)
                        )
                        WorkerTypeChip(
                            label = "Expert",
                            isSelected = isWorker,
                            onClick = { isWorker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(
                        visible = isWorker,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Spacer(Modifier.height(16.dp))
                            Text("Expert Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Orange500)
                            
                            ExposedDropdownMenuBox(expanded = skillExpanded, onExpandedChange = { skillExpanded = !skillExpanded }) {
                                OutlinedTextField(
                                    value = skillType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Service Type *") },
                                    leadingIcon = { Icon(Icons.Default.Build, null, tint = Orange500) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Orange500,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    )
                                )
                                ExposedDropdownMenu(expanded = skillExpanded, onDismissRequest = { skillExpanded = false }) {
                                    Constants.SKILL_TYPES.forEach { skill ->
                                        DropdownMenuItem(text = { Text(skill) }, onClick = { skillType = skill; skillExpanded = false })
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                PremiumOnboardingTextField(value = dailyRate, onValueChange = { dailyRate = it }, label = "Rate (₹)", icon = Icons.Default.Payments, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                PremiumOnboardingTextField(value = experience, onValueChange = { experience = it }, label = "Exp (yrs)", icon = Icons.Default.Timer, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                            }

                            PremiumOnboardingTextField(value = bio, onValueChange = { bio = it }, label = "Brief Portfolio / Bio", icon = Icons.Default.Info, maxLines = 3)

                            Column {
                                Text("Portfolio Gallery", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(galleryUris) { uri ->
                                        Box(modifier = Modifier.size(80.dp)) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            IconButton(
                                                onClick = { galleryUris = galleryUris - uri },
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).background(Color.White, CircleShape)
                                            ) { Icon(Icons.Default.Close, null, Modifier.size(12.dp), Color.Red) }
                                        }
                                    }
                                    if (galleryUris.size < 3) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(1.dp, Orange500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .background(Orange500.copy(alpha = 0.05f))
                                                    .clickable { photoPicker.launch("image/*") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.AddAPhoto, null, tint = Orange500)
                                            }
                                        }
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
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSaveAndContinue(name.trim(), phone.trim(), email.trim(), address.trim(), city.trim(), isWorker, skillType, dailyRate.toIntOrNull() ?: 0, experience.toIntOrNull() ?: 0, bio.trim(), galleryUris)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                enabled = name.isNotBlank() && phone.isNotBlank() && (!isWorker || skillType.isNotBlank()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Orange500,
                    disabledContainerColor = Color.White.copy(alpha = 0.4f),
                    disabledContentColor = Orange500.copy(alpha = 0.6f)
                )
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.ArrowForward, null)
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun PremiumOnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Orange500, modifier = Modifier.size(20.dp)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = maxLines == 1,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange500,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.2f),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Orange500,
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Composable
private fun WorkerTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) Orange500 else Color.Gray.copy(alpha = 0.05f),
        contentColor = if (isSelected) Color.White else Color.Gray
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}
