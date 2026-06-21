package se.djupfeldt.paperduck

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class AiService(
    private val chatClient: ChatClient,
    private val writingTools: WritingTools,
    private val tagService: TagService
) {
    private val log = LoggerFactory.getLogger(AiService::class.java)

    fun chat(query: String, tags: Set<String>): ChatResult {
        try {
            val call = callAI(query, tags)
            return ChatResult(
                call.content(),
                writingTools.invocations.toList()
            ).also {
                log.info("AI response: ${it.answer}")
                log.info("Tags used: ${it.toolCalls.flatMap { it.tags }.distinct()}")
                log.info("Tools used: ${it.toolCalls.map { it.tool }.distinct()}")
            }
        } catch (e: Exception) {
            log.error("Error calling AI: ${e.message}", e)
            return ChatResult("Sorry, I'm having trouble connecting to the AI. Error: ${e.message}", emptyList())
        }
    }

    fun callAI(query: String, tags: Set<String>): ChatClient.CallResponseSpec {
        val requestedTags = if (tags.isNotEmpty()) "\nRequested tags are: ${tags.joinToString(", ")}." else ""
        val systemPrompt = """
            You are a helpful assistant for writing novels and short stories.
            Only answer based on data returned by tools.
            Answer in a clear and concise manner. You may make reasonable assumptions.
            Select appropriate tags from the available tags: ${tagService.getTags().joinToString(", ")}.
            You may create new tags if more information is needed.$requestedTags
        """.trimIndent()
        return chatClient.prompt()
            .system(
                systemPrompt
            )
            .user(query)
            .tools(writingTools)
            .call()
    }
}
