package com.zenika

import com.zenika.controller.ReactiveDrawingController
import com.zenika.domain.Drawing
import com.zenika.domain.DrawingInfo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.http.HttpMethod.*
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Configuration
open class RouteConfig {

    val ALLOW_CROSS_ORIGIN_RESPONSE: Mono<ServerResponse> = initJsonResponse()
            .header("Access-Control-Allow-Headers", "Content-Type")
            .header("Access-Control-Allow-Methods", "POST, GET")
            .build()

    @Bean
    open fun apiRouter(controller: ReactiveDrawingController) = router {
        "/drawing".nest {
            method(OPTIONS, {
                ALLOW_CROSS_ORIGIN_RESPONSE
            })
            method(POST).nest {
                contentType(APPLICATION_JSON) {
                    req -> initResponse().body(controller.add(req.bodyToMono<Drawing>(Drawing::class.java)), String::class.java)
                }
            }
        }.also {
            "/drawings".nest {
                GET("/", {
                    initResponse().contentType(TEXT_EVENT_STREAM)
                            .body<DrawingInfo, Flux<DrawingInfo>>(controller.drawings, DrawingInfo::class.java)
                })
            }
        }
    }

    private fun initJsonResponse(): ServerResponse.BodyBuilder {
        return initResponse().contentType(MediaType.APPLICATION_JSON)
    }

    private fun initResponse(): ServerResponse.BodyBuilder {
        return ok().header("Access-Control-Allow-Origin", "https://localhost:8443")
    }
}