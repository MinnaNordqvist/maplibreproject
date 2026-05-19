package org.example.project

import androidx.compose.runtime.Composable

interface PermissionChecker {
    val hasLocationPermission: Boolean
    fun requestLocationPermission()
}

@Composable
expect fun rememberPermissionChecker(onPermissionResult: (Boolean) -> Unit): PermissionChecker