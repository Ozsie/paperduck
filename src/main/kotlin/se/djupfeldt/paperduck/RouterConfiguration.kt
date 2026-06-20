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
            val question = request.paramOrNull("question") ?: ""
            val tags = (request.paramOrNull("tags") ?: "").split(",").toSet()
            aiService.chat(question, tags).let { ServerResponse.ok().body(it) }
        }
    }
}
