import type { AwsBedrockProviderConfig } from '../types'
import type { ChatCompletionRequest, LlmProvider } from './provider'

/**
 * Creates an AWS Bedrock LLM provider.
 *
 * Uses the Bedrock Runtime `converse` API to invoke foundation models.
 * Authentication is handled via AWS Signature V4 signing, which the consumer
 * must apply using the headers and request body provided by this provider.
 *
 * For direct usage, the consumer should use an AWS SDK or signing library
 * to sign the requests before sending them.
 *
 * @see https://docs.aws.amazon.com/bedrock/latest/APIReference/API_runtime_Converse.html
 */
export function createAwsBedrockProvider(config: AwsBedrockProviderConfig): LlmProvider {
  return {
    name: 'aws-bedrock',

    buildHeaders(): Record<string, string> {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        /**
         * AWS credentials are passed as headers so the consuming application
         * can use them with an AWS SDK or signing utility.
         */
        'X-Aws-Access-Key-Id': config.accessKeyId,
        'X-Aws-Secret-Access-Key': config.secretAccessKey,
        'X-Aws-Region': config.region,
      }

      if (config.sessionToken) {
        headers['X-Aws-Session-Token'] = config.sessionToken
      }

      if (config.headers) {
        Object.assign(headers, config.headers)
      }

      return headers
    },

    buildUrl(): string {
      return `https://bedrock-runtime.${config.region}.amazonaws.com/model/${encodeURIComponent(config.modelId)}/converse`
    },

    buildRequestBody(request: ChatCompletionRequest): Record<string, unknown> {
      return {
        modelId: config.modelId,
        messages: request.messages.map((msg) => ({
          role: msg.role === 'assistant' ? 'assistant' : 'user',
          content: [{ text: msg.content }],
        })),
        ...(request.messages.some((msg) => msg.role === 'system')
          ? {
              system: request.messages
                .filter((msg) => msg.role === 'system')
                .map((msg) => ({ text: msg.content })),
            }
          : {}),
      }
    },
  }
}
