package se.djupfeldt.paperduck.ai

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Service
import se.djupfeldt.paperduck.information.TagService

@Service
class AiService(
    private val chatClient: ChatClient,
    private val writingTools: WritingTools,
    private val tagService: TagService
) {
    private val log = LoggerFactory.getLogger(AiService::class.java)

    fun chat(query: String, tags: Set<String>, history: List<ChatMessage> = emptyList(), repoId: String? = null): ChatResult {
        try {
            writingTools.invocations.clear()
            writingTools.currentRepoId = repoId
            val call = callAI(query, tags, history, repoId)
            val answer = call.content()
            val toolInvocations = writingTools.invocations.toList()
            val tagsUsed = toolInvocations.flatMap { it.tags }.distinct()
            return ChatResult(
                answer,
                toolInvocations,
                tagsUsed
            ).also {
                log.info("AI response: ${it.answer}")
                log.info("Tags used: ${it.tagsUsed}")
                log.info("Tools used: ${it.toolCalls.map { it.tool }.distinct()}")
            }
        } catch (e: Exception) {
            log.error("Error calling AI: ${e.message}", e)
            return ChatResult("Sorry, I'm having trouble connecting to the AI. Error: ${e.message}", emptyList())
        }
    }

    fun callAI(query: String, tags: Set<String>, history: List<ChatMessage> = emptyList(), repoId: String? = null): ChatClient.CallResponseSpec {
        val requestedTags = if (tags.isNotEmpty()) "\nRequested tags are: ${tags.joinToString(", ")}." else ""
        val systemPrompt = """
            You are a helpful assistant for writing novels and short stories.
            Only answer based on data returned by tools.
            Answer in a clear and concise manner. You may make reasonable assumptions.
            
            IMPORTANT: You MUST insert links to knowledge files in the response whenever you mention a topic that exists in the knowledge base.
            To do this correctly, you MUST ALWAYS call 'getKnowledgeLinkingInstructions' to get the current list of available files and the MANDATORY link format.
            DO NOT guess links or use formats like [Text](kb/file.md).
            
            DO NOT include any citations or references in the format [1], [2], etc., or use any built-in citation features.
            
            Select appropriate tags from the available tags: ${tagService.getTags(repoId).joinToString(", ")}.$requestedTags
        """.trimIndent()
        val chatQuery = query.ifBlank { "Vad handlar dessa tags om?" }
        val prompt = chatClient.prompt()
            .system(systemPrompt)

        history.forEach { msg ->
            when (msg.role.lowercase()) {
                "user" -> prompt.messages(UserMessage(msg.content))
                "assistant", "ai" -> prompt.messages(AssistantMessage(msg.content))
            }
        }

        return prompt
            .user(chatQuery)
            .tools(writingTools)
            .call()
    }
}
