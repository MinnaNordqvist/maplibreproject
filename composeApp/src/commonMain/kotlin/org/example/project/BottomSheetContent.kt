package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import maplibreproject.composeapp.generated.resources.Res
import maplibreproject.composeapp.generated.resources.info
import org.jetbrains.compose.resources.painterResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import kotlinx.coroutines.runBlocking
import maplibreproject.composeapp.generated.resources.refresh
import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.hourglass
import org.example.project.data.Response
import org.example.project.data.Routes
import org.example.project.data.Trips

import org.example.project.data.getRoutes
import org.example.project.data.getStopTimes
import org.example.project.data.getTrips
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel

//Linjojen värit
fun String.toColor(): Color {
    val hex = this
    val colorLong = when (hex.length) {
        6 -> ("FF$hex").toLong(16) // Add full opacity if missing
        8 -> hex.toLong(16)        // Use provided alpha
        else -> throw IllegalArgumentException("Invalid hex color format")
    }
    return Color(colorLong)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomSheetContent(
    feature: Feature<Geometry, JsonObject?>?,
    onDismiss: () -> Unit
) {

    // Kun ohjelma käynnistyy, 2 lähetetään http-get pyyntöä GTFS rajapintaan
    // https://data.foli.fi/gtfs/routes
    val routes: List<Routes> = remember { runBlocking { getRoutes() } }
    // https://data.foli.fi/gtfs/trips/all tämä hidastaa ohjelman käynnistymistä emulaattorissa
    val trips: List<Trips> = remember { runBlocking { getTrips() } }


    val lazyListState = rememberLazyListState()
    val overScrollEffect = rememberOverscrollEffect()


    val scope = rememberCoroutineScope()


    val busViewModel: BusViewModel = koinViewModel()
    val uiState by busViewModel.uiState.collectAsStateWithLifecycle()
    val lines by busViewModel.linesList.collectAsStateWithLifecycle()


    var httpStatus by remember { mutableStateOf(0) }
    var stopSearch: String? by remember {
        mutableStateOf(
            feature?.getStringProperty("stop_code")
        )
    }
    val stopTimes = remember { mutableMapOf<String, Set<String>>() }

    // Route
    val routeDetails = remember { mutableMapOf<String, String>() }
    val routesAndIds = remember { mutableMapOf<String, String>() }
    for (value in routes) {
        routeDetails.put(value.route_short_name, value.route_color)
        routesAndIds.put(value.route_id, value.route_short_name)
    }

    val displayNames = remember { mutableStateMapOf<String, List<String>>() }



    LaunchedEffect(feature?.getStringProperty("stop_code")) {
        stopSearch = feature?.getStringProperty("stop_code")

        if (stopSearch != null) {
            busViewModel.getBusList(stopSearch)


            //Jos pysäkkiä ei ole vielä klikattu, lähetetään HTTP-pyyntö GTFS-rajapintaan
            if (!stopTimes.containsKey(stopSearch)) {
                //  https://data.foli.fi/gtfs/stop_times/stop/stop_id
                val tripIds = getStopTimes(stopSearch).map { it.trip_id }.toSet()
                val routeroutes = arrayListOf<String>()
                trips
                    .filter { tripIds.contains(it.trip_id) }
                    .forEach {
                        routesAndIds[it.route_id]?.let { element ->
                            routeroutes.add(element)
                        }
                    }
                displayNames.put(stopSearch!!, routeroutes)
                stopTimes.put(stopSearch!!, tripIds)

            }

            httpStatus = busViewModel.getResponseStatus(stopSearch).value
        }


    }

    fun getDisplayNames(): String? {
        return displayNames[stopSearch]?.toList()?.distinct()?.joinToString { it } ?: ""
    }



    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 1.dp, top = 6.dp, end = 1.dp, bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = lazyListState,
        userScrollEnabled = true,
        reverseLayout = false,
        overscrollEffect = overScrollEffect,
    ) {

        if (feature?.getStringProperty("stop_code").isNullOrEmpty()) {
            item {
                Column(Modifier.padding(top = 5.dp)) {
                    Text(
                        text = "Valitse pysäkki",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )
                }

            }
        }
        stickyHeader {
            feature?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = " ${it.getStringProperty("stop_name")}  ${
                                    it.getStringProperty(
                                        "stop_code"
                                    )
                                } ",
                                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp)
                                    .weight(3.0f)
                            )
                            OutlinedIconButton(
                                onClick = {
                                    scope.launch {
                                        busViewModel.getBusList(it.getStringProperty("stop_code"))
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 6.dp)
                                    .height(30.dp)
                                    .width(30.dp),
                                enabled = true,
                                shape = RoundedCornerShape(1.dp),
                                border = BorderStroke(1.dp, Color.Black),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.refresh),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Text(text = ("Linjat: " + getDisplayNames()), fontWeight = FontWeight.Light)
                    }
                }
            }
        }
        itemsIndexed(uiState.busList) { index, bus ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0XFFf3f6f4)
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(2.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 0.dp, top = 0.dp, end = 6.dp, bottom = 0.dp)
                                .size(60.dp, 40.dp)
                                //.weight(0.8f)
                                .background(routeDetails[bus.lineref]!!.toColor()),

                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = bus.lineref,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Text(
                            text = bus.destinationdisplay,
                            //fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .weight(3.0f)
                                .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp),
                            style = MaterialTheme.typography.titleMedium,
                            softWrap = true,

                            )
                        if (bus.aikaero() >= 60) {
                            Image(
                                painter = painterResource(Res.drawable.hourglass),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp, 30.dp)
                                    .padding(start = 2.dp, top = 8.dp, end = 0.dp, bottom = 0.dp),
                                alignment = Alignment.TopEnd
                            )
                        }
                        if (bus.aikaero() <= -60) {
                            Image(
                                painter = painterResource(Res.drawable.bolt),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp, 30.dp)
                                    .padding(start = 2.dp, top = 8.dp, end = 0.dp, bottom = 0.dp),
                                alignment = Alignment.TopEnd
                            )
                            println(bus.aikaero())
                        }
                        Text(
                            text = bus.getDeparture(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                //.weight(0.8f)
                                .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        println(bus.destinationdisplay + " aikataulun mukainen " + bus.getAimedDeparture() + " reealiaika " + bus.getDeparture())
                    }
                }
            }
        }

        if (httpStatus == 200 && uiState.busList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0XFFf3f6f4)
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ei linjoja seuraavan tunnin aikana",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }


    }
}
