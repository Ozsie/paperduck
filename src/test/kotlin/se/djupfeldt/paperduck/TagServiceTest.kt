package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class TagServiceTest {

    @Test
    fun `getTags should include remote tags when repoId is provided`() {
        val gitHubService = mock(GitHubService::class.java)
        val remoteContent = "remote1, remote2, local1"

        `when`(gitHubService.getFileContent(null, "tags", "repo1")).thenReturn(remoteContent)

        val tagService = TagService(gitHubService)
        val tags = tagService.getTags("repo1")

        assertEquals(listOf("remote1", "remote2", "local1"), tags)
    }

    @Test
    fun `getTags should return empty list when no repoId provided`() {
        val gitHubService = mock(GitHubService::class.java)
        val tagService = TagService(gitHubService)
        val tags = tagService.getTags(null)
        assertEquals(emptyList<String>(), tags)
    }
}
