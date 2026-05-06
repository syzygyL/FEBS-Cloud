package cc.mrbird.febs.agent.controller;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import cc.mrbird.febs.agent.entity.Conversation;
import cc.mrbird.febs.agent.service.AgentService;
import cc.mrbird.febs.agent.service.ConversationService;
import cc.mrbird.febs.agent.tool.ToolRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent控制器
 * 提供智能客服API接口
 * 
 * @author mrbird
 */
@RestController
@RequestMapping("/agent")
@Api(tags = "智能客服Agent")
public class AgentController {
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    @PostMapping("/chat")
    @ApiOperation("发送消息给Agent")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // 设置会话ID
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString());
        }
        
        // 获取或创建会话
        Conversation conversation = conversationService.getOrCreateConversation(
            request.getUserId(), request.getSessionId());
        
        // 添加用户消息到会话历史
        ChatRequest.ChatMessage userMessage = new ChatRequest.ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        conversationService.addMessage(request.getSessionId(), userMessage);
        
        // 设置对话历史
        request.setHistory(conversation.getMessages());
        
        // 处理消息
        ChatResponse response = agentService.handleMessage(request);
        
        // 添加助手回复到会话历史
        ChatRequest.ChatMessage assistantMessage = new ChatRequest.ChatMessage();
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(response.getContent());
        conversationService.addMessage(request.getSessionId(), assistantMessage);
        
        return response;
    }
    
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "FEBS-Agent",
            "timestamp", System.currentTimeMillis(),
            "tools", toolRegistry.getAllTools().size()
        );
    }
    
    @GetMapping("/tools")
    @ApiOperation("获取可用工具列表")
    public List<Map<String, Object>> getTools() {
        return toolRegistry.getToolDefinitions();
    }
    
    @GetMapping("/conversations")
    @ApiOperation("获取会话列表")
    public List<Conversation> getConversations(@RequestParam String userId) {
        // 实际应从Redis查询该用户的所有会话
        return List.of();
    }
    
    @GetMapping("/conversation/{sessionId}")
    @ApiOperation("获取会话详情")
    public Conversation getConversation(@PathVariable String sessionId) {
        return conversationService.getConversation(sessionId);
    }
    
    @DeleteMapping("/conversation/{sessionId}")
    @ApiOperation("清空会话")
    public Map<String, Object> clearConversation(@PathVariable String sessionId) {
        conversationService.clearConversation(sessionId);
        return Map.of("code", 200, "message", "会话已清空");
    }
}