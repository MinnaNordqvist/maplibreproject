package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer

import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource


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

    SymbolLayer(
        id = "bus-stop",
        source = markerSource,
        iconImage = image(marker),
        visible = true,
        iconAllowOverlap = const(true),
        iconAnchor = const(SymbolAnchor.Center),
        minZoom = 0.0f,
        maxZoom = 24.0f,
        iconSize = const(3.0f)
    )

}