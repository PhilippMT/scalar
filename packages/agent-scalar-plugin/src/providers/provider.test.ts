import { describe, expect, it } from 'vitest'

import { createProvider } from './provider'

describe('provider', () => {
  describe('createProvider', () => {
    it('creates an OpenAI provider from config', () => {
      const provider = createProvider({
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
        apiKey: 'sk-test',
        model: 'gpt-4o',
      })

      expect(provider.name).toBe('openai')
      expect(provider.buildUrl()).toContain('chat/completions')
    })

    it('creates an AWS Bedrock provider from config', () => {
      const provider = createProvider({
        type: 'aws-bedrock',
        region: 'us-east-1',
        accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
        secretAccessKey: 'secret',
        modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
      })

      expect(provider.name).toBe('aws-bedrock')
      expect(provider.buildUrl()).toContain('bedrock-runtime')
    })
  })
})
