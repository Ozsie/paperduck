package se.djupfeldt.paperduck

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

class KnowledgeServiceTest {

    private lateinit var resourceLoader: ResourceLoader
    private lateinit var knowledgeService: KnowledgeService

    @BeforeEach
    fun setUp() {
        resourceLoader = mock(ResourceLoader::class.java)
        knowledgeService = KnowledgeService(resourceLoader)
    }

    @Test
    fun `getKnowledge should render markdown to html`() {
        val document = "test"
        val markdown = "# Title\n\nContent"
        val expectedHtml = "<h1>Title</h1>\n<p>Content</p>\n"

        val resource = mock(Resource::class.java)
        val tempFile = File.createTempFile("knowledge", ".md")
        tempFile.writeText(markdown)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:knowledge/$document.md")).thenReturn(resource)
        `when`(resource.exists()).thenReturn(true)
        `when`(resource.file).thenReturn(tempFile)

        val result = knowledgeService.getKnowledge(document)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun `getKnowledge should return null if resource does not exist`() {
        val document = "nonexistent"
        val resource = mock(Resource::class.java)

        `when`(resourceLoader.getResource("classpath:knowledge/$document.md")).thenReturn(resource)
        `when`(resource.exists()).thenReturn(false)

        val result = knowledgeService.getKnowledge(document)
        assertNull(result)
    }

    @Test
    fun `getKnowledge should handle special characters`() {
        val document = "special"
        val markdown = "# Anûtu\n\nAnûtu är ett skepp."
        val expectedHtml = "<h1>Anûtu</h1>\n<p>Anûtu är ett skepp.</p>\n"

        val resource = mock(Resource::class.java)
        val tempFile = File.createTempFile("knowledge", ".md")
        tempFile.writeText(markdown, Charsets.UTF_8)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:knowledge/$document.md")).thenReturn(resource)
        `when`(resource.exists()).thenReturn(true)
        `when`(resource.file).thenReturn(tempFile)

        val result = knowledgeService.getKnowledge(document)
        assertEquals(expectedHtml, result)
    }
}
