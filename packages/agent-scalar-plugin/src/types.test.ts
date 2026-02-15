import { describe, expect, it } from 'vitest'

import { awsBedrockProviderConfigSchema, llmProviderConfigSchema, openAiProviderConfigSchema } from './types'

describe('types', () => {
  describe('openAiProviderConfigSchema', () => {
    it('validates a valid OpenAI config', () => {
      const result = openAiProviderConfigSchema.safeParse({
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
        apiKey: 'sk-test',
        model: 'gpt-4o',
      })

      expect(result.success).toBe(true)
    })

    it('validates with optional fields', () => {
      const result = openAiProviderConfigSchema.safeParse({
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
        apiKey: 'sk-test',
        model: 'gpt-4o',
        organizationId: 'org-12345',
        headers: { 'X-Custom': 'value' },
      })

      expect(result.success).toBe(true)
    })

    it('rejects an invalid URL', () => {
      const result = openAiProviderConfigSchema.safeParse({
        type: 'openai',
        baseUrl: 'not-a-url',
        apiKey: 'sk-test',
        model: 'gpt-4o',
      })

      expect(result.success).toBe(false)
    })

    it('rejects missing required fields', () => {
      const result = openAiProviderConfigSchema.safeParse({
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
      })

      expect(result.success).toBe(false)
    })
  })

  describe('awsBedrockProviderConfigSchema', () => {
    it('validates a valid Bedrock config', () => {
      const result = awsBedrockProviderConfigSchema.safeParse({
        type: 'aws-bedrock',
        region: 'us-east-1',
        accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
        secretAccessKey: 'secret',
        modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
      })

      expect(result.success).toBe(true)
    })

    it('validates with optional session token', () => {
      const result = awsBedrockProviderConfigSchema.safeParse({
        type: 'aws-bedrock',
        region: 'us-east-1',
        accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
        secretAccessKey: 'secret',
        sessionToken: 'token',
        modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
      })

      expect(result.success).toBe(true)
    })

    it('rejects missing required fields', () => {
      const result = awsBedrockProviderConfigSchema.safeParse({
        type: 'aws-bedrock',
        region: 'us-east-1',
      })

      expect(result.success).toBe(false)
    })
  })

  describe('llmProviderConfigSchema', () => {
    it('accepts OpenAI config', () => {
      const result = llmProviderConfigSchema.safeParse({
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
        apiKey: 'sk-test',
        model: 'gpt-4o',
      })

      expect(result.success).toBe(true)
    })

    it('accepts Bedrock config', () => {
      const result = llmProviderConfigSchema.safeParse({
        type: 'aws-bedrock',
        region: 'us-east-1',
        accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
        secretAccessKey: 'secret',
        modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
      })

      expect(result.success).toBe(true)
    })

    it('rejects an unknown provider type', () => {
      const result = llmProviderConfigSchema.safeParse({
        type: 'unknown-provider',
        baseUrl: 'https://example.com',
      })

      expect(result.success).toBe(false)
    })
  })
})
