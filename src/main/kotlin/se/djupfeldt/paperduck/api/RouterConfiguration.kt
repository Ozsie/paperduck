package se.djupfeldt.paperduck.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.paramOrNull
import org.springframework.web.servlet.function.router
import se.djupfeldt.paperduck.AiService
import se.djupfeldt.paperduck.ChatMessage
import se.djupfeldt.paperduck.KnowledgeService
import se.djupfeldt.paperduck.TagService

@Configuration
class RouterConfiguration(
    private val aiService: AiService,
    private val tagService: TagService,
    private val knowledgeService: KnowledgeService
) {

    @Bean
    fun askRouter() = router {
        POST("/ask") { request ->
            val body = request.body(AskRequest::class.java)
            val question = body.question ?: ""
            val tags = body.tags?.toSet() ?: emptySet()
            val history = body.history ?: emptyList()
            aiService.chat(question, tags, history).let { ServerResponse.ok().body(it) }
        }
        GET("/tags") {
            ServerResponse.ok().body(tagService.getTags())
        }
        PUT("/tags/{tag}") { request ->
            val tag = request.pathVariable("tag")
            tagService.addTag(tag)
            ServerResponse.ok().body(tag)
        }
        GET("/knowledge/{document}") { request ->
            val document = request.pathVariable("document")
            val knowledge = knowledgeService.getKnowledge(document)
            if (knowledge != null) {
                ServerResponse.ok()
                    .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                    .body(knowledge)
            } else {
                ServerResponse.notFound().build()
            }
        }
    }
}

data class AskRequest(
    val question: String?,
    val tags: List<String>?,
    val history: List<ChatMessage>?
)
