package se.djupfeldt.paperduck

import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.any

@SpringBootTest
class AiServiceTest {

    @Autowired
    lateinit var aiService: AiService

    @MockitoBean
    lateinit var chatClient: ChatClient

    @Test
    fun contextLoads() {
    }
}
