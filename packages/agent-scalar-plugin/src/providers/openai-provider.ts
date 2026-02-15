import type { OpenAiProviderConfig } from '../types'
import type { ChatCompletionRequest, LlmProvider } from './provider'

/**
 * Creates an OpenAI-compatible LLM provider.
 *
 * Works with any service that implements the OpenAI Chat Completions API:
 * - OpenAI (https://api.openai.com/v1)
 * - Azure OpenAI
 * - Ollama (http://localhost:11434/v1)
 * - vLLM, LiteLLM, and other compatible services
 */
export function createOpenAiProvider(config: OpenAiProviderConfig): LlmProvider {
  return {
    name: 'openai',

    buildHeaders(): Record<string, string> {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${config.apiKey}`,
      }

      if (config.organizationId) {
        headers['OpenAI-Organization'] = config.organizationId
      }

      if (config.headers) {
        Object.assign(headers, config.headers)
      }

      return headers
    },

    buildUrl(): string {
      const base = config.baseUrl.replace(/\/+$/, '')
      return `${base}/chat/completions`
    },

    buildRequestBody(request: ChatCompletionRequest): Record<string, unknown> {
      return {
        model: config.model,
        messages: request.messages,
        stream: request.stream ?? false,
      }
    },
  }
}
