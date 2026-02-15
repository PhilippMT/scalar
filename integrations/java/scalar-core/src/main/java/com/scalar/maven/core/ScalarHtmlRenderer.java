package com.scalar.maven.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.maven.core.config.ScalarAgentOptions;
import com.scalar.maven.core.config.ScalarLlmProvider;
import com.scalar.maven.core.internal.ScalarConfiguration;
import com.scalar.maven.core.internal.ScalarConfigurationMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Static utility class for rendering HTML content for the Scalar API Reference interface.
 *
 * <p>
 * This class is framework-agnostic and provides the core HTML rendering
 * functionality. It loads the HTML template, serializes the configuration to JSON,
 * and replaces placeholders with actual values.
 * </p>
 */
public final class ScalarHtmlRenderer {

    private static final String HTML_TEMPLATE_PATH = "/META-INF/resources/webjars/scalar/index.html";
    private static final String JS_BUNDLE_PATH = "/META-INF/resources/webjars/scalar/" + ScalarConstants.JS_FILENAME;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ScalarHtmlRenderer() {
        // Utility class - prevent instantiation
    }

    /**
     * Normalizes the base path by returning the default path if the provided
     * base path is null or empty.
     *
     * @param basePath the base path to normalize
     * @return the normalized base path, or {@link ScalarConstants#DEFAULT_PATH} if null or empty
     */
    private static String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isEmpty()) {
            return ScalarConstants.DEFAULT_PATH;
        }
        return basePath;
    }

    /**
     * Renders the complete HTML content for the Scalar API Reference interface.
     *
     * @param properties the configuration properties for the Scalar integration
     * @return the rendered HTML content
     * @throws IOException if the HTML template cannot be loaded
     */
    public static String render(ScalarProperties properties) throws IOException {
        Objects.requireNonNull(properties, "properties must not be null");

        String basePath = normalizeBasePath(properties.getPath());

        // Load the template HTML
        InputStream templateStream = ScalarHtmlRenderer.class.getResourceAsStream(HTML_TEMPLATE_PATH);
        if (templateStream == null) {
            throw new IOException("HTML template not found at: " + HTML_TEMPLATE_PATH);
        }

        String html;
        try (InputStream inputStream = templateStream) {
            html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Build the JS bundle URL from the base path
        String bundleUrl = buildJsBundleUrl(basePath);
        String pageTitle = Objects.requireNonNullElse(properties.getPageTitle(), "Scalar API Reference");

        // Serialize configuration to JSON
        String configurationJson = buildConfigurationJson(properties);

        // Build plugin script if LLM provider is configured
        String pluginScript = buildPluginScript(properties);

        // Replace placeholders
        return html
                .replace("__JS_BUNDLE_URL__", bundleUrl)
                .replace("__PAGE_TITLE__", pageTitle)
                .replace("__PLUGIN_SCRIPT__", pluginScript)
                .replace("__CONFIGURATION__", configurationJson);
    }

    /**
     * Builds the URL for the Scalar JavaScript bundle based on the base path.
     *
     * @param basePath the base path
     * @return the complete URL for the JavaScript bundle
     */
    private static String buildJsBundleUrl(String basePath) {
        // Remove trailing slash to avoid double slashes when concatenating
        String path = basePath;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path + "/" + ScalarConstants.JS_FILENAME;
    }

    /**
     * Builds the configuration JSON for the Scalar API Reference.
     * When an LLM provider is configured, the plugins array is injected
     * as a raw JavaScript expression in the configuration.
     *
     * @param properties the properties to serialize
     * @return the configuration JSON as a string
     */
    private static String buildConfigurationJson(ScalarProperties properties) {
        try {
            ScalarConfiguration config = ScalarConfigurationMapper.map(properties);
            String json = OBJECT_MAPPER.writeValueAsString(config);

            ScalarLlmProvider llmProvider = getLlmProvider(properties);
            if (llmProvider != null && json.endsWith("}")) {
                // Inject the plugins array as a raw JavaScript expression.
                // The JSON from ObjectMapper always produces a valid object ending with "}".
                // We append the plugins property referencing the inline plugin function
                // defined in the __PLUGIN_SCRIPT__ section of the HTML template.
                json = json.substring(0, json.length() - 1)
                        + ",\"plugins\":[__scalarAgentPlugin()]}";
            }

            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Scalar configuration", e);
        }
    }

    /**
     * Builds the inline plugin script tag when an LLM provider is configured.
     * The script defines a factory function that creates an ApiClientPlugin
     * matching the behavior of @scalar/agent-scalar-plugin.
     *
     * @param properties the properties to check for LLM provider config
     * @return the plugin script tag or empty string
     */
    static String buildPluginScript(ScalarProperties properties) {
        ScalarLlmProvider llmProvider = getLlmProvider(properties);
        if (llmProvider == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n    <!-- Agent Scalar LLM Provider Plugin -->\n");
        sb.append("    <script>\n");
        sb.append("      function __scalarAgentPlugin() {\n");
        sb.append("        return function() {\n");
        sb.append("          return {\n");
        sb.append("            name: '@scalar/agent-scalar-plugin',\n");
        sb.append("            hooks: {\n");
        sb.append("              onBeforeRequest: function(ctx) {\n");
        sb.append("                var req = ctx.request;\n");
        // Match the default Agent Scalar chat endpoint used by the Vercel AI SDK transport.
        // See: packages/agent-chat/src/state/state.ts (DefaultChatTransport target URL)
        sb.append("                if (req.url.indexOf('/vector/openapi/chat') === -1) return;\n");

        if ("openai".equals(llmProvider.getType())) {
            appendOpenAiHeaders(sb, llmProvider);
        } else if ("aws-bedrock".equals(llmProvider.getType())) {
            appendBedrockHeaders(sb, llmProvider);
        }

        appendCustomHeaders(sb, llmProvider);

        sb.append("                req.headers.set('X-Scalar-Llm-Provider', ");
        sb.append(jsStringLiteral(llmProvider.getType()));
        sb.append(");\n");

        sb.append("                req.headers.set('X-Scalar-Llm-Endpoint', ");
        sb.append(jsStringLiteral(buildLlmEndpointUrl(llmProvider)));
        sb.append(");\n");

        sb.append("              }\n");
        sb.append("            }\n");
        sb.append("          };\n");
        sb.append("        };\n");
        sb.append("      }\n");
        sb.append("    </script>\n");

        return sb.toString();
    }

    private static void appendOpenAiHeaders(StringBuilder sb, ScalarLlmProvider provider) {
        sb.append("                req.headers.set('Content-Type', 'application/json');\n");
        sb.append("                req.headers.set('Authorization', 'Bearer ' + ");
        sb.append(jsStringLiteral(provider.getApiKey()));
        sb.append(");\n");
        if (provider.getOrganizationId() != null) {
            sb.append("                req.headers.set('OpenAI-Organization', ");
            sb.append(jsStringLiteral(provider.getOrganizationId()));
            sb.append(");\n");
        }
    }

    private static void appendBedrockHeaders(StringBuilder sb, ScalarLlmProvider provider) {
        sb.append("                req.headers.set('Content-Type', 'application/json');\n");
        sb.append("                req.headers.set('X-Aws-Access-Key-Id', ");
        sb.append(jsStringLiteral(provider.getAccessKeyId()));
        sb.append(");\n");
        sb.append("                req.headers.set('X-Aws-Secret-Access-Key', ");
        sb.append(jsStringLiteral(provider.getSecretAccessKey()));
        sb.append(");\n");
        sb.append("                req.headers.set('X-Aws-Region', ");
        sb.append(jsStringLiteral(provider.getRegion()));
        sb.append(");\n");
        if (provider.getSessionToken() != null) {
            sb.append("                req.headers.set('X-Aws-Session-Token', ");
            sb.append(jsStringLiteral(provider.getSessionToken()));
            sb.append(");\n");
        }
    }

    private static void appendCustomHeaders(StringBuilder sb, ScalarLlmProvider provider) {
        if (provider.getHeaders() != null) {
            for (Map.Entry<String, String> entry : provider.getHeaders().entrySet()) {
                sb.append("                req.headers.set(");
                sb.append(jsStringLiteral(entry.getKey()));
                sb.append(", ");
                sb.append(jsStringLiteral(entry.getValue()));
                sb.append(");\n");
            }
        }
    }

    /**
     * Builds the LLM endpoint URL for the configured provider.
     *
     * @param provider the LLM provider config (must have a non-null type)
     * @return the fully-qualified endpoint URL, or empty string for unknown types
     */
    private static String buildLlmEndpointUrl(ScalarLlmProvider provider) {
        if ("openai".equals(provider.getType())) {
            String base = provider.getBaseUrl();
            if (base == null || base.isEmpty()) {
                return "/chat/completions";
            }
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "/chat/completions";
        } else if ("aws-bedrock".equals(provider.getType())) {
            return "https://bedrock-runtime." + provider.getRegion()
                    + ".amazonaws.com/model/" + provider.getModelId() + "/converse";
        }
        return "";
    }

    /**
     * Escapes a Java string for use as a JavaScript string literal.
     */
    static String jsStringLiteral(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("</", "<\\/")
                + "'";
    }

    /**
     * Extracts the LLM provider from the agent options if present.
     */
    private static ScalarLlmProvider getLlmProvider(ScalarProperties properties) {
        if (properties.getAgent() != null && properties.getAgent().getLlmProvider() != null) {
            ScalarLlmProvider provider = properties.getAgent().getLlmProvider();
            if (provider.getType() != null) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Gets the JavaScript bundle content.
     *
     * @return the JavaScript bundle content as bytes
     * @throws IOException if the JavaScript file cannot be loaded
     */
    public static byte[] getScalarJsContent() throws IOException {
        InputStream jsStream = ScalarHtmlRenderer.class.getResourceAsStream(JS_BUNDLE_PATH);
        if (jsStream == null) {
            throw new IOException("JavaScript bundle not found at: " + JS_BUNDLE_PATH);
        }

        try (InputStream inputStream = jsStream) {
            return inputStream.readAllBytes();
        }
    }
}
