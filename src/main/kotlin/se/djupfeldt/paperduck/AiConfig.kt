package se.djupfeldt.paperduck

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.mistralai.MistralAiChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    @ConditionalOnProperty(name = ["paperduck.ai.service"], havingValue = "openai")
    fun openAiChatClient(chatModel: OpenAiChatModel): ChatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(SimpleLoggerAdvisor())
        .build()

    @Bean
    @ConditionalOnProperty(name = ["paperduck.ai.service"], havingValue = "mistral", matchIfMissing = true)
    fun mistralAiChatClient(chatModel: MistralAiChatModel): ChatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(SimpleLoggerAdvisor())
        .build()
}
