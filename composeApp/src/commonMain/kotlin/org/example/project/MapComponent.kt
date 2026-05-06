package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import mablibreproject.composeapp.generated.resources.Res
import mablibreproject.composeapp.generated.resources.bus
import org.example.project.data.Stop
import org.example.project.data.getStopStatus
import org.example.project.data.getStops
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
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
import org.maplibre.spatialk.geojson.Position
import org.maplibre.compose.style.StyleState
import org.maplibre.compose.util.FeaturesClickHandler
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.dsl.featureCollectionOf
import org.maplibre.spatialk.geojson.toJson



private suspend fun getStopsAsGeoJson(): String{
    // lähetetään http-get pyyntö GTFS-rajapintaan
    // https://data.foli.fi/gtfs/stops
    val mapping: Map<String, Stop> = getStops()
    //println("Pysäkkejä: " + mapping.size)

    val features = mapping.values.map{ value ->
        Feature(
            geometry =
                Point(
                    Position(
                        longitude = value.stop_lon,
                        latitude = value.stop_lat,
                    )
                ),
            properties =
                mapOf(
                    "stop_code" to (value.stop_code),
                    "stop_name" to (value.stop_name)
                ),
        )
    }
    return FeatureCollection(features).toJson()
}

private  var data by mutableStateOf(featureCollectionOf().toJson())
private var httpStat by mutableStateOf(0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapComponent(
    onMarkerClick: (feature:Feature<Geometry, JsonObject?>) -> Unit
){
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            try {
                httpStat = getStopStatus().value
                data = getStopsAsGeoJson()
                isLoading = false
                //println(httpStat)
            } catch(e: Exception){
                e.printStackTrace()
            }
        }
    }

    val marker = painterResource(Res.drawable.bus)
    var selectedFeature by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }


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
        //Map Layer
        MaplibreMap(
            baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")),
            cameraState = camera,
            styleState = styleState,
            zoomRange = 6.8f..17f,
            onMapClick = { point, screenPoint ->
                selectedFeature = null
                ClickResult.Pass
            },

            onMapLongClick = { point, screenPoint ->
                selectedFeature = null
                ClickResult.Pass
            },

            options = MapOptions(
                ornamentOptions = OrnamentOptions.OnlyLogo,
                gestureOptions = GestureOptions.Standard
            ),

            ) {
            //Symbol Layer

            val markerSource = rememberGeoJsonSource(
                GeoJsonData.JsonString(data)
            )
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
                    selectedFeature = features.firstOrNull()

                    selectedFeature?.let { onMarkerClick(it) }
                    println("Clicked on ${features[0].toJson()}")
                    ClickResult.Consume
                },
            )

        }
            //UI Layer

        if (isLoading && httpStat == 0) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

            }
        }

            if (selectedFeature != null) {
                selectedFeature?.let { feature ->
                    PopupCard(
                        feature = feature,
                        cameraState = camera,
                        onDismiss = {
                            selectedFeature = null

                        }
                    )

                }
            }



    }

}

@Composable
fun PopupCard(
    feature: Feature<Geometry, JsonObject?>,
    cameraState: CameraState,
    onDismiss: () -> Unit
) {

    val pos = (feature.geometry as Point).coordinates

    val dpTarg = remember(pos, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(pos)
    }

    val off = with(LocalDensity.current) { Offset(dpTarg?.x?.toPx() ?: 0f, dpTarg?.y?.toPx() ?: 0f) }


    Card(
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt().minus(100), // center horizontally (adjust based on card width)
                    y = off.y.toInt().minus(190)  // position above the marker
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = feature.getStringProperty("stop_name") ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = feature.getStringProperty("stop_code") ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
