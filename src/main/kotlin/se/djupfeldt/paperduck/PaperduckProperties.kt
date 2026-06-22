package se.djupfeldt.paperduck

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "paperduck")
class PaperduckProperties {
    var repositories: List<RepositoryConfig> = mutableListOf()

    data class RepositoryConfig(
        var id: String = "",
        var name: String = "",
        var repoUrl: String = "",
        var rawUrl: String = ""
    )
}
