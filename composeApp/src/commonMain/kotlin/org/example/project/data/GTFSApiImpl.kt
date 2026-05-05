package org.example.project.data

import io.ktor.client.call.body
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
