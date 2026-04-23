package org.example.project

import androidx.compose.runtime.Composable
import mablibreproject.composeapp.generated.resources.Res
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.spatialk.geojson.Position

@Composable
actual fun MapComponent() {

    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 60.448985, longitude = 22.292180),
                    zoom = 16.0
                )
        )

    MaplibreMap(
        baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")),

        cameraState = camera,
        styleState = rememberStyleState(),

        options =
            MapOptions(
                
                gestureOptions =
                    GestureOptions(
                        isTiltEnabled = true,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true,
                    )
            )
    )
}
