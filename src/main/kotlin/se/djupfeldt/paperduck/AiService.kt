package se.djupfeldt.paperduck

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.tool.annotation.Tool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class AiService(
    private val chatClient: ChatClient,
    private val writingTools: WritingTools,
    context: ApplicationContext
) {

    private val availableTags = try {
        context.getResource("classpath:tags.md").file.readText().split(",").map { it.trim().lowercase() }.toSet()
    } catch (_: Exception) {
        emptySet()
    }

    fun chat(query: String, tags: Set<String>): Any {
        val requestedTags = if (tags.isNotEmpty()) "\nRequested tags are: ${tags.joinToString(", ")}." else ""
        val systemPrompt = """
            You are a helpful assistant for writing novels and short stories.
            Only answer based on data returned by tools.
            Answer in a clear and concise manner. You may make reasonable assumptions.
            Select appropriate tags from the available tags: ${availableTags.joinToString(", ")}.
            You may create new tags if more information is needed.$requestedTags
        """.trimIndent()
        println(systemPrompt)
        try {
            val call = chatClient.prompt()
                .system(
                    systemPrompt
                )
                .user(query)
                .tools(writingTools)
                .call()
            return ChatResult(
                answer = call.content(),
                toolCalls = writingTools.invocations.toList()
            ).also {
                println("AI response: ${it.answer}")
                println("Tags used: ${it.toolCalls.flatMap { it.tags }.distinct()}")
                println("Tools used: ${it.toolCalls.map { it.tool }.distinct()}")
            }
        } catch (e: Exception) {
            println("Error calling AI: ${e.message}")
            e.printStackTrace()
            return "Sorry, I'm having trouble connecting to the AI. Error: ${e.message}"
        }
    }
}

@Configuration
class AiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient =
        builder
            .defaultAdvisors(SimpleLoggerAdvisor())
            .build()
}

@Component
class WritingTools(
    private val context: ApplicationContext
) {
    val invocations = mutableListOf<ToolInvocation>()

    data class ToolInvocation(val tool: String, val tags: List<String>, val result: String)

    @Tool(
        description = """
        Look up information about the world, including people, places, events, and history.
        Tags should be single words in base form.
    """
    )
    fun getWorldInformation(
        tags: List<String>
    ): String {
        println("Getting information about the world. Tags: ${tags.joinToString(", ")}")
        val resources = try {
            context.getResources("classpath:knowledge/*")
        } catch (_: Exception) {
            println("No knowledge files found")
            emptyArray()
        }
        println("Available resources: ${resources.joinToString(", ") { it.filename ?: "null" }}")

        val matchingContent = resources.mapNotNull { resource ->
            val content = try {
                resource.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                null
            }
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                println("Found matching content in: ${resource.filename}")
                content
            } else {
                null
            }
        }

        return if (matchingContent.isEmpty()) {
            "No information found for tags: ${tags.joinToString(", ")}"
        } else {
            matchingContent.joinToString("\n---\n")
        }.also {
            invocations.add(ToolInvocation("getWorldInformation", tags, matchingContent.joinToString("\n---\n")))
        }
    }


    @Tool(
        description = """
        Look up information from stories in the world.
        Tags should be single words in base form.
    """
    )
    fun getStoryInformation(
        tags: List<String>
    ): String {
        println("Getting information about stories. Tags: ${tags.joinToString(", ")}")
        val resources = try {
            context.getResources("classpath:stories/*")
        } catch (_: Exception) {
            println("No story files found")
            emptyArray()
        }
        println("Available resources: ${resources.joinToString(", ") { it.filename ?: "null" }}")

        val matchingContent = resources.mapNotNull { resource ->
            val content = try {
                resource.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                null
            }
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                println("Found matching content in: ${resource.filename}")
                content
            } else {
                null
            }
        }

        return if (matchingContent.isEmpty()) {
            "No information found for tags: ${tags.joinToString(", ")}"
        } else {
            matchingContent.joinToString("\n---\n")
        }.also {
            invocations.add(ToolInvocation("getStoryInformation", tags, matchingContent.joinToString("\n---\n")))
        }
    }
}

data class ChatResult(val answer: String?, val toolCalls: List<WritingTools.ToolInvocation>)
