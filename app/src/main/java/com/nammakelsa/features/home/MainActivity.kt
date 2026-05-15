package com.nammakelsa.features.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.ui.theme.NammaKelsaTheme
import com.nammakelsa.core.ui.theme.Orange500
import com.nammakelsa.core.utils.LocaleHelper
import com.nammakelsa.features.bookings.BookingsScreen
import com.nammakelsa.features.chat.ChatListScreen
import com.nammakelsa.features.chat.ChatScreen
import com.nammakelsa.features.reviews.WriteReviewScreen
import com.nammakelsa.features.search.SearchScreen
import com.nammakelsa.features.worker_detail.WorkerDetailScreen
import com.nammakelsa.features.worker_profile.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class NavItem(val route: String, val icon: ImageVector, val label: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var prefs: PreferencesManager

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            try {
                // We need a temporary prefs instance since Hilt hasn't injected yet
                val tempPrefs = PreferencesManager(newBase)
                tempPrefs.getAppLanguage()
            } catch (_: Exception) { "en" }
        }
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkMode by prefs.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            NammaKelsaTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
                MainScreen(prefs = prefs)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(prefs: PreferencesManager) {
    val navController = rememberNavController()
    val items = listOf(
        NavItem("home", Icons.Default.Home, androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.home)),
        NavItem("search", Icons.Default.Search, androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.search)),
        NavItem("bookings", Icons.Default.EventNote, androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.bookings)),
        NavItem("chat", Icons.Default.Chat, androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.chat)),
        NavItem("profile", Icons.Default.Person, androidx.compose.ui.res.stringResource(com.nammakelsa.R.string.profile))
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var currentUserId by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { currentUserId = prefs.getUserId() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Orange500, selectedTextColor = Orange500, indicatorColor = Orange500.copy(alpha = 0.1f))
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    prefs = prefs,
                    currentUserId = currentUserId,
                    onChatClick = { workerId, _ ->
                        if (currentUserId.isNotEmpty()) {
                            val ids = listOf(currentUserId, workerId).sorted()
                            navController.navigate("chat/" + ids[0] + "_" + ids[1])
                        }
                    },
                    onViewClick = { workerId ->
                        if (currentUserId.isNotEmpty()) navController.navigate("worker_detail/" + workerId)
                    }
                )
            }
            composable("search") {
                SearchScreen(
                    onChatClick = { workerId, _ ->
                        if (currentUserId.isNotEmpty()) {
                            val ids = listOf(currentUserId, workerId).sorted()
                            navController.navigate("chat/" + ids[0] + "_" + ids[1])
                        }
                    },
                    onViewClick = { workerId ->
                        if (currentUserId.isNotEmpty()) navController.navigate("worker_detail/" + workerId)
                    }
                )
            }
            composable("bookings") {
                BookingsScreen(onWriteReview = { workerId, bookingId ->
                    navController.navigate("write_review/" + workerId + "/" + bookingId)
                })
            }
            composable("chat") { ChatListScreen(onChatClick = { chatId -> navController.navigate("chat/" + chatId) }) }
            composable("profile") { ProfileScreen() }

            composable("chat/{chatId}", arguments = listOf(navArgument("chatId") { type = NavType.StringType })) {
                ChatScreen(onBack = { navController.popBackStack() })
            }
            composable("worker_detail/{workerId}", arguments = listOf(navArgument("workerId") { type = NavType.StringType })) {
                WorkerDetailScreen(onBack = { navController.popBackStack() })
            }
            composable("write_review/{workerId}/{bookingId}", arguments = listOf(
                navArgument("workerId") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )) {
                WriteReviewScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
