package com.example

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.example.plugins.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.runBlocking

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}

fun customTest(url: String, httpMethod: HttpMethod, body: Any? = null): HttpResponse {
    return runBlocking {
        val testApp = TestApplication {
            environment {
                // ここになにか設定を記述する
            }
        }
        try {
            testApp.createClient {
                install(DefaultRequest) {
                    header("Authorization", "Bearer token")
                    contentType(ContentType.Application.Json)
                }
                install(ContentNegotiation) {
                    jackson {
                        // jackson の設定を追記する
                    }
                }
            }.request(urlString = url) {
                method = httpMethod
                setBody(body)
            }
        } finally {
            testApp.stop()
        }
    }
}
