package org.example.project.data

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

/*
class SiriApiImpl (

) : SiriApi {
    override suspend fun getBusList(stop_code: String?): List<Response.Bus>? {
        return org.example.project.client.get("/siri/sm/$stop_code") {
            method = HttpMethod.Get
        }.body<Response>().result
    }

    override suspend fun getResponseStatus(stop_code: String?): HttpStatusCode {
        return org.example.project.client.get("/siri/sm/$stop_code") {
            method = HttpMethod.Get
        }.status
    }
}

 */


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

