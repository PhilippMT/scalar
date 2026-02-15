package com.scalar.maven.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScalarAgentOptions")
class ScalarAgentOptionsTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("creates options with default constructor")
        void createsWithDefaultConstructor() {
            ScalarAgentOptions options = new ScalarAgentOptions();
            assertThat(options).isNotNull();
            assertThat(options.getKey()).isNull();
            assertThat(options.getDisabled()).isNull();
            assertThat(options.getLlmProvider()).isNull();
        }
    }

    @Nested
    @DisplayName("properties")
    class Properties {

        @Test
        @DisplayName("sets and gets key")
        void setsAndGetsKey() {
            ScalarAgentOptions options = new ScalarAgentOptions();
            options.setKey("agent-key-123");
            assertThat(options.getKey()).isEqualTo("agent-key-123");
        }

        @Test
        @DisplayName("sets and gets disabled")
        void setsAndGetsDisabled() {
            ScalarAgentOptions options = new ScalarAgentOptions();
            options.setDisabled(true);
            assertThat(options.getDisabled()).isTrue();
            options.setDisabled(false);
            assertThat(options.getDisabled()).isFalse();
            options.setDisabled(null);
            assertThat(options.getDisabled()).isNull();
        }

        @Test
        @DisplayName("sets and gets llmProvider")
        void setsAndGetsLlmProvider() {
            ScalarAgentOptions options = new ScalarAgentOptions();
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            provider.setBaseUrl("https://api.openai.com/v1");
            provider.setApiKey("sk-test");
            provider.setModel("gpt-4o");
            options.setLlmProvider(provider);

            assertThat(options.getLlmProvider()).isNotNull();
            assertThat(options.getLlmProvider().getType()).isEqualTo("openai");
        }
    }

    @Nested
    @DisplayName("JSON serialization")
    class JsonSerialization {

        @Test
        @DisplayName("serializes key and disabled to JSON")
        void serializesToJson() throws JsonProcessingException {
            ScalarAgentOptions options = new ScalarAgentOptions();
            options.setKey("my-key");
            options.setDisabled(true);

            String json = new ObjectMapper().writeValueAsString(options);

            assertThat(json).contains("\"key\":\"my-key\"");
            assertThat(json).contains("\"disabled\":true");
        }

        @Test
        @DisplayName("omits null fields from JSON")
        void omitsNullFieldsFromJson() throws JsonProcessingException {
            ScalarAgentOptions options = new ScalarAgentOptions();

            String json = new ObjectMapper().writeValueAsString(options);

            assertThat(json).isEqualTo("{}");
        }

        @Test
        @DisplayName("excludes llmProvider from JSON serialization")
        void excludesLlmProviderFromJson() throws JsonProcessingException {
            ScalarAgentOptions options = new ScalarAgentOptions();
            options.setKey("my-key");
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            options.setLlmProvider(provider);

            String json = new ObjectMapper().writeValueAsString(options);

            assertThat(json).contains("\"key\":\"my-key\"");
            assertThat(json).doesNotContain("llmProvider");
            assertThat(json).doesNotContain("openai");
        }
    }
}
