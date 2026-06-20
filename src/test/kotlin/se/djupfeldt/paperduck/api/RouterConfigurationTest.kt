package se.djupfeldt.paperduck.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.function.RouterFunctions
import se.djupfeldt.paperduck.AiService
import se.djupfeldt.paperduck.ChatResult

class RouterConfigurationTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var aiService: AiService

    @BeforeEach
    fun setUp() {
        aiService = mock(AiService::class.java)
        val routerConfiguration = RouterConfiguration(aiService)
        mockMvc = MockMvcBuilders.routerFunctions(routerConfiguration.askRouter()).build()
    }

    @Test
    fun `GET ask should return chat result`() {
        val question = "What is the meaning of life?"
        val tags = setOf("life")
        val chatResult = ChatResult("42", emptyList())

        `when`(aiService.chat(question, tags)).thenReturn(chatResult)

        mockMvc.perform(get("/ask")
            .param("question", question)
            .param("tags", "life"))
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
}
