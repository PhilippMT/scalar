package com.scalar.maven.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Configuration for a custom LLM provider used by the Agent Scalar plugin.
 * <p>
 * Supports OpenAI-compatible endpoints (OpenAI, Azure OpenAI, Ollama, vLLM,
 * LiteLLM) and AWS Bedrock.
 * </p>
 *
 * <p>
 * Example usage in application.properties:
 * </p>
 * <pre>
 * # OpenAI provider
 * scalar.agent.llmProvider.type=openai
 * scalar.agent.llmProvider.baseUrl=https://api.openai.com/v1
 * scalar.agent.llmProvider.apiKey=sk-...
 * scalar.agent.llmProvider.model=gpt-4o
 *
 * # AWS Bedrock provider
 * scalar.agent.llmProvider.type=aws-bedrock
 * scalar.agent.llmProvider.region=us-east-1
 * scalar.agent.llmProvider.accessKeyId=AKIA...
 * scalar.agent.llmProvider.secretAccessKey=...
 * scalar.agent.llmProvider.modelId=anthropic.claude-3-5-sonnet-20241022-v2:0
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScalarLlmProvider {

    /**
     * The provider type. Must be either "openai" or "aws-bedrock".
     */
    private String type;

    // --- OpenAI-compatible fields ---

    /**
     * Base URL for the OpenAI-compatible API.
     * Required when type is "openai".
     *
     * @see <a href="https://platform.openai.com/docs/api-reference">OpenAI API Reference</a>
     */
    private String baseUrl;

    /**
     * API key for OpenAI-compatible authentication.
     * Required when type is "openai".
     */
    private String apiKey;

    /**
     * The model identifier for OpenAI-compatible completions.
     * Required when type is "openai".
     */
    private String model;

    /**
     * Optional organization ID for OpenAI API requests.
     */
    private String organizationId;

    // --- AWS Bedrock fields ---

    /**
     * The AWS region where the Bedrock model is available.
     * Required when type is "aws-bedrock".
     */
    private String region;

    /**
     * AWS access key ID for authentication.
     * Required when type is "aws-bedrock".
     */
    private String accessKeyId;

    /**
     * AWS secret access key for authentication.
     * Required when type is "aws-bedrock".
     */
    private String secretAccessKey;

    /**
     * Optional AWS session token for temporary credentials.
     */
    private String sessionToken;

    /**
     * The Bedrock model identifier.
     * Required when type is "aws-bedrock".
     */
    private String modelId;

    /**
     * Optional extra headers to include in every request.
     */
    private Map<String, String> headers;

    public ScalarLlmProvider() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
