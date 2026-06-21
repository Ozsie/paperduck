package se.djupfeldt.paperduck

data class ChatResult(val answer: String?, val toolCalls: List<ToolInvocation>, val tagsUsed: List<String> = emptyList())
