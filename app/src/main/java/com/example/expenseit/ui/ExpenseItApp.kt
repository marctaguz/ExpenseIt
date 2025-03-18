package com.example.expenseit.ui

import SettingsScreen
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expenseit.R
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.exyte.animatednavbar.utils.noRippleClickable

//https://github.com/exyte/AndroidAnimatedNavigationBar -> Animated Navigation Bar

enum class NavigationBarItems(val iconRes: Int, val route: String) {
    ExpenseList(R.drawable.ic_home,"expense_list"),
    ReceiptScan(R.drawable.ic_documentscanner, "receipt_scan"),
    ExpenseStats(R.drawable.ic_stats, "expense_stats"),
    Settings(R.drawable.ic_settings, "settings")
}

@Composable
fun ExpenseItApp() {
    val navController = rememberNavController()
    val navigationBarItems = remember { NavigationBarItems.entries.toTypedArray() }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf("expense_list") } // Track current route

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "expense_list"

            // Get the index of the destination in the navigationBarItems list
            val newIndex = navigationBarItems.indexOfFirst { it.route == destination.route }
            if (newIndex >= 0) {
                selectedIndex = if (newIndex >= 2) newIndex + 1 else newIndex
            }
        }
    }

    val hideBottomBarScreens = listOf("add_expense", "add_expense/{expenseId}", "category_list", "receipt_details/{receiptId}", "edit_category")

    Scaffold(
        floatingActionButton = {
            if (currentRoute !in hideBottomBarScreens) {  // Hide FAB on specific screens
                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_expense") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
//                        .align(Alignment.Center)
                        .size(70.dp)
                        .offset(y = 70.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Expense")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (currentRoute !in hideBottomBarScreens) { // Hide Bottom Bar on specific screens
                AnimatedNavigationBar(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(64.dp),
                    selectedIndex = selectedIndex,
                    cornerRadius = shapeCornerRadius(cornerRadius = 34.dp),
                    ballAnimation = Parabolic(tween(300)),
                    indentAnimation = Height(tween(300)),
                    barColor = MaterialTheme.colorScheme.primary,
                    ballColor = MaterialTheme.colorScheme.primary,
                ) {
                    navigationBarItems.forEachIndexed { index, item ->
                        if (index == 2) {
                            //Spacer for FAB
                            Spacer(Modifier.width(55.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .noRippleClickable {
                                    selectedIndex = item.ordinal
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = item.iconRes),
                                modifier = Modifier.size(26.dp),
                                contentDescription = "Bottom Bar Icon",
                                colorFilter = if (selectedIndex <= 2 && selectedIndex == item.ordinal || selectedIndex > 2 && selectedIndex - 1 == item.ordinal) {
                                    // Change color for selected icon
                                    ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    // Change color for unselected icon
                                    ColorFilter.tint(MaterialTheme.colorScheme.inversePrimary)
                                }
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .padding(all = 12.dp)

        NavHost(
            navController = navController,
            startDestination = "expense_list",
            Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            composable("expense_list") {
                ExpenseListScreen(navController = navController, modifier = contentModifier)
            }
            composable("receipt_scan") {
                ReceiptScanScreen(navController = navController, modifier = contentModifier)
            }
            composable("expense_stats") {
                ExpenseStatsScreen(navController = navController, modifier = contentModifier)
            }
            composable("settings") {
                SettingsScreen(navController = navController, modifier = contentModifier)
            }
            composable("add_expense") {
                ExpenseFormScreen(navController = navController, expenseId = null)
            }
            composable("add_expense/{expenseId}") { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId")
                ExpenseFormScreen(navController = navController, expenseId = expenseId)
            }
            composable("category_list") {
                CategoryListScreen(navController = navController)
            }
            composable("receipt_details/{receiptId}") {
                val receiptId = it.arguments?.getString("receiptId")?.toIntOrNull()
                if (receiptId != null) {
                    ReceiptDetailsScreen(navController = navController, receiptId = receiptId)
                } else {
                    Log.d("ExpenseItApp", "ReceiptId is null")
                }
            }
            composable("edit_category") {
                EditCategoriesScreen(navController = navController)
            }
        }
    }
}


