package dev.langchain4j.model.azure;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.TokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.azure.AzureOpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.azure.InternalAzureOpenAiHelper.setupOpenAIClient;
import static dev.langchain4j.model.azure.InternalAzureOpenAiHelper.toFunctions;
import static java.util.Collections.singletonList;

/**
 * Represents an OpenAI language model, hosted on Azure, that has a chat completion interface, such as gpt-3.5-turbo.
 * The model's response is streamed token by token and should be handled with {@link StreamingResponseHandler}.
 * <p>
 * Mandatory parameters for initialization are: endpoint, serviceVersion, apikey (or an alternate authentication method, see below for more information) and deploymentName.
 * You can also provide your own OpenAIClient instance, if you need more flexibility.
 * <p>
 * There are 3 authentication methods:
 * <p>
 * 1. Azure OpenAI API Key Authentication: this is the most common method, using an Azure OpenAI API key.
 * You need to provide the OpenAI API Key as a parameter, using the apiKey() method in the Builder, or the apiKey parameter in the constructor:
 * For example, you would use `builder.apiKey("{key}")`.
 * <p>
 * 2. non-Azure OpenAI API Key Authentication: this method allows to use the OpenAI service, instead of Azure OpenAI.
 * You can use the nonAzureApiKey() method in the Builder, which will also automatically set the endpoint to "https://api.openai.com/v1".
 * For example, you would use `builder.nonAzureApiKey("{key}")`.
 * The constructor requires a KeyCredential instance, which can be created using `new AzureKeyCredential("{key}")`, and doesn't set up the endpoint.
 * <p>
 * 3. Azure OpenAI client with Microsoft Entra ID (formerly Azure Active Directory) credentials.
 * - This requires to add the `com.azure:azure-identity` dependency to your project, which is an optional dependency to this library.
 * - You need to provide a TokenCredential instance, using the tokenCredential() method in the Builder, or the tokenCredential parameter in the constructor.
 * As an example, DefaultAzureCredential can be used to authenticate the client: Set the values of the client ID, tenant ID, and
 * client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.
 * Then, provide the DefaultAzureCredential instance to the builder: `builder.tokenCredential(new DefaultAzureCredentialBuilder().build())`.
 */
public class AzureOpenAiStreamingChatModel implements StreamingChatLanguageModel, TokenCountEstimator {

    private OpenAIClient client;
    private final String deploymentName;
    private final Tokenizer tokenizer;
    private final Double temperature;
    private final Double topP;
    private final Integer maxTokens;
    private final List<String> stop;
    private final Double presencePenalty;
    private final Double frequencyPenalty;

    public AzureOpenAiStreamingChatModel(OpenAIClient client,
                                         String deploymentName,
                                         Tokenizer tokenizer,
                                         Double temperature,
                                         Double topP,
                                         Integer maxTokens,
                                         List<String> stop,
                                         Double presencePenalty,
                                         Double frequencyPenalty) {

        this(deploymentName, tokenizer, temperature, topP, maxTokens, stop, presencePenalty, frequencyPenalty);
        this.client = client;
    }

    public AzureOpenAiStreamingChatModel(String endpoint,
                                         String serviceVersion,
                                         String apiKey,
                                         String deploymentName,
                                         Tokenizer tokenizer,
                                         Double temperature,
                                         Double topP,
                                         Integer maxTokens,
                                         List<String> stop,
                                         Double presencePenalty,
                                         Double frequencyPenalty,
                                         Duration timeout,
                                         Integer maxRetries,
                                         ProxyOptions proxyOptions,
                                         boolean logRequestsAndResponses) {

        this(deploymentName, tokenizer, temperature, topP, maxTokens, stop, presencePenalty, frequencyPenalty);
        this.client = setupOpenAIClient(endpoint, serviceVersion, apiKey, timeout, maxRetries, proxyOptions, logRequestsAndResponses);
    }

    public AzureOpenAiStreamingChatModel(String endpoint,
                                         String serviceVersion,
                                         KeyCredential keyCredential,
                                         String deploymentName,
                                         Tokenizer tokenizer,
                                         Double temperature,
                                         Double topP,
                                         Integer maxTokens,
                                         List<String> stop,
                                         Double presencePenalty,
                                         Double frequencyPenalty,
                                         Duration timeout,
                                         Integer maxRetries,
                                         ProxyOptions proxyOptions,
                                         boolean logRequestsAndResponses) {

        this(deploymentName, tokenizer, temperature, topP, maxTokens, stop, presencePenalty, frequencyPenalty);
        this.client = setupOpenAIClient(endpoint, serviceVersion, keyCredential, timeout, maxRetries, proxyOptions, logRequestsAndResponses);
    }

