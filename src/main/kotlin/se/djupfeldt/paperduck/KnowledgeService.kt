package se.djupfeldt.paperduck

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class KnowledgeService(private val gitHubService: GitHubService) {
    private val log = LoggerFactory.getLogger(KnowledgeService::class.java)

    private val options = MutableDataSet()
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun getKnowledge(document: String, repoId: String? = null): String? {
        if (document.isBlank()) return null
        val fileName = if (document.endsWith(".md").not()) "$document.md" else document
        val markdown = gitHubService.getFileContent("knowledge", fileName, repoId) ?: return null

        val documentNode = parser.parse(markdown).also { log.info("Parsed markdown for document: $document") }
        return renderer.render(documentNode)
    }
}
