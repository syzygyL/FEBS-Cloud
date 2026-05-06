package cc.mrbird.febs.agent.controller;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import cc.mrbird.febs.agent.entity.Conversation;
import cc.mrbird.febs.agent.service.AgentService;
import cc.mrbird.febs.agent.service.ConversationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
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
    public String health() {
        return "Agent service is running";
    }
    
    @GetMapping("/conversations")
    @ApiOperation("获取会话列表")
    public List<Conversation> getConversations(@RequestParam String userId) {
        // 这里应该实现会话列表查询逻辑
        return List.of();
    }
    
    @DeleteMapping("/conversation/{sessionId}")
    @ApiOperation("清空会话")
    public String clearConversation(@PathVariable String sessionId) {
        conversationService.clearConversation(sessionId);
        return "会话已清空";
    }
}