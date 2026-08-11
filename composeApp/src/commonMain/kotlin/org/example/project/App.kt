package org.example.project


import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState

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

import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.example.project.di.koinConfig


import org.koin.compose.KoinApplication

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.maplibre.spatialk.geojson.Position

private var isPermissionGranted by mutableStateOf(false)
private  var isPermissionDialogCompleted by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration(koinConfig), content = {


        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState
        )

        var selectedMarker by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }

        val onMarkerClick: (feature: Feature<Geometry, JsonObject?>) -> Unit = { markerdata ->
            selectedMarker = markerdata

        }

        var position by remember { mutableStateOf<Position?>(null) }

        val selectedPosition: (position: Position?) -> Unit = { selectedPos ->
            position = selectedPos
        }

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
                sheetPeekHeight = 300.dp,
                sheetMaxWidth = Dp.Unspecified,

                sheetContent = {
                    BottomSheetContent(
                        feature = selectedMarker,
                        selectedPosition = selectedPosition,
                    )
                    LaunchedEffect(position){
                        scaffoldState.bottomSheetState.partialExpand()
                    }
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
                            InfoButton()
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
                            MapComponent(
                                onMarkerClick = onMarkerClick,
                                locationPermission = isPermissionGranted,
                                selectedPosition = position,
                                onClearPosition = { position = null }
                            )
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

