package dev.hoon.rwc.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * API Document 설정
 */
@Configuration
class DocConfig {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Coroutine으로 개발하는 RabbitMQ Consumer와 Publisher")
                .version("1.0.0")
                .contact(
                    Contact().name("박일훈").email("chiwoo2074@gmail.com")
                )
        )
}