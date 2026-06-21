package se.djupfeldt.paperduck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mistralai.MistralAiChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AiConfigIntegrationTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(AiConfig::class.java)

    @Test
    fun `should create mistral chat client by default`() {
        contextRunner
            .withBean(MistralAiChatModel::class.java, { org.mockito.Mockito.mock(MistralAiChatModel::class.java) })
            .run { context ->
                assertThat(context).hasSingleBean(ChatClient::class.java)
                assertThat(context).hasBean("mistralAiChatClient")
            }
    }

    @Test
    fun `should create mistral chat client when property is mistral`() {
        contextRunner
            .withPropertyValues("paperduck.ai.service=mistral")
            .withBean(MistralAiChatModel::class.java, { org.mockito.Mockito.mock(MistralAiChatModel::class.java) })
            .run { context ->
                assertThat(context).hasSingleBean(ChatClient::class.java)
                assertThat(context).hasBean("mistralAiChatClient")
            }
    }

    @Test
    fun `should create openai chat client when property is openai`() {
        contextRunner
            .withPropertyValues("paperduck.ai.service=openai")
            .withBean(OpenAiChatModel::class.java, { org.mockito.Mockito.mock(OpenAiChatModel::class.java) })
            .run { context ->
                assertThat(context).hasSingleBean(ChatClient::class.java)
                assertThat(context).hasBean("openAiChatClient")
            }
    }
}
