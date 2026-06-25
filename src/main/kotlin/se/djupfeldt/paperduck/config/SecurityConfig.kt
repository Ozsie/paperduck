package se.djupfeldt.paperduck.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests { authorize ->
            authorize.anyRequest().authenticated()
        }
        .httpBasic(Customizer.withDefaults())
        .csrf { csrf -> csrf.disable() } // Disabling CSRF for simplicity in this small project
        .build()
}
