import { describe, expect, it } from 'vitest'

import { createOpenAiProvider } from './openai-provider'

describe('openai-provider', () => {
  const baseConfig = {
    type: 'openai' as const,
    baseUrl: 'https://api.openai.com/v1',
    apiKey: 'sk-test-key',
    model: 'gpt-4o',
  }

  describe('createOpenAiProvider', () => {
    it('creates a provider with the correct name', () => {
      const provider = createOpenAiProvider(baseConfig)
      expect(provider.name).toBe('openai')
    })

    it('builds the correct URL for chat completions', () => {
      const provider = createOpenAiProvider(baseConfig)
      expect(provider.buildUrl()).toBe('https://api.openai.com/v1/chat/completions')
    })

    it('strips trailing slashes from the base URL', () => {
      const provider = createOpenAiProvider({
        ...baseConfig,
        baseUrl: 'https://api.openai.com/v1/',
      })
      expect(provider.buildUrl()).toBe('https://api.openai.com/v1/chat/completions')
    })

    it('builds headers with Authorization', () => {
      const provider = createOpenAiProvider(baseConfig)
      const headers = provider.buildHeaders()

      expect(headers['Content-Type']).toBe('application/json')
      expect(headers['Authorization']).toBe('Bearer sk-test-key')
    })

    it('includes organization ID when provided', () => {
      const provider = createOpenAiProvider({
        ...baseConfig,
        organizationId: 'org-12345',
      })
      const headers = provider.buildHeaders()

      expect(headers['OpenAI-Organization']).toBe('org-12345')
    })

    it('does not include organization header when not provided', () => {
      const provider = createOpenAiProvider(baseConfig)
      const headers = provider.buildHeaders()

      expect(headers['OpenAI-Organization']).toBeUndefined()
    })

    it('includes custom headers when provided', () => {
      const provider = createOpenAiProvider({
        ...baseConfig,
        headers: { 'X-Custom': 'value' },
      })
      const headers = provider.buildHeaders()

      expect(headers['X-Custom']).toBe('value')
      expect(headers['Authorization']).toBe('Bearer sk-test-key')
    })

    it('builds a request body with model and messages', () => {
      const provider = createOpenAiProvider(baseConfig)
      const body = provider.buildRequestBody({
        messages: [
          { role: 'system', content: 'You are helpful.' },
          { role: 'user', content: 'Hello' },
        ],
      })

      expect(body).toEqual({
        model: 'gpt-4o',
        messages: [
          { role: 'system', content: 'You are helpful.' },
          { role: 'user', content: 'Hello' },
        ],
        stream: false,
      })
    })

    it('sets stream to true when requested', () => {
      const provider = createOpenAiProvider(baseConfig)
      const body = provider.buildRequestBody({
        messages: [{ role: 'user', content: 'Hello' }],
        stream: true,
      })

      expect(body.stream).toBe(true)
    })

    it('works with Ollama-style local URLs', () => {
      const provider = createOpenAiProvider({
        ...baseConfig,
        baseUrl: 'http://localhost:11434/v1',
      })

      expect(provider.buildUrl()).toBe('http://localhost:11434/v1/chat/completions')
    })
  })
})
