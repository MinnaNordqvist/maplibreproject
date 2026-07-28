package org.example.project



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.ktor.client.request.invoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import maplibreproject.composeapp.generated.resources.Res
import org.example.project.data.getStopStatus
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
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
import org.maplibre.compose.location.BearingUpdate
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.location.LocationPuckSizes
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import maplibreproject.composeapp.generated.resources.bus_map_pin
import maplibreproject.composeapp.generated.resources.my_location
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.FeatureCollection


private  var data by mutableStateOf(featureCollectionOf().toJson())
private var httpStat by mutableStateOf(0)

private var enableTracking by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapComponent(
    onMarkerClick: (feature:Feature<Geometry, JsonObject?>) -> Unit,
    locationPermission : Boolean,
    selectedPosition: Position?,
    onClearPosition: () -> Unit
) {

    var locationProvider: LocationProvider?

    if (locationPermission) {
        locationProvider = rememberDefaultLocationProvider()
        enableTracking = true

    } else {
        locationProvider = rememberNullLocationProvider()
    }

    val coroutineScope = rememberCoroutineScope()
    val locationState = rememberUserLocationState(locationProvider)

    var isLoading by remember { mutableStateOf(true) }

    // POI icons
    var svgService by remember { mutableStateOf("") }
    var svgTicket by remember { mutableStateOf("") }
    var svgLoading by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            try {
                data = getStopsAsGeoJson()
                httpStat = getStopStatus().value
                svgService = getSVG("service_points")
                svgTicket = getSVG("ticket_machines")
                svgLoading = getSVG("loading_points")
                isLoading = false
                //println(httpStat)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = 60.45195547084046, longitude = 22.267010954960753),
            zoom = 15.0
        )
    )

    val styleState = rememberStyleState()

    var selectedFeature by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }
    var selectedStop by remember { mutableStateOf<String?>("") }

    var selectedPoi by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }



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
                selectedPoi = null
                onClearPosition()
                ClickResult.Pass
            },

            onMapLongClick = { point, screenPoint ->
                selectedFeature = null
                selectedStop = ""
                selectedPoi = null
                onClearPosition()
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
                    colors = LocationPuckColors(dotFillColorCurrentLocation = Color(0XFF27A3F5)),
                    accuracyThreshold = Float.POSITIVE_INFINITY,
                    sizes = LocationPuckSizes(dotRadius = 10.dp),
                    showBearing = true
                )

                LocationTrackingEffect(
                    locationState = locationState,
                    enabled = enableTracking,
                ) {
                    camera.updateFromLocation(updateBearing = BearingUpdate.IGNORE)

                }

                LaunchedEffect(camera.moveReason) {
                    println("Camera moved: " + camera.moveReason + " tracking " + enableTracking)
                    if (camera.moveReason == CameraMoveReason.GESTURE) {
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
                iconSize = const(1.0f),
                iconHaloColor = const(Color.White),
                iconHaloWidth = const(18.dp),
                visible = true,
                iconAllowOverlap = const(true),
                minZoom = 0.0f,
                maxZoom = 24.0f,

                onClick = { features ->
                    selectedFeature = features.firstOrNull()
                    selectedStop = selectedFeature?.getStringProperty("stop_code")
                    selectedFeature?.let { onMarkerClick(it) }

                    ClickResult.Consume
                },
            )

            // Vaihdetaan valitun pysäkin ikonin taustaväri
            SymbolLayer(
                id = "highlight-layer",
                source = markerSource(data),
                iconImage = image((markerImage()), drawAsSdf = true),
                iconSize = const(1.1f),
                iconColor = const(Color(0xFF789DE5)),
                iconHaloColor = const(Color.Black),
                iconHaloWidth = const(19.dp),
                iconAllowOverlap = const(true),
                filter = get("stop_code").asString().eq(const(selectedStop ?: "")),

                )

            // POI layers from GeoJSON source
            SymbolLayer(
                id = "service_points",
                source = poiSource("https://data.foli.fi/geojson/poi/service_points"),
                iconImage = image(poiIcon(svgService)),
                iconSize = const(0.09f),
                minZoom = 0.0f,
                maxZoom = 30.0f,
                visible = true,
                iconAllowOverlap = const(true),
                onClick = { features ->
                    selectedPoi = features.firstOrNull()
                    //println("Clicked on ${features[0].toJson()}")
                    ClickResult.Consume
                }
            )

            SymbolLayer(
                id = "ticket_machines",
                source = poiSource("https://data.foli.fi/geojson/poi/ticket_machines"),
                iconImage = image(poiIcon(svgTicket)),
                iconSize = const(0.09f),
                minZoom = 0.0f,
                maxZoom = 24.0f,
                visible = true,
                iconAllowOverlap = const(true),
                onClick = { features ->
                    selectedPoi = features.firstOrNull()
                    //println("Clicked on ${features[0].toJson()}")
                    ClickResult.Consume
                }
            )

            SymbolLayer(
                id = "loading_points",
                source = poiSource("https://data.foli.fi/geojson/poi/loading_points"),
                iconImage = image(poiIcon(svgLoading)),
                iconSize = const(0.09f),
                minZoom = 0.0f,
                maxZoom = 24.0f,
                visible = true,
                iconAllowOverlap = const(true),
                onClick = { features ->
                    selectedPoi = features.firstOrNull()
                    //println("Clicked on ${features[0].toJson()}")
                    ClickResult.Consume
                }
            )

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

                    val horizontalMargin = 55.dp
                    val topMargin = 75.dp
                    val bottomMargin = 35.dp
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
                            duration = 100.milliseconds
                        )
                    }

                }
                PopUpCard(
                    feature = feature,
                    cameraState = camera,
                )

            }

        }

        // Näytetään POI kortti klikattaessa
        if (selectedPoi != null) {
            selectedPoi?.let { feature ->
                LaunchedEffect(feature.geometry) {

                }
                PoiInfoCard(
                    feature = feature,
                    cameraState = camera
                )
            }

        }

        // Näytetään location button jos on lupa käyttää sijaintia
        if (locationPermission) {
            Box(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val zoom = camera.position.zoom
                UseMyLocation(
                    onClick = {
                        coroutineScope.launch {
                            locationState.location?.position?.let {
                                camera.animateTo(CameraPosition(target = it, zoom = zoom))
                            }
                        }
                    }
                )
            }
        }


        // Näytetään valitun bussin sijainti
        if (selectedPosition != null) {

            val zoom = camera.position.zoom

            LaunchedEffect(selectedPosition) {
                camera.animateTo(CameraPosition(target = selectedPosition, zoom = zoom), duration = 1200.milliseconds)
                println("Animating to $selectedPosition")
            }
            BusLocationInfo(
                position = selectedPosition,
                cameraState = camera,
                onClick = {
                    coroutineScope.launch {
                        camera.animateTo(CameraPosition(target = (selectedFeature?.geometry as Point).coordinates, zoom = zoom), duration = 1200.milliseconds)
                    }
                    onClearPosition()
                }
            )
        }


    }
}

@Composable
fun UseMyLocation(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0XFF27A3F5), containerColor = Color.White)
        ) {
        Icon(
            imageVector = vectorResource(Res.drawable.my_location),
            contentDescription = "Center the camera to location puck"
        )
    }
}

