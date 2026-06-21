package se.djupfeldt.paperduck

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class WritingTools(private val context: ApplicationContext) {
    private val log = LoggerFactory.getLogger(WritingTools::class.java)

    val invocations = mutableListOf<ToolInvocation>()

    @Tool(
        description = """
        Look up information about the world, including people, places, events, and history.
        Tags should be single words in base form.
    """
    )
    fun getWorldInformation(
        tags: List<String>
    ): String {
        log.info("Getting information about the world. Tags: ${tags.joinToString(", ")}")
        val resources = try {
            context.getResources("classpath:knowledge/*")
        } catch (_: Exception) {
            log.error("No knowledge files found")
            emptyArray()
        }
        log.info("Available resources: ${resources.joinToString(", ") { it.filename ?: "null" }}")

        val matchingContent = resources.mapNotNull { resource ->
            val content = try {
                resource.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                null
            }
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                log.info("Found matching content in: ${resource.filename}")
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
            invocations.add(ToolInvocation("getWorldInformation", tags))
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
        log.info("Getting information about stories. Tags: ${tags.joinToString(", ")}")
        val resources = try {
            context.getResources("classpath:stories/*")
        } catch (_: Exception) {
            log.error("No story files found")
            emptyArray()
        }
        log.info("Available resources: ${resources.joinToString(", ") { it.filename ?: "null" }}")

        val matchingContent = resources.mapNotNull { resource ->
            val content = try {
                resource.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                null
            }
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                log.info("Found matching content in: ${resource.filename}")
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
            invocations.add(ToolInvocation("getStoryInformation", tags))
        }
    }
}
