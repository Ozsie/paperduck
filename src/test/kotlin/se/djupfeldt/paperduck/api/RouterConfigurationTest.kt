package se.djupfeldt.paperduck.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import se.djupfeldt.paperduck.*

class RouterConfigurationTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var aiService: AiService
    private lateinit var tagService: TagService
    private lateinit var knowledgeService: KnowledgeService
    private lateinit var properties: PaperduckProperties

    @BeforeEach
    fun setUp() {
        aiService = mock(AiService::class.java)
        tagService = mock(TagService::class.java)
        knowledgeService = mock(KnowledgeService::class.java)
        properties = PaperduckProperties()
        val routerConfiguration = RouterConfiguration(aiService, tagService, knowledgeService, properties)
        mockMvc = MockMvcBuilders.routerFunctions(routerConfiguration.askRouter()).build()
    }

    @Test
    fun `POST ask should return chat result`() {
        val question = "What is the meaning of life?"
        val tags = setOf("life")
        val chatResult = ChatResult("42", emptyList())

        `when`(aiService.chat(question, tags, emptyList(), "anutu")).thenReturn(chatResult)

        mockMvc.perform(
            post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"question": "$question", "tags": ["life"], "history": [], "repoId": "anutu"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.answer").value("42"))
    }

    @Test
    fun `POST ask should handle empty body`() {
        val chatResult = ChatResult("I don't know", emptyList())

        `when`(aiService.chat("", emptySet(), emptyList(), null)).thenReturn(chatResult)

        mockMvc.perform(
            post("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
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
        `when`(knowledgeService.getKnowledge(document, "anutu")).thenReturn(renderedContent)

        mockMvc.perform(get("/knowledge/$document").param("repoId", "anutu"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(content().string(renderedContent))
    }

    @Test
    fun `GET knowledge should return 404 if not found`() {
        val document = "non-existent"
        `when`(knowledgeService.getKnowledge(document, null)).thenReturn(null)

        mockMvc.perform(get("/knowledge/$document"))
            .andExpect(status().isNotFound)
    }
}
