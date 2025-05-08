package com.example.expenseit.ui

import SettingsScreen
import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentRoute by remember { mutableStateOf("expense_list") }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "expense_list"

            val newIndex = navigationBarItems.indexOfFirst { it.route == destination.route }
            if (newIndex >= 0) {
                selectedIndex = newIndex
            }
        }
    }

    val hideBottomBarScreens = listOf("add_expense", "add_expense/{expenseId}", "category_list", "receipt_details/{receiptId}", "edit_category")
    val scrollToTop = remember { mutableStateOf(false) }

    Scaffold(
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .noRippleClickable {
                                    if (currentRoute != item.route) {
                                        selectedIndex = item.ordinal
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else {
                                        when (item.route) {
                                            "expense_list" -> {
                                                scrollToTop.value = true
                                            }

                                            "receipt_scan" -> {

                                            }
                                        }
                                    }

                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = item.iconRes),
                                modifier = Modifier.size(26.dp),
                                contentDescription = "Bottom Bar Icon",
                                colorFilter = if (selectedIndex == item.ordinal) {
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
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            composable("expense_list") {
                ExpenseListScreen(
                    navController = navController,
                    modifier = contentModifier,
                    scrollToTop = scrollToTop.value,
                    onScrollToTopCompleted = { scrollToTop.value = false }
                )
            }
            composable(
                route = "receipt_scan",
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) {
                ReceiptListScreen(
                    navController = navController,
                    modifier = contentModifier,
                    scrollToTop = scrollToTop.value,
                    onScrollToTopCompleted = { scrollToTop.value = false }
                )
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
            composable(
                route = "receipt_details/{receiptId}",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(300, easing = LinearEasing))
                },
                exitTransition = {
                    // Slide out to the right when returning to ReceiptScanScreen
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
            ) {
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


