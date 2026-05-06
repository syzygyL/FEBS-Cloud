package cc.mrbird.febs.agent.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * LLM配置
 * 
 * @author mrbird
 */
@Configuration
public class LLMConfigure {
    
    /**
     * LLM提供商类型：openai / local / mock
     */
    @Value("${llm.provider:mock}")
    private String provider;
    
    /**
     * OpenAI API Key
     */
    @Value("${openai.api-key:}")
    private String openaiApiKey;
    
    /**
     * OpenAI模型名称
     */
    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;
    
    /**
     * OpenAI API Base URL
     */
    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;
    
    /**
     * 本地LLM API地址
     */
    @Value("${local.llm.url:http://localhost:11434}")
    private String localLlmUrl;
    
    /**
     * 本地LLM模型名称
     */
    @Value("${local.llm.model:qwen2.5:7b}")
    private String localLlmModel;
    
    // Getters
    public String getProvider() { return provider; }
    public String getOpenaiApiKey() { return openaiApiKey; }
    public String getOpenaiModel() { return openaiModel; }
    public String getOpenaiBaseUrl() { return openaiBaseUrl; }
    public String getLocalLlmUrl() { return localLlmUrl; }
    public String getLocalLlmModel() { return localLlmModel; }
}