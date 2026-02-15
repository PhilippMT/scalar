import type { LlmProviderConfig } from '../types'
import { createAwsBedrockProvider } from './aws-bedrock-provider'
import { createOpenAiProvider } from './openai-provider'

/**
 * Represents a chat message in the OpenAI format.
 */
export type ChatMessage = {
  role: 'system' | 'user' | 'assistant'
  content: string
}

/**
 * Represents a chat completion request.
 */
export type ChatCompletionRequest = {
  messages: ChatMessage[]
  stream?: boolean
}

/**
 * Represents a non-streaming chat completion response.
 */
export type ChatCompletionResponse = {
  id: string
  choices: {
    message: ChatMessage
    finish_reason: string
  }[]
  usage?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

/**
 * A provider handles sending chat completion requests to a specific LLM backend.
 */
export type LlmProvider = {
  /** Human-readable name of the provider */
  name: string
  /** Build the headers for the request */
  buildHeaders: () => Record<string, string>
  /** Build the full URL for the chat completions endpoint */
  buildUrl: () => string
  /** Build the request body from chat messages */
  buildRequestBody: (request: ChatCompletionRequest) => Record<string, unknown>
}

/**
 * Creates an LLM provider instance from a configuration object.
 */
export function createProvider(config: LlmProviderConfig): LlmProvider {
  switch (config.type) {
    case 'openai':
      return createOpenAiProvider(config)
    case 'aws-bedrock':
      return createAwsBedrockProvider(config)
  }
}

export { createOpenAiProvider } from './openai-provider'
export { createAwsBedrockProvider } from './aws-bedrock-provider'
