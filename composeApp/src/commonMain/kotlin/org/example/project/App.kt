package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

import mablibreproject.composeapp.generated.resources.Res
import mablibreproject.composeapp.generated.resources.compose_multiplatform
import org.example.project.data.getStopStatus
import org.maplibre.compose.map.MaplibreMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    MaterialTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetSwipeEnabled = true,
            sheetPeekHeight = 260.dp,
            sheetMaxWidth = Dp.Unspecified,


            sheetContent = {

                BottomSheetContent()

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
                    }
                )
            },  // Main content
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    MapComponent()
                }

            },

            modifier = Modifier.fillMaxWidth()

        )

    }
}

