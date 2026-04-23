package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType.Application.Json

import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendIfNameAbsent
import kotlinx.serialization.json.Json



actual val client: HttpClient = HttpClient(OkHttp) {
    //Timeout plugin to set up timeout milliseconds for client
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL

    }
    install(UserAgent) {
        agent = "MapProject Android"
    }

    defaultRequest {
        val rnds = (0..100).random()
        url {
            protocol = URLProtocol.HTTPS
            host = "data.foli.fi"
            parameters.append("?hash_id=", "$rnds")
        }
        header("MapProject-Android-Header", "Hello")
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }

}