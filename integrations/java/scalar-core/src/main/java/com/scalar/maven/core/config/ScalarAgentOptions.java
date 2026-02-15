package com.scalar.maven.core.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Agent Scalar configuration options.
 * Enables the AI chat interface in the API Reference or disables it entirely.
 * Optionally configures a custom LLM provider (OpenAI-compatible or AWS Bedrock).
 *
 * @see <a href="https://github.com/scalar/scalar/blob/main/documentation/configuration.md">Configuration</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScalarAgentOptions {

    /**
     * Agent Scalar key for production deployments. Required for Agent Scalar to
     * appear in production; when omitted, the agent is enabled only on localhost
     * with limited free messages.
     */
    private String key;

    /**
     * When true, disables the Agent Scalar chat interface for this source or
     * for the entire reference (when set at top level).
     */
    private Boolean disabled;

    /**
     * Custom LLM provider configuration for routing agent chat requests
     * to an OpenAI-compatible endpoint or AWS Bedrock instead of the
     * default Scalar backend.
     * <p>
     * This field is excluded from JSON serialization because it is
     * handled separately as a JavaScript plugin in the HTML rendering.
     * </p>
     */
    @JsonIgnore
    private ScalarLlmProvider llmProvider;

    /**
     * Creates agent options with no key and not disabled.
     */
    public ScalarAgentOptions() {
    }

    /**
     * Gets the Agent Scalar key.
     *
     * @return the key or null
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the Agent Scalar key.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets whether the agent is disabled.
     *
     * @return true if disabled, false if enabled, null if not set
     */
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * Sets whether the agent is disabled.
     *
     * @param disabled true to disable, false or null to use default behavior
     */
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Gets the custom LLM provider configuration.
     *
     * @return the LLM provider configuration or null
     */
    public ScalarLlmProvider getLlmProvider() {
        return llmProvider;
    }

    /**
     * Sets the custom LLM provider configuration.
     *
     * @param llmProvider the LLM provider configuration
     */
    public void setLlmProvider(ScalarLlmProvider llmProvider) {
        this.llmProvider = llmProvider;
    }
}
