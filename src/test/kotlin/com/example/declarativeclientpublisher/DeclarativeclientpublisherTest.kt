package com.example.declarativeclientpublisher

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.reactivestreams.Publisher

@MicronautTest
class DeclarativeclientpublisherTest(
    private val client: TestClient
): StringSpec({

    "unauthed status throws" {
        shouldThrow<HttpClientResponseException> {
            client.auth("asd").awaitFirstOrNull()
        }.status shouldBe HttpStatus.UNAUTHORIZED
    }

    "bad request status throws" {
        shouldThrow<HttpClientResponseException> {
            client.bad().awaitFirstOrNull()
        }.status shouldBe HttpStatus.BAD_REQUEST
    }

    "server error status throws" {
        shouldThrow<HttpClientResponseException> {
            client.error().awaitFirstOrNull()
        }.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    }
})

@Controller
@Produces(MediaType.APPLICATION_JSON)
class TestController {

    @Get("/auth")
    fun auth(
        @Header("Authorization") auth: String
    ): HttpResponse<String> {
        return if (auth != "auth") {
            HttpResponse.unauthorized<String>()
                // forced but not unreasonable to expect 3rd party APIs to do
                // this causes DefaultHttpClient to process as a FullHttpResponse in a streaming context
                // which has no handling for non-200 statuses
                .header("content-length", "0")
        } else {
            HttpResponse.ok(auth)
        }
    }

    @Get("/bad")
    fun bad(): HttpResponse<String> {
        return HttpResponse.badRequest<String>()
                .header("content-length", "0")
    }

    @Get("/error")
    fun error(): HttpResponse<String> {
        return HttpResponse.serverError<String>()
            .header("content-length", "0")
    }
}

@Client("http://localhost:8080")
@Header(name = HttpHeaders.USER_AGENT, value = "rawilder-test")
interface TestClient {

    @Get("/auth")
    fun auth(@Header("Authorization") authorization: String): Publisher<String>

    @Get("/bad")
    fun bad(): Publisher<String>

    @Get("/error")
    fun error(): Publisher<String>
}
