package org.example.project.data

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode




interface SiriApi {
    suspend fun getBusList(stop_code: String?): List<Response.Bus>?
    suspend fun getResponseStatus(stop_code: String?): HttpStatusCode

    suspend fun getSiriStatus(stop_code: String?): String
}

class SiriApiImpl (

) : SiriApi {
    override suspend fun getBusList(stop_code: String?): List<Response.Bus>? {
        return try {
            org.example.project.client.get("/siri/sm/$stop_code") {
                method = HttpMethod.Get
            }.body<Response>().result
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout fetching bus list for stop $stop_code")
            null

        } catch (e: Exception) {
            println("Failed to fetch bus list for stop $stop_code  ${e.message}")
            null
        }

    }



    override suspend fun getResponseStatus(stop_code: String?): HttpStatusCode {
        return org.example.project.client.get("/siri/sm/$stop_code") {
            method = HttpMethod.Get
        }.status
    }

    override suspend fun getSiriStatus(stop_code: String?): String {
        return org.example.project.client.get("/siri/sm/$stop_code") {
            method = HttpMethod.Get
        }.body<Response>().status
    }
}



/*
suspend fun getBusList(stop_code: String?): List<Response.Bus>? {
    return  org.example.project.client.get("/siri/sm/$stop_code") {
        method = HttpMethod.Get

    }.body<Response>().result
}


suspend fun getResponseStatus(stop_code: String?): HttpStatusCode {
    return org.example.project.client.get("/siri/sm/$stop_code") {
        method = HttpMethod.Get
    }.status
}
*/
