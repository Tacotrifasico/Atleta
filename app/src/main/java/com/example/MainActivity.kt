package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athletic.ui.AthleticViewModel
import com.example.athletic.ui.NavTab
import com.example.athletic.ui.dialogs.CheckInDialog
import com.example.athletic.ui.dialogs.GarminConnectDialog
import com.example.athletic.ui.dialogs.SpontaneousActivityDialog
import com.example.athletic.ui.dialogs.TimeVariantsDialog
import com.example.athletic.ui.dialogs.WhatIfDialog
import com.example.athletic.ui.screens.CoachAndSheetsScreen
import com.example.athletic.ui.screens.ExerciseLibraryScreen
import com.example.athletic.ui.screens.TodayDashboardScreen
import com.example.athletic.ui.screens.TrainingModeScreen
import com.example.athletic.ui.screens.WeekPlannerScreen
import com.example.ui.theme.AthleticOSTheme
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AthleticOSTheme(darkTheme = true) {
                AthleticAppRoot()
            }
        }
    }
}

@Composable
fun AthleticAppRoot(
    viewModel: AthleticViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button: return to TODAY if on other tab
    BackHandler(enabled = state.currentTab != NavTab.TODAY) {
        viewModel.setTab(NavTab.TODAY)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = state.currentTab == NavTab.TODAY,
                    onClick = { viewModel.setTab(NavTab.TODAY) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Hoy", modifier = Modifier.size(20.dp)) },
                    label = { Text("Hoy", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AthleticOrange,
                        selectedTextColor = AthleticOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = state.currentTab == NavTab.WEEK,
                    onClick = { viewModel.setTab(NavTab.WEEK) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Semana", modifier = Modifier.size(20.dp)) },
                    label = { Text("Semana", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AthleticOrange,
                        selectedTextColor = AthleticOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_week")
                )

                NavigationBarItem(
                    selected = state.currentTab == NavTab.WORKOUT,
                    onClick = { viewModel.setTab(NavTab.WORKOUT) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Rutina Notion", modifier = Modifier.size(20.dp)) },
                    label = { Text("Notion", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AthleticOrange,
                        selectedTextColor = AthleticOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_workout")
                )

                NavigationBarItem(
                    selected = state.currentTab == NavTab.COACH,
                    onClick = { viewModel.setTab(NavTab.COACH) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Coach & Sheets", modifier = Modifier.size(20.dp)) },
                    label = { Text("Coach", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AthleticOrange,
                        selectedTextColor = AthleticOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_coach")
                )

                NavigationBarItem(
                    selected = state.currentTab == NavTab.LIBRARY,
                    onClick = { viewModel.setTab(NavTab.LIBRARY) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Biblioteca", modifier = Modifier.size(20.dp)) },
                    label = { Text("Biblioteca", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AthleticOrange,
                        selectedTextColor = AthleticOrange,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_library")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                NavTab.TODAY -> TodayDashboardScreen(state = state, viewModel = viewModel)
                NavTab.WEEK -> WeekPlannerScreen(state = state, viewModel = viewModel)
                NavTab.WORKOUT -> TrainingModeScreen(state = state, viewModel = viewModel)
                NavTab.COACH -> CoachAndSheetsScreen(state = state, viewModel = viewModel)
                NavTab.LIBRARY -> ExerciseLibraryScreen(state = state, viewModel = viewModel)
            }

            // Global Dialogs
            SpontaneousActivityDialog(state = state, viewModel = viewModel)
            WhatIfDialog(state = state, viewModel = viewModel)
            TimeVariantsDialog(state = state, viewModel = viewModel)
            CheckInDialog(state = state, viewModel = viewModel)
            GarminConnectDialog(state = state, viewModel = viewModel)
        }
    }
}
