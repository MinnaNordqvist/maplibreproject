package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
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
            "coordinates": [ 60.448985, 22.292180]
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
        iconImage = image((marker), drawAsSdf = true),
        iconSize = const(1.5f)
    )

}