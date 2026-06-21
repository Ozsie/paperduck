package se.djupfeldt.paperduck

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class KnowledgeService(private val resourceLoader: ResourceLoader) {
    private val log = LoggerFactory.getLogger(KnowledgeService::class.java)

    private val options = MutableDataSet()
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun getKnowledge(document: String): String? {
        if (document.isBlank()) return null
        val fileName = if (document.endsWith(".md").not()) "$document.md" else document
        val resource = try {
            resourceLoader.getResource("classpath:knowledge/$fileName")
        } catch (_: Exception) {
            log.warn("No knowledge file found for document: $document")
            null
        }
        val file = if (resource != null && resource.exists()) resource.file else null
        val markdown = file?.readText(Charsets.UTF_8) ?: return null

        val documentNode = parser.parse(markdown).also { log.info("Parsed markdown for document: $document") }
        return renderer.render(documentNode)
    }
}
