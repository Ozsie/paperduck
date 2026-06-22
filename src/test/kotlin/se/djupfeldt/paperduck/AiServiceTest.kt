package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.Message
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ByteArrayResource

class AiServiceTest {

    private lateinit var chatClient: ChatClient
    private lateinit var writingTools: WritingTools
    private lateinit var gitHubService: GitHubService
    private lateinit var context: ApplicationContext
    private lateinit var tagService: TagService
    private lateinit var aiService: AiService

    @BeforeEach
    fun setUp() {
        chatClient = mock(ChatClient::class.java)
        gitHubService = mock(GitHubService::class.java)
        context = mock(ApplicationContext::class.java)

        // Mock tags.md resource
        val tagsResource = ByteArrayResource("tag1, tag2".toByteArray())
        `when`(context.getResource("classpath:tags.md")).thenReturn(tagsResource)

        writingTools = WritingTools(gitHubService)
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
        `when`(promptSpec.messages(any<Message>())).thenReturn(promptSpec)
        `when`(promptSpec.tools(writingTools)).thenReturn(promptSpec)
        `when`(promptSpec.call()).thenReturn(callResponseSpec)
        `when`(callResponseSpec.content()).thenReturn("Anutu is a god.")

        // Simulate tool calls during the AI call
        `when`(callResponseSpec.content()).thenAnswer {
            assertEquals("anutu-repo", writingTools.currentRepoId)
            writingTools.getKnowledgeLinkingInstructions()
            writingTools.getWorldInformation(listOf("anutu"))
            "Anutu is a god."
        }

        val result = aiService.chat(query, tags, repoId = "anutu-repo")

        assertEquals("Anutu is a god.", result.answer)
        assertEquals(2, result.toolCalls.size)
        assertTrue(result.toolCalls.any { it.tool == "getKnowledgeLinkingInstructions" })
        assertTrue(result.toolCalls.any { it.tool == "getWorldInformation" })
        assertEquals(listOf("anutu"), result.tagsUsed)
    }

    @Test
    fun `chat should handle exceptions`() {
        `when`(chatClient.prompt()).thenThrow(RuntimeException("AI failure"))

        val result = aiService.chat("test", emptySet())

        assertTrue(result.answer!!.contains("Sorry, I'm having trouble connecting to the AI."))
        assertTrue(result.answer.contains("AI failure"))
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
        `when`(promptSpec.messages(any<Message>())).thenReturn(promptSpec)
        `when`(promptSpec.tools(writingTools)).thenReturn(promptSpec)
        `when`(promptSpec.call()).thenReturn(callResponseSpec)
        `when`(callResponseSpec.content()).thenReturn("Here is some info about tag1.")

        val result = aiService.chat(query, tags)

        assertEquals("Here is some info about tag1.", result.answer)
    }

    @Test
    fun `chat should include history in prompt`() {
        val query = "What was my last question?"
        val history = listOf(ChatMessage("user", "Who is Anutu?"), ChatMessage("assistant", "Anutu is a god."))
        
        val promptSpec = mock(ChatClient.ChatClientRequestSpec::class.java)
        val callResponseSpec = mock(ChatClient.CallResponseSpec::class.java)

        `when`(chatClient.prompt()).thenReturn(promptSpec)
        `when`(promptSpec.system(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.user(anyString())).thenReturn(promptSpec)
        `when`(promptSpec.messages(any<Message>())).thenReturn(promptSpec)
        `when`(promptSpec.tools(writingTools)).thenReturn(promptSpec)
        `when`(promptSpec.call()).thenReturn(callResponseSpec)
        `when`(callResponseSpec.content()).thenReturn("Your last question was about Anutu.")

        val result = aiService.chat(query, emptySet(), history)

        assertEquals("Your last question was about Anutu.", result.answer)
        
        // Verify that messages were called at least twice (for the history)
        org.mockito.Mockito.verify(promptSpec, org.mockito.Mockito.atLeast(2)).messages(any<Message>())
    }
    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertTrue(condition)
    }
}
