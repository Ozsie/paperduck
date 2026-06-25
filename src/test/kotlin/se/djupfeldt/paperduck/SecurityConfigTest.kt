package se.djupfeldt.paperduck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.web.SecurityFilterChain
import se.djupfeldt.paperduck.config.SecurityConfig

class SecurityConfigTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(SecurityConfig::class.java)

    @Test
    fun `should create security filter chain bean`() {
        contextRunner
            .run { context ->
                assertThat(context).hasSingleBean(SecurityFilterChain::class.java)
            }
    }
}
