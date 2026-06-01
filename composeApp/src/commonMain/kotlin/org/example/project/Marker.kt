package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.FeatureSerializer
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.SvgPathParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import maplibreproject.composeapp.generated.resources.Res
import maplibreproject.composeapp.generated.resources.bus
import org.example.project.data.Stop
import org.example.project.data.getStops
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson


suspend fun getStopsAsGeoJson(): String{
    // lähetetään http-get pyyntö GTFS-rajapintaan
    // https://data.foli.fi/gtfs/stops
    val mapping: Map<String, Stop> = getStops()
    //println("Pysäkkejä: " + mapping.size)

    val features = mapping.values.map{ value ->
        Feature(
            geometry =
                Point(
                    Position(
                        longitude = value.stop_lon,
                        latitude = value.stop_lat,
                    )
                ),
            properties =
                mapOf(
                    "stop_code" to (value.stop_code),
                    "stop_name" to (value.stop_name)
                ),
        )
    }
    return FeatureCollection(features).toJson()
}

@Composable
fun markerImage(): Painter {
    return painterResource(Res.drawable.bus)
}

@Composable
fun markerSource(data: String): GeoJsonSource {
    return rememberGeoJsonSource(GeoJsonData.JsonString(data))
}


@Composable
fun PopUpCard(
    feature: Feature<Geometry, JsonObject?>,
    cameraState: CameraState,
    onDismiss: () -> Unit
){
    val pos = (feature.geometry as Point).coordinates

    val dpTarg = remember(pos, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(pos)
    }

    val off = with(LocalDensity.current) { Offset(dpTarg?.x?.toPx() ?: 0f, dpTarg?.y?.toPx() ?: 0f) }

    var width by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .onSizeChanged{width = it.width}
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt() - (width/2), //  horizontal
                    y = off.y.toInt() - (220)  // position above the marker
                )
            },
        shape = TooltipShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width=1.dp, color = Color.Black)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = feature.getStringProperty("stop_name") ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(bottom = 6.dp),
                text = feature.getStringProperty("stop_code") ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


class TooltipShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Start Top-Left
            moveTo(0f, 0f)
            // Top-Right
            lineTo(w, 0f)
            // Bottom-Right (Start of the flat bottom)
            val pointerHeight = with(density) { 8.dp.toPx() }
            val bodyBottom = h - pointerHeight

            lineTo(w, bodyBottom)
            // Right side of pointer
            lineTo((w * 0.5f) + 20f, bodyBottom)
            // Tip of pointer (Center Bottom)
            lineTo(w * 0.5f, h)
            // Left side of pointer
            lineTo((w * 0.5f) - 20f, bodyBottom)
            // Bottom-Left
            lineTo(0f, bodyBottom)
            close()
        }
        return Outline.Generic(path)
    }

}


@Composable
fun rememberDynamicSvgPainter(svgString: String): Painter {
    return rememberVectorPainter(
        remember(svgString) {
            // 1. Create a base Vector Builder matchable to your 256x256 viewpoints
            val builder = ImageVector.Builder(
                name = "DynamicPoiIcon",
                defaultWidth = 256.dp,
                defaultHeight = 256.dp,
                viewportWidth = 256f,
                viewportHeight = 256f
            )

            try {
                // 2. Extract all path data chunks and their accompanying fill colors
                // This regex finds the contents of d="..." and optionally fill="..." inside the nodes
                val pathRegex = """<path[^>]*d="([^"]+)"[^>]*>""".toRegex()
                val fillRegex = """fill="([^"]+)"""".toRegex()

                // Also look inside group tags <g fill="..."> if paths don't have explicit colors
                val groupFillRegex = """<g[^>]*fill="([^"]+)"""".toRegex()
                val defaultGroupColor = groupFillRegex.find(svgString)?.groupValues?.get(1) ?: "#FFFFFF"

                val matches = pathRegex.findAll(svgString)

                for (match in matches) {
                    val pathData = match.groupValues[1]

                    // Determine color for this specific path layer
                    val explicitFill = fillRegex.find(match.value)?.groupValues?.get(1)
                    val colorHex = explicitFill ?: defaultGroupColor

                    val pathColor = try {
                        Color(parseHtmlColor(colorHex))
                    } catch (e: Exception) {
                        Color.White // Fallback if color format mismatch
                    }

                    // 3. Convert the raw SVG string geometry straight into Compose vector nodes
                    builder.addPath(
                        pathData = PathParser().parsePathString(pathData).toNodes(),
                        fill = SolidColor(pathColor)
                    )
                }
            } catch (e: Exception) {
                // Fallback safe path (e.g. an empty/error square) if parsing completely fails
                e.printStackTrace()
            }

            builder.build()
        }
    )
}

fun parseHtmlColor(colorString: String): Long {
    val cleanHex = colorString.replace("#", "").trim()
    return when (cleanHex.length) {
        6 -> "FF$cleanHex".toLong(16) // Add full alpha layer if missing
        8 -> cleanHex.toLong(16)
        else -> 0xFFFFFFFF // Default fallback White
    }
}


fun getSVGstring(geoJson: String): String{
    var svgIcon = ""
    val json = Json.parseToJsonElement(geoJson).jsonObject
    val features = json["features"]?.jsonArray
    if (!features.isNullOrEmpty()) {
        val firstFeatureProps = features[0].jsonObject["properties"]?.jsonObject
        val iconObj = firstFeatureProps?.get("icon")?.jsonObject
        svgIcon = iconObj?.get("svg")?.jsonPrimitive?.content.toString()
    }
    return svgIcon
}


@Composable
fun PoiInfoCard(
    feature: Feature<Geometry, JsonObject?>,
    cameraState: CameraState,
){
    val pos = (feature.geometry as Point).coordinates

    val dpTarg = remember(pos, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(pos)
    }

    val off = with(LocalDensity.current) { Offset(dpTarg?.x?.toPx() ?: 0f, dpTarg?.y?.toPx() ?: 0f) }

    Card(
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt() - (100), //  horizontal
                    y = off.y.toInt() - (250)  // position above the marker
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width=1.dp, color = Color.Black)
    ){
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = feature.getStringProperty("category") ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(bottom = 6.dp),
                text = feature.getStringProperty("text") ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}




