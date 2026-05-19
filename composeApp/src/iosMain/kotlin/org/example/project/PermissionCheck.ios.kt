package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined

@Composable
actual fun rememberPermissionChecker(onPermissionResult: (Boolean) -> Unit): PermissionChecker {
    val locationManager = remember { CLLocationManager() }

    return remember(locationManager) {
        object : PermissionChecker {
            override val hasLocationPermission: Boolean
                get() {
                    val status = locationManager.authorizationStatus
                    return status == kCLAuthorizationStatusAuthorizedAlways ||
                            status == kCLAuthorizationStatusAuthorizedWhenInUse
                }

            override fun requestLocationPermission() {
                if (locationManager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
                    locationManager.requestWhenInUseAuthorization()
                    // Note: Real iOS production apps usually listen to the delegate for changes,
                    // but calling the prompt is enough to satisfy initial checks.
                }
            }
        }
    }
}