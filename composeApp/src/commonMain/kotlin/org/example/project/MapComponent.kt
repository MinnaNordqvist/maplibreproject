package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.ktor.http.ContentType.Image.SVG
import io.ktor.util.collections.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import maplibreproject.composeapp.generated.resources.Res
import org.example.project.data.getGeoJson
import org.example.project.data.getStopStatus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.core.qualifier.qualifier
import org.maplibre.compose.camera.CameraMoveReason
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
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.dsl.featureCollectionOf
import org.maplibre.spatialk.geojson.toJson
import org.maplibre.compose.expressions.dsl.Feature.get
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.BearingUpdate
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import androidx.compose.ui.graphics.ImageBitmap


private  var data by mutableStateOf(featureCollectionOf().toJson())
private var httpStat by mutableStateOf(0)

private var enableTracking by mutableStateOf(false)

private var geoJsonString by  mutableStateOf("")



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapComponent(
    onMarkerClick: (feature:Feature<Geometry, JsonObject?>) -> Unit,
    locationPermission : Boolean
) {

    var locationProvider: LocationProvider?

    if (locationPermission){
        locationProvider = rememberDefaultLocationProvider()
        enableTracking = true

    } else {
        locationProvider = rememberNullLocationProvider()
    }


    val locationState = rememberUserLocationState(locationProvider)


    var isLoading by remember { mutableStateOf(true) }


    var svgIcon by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            try {
                data = getStopsAsGeoJson()
                httpStat = getStopStatus().value
                geoJsonString = getGeoJson("service_points")
                svgIcon = getSVGstring(geoJsonString)
                isLoading = false
                //println(httpStat)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val servIcon = rememberDynamicSvgPainter(svgIcon)



    val camera = rememberCameraState(
        firstPosition =
            CameraPosition(
                target = Position(latitude = 60.45195547084046, longitude = 22.267010954960753),
                zoom = 15.0
            )
    )

    val styleState = rememberStyleState()

    var selectedFeature by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }
    var selectedStop by remember { mutableStateOf<String?>("") }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidth = with(density) { maxWidth }
        val screenHeight = with(density) { maxHeight }

        //Map Layer
        MaplibreMap(
            baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")),
            cameraState = camera,
            styleState = styleState,
            zoomRange = 6.8f..17f,
            onMapClick = { point, screenPoint ->
                selectedFeature = null
                selectedStop = ""
                ClickResult.Pass
            },

            onMapLongClick = { point, screenPoint ->
                selectedFeature = null
                selectedStop = ""
                ClickResult.Pass
            },

            options = MapOptions(
                ornamentOptions = OrnamentOptions.OnlyLogo,
                gestureOptions = GestureOptions.RotationLocked
            ),

            ) {
            // Näytetään LocationPuck jos sijainnin käyttö on sallittu
            if (locationPermission) {
                LocationPuck(
                    idPrefix = "user",
                    locationState = locationState,
                    cameraState = camera,
                    colors = LocationPuckColors(),
                    accuracyThreshold = Float.POSITIVE_INFINITY,
                    sizes = LocationPuckSizes(),
                    showBearing = true
                )

                LocationTrackingEffect(
                    locationState = locationState,
                    enabled = enableTracking,
                ) {

                    camera.updateFromLocation(updateBearing = BearingUpdate.IGNORE)
                    if (camera.moveReason == CameraMoveReason.GESTURE){
                        enableTracking = false
                    }

                }

            }

            //SymbolLayer
            SymbolLayer(
                id = "bus-stop",
                source = markerSource(data),
                iconImage = image((markerImage()), drawAsSdf = true),
                iconColor = const(Color(0xFF5985E1)),
                iconSize = const(3.0f),
                iconHaloColor = const(Color.White),
                iconHaloWidth = const(18.dp),
                visible = true,
                iconAllowOverlap = const(true),
                iconAnchor = const(SymbolAnchor.Center),
                minZoom = 0.0f,
                maxZoom = 24.0f,

                onClick = { features ->
                    selectedFeature = features.firstOrNull()
                    selectedStop = selectedFeature?.getStringProperty("stop_code")
                    selectedFeature?.let { onMarkerClick(it) }

                    println("Clicked on ${features[0].toJson()}")
                    ClickResult.Consume
                },
            )

            // Vaihdetaan valitun pysäkin ikonin taustaväri
            SymbolLayer(
                id = "highlight-layer",
                source = markerSource(data),
                iconImage = image((markerImage()), drawAsSdf = true),
                iconSize = const(3.1f),
                iconColor = const(Color(0xFF789DE5)),
                iconHaloColor = const(Color.Black),
                iconHaloWidth = const(19.dp),
                filter = get("stop_code").asString().eq(const(selectedStop ?: "")),
                onClick = { features ->
                    features.firstOrNull()?.let {
                        selectedStop = it.getStringProperty("stop_code")
                    }
                    ClickResult.Consume
                }
            )



            val poiSource = rememberGeoJsonSource(
                    data =
                        GeoJsonData.Uri(
                            "https://data.foli.fi/geojson/poi/service_points"
                        ),
                    options = GeoJsonOptions(tolerance = 0.1f),
                )


            if(!isLoading) {
                SymbolLayer(
                    id = "service_points",
                    source = poiSource,
                    iconImage = image(servIcon),
                    iconSize = const(0.1f),
                    visible = true,
                    onClick = { features ->
                        println("Clicked on ${features[0].toJson()}")
                        ClickResult.Consume
                    },
                )
            }

        }
        //UI Layer
        // Näytetään Progress Indicator kun pysäkkejä haetaan GTFS-rajapinnasta

        if (isLoading && httpStat == 0) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

            }
        }

        // Näytetään PopUpCard kun pysäkki valitaan

        if (selectedFeature != null) {
            selectedFeature?.let { feature ->
                LaunchedEffect(feature.geometry) {
                    val currentZoom = camera.position.zoom
                    val targ = (feature.geometry as Point).coordinates
                    val screenPos = camera.projection?.screenLocationFromPosition(targ)

                    val horizontalMargin = 50.dp
                    val topMargin = 70.dp
                    val bottomMargin = 30.dp
                    val screenX = screenPos?.x
                    val screenY = screenPos?.y

                    val needsUpdate = screenX!! < horizontalMargin ||
                            screenX > (screenWidth - horizontalMargin) ||
                            screenY!! < topMargin ||
                            screenY > (screenHeight - bottomMargin)

                    if (needsUpdate) {
                        camera.animateTo(
                            CameraPosition(
                                target = targ,
                                zoom = currentZoom,

                                ),
                            //duration = 800.milliseconds
                        )
                    }

                }
                PopUpCard(
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




