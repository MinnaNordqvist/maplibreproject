package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import mablibreproject.composeapp.generated.resources.Res
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap

import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
//import org.maplibre.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.geojson.FeatureCollection.fromFeature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point


@Composable
actual fun MapComponent() {

    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 60.448985, longitude = 22.292180),
                    zoom = 15.0
                )
        )

    val styleState = rememberStyleState()






    MaplibreMap(
        baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")),
        cameraState = camera,
        styleState = styleState,

        options =
            MapOptions(
                ornamentOptions = OrnamentOptions.OnlyLogo,
                gestureOptions =
                    GestureOptions(
                        isTiltEnabled = true,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true,
                    )
            ),

    ) {
        Marker()
    }

}
