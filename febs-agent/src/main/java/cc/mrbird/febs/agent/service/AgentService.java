package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.configure.LLMConfigure;
import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import cc.mrbird.febs.agent.tool.AgentTool;
import cc.mrbird.febs.agent.tool.ToolRegistry;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent服务
 * 核心服务类，负责协调LLM和工具调用
 * 支持多种LLM实现：OpenAI / 本地Ollama / 模拟
 * 
 * @author mrbird
 */
@Service
public class AgentService {
    
    @Autowired
    private LLMConfigure llmConfigure;
    
    @Autowired
    @Qualifier("mockLLMService")
    private LLMService mockLlmService;
    
    @Autowired
    @Qualifier("openAILLMService")
    private LLMService openAILlmService;
    
    @Autowired
    @Qualifier("localLLMService")
    private LLMService localLlmService;
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    @Autowired
    private Gson gson;
    
    /**
     * 获取当前配置的LLM服务
     */
    private LLMService getLlmService() {
        String provider = llmConfigure.getProvider();
        switch (provider.toLowerCase()) {
            case "openai":
                return openAILlmService;
            case "local":
                return localLlmService;
            default:
                return mockLlmService;
        }
    }
    
    /**
     * 处理用户消息
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse handleMessage(ChatRequest request) {
        String message = request.getMessage();
        
        // 1. 获取工具定义
        List<Map<String, Object>> toolDefinitions = toolRegistry.getToolDefinitions();
        
        // 2. 调用LLM处理
        LLMService llmService = getLlmService();
        ChatResponse llmResponse = llmService.chatWithTools(request, toolDefinitions);
        
        // 3. 如果LLM决定调用工具，执行工具
        if (llmResponse.isHasToolCall() && llmResponse.getToolCall() != null) {
            return executeToolCall(llmResponse.getToolCall());
        }
        
        // 4. 如果LLM没有调用工具，使用本地意图识别作为fallback
        if (llmResponse.getContent() == null || llmResponse.getContent().isEmpty()) {
            return handleWithLocalIntent(message);
        }
        
        return llmResponse;
    }
    
    /**
     * 执行工具调用
     */
    private ChatResponse executeToolCall(ChatResponse.ToolCall toolCall) {
        ChatResponse response = new ChatResponse();
        
        String toolName = toolCall.getName();
        String arguments = toolCall.getArguments();
        
        AgentTool tool = toolRegistry.getTool(toolName);
        if (tool != null) {
            String toolResult = tool.execute(arguments);
            response.setContent(formatToolResult(toolName, toolResult));
            response.setHasToolCall(true);
            response.setToolCall(toolCall);
        } else {
            response.setContent("未找到对应的工具: " + toolName);
        }
        
        return response;
    }
    
    /**
     * 本地意图识别（作为LLM的fallback）
     */
    private ChatResponse handleWithLocalIntent(String message) {
        String intent = recognizeIntent(message);
        Map<String, Object> params = extractParameters(message);
        
        if (!"general".equals(intent)) {
            return executeLocalIntent(intent, params);
        }
        
        return generateGeneralResponse();
    }
    
    private String recognizeIntent(String message) {
        if (Pattern.compile("(安全|审计|登录日志|异常|攻击|黑名单)").matcher(message).find()) {
            return "security";
        }
        if (Pattern.compile("(定时|任务|调度|备份|同步|清理)").matcher(message).find()) {
            return "job";
        }
        if (Pattern.compile("(代码生成|生成代码|CRUD|表结构)").matcher(message).find()) {
            return "generator";
        }
        if (Pattern.compile("(系统|服务|状态|健康|监控)").matcher(message).find()) {
            return "system";
        }
        if (Pattern.compile("(角色|权限|授权|分配)").matcher(message).find()) {
            return "permission";
        }
        if (Pattern.compile("(用户|账号|登录|密码)").matcher(message).find()) {
            return "user";
        }
        return "general";
    }
    
    private Map<String, Object> extractParameters(String message) {
        Map<String, Object> params = new java.util.HashMap<>();
        
        if (Pattern.compile("(查询|列出|查看|显示)").matcher(message).find()) {
            params.put("action", "list");
        } else if (Pattern.compile("(新增|添加|创建)").matcher(message).find()) {
            params.put("action", "create");
        }
        
        return params;
    }
    
    private ChatResponse executeLocalIntent(String intent, Map<String, Object> params) {
        String toolName;
        switch (intent) {
            case "user": toolName = "user_management"; break;
            case "permission": toolName = "permission_management"; break;
            case "job": toolName = "job_management"; break;
            case "security": toolName = "security_audit"; break;
            case "generator": toolName = "code_generator"; break;
            case "system": toolName = "system_monitor"; break;
            default: return generateGeneralResponse();
        }
        
        AgentTool tool = toolRegistry.getTool(toolName);
        if (tool != null) {
            String arguments = gson.toJson(params);
            String toolResult = tool.execute(arguments);
            
            ChatResponse response = new ChatResponse();
            response.setContent(formatToolResult(toolName, toolResult));
            response.setHasToolCall(true);
            
            ChatResponse.ToolCall tc = new ChatResponse.ToolCall();
            tc.setName(toolName);
            tc.setArguments(arguments);
            response.setToolCall(tc);
            return response;
        }
        
        return generateGeneralResponse();
    }
    
    private String formatToolResult(String toolName, String toolResult) {
        try {
            Map<String, Object> resultMap = gson.fromJson(toolResult, Map.class);
            String msg = (String) resultMap.get("message");
            Object data = resultMap.get("data");
            
            StringBuilder sb = new StringBuilder();
            sb.append("✅ ").append(msg != null ? msg : "操作成功").append("\n\n");
            if (data != null) {
                sb.append(gson.toJson(data));
            }
            return sb.toString();
        } catch (Exception e) {
            return "工具执行结果：\n" + toolResult;
        }
    }
    
    private ChatResponse generateGeneralResponse() {
        ChatResponse response = new ChatResponse();
        response.setContent("""
            👋 您好！我是FEBS Cloud智能助手，可以帮您：
            
            👤 **用户管理** — 查询用户列表、用户详情
            🔑 **权限管理** — 查询角色列表、角色详情
            ⏰ **定时任务** — 查看任务列表、创建/暂停/恢复任务
            🛡️ **安全审计** — 登录异常分析、安全报告
            💻 **代码生成** — 列出数据库表、生成CRUD代码
            🖥️ **系统监控** — 服务健康状态、性能指标
            
            💡 **示例指令**：
            - "查询所有用户"
            - "查看角色列表"
            - "这周有哪些异常登录"
            - "查看系统健康状态"
            """);
        return response;
    }
}