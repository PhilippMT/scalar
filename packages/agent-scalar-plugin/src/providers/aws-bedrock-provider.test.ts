import { describe, expect, it } from 'vitest'

import { createAwsBedrockProvider } from './aws-bedrock-provider'

describe('aws-bedrock-provider', () => {
  const baseConfig = {
    type: 'aws-bedrock' as const,
    region: 'us-east-1',
    accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
    secretAccessKey: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY',
    modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
  }

  describe('createAwsBedrockProvider', () => {
    it('creates a provider with the correct name', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      expect(provider.name).toBe('aws-bedrock')
    })

    it('builds the correct Bedrock converse URL', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      expect(provider.buildUrl()).toBe(
        'https://bedrock-runtime.us-east-1.amazonaws.com/model/anthropic.claude-3-5-sonnet-20241022-v2%3A0/converse',
      )
    })

    it('builds the URL with the correct region', () => {
      const provider = createAwsBedrockProvider({
        ...baseConfig,
        region: 'eu-west-1',
      })
      expect(provider.buildUrl()).toContain('bedrock-runtime.eu-west-1.amazonaws.com')
    })

    it('builds headers with AWS credential headers', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      const headers = provider.buildHeaders()

      expect(headers['Content-Type']).toBe('application/json')
      expect(headers['X-Aws-Access-Key-Id']).toBe('AKIAIOSFODNN7EXAMPLE')
      expect(headers['X-Aws-Secret-Access-Key']).toBe('wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY')
      expect(headers['X-Aws-Region']).toBe('us-east-1')
    })

    it('includes session token when provided', () => {
      const provider = createAwsBedrockProvider({
        ...baseConfig,
        sessionToken: 'FwoGZXIvYXdzEBYaDHqa0AP1',
      })
      const headers = provider.buildHeaders()

      expect(headers['X-Aws-Session-Token']).toBe('FwoGZXIvYXdzEBYaDHqa0AP1')
    })

    it('does not include session token header when not provided', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      const headers = provider.buildHeaders()

      expect(headers['X-Aws-Session-Token']).toBeUndefined()
    })

    it('includes custom headers when provided', () => {
      const provider = createAwsBedrockProvider({
        ...baseConfig,
        headers: { 'X-Custom': 'value' },
      })
      const headers = provider.buildHeaders()

      expect(headers['X-Custom']).toBe('value')
      expect(headers['X-Aws-Access-Key-Id']).toBe('AKIAIOSFODNN7EXAMPLE')
    })

    it('separates system messages from user/assistant messages in the request body', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      const body = provider.buildRequestBody({
        messages: [
          { role: 'system', content: 'You are helpful.' },
          { role: 'user', content: 'Hello' },
          { role: 'assistant', content: 'Hi there!' },
        ],
      })

      expect(body.modelId).toBe('anthropic.claude-3-5-sonnet-20241022-v2:0')
      expect(body.system).toEqual([{ text: 'You are helpful.' }])
      expect(body.messages).toEqual([
        { role: 'user', content: [{ text: 'Hello' }] },
        { role: 'assistant', content: [{ text: 'Hi there!' }] },
      ])
    })

    it('omits system field when no system messages exist', () => {
      const provider = createAwsBedrockProvider(baseConfig)
      const body = provider.buildRequestBody({
        messages: [{ role: 'user', content: 'Hello' }],
      })

      expect(body.system).toBeUndefined()
      expect(body.messages).toEqual([{ role: 'user', content: [{ text: 'Hello' }] }])
    })
  })
})
