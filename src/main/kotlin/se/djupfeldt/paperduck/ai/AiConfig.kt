package se.djupfeldt.paperduck.ai

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.mistralai.MistralAiChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {
    private val log = LoggerFactory.getLogger(AiConfig::class.java)

    @Bean
    @ConditionalOnProperty(name = ["paperduck.ai.service"], havingValue = "openai")
    fun openAiChatClient(chatModel: OpenAiChatModel) = ChatClient.builder(chatModel)
        .defaultAdvisors(SimpleLoggerAdvisor())
        .build().also {
            log.info("Using OpenAI chat client")
        }

    @Bean
    @ConditionalOnProperty(name = ["paperduck.ai.service"], havingValue = "mistral", matchIfMissing = true)
    fun mistralAiChatClient(chatModel: MistralAiChatModel) = ChatClient.builder(chatModel)
        .defaultAdvisors(SimpleLoggerAdvisor())
        .build().also {
            log.info("Using Mistral AI chat client")
        }
}
