package se.djupfeldt.paperduck

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class TagService(private val resourceLoader: ResourceLoader) {
    fun getTags(): List<String> {
        return try {
            val resource = resourceLoader.getResource("classpath:tags.md")
            resource.inputStream.bufferedReader().use { reader ->
                reader.readLine()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
