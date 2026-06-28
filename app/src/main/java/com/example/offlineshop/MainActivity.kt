package com.example.offlineshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.offlineshop.ui.InventoryScreen
import com.example.offlineshop.ui.InventoryViewModel
import com.example.offlineshop.ui.SalesHistoryScreen
import com.example.offlineshop.ui.SellScreen

sealed class Screen(val route: String, val label: String) {
    data object Sell : Screen("sell", "Sell")
    data object Inventory : Screen("inventory", "Inventory")
    data object History : Screen("history", "History")
}

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OfflineShopApp(viewModel)
            }
        }
    }
}

@Composable
fun OfflineShopApp(viewModel: InventoryViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Sell, Screen.Inventory, Screen.History)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val icon = when (screen) {
                        Screen.Sell -> Icons.Default.ShoppingCart
                        Screen.Inventory -> Icons.Default.Inventory
                        Screen.History -> Icons.Default.History
                    }
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sell.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Sell.route) { SellScreen(viewModel) }
            composable(Screen.Inventory.route) { InventoryScreen(viewModel) }
            composable(Screen.History.route) { SalesHistoryScreen(viewModel) }
        }
    }
}
