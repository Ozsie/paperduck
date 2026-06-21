package se.djupfeldt.paperduck.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import se.djupfeldt.paperduck.AiService
import se.djupfeldt.paperduck.ChatResult
import se.djupfeldt.paperduck.KnowledgeService
import se.djupfeldt.paperduck.TagService

class RouterConfigurationTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var aiService: AiService
    private lateinit var tagService: TagService
    private lateinit var knowledgeService: KnowledgeService

    @BeforeEach
    fun setUp() {
        aiService = mock(AiService::class.java)
        tagService = mock(TagService::class.java)
        knowledgeService = mock(KnowledgeService::class.java)
        val routerConfiguration = RouterConfiguration(aiService, tagService, knowledgeService)
        mockMvc = MockMvcBuilders.routerFunctions(routerConfiguration.askRouter()).build()
    }

    @Test
    fun `GET ask should return chat result`() {
        val question = "What is the meaning of life?"
        val tags = setOf("life")
        val chatResult = ChatResult("42", emptyList())

        `when`(aiService.chat(question, tags)).thenReturn(chatResult)

        mockMvc.perform(
            get("/ask")
                .param("question", question)
                .param("tags", "life")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.answer").value("42"))
    }

    @Test
    fun `GET ask should handle empty parameters`() {
        val chatResult = ChatResult("I don't know", emptyList())

        `when`(aiService.chat("", setOf(""))).thenReturn(chatResult)

        mockMvc.perform(get("/ask"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.answer").value("I don't know"))
    }

    @Test
    fun `GET tags should return available tags`() {
        val tags = listOf("tag1", "tag2")
        `when`(tagService.getTags()).thenReturn(tags)

        mockMvc.perform(get("/tags"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0]").value("tag1"))
            .andExpect(jsonPath("$[1]").value("tag2"))
    }

    @Test
    fun `GET knowledge should return rendered markdown`() {
        val document = "test-doc"
        val renderedContent = "<h1>Test</h1>"
        `when`(knowledgeService.getKnowledge(document)).thenReturn(renderedContent)

        mockMvc.perform(get("/knowledge/$document"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(content().string(renderedContent))
    }

    @Test
    fun `GET knowledge should return 404 if not found`() {
        val document = "non-existent"
        `when`(knowledgeService.getKnowledge(document)).thenReturn(null)

        mockMvc.perform(get("/knowledge/$document"))
            .andExpect(status().isNotFound)
    }
}
