import { describe, expect, it } from 'vitest'

import { AgentScalarPlugin } from './plugin'

describe('plugin', () => {
  describe('AgentScalarPlugin', () => {
    it('returns a valid API client plugin factory', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'openai',
          baseUrl: 'https://api.openai.com/v1',
          apiKey: 'sk-test',
          model: 'gpt-4o',
        },
      })

      const plugin = pluginFactory()
      expect(plugin.name).toBe('@scalar/agent-scalar-plugin')
      expect(plugin.hooks).toBeDefined()
      expect(plugin.hooks?.onBeforeRequest).toBeDefined()
    })

    it('adds provider headers to matching chat requests', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'openai',
          baseUrl: 'https://api.openai.com/v1',
          apiKey: 'sk-test',
          model: 'gpt-4o',
        },
      })

      const plugin = pluginFactory()
      const request = new Request('https://example.com/vector/openapi/chat', {
        method: 'POST',
        body: JSON.stringify({ messages: [] }),
      })

      plugin.hooks?.onBeforeRequest?.({ request })

      expect(request.headers.get('Authorization')).toBe('Bearer sk-test')
      expect(request.headers.get('X-Scalar-Llm-Provider')).toBe('openai')
      expect(request.headers.get('X-Scalar-Llm-Endpoint')).toBe('https://api.openai.com/v1/chat/completions')
    })

    it('does not modify requests that do not match the chat endpoint', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'openai',
          baseUrl: 'https://api.openai.com/v1',
          apiKey: 'sk-test',
          model: 'gpt-4o',
        },
      })

      const plugin = pluginFactory()
      const request = new Request('https://example.com/api/users', {
        method: 'GET',
      })

      plugin.hooks?.onBeforeRequest?.({ request })

      expect(request.headers.get('Authorization')).toBeNull()
      expect(request.headers.get('X-Scalar-Llm-Provider')).toBeNull()
    })

    it('allows customizing the chat endpoint pattern', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'openai',
          baseUrl: 'https://api.openai.com/v1',
          apiKey: 'sk-test',
          model: 'gpt-4o',
        },
        chatEndpointPattern: '/custom/chat',
      })

      const plugin = pluginFactory()

      // Should not match default pattern
      const defaultRequest = new Request('https://example.com/vector/openapi/chat', {
        method: 'POST',
        body: JSON.stringify({ messages: [] }),
      })
      plugin.hooks?.onBeforeRequest?.({ request: defaultRequest })
      expect(defaultRequest.headers.get('X-Scalar-Llm-Provider')).toBeNull()

      // Should match custom pattern
      const customRequest = new Request('https://example.com/custom/chat', {
        method: 'POST',
        body: JSON.stringify({ messages: [] }),
      })
      plugin.hooks?.onBeforeRequest?.({ request: customRequest })
      expect(customRequest.headers.get('X-Scalar-Llm-Provider')).toBe('openai')
    })

    it('works with AWS Bedrock provider', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'aws-bedrock',
          region: 'us-east-1',
          accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
          secretAccessKey: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY',
          modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
        },
      })

      const plugin = pluginFactory()
      const request = new Request('https://example.com/vector/openapi/chat', {
        method: 'POST',
        body: JSON.stringify({ messages: [] }),
      })

      plugin.hooks?.onBeforeRequest?.({ request })

      expect(request.headers.get('X-Scalar-Llm-Provider')).toBe('aws-bedrock')
      expect(request.headers.get('X-Aws-Access-Key-Id')).toBe('AKIAIOSFODNN7EXAMPLE')
      expect(request.headers.get('X-Aws-Region')).toBe('us-east-1')
      expect(request.headers.get('X-Scalar-Llm-Endpoint')).toContain('bedrock-runtime.us-east-1.amazonaws.com')
    })

    it('includes Bedrock session token when provided', () => {
      const pluginFactory = AgentScalarPlugin({
        provider: {
          type: 'aws-bedrock',
          region: 'us-east-1',
          accessKeyId: 'AKIAIOSFODNN7EXAMPLE',
          secretAccessKey: 'secret',
          sessionToken: 'session-token-value',
          modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
        },
      })

      const plugin = pluginFactory()
      const request = new Request('https://example.com/vector/openapi/chat', {
        method: 'POST',
        body: JSON.stringify({ messages: [] }),
      })

      plugin.hooks?.onBeforeRequest?.({ request })

      expect(request.headers.get('X-Aws-Session-Token')).toBe('session-token-value')
    })
  })
})
