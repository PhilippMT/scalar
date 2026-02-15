import type { ApiClientPlugin } from '@scalar/types/api-reference'

import { createProvider } from './providers'
import type { LlmProviderConfig } from './types'

/**
 * Configuration options for the Agent Scalar LLM plugin.
 */
export type AgentScalarPluginOptions = {
  /**
   * The LLM provider configuration.
   * Determines which backend is used for AI chat completions.
   */
  provider: LlmProviderConfig
  /**
   * Optional URL pattern to match agent chat requests.
   * Only requests matching this pattern will be intercepted.
   *
   * @default '/vector/openapi/chat'
   */
  chatEndpointPattern?: string
}

const DEFAULT_CHAT_ENDPOINT_PATTERN = '/vector/openapi/chat'

/**
 * Creates an API Client plugin that configures a custom LLM provider
 * for Agent Scalar's AI chat functionality.
 *
 * This plugin intercepts chat requests and adds the necessary headers
 * and configuration for routing them to your chosen LLM backend
 * (OpenAI-compatible or AWS Bedrock).
 *
 * @example OpenAI-compatible provider
 * ```ts
 * import { AgentScalarPlugin } from '@scalar/agent-scalar-plugin'
 *
 * const config = {
 *   plugins: [
 *     AgentScalarPlugin({
 *       provider: {
 *         type: 'openai',
 *         baseUrl: 'https://api.openai.com/v1',
 *         apiKey: 'sk-...',
 *         model: 'gpt-4o',
 *       },
 *     }),
 *   ],
 * }
 * ```
 *
 * @example AWS Bedrock provider
 * ```ts
 * import { AgentScalarPlugin } from '@scalar/agent-scalar-plugin'
 *
 * const config = {
 *   plugins: [
 *     AgentScalarPlugin({
 *       provider: {
 *         type: 'aws-bedrock',
 *         region: 'us-east-1',
 *         accessKeyId: 'AKIA...',
 *         secretAccessKey: '...',
 *         modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
 *       },
 *     }),
 *   ],
 * }
 * ```
 */
export function AgentScalarPlugin(options: AgentScalarPluginOptions): ApiClientPlugin {
  return () => {
    const provider = createProvider(options.provider)
    const chatPattern = options.chatEndpointPattern ?? DEFAULT_CHAT_ENDPOINT_PATTERN

    return {
      name: '@scalar/agent-scalar-plugin',
      hooks: {
        /**
         * Intercepts outgoing requests that target the agent chat endpoint.
         * Adds the provider's authentication headers and rewrites the URL
         * to point to the configured LLM backend.
         */
        onBeforeRequest({ request }) {
          if (!request.url.includes(chatPattern)) {
            return
          }

          const headers = provider.buildHeaders()

          for (const [key, value] of Object.entries(headers)) {
            request.headers.set(key, value)
          }

          /**
           * Store provider metadata in a custom header so downstream
           * consumers (e.g., a proxy or middleware) know which provider
           * and endpoint to use.
           */
          request.headers.set('X-Scalar-Llm-Provider', provider.name)
          request.headers.set('X-Scalar-Llm-Endpoint', provider.buildUrl())
        },
      },
    }
  }
}
