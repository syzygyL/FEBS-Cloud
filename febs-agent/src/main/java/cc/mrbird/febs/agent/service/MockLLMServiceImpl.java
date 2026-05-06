package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 模拟LLM服务实现
 * 用于开发测试，不依赖外部LLM API
 * 
 * @author mrbird
 */
@Service("mockLLMService")
public class MockLLMServiceImpl implements LLMService {
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatResponse response = new ChatResponse();
        response.setContent("我是模拟LLM，用于开发测试。请配置真实的LLM服务。");
        return response;
    }
    
    @Override
    public ChatResponse chatWithTools(ChatRequest request, List<Map<String, Object>> toolDefinitions) {
        // 模拟实现，直接返回空响应，让AgentService的本地意图识别处理
        ChatResponse response = new ChatResponse();
        return response;
    }
}