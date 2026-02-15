import { z } from 'zod'

/**
 * Configuration for an OpenAI-compatible LLM provider.
 *
 * Works with any endpoint that implements the OpenAI Chat Completions API,
 * such as OpenAI, Azure OpenAI, Ollama, vLLM, LiteLLM, or any compatible service.
 */
export const openAiProviderConfigSchema = z.object({
  type: z.literal('openai'),
  /**
   * Base URL for the OpenAI-compatible API.
   *
   * @example 'https://api.openai.com/v1'
   * @example 'https://my-deployment.openai.azure.com/openai/deployments/my-model'
   * @example 'http://localhost:11434/v1' (Ollama)
   */
  baseUrl: z.string().url(),
  /**
   * API key for authentication.
   * Sent as a Bearer token in the Authorization header.
   */
  apiKey: z.string(),
  /**
   * The model identifier to use for completions.
   *
   * @example 'gpt-4o'
   * @example 'gpt-4o-mini'
   */
  model: z.string(),
  /**
   * Optional organization ID for OpenAI API requests.
   */
  organizationId: z.string().optional(),
  /**
   * Optional extra headers to include in every request.
   */
  headers: z.record(z.string(), z.string()).optional(),
})

/**
 * Configuration for an AWS Bedrock LLM provider.
 *
 * Uses the AWS Bedrock Runtime API to invoke foundation models.
 */
export const awsBedrockProviderConfigSchema = z.object({
  type: z.literal('aws-bedrock'),
  /**
   * The AWS region where the Bedrock model is available.
   *
   * @example 'us-east-1'
   * @example 'eu-west-1'
   */
  region: z.string(),
  /**
   * AWS access key ID for authentication.
   */
  accessKeyId: z.string(),
  /**
   * AWS secret access key for authentication.
   */
  secretAccessKey: z.string(),
  /**
   * Optional AWS session token for temporary credentials.
   */
  sessionToken: z.string().optional(),
  /**
   * The Bedrock model identifier.
   *
   * @example 'anthropic.claude-3-5-sonnet-20241022-v2:0'
   * @example 'amazon.titan-text-express-v1'
   * @example 'meta.llama3-70b-instruct-v1:0'
   */
  modelId: z.string(),
  /**
   * Optional extra headers to include in every request.
   */
  headers: z.record(z.string(), z.string()).optional(),
})

/**
 * Union of all supported LLM provider configurations.
 */
export const llmProviderConfigSchema = z.discriminatedUnion('type', [
  openAiProviderConfigSchema,
  awsBedrockProviderConfigSchema,
])

export type OpenAiProviderConfig = z.infer<typeof openAiProviderConfigSchema>
export type AwsBedrockProviderConfig = z.infer<typeof awsBedrockProviderConfigSchema>
export type LlmProviderConfig = z.infer<typeof llmProviderConfigSchema>
