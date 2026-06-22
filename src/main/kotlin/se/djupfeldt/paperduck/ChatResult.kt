package se.djupfeldt.paperduck

data class ChatResult(val answer: String?, val toolCalls: List<ToolInvocation>, val tagsUsed: List<String> = emptyList())

data class ChatMessage(val role: String, val content: String)
