package se.djupfeldt.paperduck

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.paramOrNull
import org.springframework.web.servlet.function.router

@Configuration
class RouterConfiguration(
    private val aiService: AiService
) {

    @Bean
    fun askRouter(): RouterFunction<ServerResponse> = router {
        GET("/ask") { request ->
            aiService.chat(request.paramOrNull("question") ?: "").let { ServerResponse.ok().body(it) }
        }
    }
}
