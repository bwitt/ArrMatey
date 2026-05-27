package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocals to provide navigation components throughout the UI tree.
 */

val LocalNavigationManager = staticCompositionLocalOf<NavigationManager> {
    error("No NavigationManager provided")
}

val LocalArrNavigator = staticCompositionLocalOf<Navigator<ArrScreen>> {
    error("No ArrNavigator provided")
}

val LocalSeerrNavigator = staticCompositionLocalOf<Navigator<SeerrScreen>> {
    error("No SeerrNavigator provided")
}

val LocalSettingsNavigator = staticCompositionLocalOf<Navigator<SettingsScreen>> {
    error("No SettingsNavigator provided")
}

/**
 * Helper to easily access the current navigation manager.
 */
val navigationManager: NavigationManager
    @Composable
    get() = LocalNavigationManager.current

/**
 * Helper to easily access the current Arr navigator.
 */
val arrNavigator: Navigator<ArrScreen>
    @Composable
    get() = LocalArrNavigator.current

/**
 * Helper to easily access the current Seerr navigator.
 */
val seerrNavigator: Navigator<SeerrScreen>
    @Composable
    get() = LocalSeerrNavigator.current

/**
 * Helper to easily access the current Settings navigator.
 */
val settingsNavigator: Navigator<SettingsScreen>
    @Composable
    get() = LocalSettingsNavigator.current
