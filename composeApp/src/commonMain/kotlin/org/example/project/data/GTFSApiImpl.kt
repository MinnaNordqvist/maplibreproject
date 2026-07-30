package org.example.project.data

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
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


suspend fun getRoutes(): List<Route> {
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
    var stops = emptyList<StopTimes>()
    try {
    stops = org.example.project.client.get("/gtfs/stop_times/stop/$stop_id") {
            method = HttpMethod.Get
        }.body()
    } catch (e: HttpRequestTimeoutException) {
        println("Timeout fetching lines list for $stop_id")
    } catch (e: Exception) {
        println("Failed to fetch lines list for $stop_id:  ${e.message}")

    }
    return stops
}


suspend fun getGeoJson(layer: String?): String {
    return org.example.project.client.get("https://data.foli.fi/geojson/poi/$layer"){
        method = HttpMethod.Get
    }.body()
}