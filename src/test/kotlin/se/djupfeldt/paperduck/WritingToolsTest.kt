package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ByteArrayResource

class WritingToolsTest {

    private lateinit var gitHubService: GitHubService
    private lateinit var writingTools: WritingTools

    @BeforeEach
    fun setUp() {
        gitHubService = mock(GitHubService::class.java)
        writingTools = WritingTools(gitHubService)
    }

    @Test
    fun `getWorldInformation should return content when tags match`() {
        `when`(gitHubService.listFiles("knowledge", null)).thenReturn(listOf("anutu.md", "other.md"))
        `when`(gitHubService.getFileContent("knowledge", "anutu.md", null)).thenReturn("This is about Anutu.")
        `when`(gitHubService.getFileContent("knowledge", "other.md", null)).thenReturn("This is about something else.")

        val result = writingTools.getWorldInformation(listOf("Anutu"))

        assertTrue(result.contains("This is about Anutu."))
        assertTrue(!result.contains("something else."))
        assertTrue(writingTools.invocations.size == 1)
        assertTrue(writingTools.invocations[0].tool == "getWorldInformation")
    }

    @Test
    fun `getWorldInformation should return no information message when no tags match`() {
        `when`(gitHubService.listFiles("knowledge", null)).thenReturn(listOf("anutu.md"))
        `when`(gitHubService.getFileContent("knowledge", "anutu.md", null)).thenReturn("This is about Anutu.")

        val result = writingTools.getWorldInformation(listOf("Unknown"))

        assertTrue(result.contains("No information found for tags: Unknown"))
    }

    @Test
    fun `getStoryInformation should return content when tags match`() {
        `when`(gitHubService.listFiles("stories", null)).thenReturn(listOf("story1.md"))
        `when`(gitHubService.getFileContent("stories", "story1.md", null)).thenReturn("A story about a duck.")

        val result = writingTools.getStoryInformation(listOf("duck"))

        assertTrue(result.contains("A story about a duck."))
    }

    @Test
    fun `getKnowledgeLinkingInstructions should return instructions with available files`() {
        `when`(gitHubService.listFiles("knowledge", null)).thenReturn(listOf("anutu.md", "sulmu.md"))

        val result = writingTools.getKnowledgeLinkingInstructions()

        assertTrue(result.contains("/knowledge/anutu"))
        assertTrue(result.contains("/knowledge/sulmu"))
        assertTrue(result.contains("anutu.md"))
        assertTrue(result.contains("sulmu.md"))
        assertTrue(writingTools.invocations.any { it.tool == "getKnowledgeLinkingInstructions" })
    }
}
