package org.example.project

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.window.Popup

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
          "properties": {}
        }
      ]
    }
        """.trimIndent()

    val markerSource = rememberGeoJsonSource(
        GeoJsonData.JsonString(markerJson)
    )

    var selectedFeature by remember {mutableStateOf<Feature<Geometry, JsonObject?>?>(null)}

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
            selectedFeature = features.firstOrNull()

            ClickResult.Consume
        },
       // textField = format(span("Hammasklinikka 1047")),
       // textFont = const(listOf("Noto Sans Regular")),
    )

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
}