    public AzureOpenAiStreamingChatModel(String endpoint,
                                         String serviceVersion,
                                         TokenCredential tokenCredential,
                                         String deploymentName,
                                         Tokenizer tokenizer,
                                         Double temperature,
                                         Double topP,
                                         Integer maxTokens,
                                         List<String> stop,
                                         Double presencePenalty,
                                         Double frequencyPenalty,
                                         Duration timeout,
                                         Integer maxRetries,
                                         ProxyOptions proxyOptions,
                                         boolean logRequestsAndResponses) {

        this(deploymentName, tokenizer, temperature, topP, maxTokens, stop, presencePenalty, frequencyPenalty);
        this.client = setupOpenAIClient(endpoint, serviceVersion, tokenCredential, timeout, maxRetries, proxyOptions, logRequestsAndResponses);
    }

    private AzureOpenAiStreamingChatModel(String deploymentName,
                                         Tokenizer tokenizer,
                                         Double temperature,
                                         Double topP,
                                         Integer maxTokens,
                                          List<String> stop,
                                         Double presencePenalty,
                                         Double frequencyPenalty) {

        this.deploymentName = getOrDefault(deploymentName, "gpt-35-turbo");
        this.tokenizer = getOrDefault(tokenizer, new OpenAiTokenizer(GPT_3_5_TURBO));
        this.temperature = getOrDefault(temperature, 0.7);
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.stop = stop;
        this.presencePenalty = presencePenalty;
        this.frequencyPenalty = frequencyPenalty;
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        generate(messages, null, null, handler);
    }

