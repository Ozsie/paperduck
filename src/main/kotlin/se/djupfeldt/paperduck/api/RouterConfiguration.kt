package se.djupfeldt.paperduck.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.paramOrNull
import org.springframework.web.servlet.function.router
import se.djupfeldt.paperduck.AiService
import se.djupfeldt.paperduck.TagService

@Configuration
class RouterConfiguration(
    private val aiService: AiService,
    private val tagService: TagService
) {

    @Bean
    fun askRouter() = router {
        GET("/ask") { request ->
            val question = request.paramOrNull("question") ?: ""
            val tags = (request.paramOrNull("tags") ?: "").split(",").toSet()
            aiService.chat(question, tags).let { ServerResponse.ok().body(it) }
        }
        GET("/tags") {
            ServerResponse.ok().body(tagService.getTags())
        }
    }
}
