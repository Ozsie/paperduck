package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ByteArrayResource

class AiServiceTest {

    private lateinit var chatClient: ChatClient
    private lateinit var writingTools: WritingTools
    private lateinit var context: ApplicationContext
    private lateinit var tagService: TagService
    private lateinit var aiService: AiService

    @BeforeEach
    fun setUp() {
        chatClient = mock(ChatClient::class.java)
        context = mock(ApplicationContext::class.java)

        // Mock tags.md resource
        val tagsResource = ByteArrayResource("tag1, tag2".toByteArray())
        `when`(context.getResource("classpath:tags.md")).thenReturn(tagsResource)

        writingTools = WritingTools(context)
        tagService = TagService(context)
        aiService = AiService(chatClient, writingTools, tagService)
    }

    @Test
    fun `chat should return successful result`() {
        val query = "What is Anutu?"
        val tags = setOf("anutu")

        val promptSpec = mock(ChatClient.ChatClientRequestSpec::class.java)
        val callResponseSpec = mock(ChatClient.CallResponseSpec::class.java)

        `when`(chatClient.prompt()).thenReturn(promptSpec)
        `when`(promptSpec.system(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.user(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.tools(writingTools)).thenReturn(promptSpec)
        `when`(promptSpec.call()).thenReturn(callResponseSpec)
        `when`(callResponseSpec.content()).thenReturn("Anutu is a god.")

        val result = aiService.chat(query, tags)
        
        assertEquals("Anutu is a god.", result.answer)
        assertTrue(result.toolCalls.isEmpty())
        assertTrue(result.tagsUsed.isEmpty())
    }

    @Test
    fun `chat should handle exceptions`() {
        `when`(chatClient.prompt()).thenThrow(RuntimeException("AI failure"))

        val result = aiService.chat("test", emptySet())

        assertTrue(result.answer!!.contains("Sorry, I'm having trouble connecting to the AI."))
        assertTrue(result.answer!!.contains("AI failure"))
    }

    @Test
    fun `chat should handle empty query with tags`() {
        val query = ""
        val tags = setOf("tag1")

        val promptSpec = mock(ChatClient.ChatClientRequestSpec::class.java)
        val callResponseSpec = mock(ChatClient.CallResponseSpec::class.java)

        `when`(chatClient.prompt()).thenReturn(promptSpec)
        `when`(promptSpec.system(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.user(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.tools(writingTools)).thenReturn(promptSpec)
        `when`(promptSpec.call()).thenReturn(callResponseSpec)
        `when`(callResponseSpec.content()).thenReturn("Here is some info about tag1.")

        val result = aiService.chat(query, tags)

        assertEquals("Here is some info about tag1.", result.answer)
    }

    // Add assertTrue if not imported
    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertTrue(condition)
    }
}
