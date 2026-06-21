package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ByteArrayResource

class WritingToolsTest {

    private lateinit var context: ApplicationContext
    private lateinit var writingTools: WritingTools

    @BeforeEach
    fun setUp() {
        context = mock(ApplicationContext::class.java)
        writingTools = WritingTools(context)
    }

    @Test
    fun `getWorldInformation should return content when tags match`() {
        val resource1 = ByteArrayResource("This is about Anutu.".toByteArray())
        val resource2 = ByteArrayResource("This is about something else.".toByteArray())

        // Mocking getResources to return our test resources
        // Note: WritingTools uses resource.filename which ByteArrayResource provides as null by default
        // We might need a better mock or a custom Resource implementation if filename is critical
        // But WritingTools only uses filename for logging.

        `when`(context.getResources("classpath:knowledge/*")).thenReturn(arrayOf(resource1, resource2))

        val result = writingTools.getWorldInformation(listOf("Anutu"))

        assertTrue(result.contains("This is about Anutu."))
        assertTrue(!result.contains("something else."))
        assertTrue(writingTools.invocations.size == 1)
        assertTrue(writingTools.invocations[0].tool == "getWorldInformation")
    }

    @Test
    fun `getWorldInformation should return no information message when no tags match`() {
        val resource1 = ByteArrayResource("This is about Anutu.".toByteArray())
        `when`(context.getResources("classpath:knowledge/*")).thenReturn(arrayOf(resource1))

        val result = writingTools.getWorldInformation(listOf("Unknown"))

        assertTrue(result.contains("No information found for tags: Unknown"))
    }

    @Test
    fun `getStoryInformation should return content when tags match`() {
        val resource1 = ByteArrayResource("A story about a duck.".toByteArray())
        `when`(context.getResources("classpath:stories/*")).thenReturn(arrayOf(resource1))

        val result = writingTools.getStoryInformation(listOf("duck"))

        assertTrue(result.contains("A story about a duck."))
    }

    @Test
    fun `getKnowledgeLinkingInstructions should return instructions with available files`() {
        val resource1 = mock(org.springframework.core.io.Resource::class.java)
        `when`(resource1.filename).thenReturn("anutu.md")
        val resource2 = mock(org.springframework.core.io.Resource::class.java)
        `when`(resource2.filename).thenReturn("sulmu.md")

        `when`(context.getResources("classpath:knowledge/*")).thenReturn(arrayOf(resource1, resource2))

        val result = writingTools.getKnowledgeLinkingInstructions()

        assertTrue(result.contains("/knowledge/anutu"))
        assertTrue(result.contains("/knowledge/sulmu"))
        assertTrue(result.contains("anutu.md"))
        assertTrue(result.contains("sulmu.md"))
        assertTrue(writingTools.invocations.any { it.tool == "getKnowledgeLinkingInstructions" })
    }
}
