package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import maplibreproject.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.runBlocking

import maplibreproject.composeapp.generated.resources.refresh
import org.example.project.data.Route
import org.example.project.data.Trips

import org.example.project.data.getRoutes
import org.example.project.data.getStopTimes
import org.example.project.data.getTrips
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.unit.center
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant


private  fun compareServerTimes(serverT: Long): Duration {
    val clock: Clock = Clock.System
    val instantNow = clock.now()
    val instantServer = Instant.fromEpochSeconds(serverT)
    val erotus = instantNow - instantServer
    return erotus
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,)
@Composable
fun BottomSheetContent(
    feature: Feature<Geometry, JsonObject?>?,
   // onDismiss: () -> Unit,
    selectedPosition: (position:Position?) -> Unit,
) {

    // Kun ohjelma käynnistyy, 2 lähetetään http-get pyyntöä GTFS rajapintaan
    // https://data.foli.fi/gtfs/routes
    val routes: List<Route> = remember { runBlocking { getRoutes() } }
    // https://data.foli.fi/gtfs/trips/all tämä hidastaa ohjelman käynnistymistä emulaattorissa
    val trips: List<Trips> = remember { runBlocking { getTrips() } }



    val lazyListState = rememberLazyListState()
    val overScrollEffect = rememberOverscrollEffect()



    val scope = rememberCoroutineScope()


    val busViewModel: BusViewModel = koinViewModel()
    val uiState by busViewModel.uiState.collectAsStateWithLifecycle()
    val lines by busViewModel.linesList.collectAsStateWithLifecycle()
    val selectedLines by busViewModel.selectedLines.collectAsStateWithLifecycle()

    var httpStatus by remember { mutableStateOf(0) }
    var siriStatus by remember { mutableStateOf("")}
    var serverTime: Long by remember { mutableStateOf(0) }


    var stopSearch: String? by rememberSaveable { mutableStateOf(feature?.getStringProperty("stop_code")) }

    var stickyText by rememberSaveable { mutableStateOf("") }

    var position : Position? by remember{ mutableStateOf<Position?>(null) }
    val onIconClick: (position:Position?) -> Unit = { selectedPos ->
        position = selectedPos
        position?.let { selectedPosition(it) }
       // println(position)
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



    LaunchedEffect(feature?.getStringProperty("stop_code") ?: "" ){
        if (feature?.getStringProperty("stop_code")?.isNotEmpty() == true) {

            stopSearch = feature.getStringProperty("stop_code")
            stickyText = "${feature.getStringProperty("stop_name")}\n" +
                    "${feature.getStringProperty("stop_code")}"
        }

        if (stopSearch?.isNotEmpty() == true) {

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

                    if(tripIds.isNotEmpty() && routeroutes.isNotEmpty() ){
                        displayNames.put(stopSearch!!, routeroutes)
                        stopTimes.put(stopSearch!!, tripIds)
                    }


            }

            httpStatus = busViewModel.getResponseStatus(stopSearch).value
            siriStatus = busViewModel.getSiriStatus(stopSearch)
            serverTime = busViewModel.getServerTime(stopSearch)

        }
    }




    fun getDisplayNames(): String? {
        return displayNames[stopSearch]?.toList()?.distinct()?.joinToString { it } ?: ""
    }

    val filteredBusList = remember(uiState.busList, selectedLines, stopSearch ?: "") {
        val selectedLine = selectedLines[stopSearch]

        if (selectedLine.isNullOrEmpty()) {
            //println("Lazy layout unfiltered: " + lazyListState.layoutInfo.viewportSize.center + " " + lazyListState.layoutInfo.totalItemsCount)
            uiState.busList
        } else {
            //println("Lazy layout: " + selectedLine + " " + lazyListState.layoutInfo.viewportSize.center + " " + lazyListState.layoutInfo.totalItemsCount)
            uiState.busList.filter { it.lineref == selectedLine }

        }
    }





    LazyColumn(
        modifier = Modifier.padding(bottom = 20.dp).fillMaxHeight(),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        state = lazyListState,
        userScrollEnabled = true,
        reverseLayout = false,
        overscrollEffect = overScrollEffect,
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {

        if ((stopSearch.isNullOrEmpty())) {
            item {
                Column(Modifier.padding(top = 5.dp)) {
                    Text(
                        text = "Select a bus stop",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()

                    )

                }
            }
            /*
        } else {
            stickyHeader {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stickyText,
                                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(
                                        start = 8.dp,
                                        top = 6.dp,
                                        end = 2.dp,
                                        bottom = 6.dp
                                    )
                                    .weight(3.0f)
                            )
                        }
                    }
                }

            }

             */


        } else {
            stickyHeader {
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
                                text = stickyText,
                                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 6.dp, end = 2.dp, bottom = 6.dp)
                                    .weight(3.0f)
                            )
                            OutlinedIconButton(
                                onClick = {
                                    scope.launch {
                                        busViewModel.getBusList(stopSearch!!)
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
                        Text(
                            text = ("Linjat: " + getDisplayNames()),
                            Modifier.padding(start = 4.dp, top = 4.dp, end = 0.dp, bottom = 2.dp),
                            fontWeight = FontWeight.Light
                        )

                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),

                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        maxItemsInEachRow = 5,
                    ) {
                        lines.forEach { label ->

                            val isSelected = stopSearch?.isNotEmpty() == true && selectedLines[stopSearch] == label

                            FilterLines(
                                isSelected = isSelected,
                                stopSearch = stopSearch,
                                label = label,
                                selectedLines = selectedLines,
                                busViewModel = busViewModel
                            )
                        }
                    }
                }

            }

            if (uiState.busList.isNotEmpty() && uiState.busList[0].compareTimes() > 1.hours) {

                item {
                   Card(
                       colors = CardDefaults.cardColors(
                           containerColor = Color(0XFFf3f6f4)
                       ),
                       border = BorderStroke(1.dp, Color.Black),
                       modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                   ) {
                       Column(
                           modifier = Modifier.padding(8.dp),
                           verticalArrangement = Arrangement.Center
                       ) {
                           Text(
                               text = "Fölin palvelimella on ongelmia.",
                               style = MaterialTheme.typography.titleLarge
                           )
                           Text(
                               text = "Viimeisin aikataulu julkaistu ${uiState.busList[0].compareTimes()} sitten.\nYritä myöhemmin uudelleen.",
                               style = MaterialTheme.typography.titleMedium
                           )
                       }
                   }
               }
            } else {
                itemsIndexed(
                    items = filteredBusList,
                    key = { _, bus -> "${stopSearch}_${bus.lineref}_${bus.expecteddeparturetime}" }) { _, bus ->
                    Timetable(bus, routeDetails, onIconClick = onIconClick)

                }
            }

            if (httpStatus == 200 && siriStatus == "SIRI_NO_DATA"){
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0XFFf3f6f4)
                        ),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Fölin palvelimella on ongelmia.\nYritä myöhemmin uudelleen.",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Server status: " + siriStatus,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            val timeAgo = compareServerTimes(serverTime)
            if (httpStatus == 200 && siriStatus == "OK" && timeAgo < 10.minutes && uiState.busList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0XFFf3f6f4)
                        ),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
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
            if (httpStatus == 200 && siriStatus == "OK" && timeAgo >= 10.minutes && uiState.busList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0XFFf3f6f4)
                        ),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Fölin palvelin lähettää vanhentunutta dataa.",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Viimeksi päivitetty ${timeAgo} sitten.\nYritä myöhemmin uudelleen.",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Server status " + siriStatus,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }



            item {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {

                }
            }

        }


    }}



