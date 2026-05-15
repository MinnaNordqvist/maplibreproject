package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.PI
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.cos


val cardHeight = 1.0f
val cardWidth = 1.1f
val cardMargin = 0.1f

private fun DrawScope.drawInfoWindow(){

    val area = Path()
    area.moveTo(size.width * cardMargin, size.height * cardMargin)
    area.lineTo(size.width * (cardMargin + cardWidth), size.height * cardMargin)
    area.lineTo(size.width * (cardMargin + cardWidth), size.height * (cardMargin + cardHeight * 0.75f))

    area.lineTo(size.width * (cardMargin + cardWidth * 0.625f), size.height * (cardMargin + cardHeight * 0.75f))

    area.lineTo(size.width * (cardMargin + cardWidth * 0.5f), size.height * (cardMargin + cardHeight))
    area.lineTo(size.width * (cardMargin + cardWidth * 0.375f), size.height * (cardMargin + cardHeight * 0.75f))

    area.lineTo(size.width * cardMargin, size.height * (cardMargin + cardHeight * 0.75f))
    area.close()

    //area.moveTo(x = size.width* cardMargin, y = 0f)
   // area.lineTo(size.width * cardMargin, size.width * cardHeight)
    //area.lineTo(size.width * (cardMargin + cardWidth), size.width * cardHeight)
   // area.lineTo(size.width*(cardMargin+cardWidth), 0f)
   // area.lineTo(size.width * (cardMargin + cardWidth), size.width * cardHeight)
    drawPath(path = area, color = Color.White)
    drawPath(area, color = Color.Black, style = Stroke(2.dp.toPx()))

}



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


    Box(
        modifier = Modifier
        .absoluteOffset {
            IntOffset(
                x = off.x.toInt().minus(190), //  horizontal
                y = off.y.toInt().minus(210)  // position above the marker
            )
        }
        .drawBehind {
            translate(left = 0f, top = 0f) {
                drawInfoWindow()
            }
        },
        contentAlignment = Alignment.TopEnd
        //.clip(RoundedCornerShape(8.dp))

    ){
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                modifier = Modifier.padding(start = 20.dp, top = 2.dp, end = 0.dp, bottom = 0.dp),
                text = feature.getStringProperty("stop_name") ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End
            )
            Text(
                modifier = Modifier.padding(start = 20.dp, top = 0.dp, end = 0.dp, bottom = 8.dp),
                text = feature.getStringProperty("stop_code") ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
        }
    }
/*
    Card(
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt().minus(10), //  horizontal
                    y = off.y.toInt().minus(205)  // position above the marker
                )

            },
       // shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
       // colors = CardDefaults.cardColors(containerColor = Color.White)
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

 */


}
