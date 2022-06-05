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
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.cio.parseResponse
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


    @Test
    fun customTest() {
        customTest(url = "/header_test", httpMethod = HttpMethod.Get) { response ->
            response.apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("Bearer token", bodyAsText())
            }
        }
    }
}

fun customTest(
    url: String,
    httpMethod: HttpMethod,
    body: Any? = null,
    assertBlock: suspend (response: HttpResponse) -> Unit
) {
    return runBlocking {
        val testApp = TestApplication {
            application {
                configureRouting()
            }
        }
        try {
            val testClient = testApp.createClient {
                install(DefaultRequest) {
                    header("Authorization", "Bearer token") // ちゃんとした jwt を生成して設定する
                    contentType(ContentType.Application.Json)
                }
                install(ContentNegotiation) {
                    jackson {
                        // jackson の設定を追記する
                    }
                }
            }
            runBlocking {
                val response = testClient.request(urlString = url) {
                    method = httpMethod
                    setBody(body)
                }
                assertBlock(response)
            }
        } finally {
            testApp.stop()
        }
    }
}
