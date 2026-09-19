package com.recipehub.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.recipehub.app.ui.detail.RecipeDetailScreen
import com.recipehub.app.ui.edit.RecipeEditScreen
import com.recipehub.app.ui.list.RecipeListScreen
import com.recipehub.app.ui.saving.SavingScreen
import com.recipehub.app.ui.theme.RecipeHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // An Activity-level property survives onNewIntent (unlike Composable-local state), which is
    // what lets a second share reach the NavHost while the app is already open (singleTask).
    private val sharedUrlState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedUrlState.value = intent.getStringExtra(ShareReceiverActivity.EXTRA_SHARED_URL)

        setContent {
            RecipeHubTheme {
                RecipeHubNavHost(sharedUrlState = sharedUrlState, onFinish = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUrlState.value = intent.getStringExtra(ShareReceiverActivity.EXTRA_SHARED_URL)
    }
}

@Composable
private fun RecipeHubNavHost(sharedUrlState: MutableState<String?>, onFinish: () -> Unit) {
    val navController = rememberNavController()
    val sharedUrl = sharedUrlState.value

    LaunchedEffect(sharedUrl) {
        sharedUrl?.let { url ->
            navController.navigate("saving/${Uri.encode(url)}")
            sharedUrlState.value = null
        }
    }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            RecipeListScreen(
                onRecipeClick = { localId -> navController.navigate("detail/$localId") },
                onEditRecipe = { localId -> navController.navigate("edit/$localId") },
            )
        }
        composable(
            route = "saving/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url").orEmpty())
            SavingScreen(
                url = url,
                onSaved = onFinish,
                onCancel = { navController.popBackStack("list", inclusive = false) },
            )
        }
        composable(
            route = "detail/{localId}",
            arguments = listOf(navArgument("localId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val localId = backStackEntry.arguments?.getLong("localId") ?: return@composable
            RecipeDetailScreen(
                onEdit = { navController.navigate("edit/$localId") },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "edit/{localId}",
            arguments = listOf(navArgument("localId") { type = NavType.LongType }),
        ) {
            RecipeEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack("list", inclusive = false) },
            )
        }
    }
}
