package se.djupfeldt.paperduck.ai

data class ChatResult(val answer: String?, val toolCalls: List<ToolInvocation>, val tagsUsed: List<String> = emptyList())

data class ChatMessage(val role: String, val content: String)

data class ToolInvocation(val tool: String, val tags: List<String>)
