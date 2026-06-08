package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
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
import org.jetbrains.compose.resources.painterResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.Hyphens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.runBlocking
import maplibreproject.composeapp.generated.resources.refresh
import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.hourglass
import org.example.project.data.Routes
import org.example.project.data.Trips

import org.example.project.data.getRoutes
import org.example.project.data.getStopTimes
import org.example.project.data.getTrips
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.getScopeId
import kotlin.collections.emptyList
import kotlin.collections.listOf
import kotlin.collections.mapOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid




@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalUuidApi::class
)
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
    val selectedLines by busViewModel.selectedLines.collectAsStateWithLifecycle()

    var isSelected by rememberSaveable {mutableStateOf(false)}

    var httpStatus by remember { mutableStateOf(0) }

    var stopSearch by rememberSaveable {
        mutableStateOf(feature?.getStringProperty("stop_code"))
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

    val filteredBusList = remember(uiState.busList, selectedLines, stopSearch?: "") {
        val selectedLine = selectedLines[stopSearch]

        if (selectedLine.isNullOrEmpty()) {

            uiState.busList
        } else {

            uiState.busList.filter { it.lineref == selectedLine }
        }
    }



    LazyColumn(
        modifier = Modifier.padding(bottom = 20.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = lazyListState,
        userScrollEnabled = true,
        reverseLayout = false,
        overscrollEffect = overScrollEffect,
    ) {

        if (stopSearch.isNullOrEmpty()) {
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
                                text = "${it.getStringProperty("stop_name")}\n${
                                    it.getStringProperty(
                                        "stop_code"
                                    )
                                }",
                                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 6.dp, end = 2.dp, bottom = 6.dp)
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
                        Text(
                            text = ("Linjat: " + getDisplayNames()),
                            Modifier.padding(start = 4.dp, top = 4.dp, end = 0.dp, bottom = 2.dp),
                            fontWeight = FontWeight.Light
                        )

                    }
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Start,
                        maxItemsInEachRow = 8,
                    ) {
                        lines.forEachIndexed { index, label ->
                            if (stopSearch?.isNotEmpty() == true){
                                isSelected = selectedLines[stopSearch!!] == label
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if ( selectedLines[stopSearch!!] == label) {
                                        busViewModel.removeLine(stopSearch!!)

                                    } else {
                                        busViewModel.addLine(stopSearch!!, label)
                                        println(selectedLines.entries)

                                    }

                                },

                                label = { Text(label) },
                                elevation = FilterChipDefaults.filterChipElevation(),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = Color.Black,
                                   // selectedContainerColor = Color.Magenta,
                                    ),
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }
                    }
                }

            }
        }

        itemsIndexed(items = filteredBusList, key = { index, bus -> "${stopSearch}_${bus.lineref}_${index}" }) { index, bus ->
            Timetable(bus, routeDetails)
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
            item {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {

                }
            }

        }
    }
