package se.djupfeldt.paperduck

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.io.ByteArrayInputStream

class TagServiceTest {

    @Test
    fun `getTags should return list of tags from resources`() {
        val resourceLoader = mock(ResourceLoader::class.java)
        val resource = mock(Resource::class.java)
        val content = "tag1, tag2, tag3"
        
        `when`(resourceLoader.getResource("classpath:tags.md")).thenReturn(resource)
        `when`(resource.inputStream).thenReturn(ByteArrayInputStream(content.toByteArray()))
        
        val tagService = TagService(resourceLoader)
        val tags = tagService.getTags()
        
        assertEquals(listOf("tag1", "tag2", "tag3"), tags)
    }

    @Test
    fun `getTags should return empty list on error`() {
        val resourceLoader = mock(ResourceLoader::class.java)
        `when`(resourceLoader.getResource("classpath:tags.md")).thenThrow(RuntimeException("Error"))

        val tagService = TagService(resourceLoader)
        val tags = tagService.getTags()

        assertEquals(emptyList<String>(), tags)
    }
}