    @Override
    public void generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications, StreamingResponseHandler<AiMessage> handler) {
        generate(messages, toolSpecifications, null, handler);
    }

    @Override
    public void generate(List<ChatMessage> messages, ToolSpecification toolSpecification, StreamingResponseHandler<AiMessage> handler) {
        generate(messages, null, toolSpecification, handler);
    }

    private void generate(List<ChatMessage> messages,
                          List<ToolSpecification> toolSpecifications,
                          ToolSpecification toolThatMustBeExecuted,
                          StreamingResponseHandler<AiMessage> handler
    ) {
        ChatCompletionsOptions options = new ChatCompletionsOptions(InternalAzureOpenAiHelper.toOpenAiMessages(messages))
                .setStream(true)
                .setModel(deploymentName)
                .setTemperature(temperature)
                .setTopP(topP)
                .setMaxTokens(maxTokens)
                .setStop(stop)
                .setPresencePenalty(presencePenalty)
                .setFrequencyPenalty(frequencyPenalty);

        Integer inputTokenCount = tokenizer == null ? null : tokenizer.estimateTokenCountInMessages(messages);

        if (toolThatMustBeExecuted != null) {
            options.setFunctions(toFunctions(singletonList(toolThatMustBeExecuted)));
            options.setFunctionCall(new FunctionCallConfig(toolThatMustBeExecuted.name()));
            if (tokenizer != null) {
                inputTokenCount += tokenizer.estimateTokenCountInForcefulToolSpecification(toolThatMustBeExecuted);
            }
        } else if (!isNullOrEmpty(toolSpecifications)) {
            options.setFunctions(toFunctions(toolSpecifications));
            if (tokenizer != null) {
                inputTokenCount += tokenizer.estimateTokenCountInToolSpecifications(toolSpecifications);
            }
        }

        AzureOpenAiStreamingResponseBuilder responseBuilder = new AzureOpenAiStreamingResponseBuilder(inputTokenCount);

        try {
            client.getChatCompletionsStream(deploymentName, options)
                    .stream()
                    .forEach(chatCompletions -> {
                        responseBuilder.append(chatCompletions);
                        handle(chatCompletions, handler);
                    });
            Response<AiMessage> response = responseBuilder.build(tokenizer, toolThatMustBeExecuted != null);
            handler.onComplete(response);
        } catch (Exception exception) {
            handler.onError(exception);
        }
    }

    private static void handle(ChatCompletions chatCompletions,
                               StreamingResponseHandler<AiMessage> handler) {

        List<ChatChoice> choices = chatCompletions.getChoices();
        if (choices == null || choices.isEmpty()) {
            return;
        }
        com.azure.ai.openai.models.ChatResponseMessage delta = choices.get(0).getDelta();
        String content = delta.getContent();
        if (content != null) {
            handler.onNext(content);
        }
    }

    @Override
    public int estimateTokenCount(List<ChatMessage> messages) {
        return tokenizer.estimateTokenCountInMessages(messages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String endpoint;
        private String serviceVersion;
        private String apiKey;
        private KeyCredential keyCredential;
        private TokenCredential tokenCredential;
        private String deploymentName;
        private Tokenizer tokenizer;
        private Double temperature;
        private Double topP;
        private Integer maxTokens;
        private List<String> stop;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Duration timeout;
        private Integer maxRetries;
        private ProxyOptions proxyOptions;
        private boolean logRequestsAndResponses;
        private OpenAIClient openAIClient;

        /**
         * Sets the Azure OpenAI endpoint. This is a mandatory parameter.
         *
         * @param endpoint The Azure OpenAI endpoint in the format: https://{resource}.openai.azure.com/
         * @return builder
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the Azure OpenAI API service version. This is a mandatory parameter.
         *
         * @param serviceVersion The Azure OpenAI API service version in the format: 2023-05-15
         * @return builder
         */
        public Builder serviceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        /**
         * Sets the Azure OpenAI API key.
         *
         * @param apiKey The Azure OpenAI API key.
         * @return builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Used to authenticate with the OpenAI service, instead of Azure OpenAI.
         * This automatically sets the endpoint to https://api.openai.com/v1.
         *
         * @param nonAzureApiKey The non-Azure OpenAI API key
         * @return builder
         */
        public Builder nonAzureApiKey(String nonAzureApiKey) {
            this.keyCredential = new KeyCredential(nonAzureApiKey);
            this.endpoint = "https://api.openai.com/v1";
            return this;
        }

        /**
         * Used to authenticate to Azure OpenAI with Azure Active Directory credentials.
         * @param tokenCredential the credentials to authenticate with Azure Active Directory
         * @return builder
         */
        public Builder tokenCredential(TokenCredential tokenCredential) {
            this.tokenCredential = tokenCredential;
            return this;
        }

        /**
         * Sets the deployment name in Azure OpenAI. This is a mandatory parameter.
         *
         * @param deploymentName The Deployment name.
         * @return builder
         */
        public Builder deploymentName(String deploymentName) {
            this.deploymentName = deploymentName;
            return this;
        }

        public Builder tokenizer(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder proxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public Builder logRequestsAndResponses(boolean logRequestsAndResponses) {
            this.logRequestsAndResponses = logRequestsAndResponses;
            return this;
        }

        /**
         * Sets the Azure OpenAI client. This is an optional parameter, if you need more flexibility than using the endpoint, serviceVersion, apiKey, deploymentName parameters.
         *
         * @param openAIClient The Azure OpenAI client.
         * @return builder
         */
        public Builder openAIClient(OpenAIClient openAIClient) {
            this.openAIClient = openAIClient;
            return this;
        }

        public AzureOpenAiStreamingChatModel build() {
            if (openAIClient == null) {
                if (tokenCredential != null) {
                    return new AzureOpenAiStreamingChatModel(
                            endpoint,
                            serviceVersion,
                            tokenCredential,
                            deploymentName,
                            tokenizer,
                            temperature,
                            topP,
                            maxTokens,
                            stop,
                            presencePenalty,
                            frequencyPenalty,
                            timeout,
                            maxRetries,
                            proxyOptions,
                            logRequestsAndResponses
                    );
                } else if (keyCredential != null) {
                    return new AzureOpenAiStreamingChatModel(
                            endpoint,
                            serviceVersion,
                            keyCredential,
                            deploymentName,
                            tokenizer,
                            temperature,
                            topP,
                            maxTokens,
                            stop,
                            presencePenalty,
                            frequencyPenalty,
                            timeout,
                            maxRetries,
                            proxyOptions,
                            logRequestsAndResponses
                    );
                }
                return new AzureOpenAiStreamingChatModel(
                        endpoint,
                        serviceVersion,
                        apiKey,
                        deploymentName,
                        tokenizer,
                        temperature,
                        topP,
                        maxTokens,
                        stop,
                        presencePenalty,
                        frequencyPenalty,
                        timeout,
                        maxRetries,
                        proxyOptions,
                        logRequestsAndResponses
                );
            } else {
                return new AzureOpenAiStreamingChatModel(
                        openAIClient,
                        deploymentName,
                        tokenizer,
                        temperature,
                        topP,
                        maxTokens,
                        stop,
                        presencePenalty,
                        frequencyPenalty
                );
            }
        }
    }
}
