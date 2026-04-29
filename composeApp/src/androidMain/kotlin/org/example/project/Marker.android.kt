package org.example.project

import android.app.LauncherActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer

import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Popup
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.toJson
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.ListItem


private var selectedFeature by mutableStateOf<Feature<Geometry, JsonObject?>?>(null)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Marker() {

    val marker = painterResource(R.drawable.bus)

    val markerJson = """
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

    val markerSource = rememberGeoJsonSource(
        GeoJsonData.JsonString(markerJson)
    )

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    var isClicked by remember {
        mutableStateOf(false)
    }

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
            /*
            features
                .firstOrNull()
                ?.let{
                    scope.launch {

                    }
            }
            */
            selectedFeature = features.firstOrNull()
            println("Clicked on ${features[0].toJson()}")

            ClickResult.Consume
        },

        )

    selectedFeature?.let { feature ->
        AlertDialog(
            onDismissRequest = { selectedFeature = null },
            confirmButton = {},
            title = { Text(feature.getStringProperty("stop_name") ?: "") },
            text = {
                Column {
                    Text("Station Code: ${feature.getStringProperty("stop_code") ?: ""}")
                }
            }
        )

    }
}

/*
    if(isClicked){
        CardColumn { Text("User Location clicked  times") }
    }


    TooltipBox(
        modifier = Modifier.fillMaxSize(),
        state = tooltipState,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text("text") },
                action = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                tooltipState.dismiss()
                            }
                        }
                    ) { Text("text") }
                }
            ) { Text("text") }
        },
        onDismissRequest = { selectedFeature = null },
    ) {

    }
    /*
    selectedFeature?.let { feature ->

       Card(
           modifier = Modifier.fillMaxSize(),
       ){
           Column(
               verticalArrangement = Arrangement.Center
           ){
               Text(feature.getStringProperty("stop_name") ?: "")
               Text("Station Code: ${feature.getStringProperty("stop_code") ?: ""}")
           }
       }


       AlertDialog(
           onDismissRequest = { selectedFeature = null },
           confirmButton = {},
           title = { Text(feature.getStringProperty("stop_name") ?: "") },
           text = {
               Column {
                   Text("Station Code: ${feature.getStringProperty("stop_code") ?: ""}")
               }
           }
       )

    }
*/
}

 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun MarkerInfo() {
     /*
        selectedFeature?.let { feature ->
            AlertDialog(
                onDismissRequest = { selectedFeature = null },
                confirmButton = {},
                title = { Text("Hammasklinikka") },
                text = {
                    Column {
                        Text("Station Code: 1047")
                    }
                }
            )
        }

*/
     val tooltipState = rememberTooltipState()
     val scope = rememberCoroutineScope()

     TooltipBox(
         modifier = Modifier.fillMaxSize(),
         state = tooltipState,
         positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
         tooltip = {
             RichTooltip(
                 title = { Text("text") },
                 action = {
                     TextButton(
                         onClick = {
                             scope.launch {
                                 tooltipState.dismiss()
                             }
                         }
                     ) { Text("text") }
                 }
             ) { Text("text") }
         },
         onDismissRequest = { selectedFeature = null },
         ) {

     }


/*
    selectedFeature?.let { feature ->
        Card {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Hammasklinikka 1047")
            }
        }
    }
*/

}

@Composable
fun CardColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(contentPadding),
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    }
}





*/