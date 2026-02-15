package com.scalar.maven.core;

import com.scalar.maven.core.config.ScalarAgentOptions;
import com.scalar.maven.core.config.ScalarLlmProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ScalarHtmlRenderer")
class ScalarHtmlRendererTest {

    @Nested
    @DisplayName("render")
    class Render {

        @Test
        @DisplayName("should render HTML with default properties")
        void shouldRenderHtmlWithDefaultProperties() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .isNotNull()
                    .contains("<!doctype html>")
                    .contains("<html>")
                    .contains("<head>")
                    .contains("<title>Scalar API Reference</title>")
                    .contains("<body>")
                    .contains("<div id=\"app\"></div>")
                    .contains("Scalar.createApiReference('#app',");
        }

        @Test
        @DisplayName("should render HTML with custom page title")
        void shouldRenderHtmlWithCustomPageTitle() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            properties.setPageTitle("My Custom API");
            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .isNotNull()
                    .contains("<title>My Custom API</title>");
        }

        @Test
        @DisplayName("should render HTML with custom path")
        void shouldRenderHtmlWithCustomPath() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            properties.setPath("/api/docs");
            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .isNotNull()
                    .contains("/api/docs/scalar.js");
        }

        @Test
        @DisplayName("should handle null properties")
        void shouldHandleNullProperties() {
            assertThatThrownBy(() -> ScalarHtmlRenderer.render(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("properties must not be null");
        }

        @Test
        @DisplayName("should handle null path in properties")
        void shouldHandleNullPathInProperties() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            properties.setPath(null);
            // Should use default path when path is null
            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .isNotNull()
                    .contains("/scalar/scalar.js");
        }

        @Test
        @DisplayName("should not include plugin script without LLM provider")
        void shouldNotIncludePluginScriptWithoutProvider() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .doesNotContain("__scalarAgentPlugin")
                    .doesNotContain("Agent Scalar LLM Provider Plugin");
        }

        @Test
        @DisplayName("should include OpenAI plugin script when configured")
        void shouldIncludeOpenAiPluginScript() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            ScalarAgentOptions agent = new ScalarAgentOptions();
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            provider.setBaseUrl("https://api.openai.com/v1");
            provider.setApiKey("sk-test-key");
            provider.setModel("gpt-4o");
            agent.setLlmProvider(provider);
            properties.setAgent(agent);

            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .contains("__scalarAgentPlugin")
                    .contains("@scalar/agent-scalar-plugin")
                    .contains("Bearer")
                    .contains("sk-test-key")
                    .contains("X-Scalar-Llm-Provider")
                    .contains("openai")
                    .contains("chat/completions")
                    .contains("\"plugins\":[__scalarAgentPlugin()]");
        }

        @Test
        @DisplayName("should include Bedrock plugin script when configured")
        void shouldIncludeBedrockPluginScript() throws IOException {
            ScalarProperties properties = new ScalarProperties();
            ScalarAgentOptions agent = new ScalarAgentOptions();
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("aws-bedrock");
            provider.setRegion("us-east-1");
            provider.setAccessKeyId("AKIAIOSFODNN7EXAMPLE");
            provider.setSecretAccessKey("secret-key");
            provider.setModelId("anthropic.claude-3-5-sonnet-20241022-v2:0");
            agent.setLlmProvider(provider);
            properties.setAgent(agent);

            String html = ScalarHtmlRenderer.render(properties);
            assertThat(html)
                    .contains("__scalarAgentPlugin")
                    .contains("X-Aws-Access-Key-Id")
                    .contains("AKIAIOSFODNN7EXAMPLE")
                    .contains("X-Aws-Region")
                    .contains("us-east-1")
                    .contains("aws-bedrock")
                    .contains("bedrock-runtime.us-east-1.amazonaws.com");
        }
    }

    @Nested
    @DisplayName("buildPluginScript")
    class BuildPluginScript {

        @Test
        @DisplayName("returns empty string when no agent is configured")
        void returnsEmptyWhenNoAgent() {
            ScalarProperties properties = new ScalarProperties();
            assertThat(ScalarHtmlRenderer.buildPluginScript(properties)).isEmpty();
        }

        @Test
        @DisplayName("returns empty string when agent has no LLM provider")
        void returnsEmptyWhenNoLlmProvider() {
            ScalarProperties properties = new ScalarProperties();
            properties.setAgent(new ScalarAgentOptions());
            assertThat(ScalarHtmlRenderer.buildPluginScript(properties)).isEmpty();
        }

        @Test
        @DisplayName("returns empty string when LLM provider has no type")
        void returnsEmptyWhenNoType() {
            ScalarProperties properties = new ScalarProperties();
            ScalarAgentOptions agent = new ScalarAgentOptions();
            agent.setLlmProvider(new ScalarLlmProvider());
            properties.setAgent(agent);
            assertThat(ScalarHtmlRenderer.buildPluginScript(properties)).isEmpty();
        }

        @Test
        @DisplayName("includes OpenAI organization ID when provided")
        void includesOrgIdWhenProvided() {
            ScalarProperties properties = new ScalarProperties();
            ScalarAgentOptions agent = new ScalarAgentOptions();
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("openai");
            provider.setBaseUrl("https://api.openai.com/v1");
            provider.setApiKey("sk-test");
            provider.setModel("gpt-4o");
            provider.setOrganizationId("org-12345");
            agent.setLlmProvider(provider);
            properties.setAgent(agent);

            String script = ScalarHtmlRenderer.buildPluginScript(properties);
            assertThat(script).contains("OpenAI-Organization");
            assertThat(script).contains("org-12345");
        }

        @Test
        @DisplayName("includes Bedrock session token when provided")
        void includesSessionTokenWhenProvided() {
            ScalarProperties properties = new ScalarProperties();
            ScalarAgentOptions agent = new ScalarAgentOptions();
            ScalarLlmProvider provider = new ScalarLlmProvider();
            provider.setType("aws-bedrock");
            provider.setRegion("us-east-1");
            provider.setAccessKeyId("AKIA");
            provider.setSecretAccessKey("secret");
            provider.setModelId("model");
            provider.setSessionToken("session-token-value");
            agent.setLlmProvider(provider);
            properties.setAgent(agent);

            String script = ScalarHtmlRenderer.buildPluginScript(properties);
            assertThat(script).contains("X-Aws-Session-Token");
            assertThat(script).contains("session-token-value");
        }
    }

    @Nested
    @DisplayName("jsStringLiteral")
    class JsStringLiteral {

        @Test
        @DisplayName("escapes single quotes")
        void escapesSingleQuotes() {
            assertThat(ScalarHtmlRenderer.jsStringLiteral("it's")).isEqualTo("'it\\'s'");
        }

        @Test
        @DisplayName("escapes backslashes")
        void escapesBackslashes() {
            assertThat(ScalarHtmlRenderer.jsStringLiteral("a\\b")).isEqualTo("'a\\\\b'");
        }

        @Test
        @DisplayName("escapes script closing tags")
        void escapesScriptClosingTags() {
            assertThat(ScalarHtmlRenderer.jsStringLiteral("</script>")).isEqualTo("'<\\/script>'");
        }

        @Test
        @DisplayName("handles null value")
        void handlesNull() {
            assertThat(ScalarHtmlRenderer.jsStringLiteral(null)).isEqualTo("''");
        }
    }

    @Nested
    @DisplayName("getScalarJsContent")
    class GetScalarJsContent {

        @Test
        @DisplayName("should return JavaScript content")
        void shouldReturnJavaScriptContent() throws IOException {
            byte[] jsContent = ScalarHtmlRenderer.getScalarJsContent();
            assertThat(jsContent)
                    .isNotNull()
                    .isNotEmpty();
        }
    }
}

