package se.djupfeldt.paperduck.information

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import se.djupfeldt.paperduck.PaperduckProperties

@Service
class GitHubService(
    private val restTemplate: RestTemplate,
    private val properties: PaperduckProperties
) {
    private val log = LoggerFactory.getLogger(GitHubService::class.java)

    data class GitHubContent(
        val name: String,
        val path: String,
        val type: String,
        val download_url: String?
    )

    private fun getRepo(repoId: String?): PaperduckProperties.RepositoryConfig = if (repoId == null) {
        properties.repositories.firstOrNull() ?: throw IllegalStateException("No repositories configured")
    } else {
        properties.repositories.find { it.id == repoId }
            ?: properties.repositories.firstOrNull()
            ?: throw IllegalStateException("No repositories configured")
    }

    fun listFiles(directory: String, repoId: String? = null): List<String> {
        val repo = getRepo(repoId)
        val url = "${repo.repoUrl}/contents/$directory"
        log.info("Listing files from GitHub: $url")
        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<GitHubContent>>() {}
            )
            response.body?.filter { it.type == "file" && it.name.endsWith(".md") }?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            log.error("Failed to list files from GitHub: $url", e)
            emptyList()
        }
    }

    fun getFileContent(directory: String?, fileName: String, repoId: String? = null): String? {
        val repo = getRepo(repoId)
        val url = if (directory != null) {
            "${repo.rawUrl}/$directory/$fileName"
        } else {
            "${repo.rawUrl}/$fileName"
        }
        log.info("Fetching file content from GitHub: $url")
        return try {
            restTemplate.getForObject(url, String::class.java)
        } catch (e: Exception) {
            log.error("Failed to fetch file content from GitHub: $url", e)
            null
        }
    }
}
