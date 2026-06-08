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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.getGeoJson
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point



suspend fun getSVG(layer: String): String{
    var svg = ""
    val geoJson = getGeoJson(layer)
    svg = getSVGstring(geoJson)
    return svg
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
fun poiSource(data: String): GeoJsonSource{
    return rememberGeoJsonSource(GeoJsonData.Uri(data), options =  GeoJsonOptions(tolerance = 0.1f),)
}

@Composable
fun poiIcon(svgString: String): Painter{
    return rememberDynamicSvgPainter(svgString)
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
    var width by remember { mutableStateOf(1) }
    var categoryName by remember {mutableStateOf("")}

    when (feature.getStringProperty("category")){
        "SERVICE_POINT" -> categoryName = "Palvelupiste"
        "LOADING_POINT" -> categoryName = "Latauspiste"
        "TICKET_MACHINE" -> categoryName = "Lippuautomaatti"
    }

    Card(
        modifier = Modifier
            .onSizeChanged{width = it.width}
            .absoluteOffset {
                IntOffset(
                    x = off.x.toInt() - (width/2), //  horizontal
                    y = off.y.toInt() - (310)  // position above the marker
                )
            },
        shape = TooltipShape(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width=1.dp, color = Color.Black)
    ){
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(bottom = 6.dp),
                text = (feature.getStringProperty("text") + "\n" + feature.getStringProperty("city")),
                style = MaterialTheme.typography.bodyMedium
            )

        }

    }
}

