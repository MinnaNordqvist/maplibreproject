package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point


@Composable
fun InfoWindow(
    feature: Feature<Geometry, JsonObject?>,
    cameraState: CameraState,
    onDismiss: () -> Unit
){
    val pos = (feature.geometry as Point).coordinates

    val dpTarg = remember(pos, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(pos)
    }

    val off = with(LocalDensity.current) { Offset(dpTarg?.x?.toPx() ?: 0f, dpTarg?.y?.toPx() ?: 0f) }
    val shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 1.dp)

    Card(
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt().minus(10), //  horizontal
                    y = off.y.toInt().minus(205)  // position above the marker
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = feature.getStringProperty("stop_name") ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = feature.getStringProperty("stop_code") ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
