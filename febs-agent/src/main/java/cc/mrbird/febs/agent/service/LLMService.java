package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;

/**
 * 大语言模型服务接口
 * 
 * @author mrbird
 */
public interface LLMService {
    
    /**
     * 发送聊天请求
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 发送包含工具调用的聊天请求
     * 
     * @param request 聊天请求
     * @param toolDefinitions 工具定义
     * @return 聊天响应
     */
    ChatResponse chatWithTools(ChatRequest request, java.util.List<java.util.Map<String, Object>> toolDefinitions);
}