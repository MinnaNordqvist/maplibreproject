package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import mablibreproject.composeapp.generated.resources.Res

import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState

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
import org.maplibre.compose.util.ClickResult
//import org.maplibre.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.toJson

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point


private   val markerJson = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "geometry": {
                "type": "Point",
                "coordinates": [ 22.291599999999998971134118619374930858612060546875, 60.45002000000000208501660381443798542022705078125]
              },
              "properties": {
                "stop_code": "1047",
                "stop_name": "Hammasklinikka"
              }
            }
          ]
        }
            """.trimIndent()


// Helper functions to convert between Pixels and DP
@Composable
fun Offset.toDpOffset(): DpOffset = with(LocalDensity.current) { DpOffset(x.toDp(), y.toDp()) }

@Composable
fun DpOffset.toOffset(): Offset = with(LocalDensity.current) { Offset(x.toPx(), y.toPx()) }




@Composable
actual fun MapComponent() {
    var selectedFeature by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }
    var popupPosition by remember { mutableStateOf<DpOffset?>(null) }
    val marker = painterResource(R.drawable.bus)


    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 60.448985, longitude = 22.292180),
                    zoom = 15.0
                )
        )

    val styleState = rememberStyleState()


    Box(modifier = Modifier.fillMaxSize()) {

        // Map Layer
        MaplibreMap(
            baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")),
            cameraState = camera,
            styleState = styleState,

            onMapClick = { point, screenPoint ->
                    popupPosition = null
                    selectedFeature = null
                    ClickResult.Pass
            },

            onMapLongClick = { point, screenPoint ->
                selectedFeature = null
                popupPosition = null
                ClickResult.Pass
            },
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


            )

        {
            // Symbol layer
            val markerSource = rememberGeoJsonSource(
                GeoJsonData.JsonString(markerJson)
            )
            var target by remember { mutableStateOf<Position?>(null) }

            SymbolLayer(
                id = "bus-stop",
                source = markerSource,
                iconImage = image(marker),
                visible = true,
                iconAllowOverlap = const(true),
                iconAnchor = const(SymbolAnchor.Center),
                minZoom = 0.0f,
                maxZoom = 24.0f,
                iconSize = const(3.0f),
                onClick = { features ->
                    features
                        .firstOrNull()
                        ?.let {
                            target = (it.geometry as Point).coordinates
                        }

                    var point = target?.let { camera.projection?.screenLocationFromPosition(it) }
                    var cardOff = point?.y?.let { DpOffset(point.x, it) }
                    popupPosition = cardOff

                    selectedFeature = features.firstOrNull()

                    println("Clicked on ${features[0].toJson()}")
                    println(selectedFeature)
                    println(popupPosition)
                    ClickResult.Consume
                },
                )


        }

            //  UI Layer
        if (selectedFeature != null) {
            selectedFeature?.let { feature ->
                popupPosition?.let { position ->
                    PopupCard(
                        feature = feature,
                        position = position,
                        onDismiss = {
                            selectedFeature = null
                            popupPosition = null
                        }
                    )

                }
            }
        }

    }
}



@Composable
fun BoxScope.PopupCard(
    feature: Feature<Geometry, JsonObject?>,
    position: DpOffset,
    onDismiss: () -> Unit
) {
    // Position the card relative to the click point
    // Adjust offset so popup appears above/beside the marker
    val off = position.toOffset()

    Card(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = off.x.toInt() - 100, // center horizontally (adjust based on card width)
                    y = off.y.toInt() - 190  // position above the marker
                )
            }
            .width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = feature.getStringProperty("stop_name") + " " + feature.getStringProperty("stop_code"),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}


