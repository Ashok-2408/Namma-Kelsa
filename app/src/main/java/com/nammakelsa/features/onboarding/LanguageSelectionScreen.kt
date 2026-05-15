package com.nammakelsa.features.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nammakelsa.core.ui.theme.Orange500
import com.nammakelsa.core.ui.theme.Orange700
import com.nammakelsa.core.utils.LocaleHelper

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Orange500, Orange700)))
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Language,
                        null,
                        Modifier.size(50.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "ನಮ್ಮ ಕೆಲಸ",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Select your preferred language to get started with local services.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(56.dp))

                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = slideInHorizontally(tween(600, delayMillis = 200)) { -it } + fadeIn(tween(600, delayMillis = 200))
                ) {
                    LanguageCard(
                        title = "English",
                        subtitle = "Continue in English",
                        isSelected = selectedLanguage == LocaleHelper.LANG_ENGLISH,
                        onClick = { selectedLanguage = LocaleHelper.LANG_ENGLISH }
                    )
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = slideInHorizontally(tween(600, delayMillis = 400)) { it } + fadeIn(tween(600, delayMillis = 400))
                ) {
                    LanguageCard(
                        title = "ಕನ್ನಡ",
                        subtitle = "ಕನ್ನಡದಲ್ಲಿ ಮುಂದುವರೆಯಿರಿ",
                        isSelected = selectedLanguage == LocaleHelper.LANG_KANNADA,
                        onClick = { selectedLanguage = LocaleHelper.LANG_KANNADA }
                    )
                }

                Spacer(Modifier.height(56.dp))

                Button(
                    onClick = {
                        selectedLanguage?.let { onLanguageSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    enabled = selectedLanguage != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Orange500,
                        disabledContainerColor = Color.White.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        if (selectedLanguage == LocaleHelper.LANG_KANNADA) "ಮುಂದುವರೆಯಿರಿ" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    Modifier.size(32.dp),
                    tint = Orange500
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(2.dp, Color.LightGray, CircleShape)
                )
            }
        }
    }
}
