package se.djupfeldt.paperduck

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class KnowledgeServiceTest {

    private lateinit var gitHubService: GitHubService
    private lateinit var knowledgeService: KnowledgeService

    @BeforeEach
    fun setUp() {
        gitHubService = mock(GitHubService::class.java)
        knowledgeService = KnowledgeService(gitHubService)
    }

    @Test
    fun `getKnowledge should render markdown to html`() {
        val document = "test"
        val markdown = "# Title\n\nContent"
        val expectedHtml = "<h1>Title</h1>\n<p>Content</p>\n"

        `when`(gitHubService.getFileContent("knowledge", "$document.md", null)).thenReturn(markdown)

        val result = knowledgeService.getKnowledge(document)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun `getKnowledge should return null if resource does not exist`() {
        val document = "nonexistent"

        `when`(gitHubService.getFileContent("knowledge", "$document.md", null)).thenReturn(null)

        val result = knowledgeService.getKnowledge(document)
        assertNull(result)
    }

    @Test
    fun `getKnowledge should handle special characters`() {
        val document = "special"
        val markdown = "# Anûtu\n\nAnûtu är ett skepp."
        val expectedHtml = "<h1>Anûtu</h1>\n<p>Anûtu är ett skepp.</p>\n"

        `when`(gitHubService.getFileContent("knowledge", "$document.md", null)).thenReturn(markdown)

        val result = knowledgeService.getKnowledge(document)
        assertEquals(expectedHtml, result)
    }
}
