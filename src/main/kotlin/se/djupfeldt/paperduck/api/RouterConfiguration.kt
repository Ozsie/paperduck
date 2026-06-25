package se.djupfeldt.paperduck.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.paramOrNull
import org.springframework.web.servlet.function.router
import se.djupfeldt.paperduck.*
import se.djupfeldt.paperduck.ai.AiService
import se.djupfeldt.paperduck.ai.ChatMessage
import se.djupfeldt.paperduck.information.KnowledgeService
import se.djupfeldt.paperduck.information.TagService

@Configuration
class RouterConfiguration(
    private val aiService: AiService,
    private val tagService: TagService,
    private val knowledgeService: KnowledgeService,
    private val properties: PaperduckProperties
) {

    @Bean
    fun askRouter() = router {
        POST("/ask") { request ->
            val body = request.body<AskRequest>()
            val question = body.question ?: ""
            val tags = body.tags?.toSet() ?: emptySet()
            val history = body.history ?: emptyList()
            val repoId = body.repoId
            aiService.chat(question, tags, history, repoId).let { ServerResponse.ok().body(it) }
        }
        GET("/repositories") {
            ServerResponse.ok().body(properties.repositories.map { mapOf("id" to it.id, "name" to it.name) })
        }
        GET("/tags") { request ->
            val repoId = request.paramOrNull("repoId")
            ServerResponse.ok().body(tagService.getTags(repoId))
        }
        GET("/knowledge/{document}") { request ->
            val document = request.pathVariable("document")
            val repoId = request.paramOrNull("repoId")
            val knowledge = knowledgeService.getKnowledge(document, repoId)
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
    val history: List<ChatMessage>?,
    val repoId: String?
)
