package se.djupfeldt.paperduck

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component

@Component
class WritingTools(private val gitHubService: GitHubService) {
    private val log = LoggerFactory.getLogger(WritingTools::class.java)

    var currentRepoId: String? = null

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
        log.info("Getting information about the world. Tags: ${tags.joinToString(", ")}, Repo: $currentRepoId")
        val fileNames = gitHubService.listFiles("knowledge", currentRepoId)
        log.info("Available knowledge files: ${fileNames.joinToString(", ")}")

        val matchingContent = fileNames.mapNotNull { fileName ->
            val content = gitHubService.getFileContent("knowledge", fileName, currentRepoId)
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                log.info("Found matching content in: $fileName")
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
        log.info("Getting information about stories. Tags: ${tags.joinToString(", ")}, Repo: $currentRepoId")
        val fileNames = gitHubService.listFiles("stories", currentRepoId)
        log.info("Available story files: ${fileNames.joinToString(", ")}")

        val matchingContent = fileNames.mapNotNull { fileName ->
            val content = gitHubService.getFileContent("stories", fileName, currentRepoId)
            if (content != null && tags.any { tag -> content.contains(tag, ignoreCase = true) }) {
                log.info("Found matching content in: $fileName")
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

    @Tool(
        description = """
        Get mandatory instructions on how to insert links to knowledge files in the response.
        CALL THIS TOOL BEFORE GENERATING ANY RESPONSE that mentions people, places, or concepts.
        This tool provides the ONLY allowed format for links and a list of available knowledge files.
    """
    )
    fun getKnowledgeLinkingInstructions(): String {
        log.info("Getting knowledge linking instructions. Repo: $currentRepoId")
        val fileNames = gitHubService.listFiles("knowledge", currentRepoId)

        val filesList = fileNames.joinToString("\n") { "- $it (Link: /knowledge/${it.removeSuffix(".md")})" }

        return """
            MANDATORY LINKING RULES:
            1. When referring to any of the files listed below, you MUST use the provided link format.
            2. Link format: [Link Text](/knowledge/filename_without_extension)
            3. DO NOT use other formats like [Text](kb/file.md) or [Text](knowledge/file).
            
            Example: For information about Anutu, use [Anutu](/knowledge/anutu).
            
            Available knowledge files and their correct links:
            ${filesList.ifEmpty { "No knowledge files available." }}
        """.trimIndent().also {
            invocations.add(ToolInvocation("getKnowledgeLinkingInstructions", emptyList()))
        }
    }
}
