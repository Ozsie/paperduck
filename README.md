# Paperduck

Paperduck is an AI-powered writing assistant designed for world-builders, novelists, and storytellers. It helps you manage your world's lore, characters, and plot lines by providing an intelligent chat interface that has direct access to your knowledge base and story drafts.

## Features

- **AI Chat Interface**: Ask questions about your world and stories.
- **RAG (Retrieval-Augmented Generation)**: The AI automatically searches your Markdown files for relevant context based on tags.
- **Knowledge Base Integration**: Direct links to lore documents are generated in the chat, allowing for quick reference.
- **Tag System**: Organize your knowledge and stories using a simple tag-based system.
- **Markdown-to-HTML Rendering**: Knowledge documents are rendered directly in the web UI.

## Getting Started

### Prerequisites

- **Java 21**
- **An AI Model Provider**: 
  - **Mistral AI**: Set the `MISTRAL_API_KEY` environment variable.
  - **OpenAI / Local LLM (LM Studio)**: Configurable in `application.properties`.

### Configuration

The application uses `src/main/resources/application.properties` for configuration. You can switch between AI providers using `paperduck.ai.service`:

- `paperduck.ai.service=mistral` (Default)
- `paperduck.ai.service=openai` (Can be used with local providers like LM Studio at `http://localhost:1234/v1`)

You must also set the following environment variables for security:
- `PAPERDUCK_USERNAME`
- `PAPERDUCK_PASSWORD`

### Running the Application

Use the Gradle wrapper to start the application:

```bash
./gradlew bootRun
```

The application will be available at `http://localhost:8080`.

## Project Structure

- `src/main/resources/knowledge/`: Lore and world-building Markdown files.
- `src/main/resources/stories/`: Story drafts and narrative content.
- `src/main/resources/static/index.html`: The web-based chat interface.
- `src/main/kotlin/se/djupfeldt/paperduck/`: Core logic including AI service, tools, and tag management.

## API Endpoints

- `GET /ask?question=...&tags=...`: Ask the AI a question.
- `GET /tags`: List all available tags.
- `PUT /tags/{tag}`: Add a new tag.
- `GET /knowledge/{document}`: Retrieve a rendered knowledge document.

## How it Works

Paperduck uses **Spring AI** to interface with Large Language Models. When you ask a question, the application:
1. Identifies relevant tags.
2. Uses `WritingTools` to retrieve content from your local Markdown files that match those tags.
3. Provides this content as context to the AI.
4. The AI generates a response, including mandatory links to your lore documents for easy navigation.
