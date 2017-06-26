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

    @Bean
    open fun apiRouter(controller: ReactiveDrawingController) = router {
        "/drawing".nest {
            method(OPTIONS, { ok().build() })
            (method(POST) and contentType(APPLICATION_JSON)).invoke {
                req -> ok().body(controller.add(req.bodyToMono<Drawing>(Drawing::class.java)), String::class.java)
            }
        }.also {
            ("/drawings" and method(GET)).invoke {
                ok().contentType(TEXT_EVENT_STREAM)
                        .body<DrawingInfo, Flux<DrawingInfo>>(controller.drawings, DrawingInfo::class.java)
            }
        }
    }
}