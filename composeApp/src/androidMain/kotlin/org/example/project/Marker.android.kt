package org.example.project

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson


private var selectedFeature by mutableStateOf<Feature<Geometry, JsonObject?>?>(null)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Marker(

) {

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
    val scope = rememberCoroutineScope()

    var target by remember { mutableStateOf<Position?>(null) }
    var stopName by remember { mutableStateOf("") }
    var stopCode by remember { mutableStateOf("") }

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
            features
                .firstOrNull()
                ?.let {
                    target = (it.geometry as Point).coordinates
                    stopName = (it.getStringProperty("stop_name") ?: "")
                    stopCode = (it.getStringProperty("stop_code") ?: "")
                }

            // selectedFeature = features.firstOrNull()
            println("Clicked on ${features[0].toJson()}")
            println(target)
            println(stopName)
            println(stopCode)
            ClickResult.Consume
        },

        )
}

/*
    selectedFeature?.let { feature ->
        AlertDialog(
            onDismissRequest = { selectedFeature = null },
            confirmButton = {},
            title = { Text(feature.getStringProperty("stop_name") ?: "") },
            text = {
                Column {
                    Text("Station Code: ${feature.getStringProperty("stop_code") ?: ""}")
                    println(feature.geometry)
                }
            },

            )

    }

*/
    /*

        MarkerInfo(
            cameraState = state,
            targetPosition = target,

            ) {

                Card(

                ) {
                    Text(stopName)
                    Text(stopCode)
                }
            }

*/





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


 @SuppressLint("UnrememberedMutableState")
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun MarkerInfo(
     targetPosition: Position?,
     modifier: Modifier = Modifier,
     //onDismiss: () -> Unit,

     contentPadding: PaddingValues = PaddingValues(12.dp),
     content: @Composable (BoxScope.() -> Unit),
 ) {
         Box(modifier = modifier.fillMaxSize()) {
             Column(
                 modifier = modifier.fillMaxSize(),
             ) {
                 Box(modifier = Modifier.padding(contentPadding)) {
                        content()

                 }
             }
         }

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




