package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import maplibreproject.composeapp.generated.resources.Res
import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.hourglass
import maplibreproject.composeapp.generated.resources.info
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.example.project.di.koinConfig
import org.jetbrains.compose.resources.painterResource

import org.koin.compose.KoinApplication

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

private var isPermissionGranted by mutableStateOf(false)
private  var isPermissionDialogCompleted by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration(koinConfig), content = {

        val coroutineScope = rememberCoroutineScope()
        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState
        )

        var selectedMarker by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }

        val onMarkerClick: (feature: Feature<Geometry, JsonObject?>) -> Unit = { markerdata ->
            selectedMarker = markerdata

        }
        val tooltipState = rememberTooltipState(isPersistent = true)

        val permissionChecker = rememberPermissionChecker(
            onPermissionResult = { granted ->
                isPermissionGranted = granted
                isPermissionDialogCompleted = true
            }
        )

        LaunchedEffect(Unit) {
            if (permissionChecker.hasLocationPermission) {
                isPermissionGranted = true
                isPermissionDialogCompleted = true
            } else {
                permissionChecker.requestLocationPermission()

            }

        }


        MaterialTheme {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetSwipeEnabled = true,
                sheetPeekHeight = 260.dp,
                sheetMaxWidth = Dp.Unspecified,


                sheetContent = {

                    BottomSheetContent(
                        feature = selectedMarker,
                        onDismiss = {
                            coroutineScope.launch {
                                selectedMarker = null
                                scaffoldState.bottomSheetState.hide()
                            }
                        }
                    )

                },
                topBar = {
                    TopAppBar(
                        colors = topAppBarColors(

                            titleContentColor = Color.Black,
                        ),
                        title = {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "Föli Zone"
                            )
                        },
                        actions = {
                            // Info button. Näytetään lähde ja selitykset symboleille
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Below
                                    ),
                                tooltip = {
                                    RichTooltip(
                                        title = { Text("Info") },
                                        caretShape = null,
                                    ) {

                                        Column(
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row() {
                                                Image(
                                                    painter = painterResource(Res.drawable.bolt),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(25.dp, 25.dp)
                                                        .padding(
                                                            start = 0.dp,
                                                            top = 0.dp,
                                                            end = 0.dp,
                                                            bottom = 0.dp
                                                        ),

                                                    )
                                                Text("Bussi on ainakin minuutin etuajassa aikataulusta \n")
                                            }
                                            Row() {
                                                Image(
                                                    painter = painterResource(Res.drawable.hourglass),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(25.dp, 25.dp)
                                                        .padding(
                                                            start = 0.dp,
                                                            top = 0.dp,
                                                            end = 0.dp,
                                                            bottom = 0.dp
                                                        ),

                                                    )
                                                Text("Bussi on ainakin minuutin myöhässä aikataulusta \n")
                                            }
                                            Text("Lähde:\nTurun seudun joukkoliikenteen liikennöinti- ja aikatauludata. Aineiston ylläpitäjä on Turun kaupungin joukkoliikennetoimisto. Aineisto on ladattu palvelusta http://data.foli.fi/ lisenssillä Creative Commons Nimeä 4.0 Kansainvälinen (CC BY 4.0).")
                                        }
                                    }

                                },
                                state = tooltipState,

                            ) {
                                IconButton(
                                    modifier = Modifier.padding(
                                        start = 0.dp,
                                        top = 0.dp,
                                        end = 0.dp,
                                        bottom = 0.dp
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            tooltipState.show()
                                        }
                                    }
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.info),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    )

                }, // Main content
                content = { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        if(isPermissionDialogCompleted) {
                            MapComponent(onMarkerClick = onMarkerClick, locationPermission = isPermissionGranted)
                        } else{
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }

                },

                modifier = Modifier.fillMaxWidth()

            )

        }
    })
}

