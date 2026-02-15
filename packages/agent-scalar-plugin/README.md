# Scalar Agent Scalar Plugin

[![Version](https://img.shields.io/npm/v/%40scalar/agent-scalar-plugin)](https://www.npmjs.com/package/@scalar/agent-scalar-plugin)
[![Downloads](https://img.shields.io/npm/dm/%40scalar/agent-scalar-plugin)](https://www.npmjs.com/package/@scalar/agent-scalar-plugin)
[![License](https://img.shields.io/npm/l/%40scalar%2Fagent-scalar-plugin)](https://www.npmjs.com/package/@scalar/agent-scalar-plugin)

A plugin for configuring custom LLM providers with [Agent Scalar](https://scalar.com/products/agent/getting-started). Route your AI chat requests to OpenAI-compatible endpoints or AWS Bedrock instead of the default Scalar backend.

## Installation

```bash
npm install @scalar/agent-scalar-plugin
```

## Usage

### OpenAI-Compatible Provider

Works with any service that implements the OpenAI Chat Completions API: OpenAI, Azure OpenAI, Ollama, vLLM, LiteLLM, and more.

```typescript
import { AgentScalarPlugin } from '@scalar/agent-scalar-plugin'

const configuration = {
  url: 'https://registry.scalar.com/@scalar/apis/galaxy?format=json',
  plugins: [
    AgentScalarPlugin({
      provider: {
        type: 'openai',
        baseUrl: 'https://api.openai.com/v1',
        apiKey: 'sk-...',
        model: 'gpt-4o',
      },
    }),
  ],
}
```

#### OpenAI Configuration Options

| Property         | Type                          | Required | Description                        |
| ---------------- | ----------------------------- | -------- | ---------------------------------- |
| `type`           | `'openai'`                    | Yes      | Provider type identifier           |
| `baseUrl`        | `string`                      | Yes      | Base URL for the API               |
| `apiKey`         | `string`                      | Yes      | API key for authentication         |
| `model`          | `string`                      | Yes      | Model identifier                   |
| `organizationId` | `string`                      | No       | Organization ID for OpenAI         |
| `headers`        | `Record<string, string>`      | No       | Extra headers for every request    |

### AWS Bedrock Provider

Uses the AWS Bedrock Runtime API to invoke foundation models like Claude, Titan, and Llama.

```typescript
import { AgentScalarPlugin } from '@scalar/agent-scalar-plugin'

const configuration = {
  url: 'https://registry.scalar.com/@scalar/apis/galaxy?format=json',
  plugins: [
    AgentScalarPlugin({
      provider: {
        type: 'aws-bedrock',
        region: 'us-east-1',
        accessKeyId: 'AKIA...',
        secretAccessKey: '...',
        modelId: 'anthropic.claude-3-5-sonnet-20241022-v2:0',
      },
    }),
  ],
}
```

#### AWS Bedrock Configuration Options

| Property           | Type                          | Required | Description                           |
| ------------------ | ----------------------------- | -------- | ------------------------------------- |
| `type`             | `'aws-bedrock'`               | Yes      | Provider type identifier              |
| `region`           | `string`                      | Yes      | AWS region                            |
| `accessKeyId`      | `string`                      | Yes      | AWS access key ID                     |
| `secretAccessKey`  | `string`                      | Yes      | AWS secret access key                 |
| `sessionToken`     | `string`                      | No       | AWS session token (temporary creds)   |
| `modelId`          | `string`                      | Yes      | Bedrock model identifier              |
| `headers`          | `Record<string, string>`      | No       | Extra headers for every request       |

### Using with Ollama (Local)

```typescript
AgentScalarPlugin({
  provider: {
    type: 'openai',
    baseUrl: 'http://localhost:11434/v1',
    apiKey: 'ollama',
    model: 'llama3',
  },
})
```

### Custom Chat Endpoint Pattern

By default, the plugin intercepts requests matching `/vector/openapi/chat`. You can customize this:

```typescript
AgentScalarPlugin({
  provider: { ... },
  chatEndpointPattern: '/my-custom/chat/endpoint',
})
```

## Advanced Usage

### Direct Provider Access

You can create providers directly for custom integrations:

```typescript
import { createOpenAiProvider, createAwsBedrockProvider, createProvider } from '@scalar/agent-scalar-plugin'

// Create from a discriminated config
const provider = createProvider({
  type: 'openai',
  baseUrl: 'https://api.openai.com/v1',
  apiKey: 'sk-...',
  model: 'gpt-4o',
})

// Use provider methods
const headers = provider.buildHeaders()
const url = provider.buildUrl()
const body = provider.buildRequestBody({
  messages: [{ role: 'user', content: 'Hello' }],
})
```

### Schema Validation

Validate provider configurations at runtime using Zod schemas:

```typescript
import { llmProviderConfigSchema } from '@scalar/agent-scalar-plugin'

const result = llmProviderConfigSchema.safeParse(userInput)
if (result.success) {
  // result.data is a valid LlmProviderConfig
}
```

## How It Works

The plugin hooks into the API client's request lifecycle via the `onBeforeRequest` hook. When a request targets the agent chat endpoint:

1. Provider-specific authentication headers are added to the request
2. Metadata headers (`X-Scalar-Llm-Provider` and `X-Scalar-Llm-Endpoint`) are set so downstream consumers know which LLM backend to use

## Community

We are API nerds. You too? Let's chat on [Discord](https://discord.gg/scalar).

## License

The source code in this repository is licensed under [MIT](https://github.com/scalar/scalar/blob/main/LICENSE).
