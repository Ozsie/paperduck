package se.djupfeldt.paperduck

import org.springframework.stereotype.Service

@Service
class TagService(
    private val gitHubService: GitHubService
) {
    private val log = org.slf4j.LoggerFactory.getLogger(TagService::class.java)

    fun getTags(repoId: String? = null): List<String> {
        val remoteTags = if (repoId != null) getRemoteTags(repoId) else emptyList()
        return remoteTags.distinct()
    }

    private fun getRemoteTags(repoId: String): List<String> = try {
        val content = gitHubService.getFileContent(null, "tags", repoId)
        content?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() } ?: emptyList()
    } catch (e: Exception) {
        log.error("Failed to load remote tags for repo $repoId", e)
        emptyList()
    }
}
