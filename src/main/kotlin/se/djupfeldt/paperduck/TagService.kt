package se.djupfeldt.paperduck

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class TagService(private val resourceLoader: ResourceLoader) {
    private val log = org.slf4j.LoggerFactory.getLogger(TagService::class.java)
    fun getTags(): List<String> {
        return try {
            val resource = resourceLoader.getResource("classpath:tags.md")
            resource.inputStream.bufferedReader().use { reader ->
                reader.readLine()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            } ?: emptyList()
        } catch (e: Exception) {
            log.error("Failed to load tags", e)
            emptyList()
        }
    }

    fun addTag(tag: String) {
        try {
            val resource = resourceLoader.getResource("classpath:tags.md")
            val file = resource.file
            log.info("Attempting to write to file: ${file.absolutePath}")
            val content = file.readText()
            
            val updatedContent = if (content.trim().isNotEmpty() && !content.trim().endsWith(",")) {
                "$content, $tag"
            } else {
                "${content.trim()}$tag"
            }
            
            file.writeText(updatedContent)
            log.info("Successfully added tag '$tag'. New content: ${file.readText()}")
            
            // Also try to write to src/main/resources if we are in a development environment
            if (file.absolutePath.contains("/build/resources/main/")) {
                val srcPath = file.absolutePath.replace("/build/resources/main/", "/src/main/resources/")
                val srcFile = java.io.File(srcPath)
                if (srcFile.exists()) {
                    srcFile.writeText(updatedContent)
                    log.info("Also updated source file: $srcPath")
                }
            }
        } catch (e: Exception) {
            log.error("Failed to add tag to file", e)
        }
    }
}
