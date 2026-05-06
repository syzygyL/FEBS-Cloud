package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import cc.mrbird.febs.agent.tool.AgentTool;
import cc.mrbird.febs.agent.tool.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Agent服务
 * 核心服务类，负责协调LLM和工具调用
 * 
 * @author mrbird
 */
@Service
public class AgentService {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    /**
     * 处理用户消息
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse handleMessage(ChatRequest request) {
        // 1. 获取工具定义
        List<Map<String, Object>> toolDefinitions = toolRegistry.getToolDefinitions();
        
        // 2. 发送给LLM处理
        ChatResponse response = llmService.chatWithTools(request, toolDefinitions);
        
        // 3. 如果LLM决定调用工具，执行工具
        if (response.isHasToolCall() && response.getToolCall() != null) {
            ChatResponse.ToolCall toolCall = response.getToolCall();
            AgentTool tool = toolRegistry.getTool(toolCall.getName());
            
            if (tool != null) {
                String toolResult = tool.execute(toolCall.getArguments());
                
                // 4. 将工具执行结果返回给LLM生成最终回复
                ChatRequest followUpRequest = new ChatRequest();
                followUpRequest.setUserId(request.getUserId());
                followUpRequest.setSessionId(request.getSessionId());
                followUpRequest.setMessage("工具执行结果：" + toolResult);
                
                // 简化处理，直接返回工具执行结果
                ChatResponse finalResponse = new ChatResponse();
                finalResponse.setContent(formatToolResult(toolCall.getName(), toolResult));
                return finalResponse;
            }
        }
        
        return response;
    }
    
    /**
     * 格式化工具执行结果
     */
    private String formatToolResult(String toolName, String toolResult) {
        return switch (toolName) {
            case "user_management" -> "【用户管理】查询结果：\n" + toolResult;
            case "permission_management" -> "【权限管理】查询结果：\n" + toolResult;
            case "system_monitor" -> "【系统监控】查询结果：\n" + toolResult;
            default -> "工具执行结果：" + toolResult;
        };
    }
}