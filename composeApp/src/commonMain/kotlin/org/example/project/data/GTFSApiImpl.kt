package org.example.project.data

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode


suspend fun getStops(): Map<String, Stop> {
    return org.example.project.client.get("/gtfs/stops"){
        method = HttpMethod.Get
    }.body()
}

suspend fun getStopStatus(): HttpStatusCode {
    return org.example.project.client.get("/gtfs/stops"){
        method = HttpMethod.Get
    }.status
}


suspend fun getRoutes(): List<Routes> {
    return org.example.project.client.get("/gtfs/routes"){
        method = HttpMethod.Get
    }.body()

}

suspend fun getTrips(): List<Trips> {
    return org.example.project.client.get("/gtfs/trips/all"){
        method = HttpMethod.Get
    }.body()
}

suspend fun getStopTimes(stop_id: String?) : List<StopTimes> {
    return org.example.project.client.get("/gtfs/stop_times/stop/$stop_id"){
        method = HttpMethod.Get
    }.body()
}

suspend fun getGeoJson(layer: String?): String {
    return org.example.project.client.get("https://data.foli.fi/geojson/poi/$layer"){
        method = HttpMethod.Get
    }.bodyAsText()
}