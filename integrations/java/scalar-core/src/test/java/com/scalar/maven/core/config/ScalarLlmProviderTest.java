package com.scalar.maven.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScalarLlmProvider")
class ScalarLlmProviderTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("creates provider with default constructor")
        void createsWithDefaultConstructor() {
            ScalarLlmProvider provider = new ScalarLlmProvider();
            assertThat(provider).isNotNull();
            assertThat(provider.getType()).isNull();
            assertThat(provider.getBaseUrl()).isNull();
            assertThat(provider.getApiKey()).isNull();
            assertThat(provider.getModel()).isNull();
            assertThat(provider.getRegion()).isNull();
            assertThat(provider.getModelId()).isNull();
        }
    }

    @Nested
    @DisplayName("OpenAI properties")
    class OpenAiProperties {

        @Test
        @DisplayName("sets and gets OpenAI provider properties")
        void setsAndGetsOpenAiProperties() {
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            provider.setBaseUrl("https://api.openai.com/v1");
            provider.setApiKey("sk-test-key");
            provider.setModel("gpt-4o");
            provider.setOrganizationId("org-12345");

            assertThat(provider.getType()).isEqualTo("openai");
            assertThat(provider.getBaseUrl()).isEqualTo("https://api.openai.com/v1");
            assertThat(provider.getApiKey()).isEqualTo("sk-test-key");
            assertThat(provider.getModel()).isEqualTo("gpt-4o");
            assertThat(provider.getOrganizationId()).isEqualTo("org-12345");
        }
    }

    @Nested
    @DisplayName("AWS Bedrock properties")
    class AwsBedrockProperties {

        @Test
        @DisplayName("sets and gets Bedrock provider properties")
        void setsAndGetsBedrockProperties() {
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("aws-bedrock");
            provider.setRegion("us-east-1");
            provider.setAccessKeyId("AKIAIOSFODNN7EXAMPLE");
            provider.setSecretAccessKey("secret");
            provider.setSessionToken("token");
            provider.setModelId("anthropic.claude-3-5-sonnet-20241022-v2:0");

            assertThat(provider.getType()).isEqualTo("aws-bedrock");
            assertThat(provider.getRegion()).isEqualTo("us-east-1");
            assertThat(provider.getAccessKeyId()).isEqualTo("AKIAIOSFODNN7EXAMPLE");
            assertThat(provider.getSecretAccessKey()).isEqualTo("secret");
            assertThat(provider.getSessionToken()).isEqualTo("token");
            assertThat(provider.getModelId()).isEqualTo("anthropic.claude-3-5-sonnet-20241022-v2:0");
        }
    }

    @Nested
    @DisplayName("custom headers")
    class CustomHeaders {

        @Test
        @DisplayName("sets and gets custom headers")
        void setsAndGetsHeaders() {
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setHeaders(Map.of("X-Custom", "value"));

            assertThat(provider.getHeaders()).containsEntry("X-Custom", "value");
        }
    }

    @Nested
    @DisplayName("JSON serialization")
    class JsonSerialization {

        @Test
        @DisplayName("omits null fields from JSON")
        void omitsNullFieldsFromJson() throws JsonProcessingException {
            ScalarLlmProvider provider = new ScalarLlmProvider();

            String json = new ObjectMapper().writeValueAsString(provider);

            assertThat(json).isEqualTo("{}");
        }

        @Test
        @DisplayName("serializes OpenAI provider to JSON")
        void serializesOpenAiProviderToJson() throws JsonProcessingException {
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            provider.setBaseUrl("https://api.openai.com/v1");
            provider.setApiKey("sk-test");
            provider.setModel("gpt-4o");

            String json = new ObjectMapper().writeValueAsString(provider);

            assertThat(json).contains("\"type\":\"openai\"");
            assertThat(json).contains("\"baseUrl\":\"https://api.openai.com/v1\"");
            assertThat(json).contains("\"model\":\"gpt-4o\"");
        }
    }
}
