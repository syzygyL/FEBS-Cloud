package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * OpenAI LLM服务实现
 * 使用Function Calling实现工具调用
 * 
 * @author mrbird
 */
@Service("openAILLMService")
public class OpenAILLMServiceImpl implements LLMService {
    
    @Value("${openai.api-key:}")
    private String apiKey;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;
    
    @Autowired
    private Gson gson;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        // 构建简单的聊天请求
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", getSystemPrompt()));
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        
        return callOpenAI(messages, null);
    }
    
    @Override
    public ChatResponse chatWithTools(ChatRequest request, List<Map<String, Object>> toolDefinitions) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", getSystemPrompt()));
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        
        return callOpenAI(messages, toolDefinitions);
    }
    
    private ChatResponse callOpenAI(List<Map<String, String>> messages, List<Map<String, Object>> toolDefinitions) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2000);
            
            // 添加消息
            JsonArray messagesArray = gson.toJsonTree(messages).getAsJsonArray();
            requestBody.add("messages", messagesArray);
            
            // 添加工具定义（如果有的话）
            if (toolDefinitions != null && !toolDefinitions.isEmpty()) {
                JsonArray toolsArray = new JsonArray();
                for (Map<String, Object> toolDef : toolDefinitions) {
                    JsonObject function = new JsonObject();
                    function.addProperty("name", (String) toolDef.get("name"));
                    function.addProperty("description", (String) toolDef.get("description"));
                    function.add("parameters", gson.toJsonTree(toolDef.get("parameters")));
                    
                    JsonObject tool = new JsonObject();
                    tool.addProperty("type", "function");
                    tool.add("function", function);
                    toolsArray.add(tool);
                }
                requestBody.add("tools", toolsArray);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setContent("LLM调用失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    private ChatResponse parseResponse(String responseBody) {
        ChatResponse response = new ChatResponse();
        
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = json.getAsJsonArray("choices");
            
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                
                // 检查是否有工具调用
                if (message.has("tool_calls") && !message.get("tool_calls").isJsonNull()) {
                    JsonArray toolCalls = message.getAsJsonArray("tool_calls");
                    if (toolCalls.size() > 0) {
                        JsonObject toolCall = toolCalls.get(0).getAsJsonObject();
                        JsonObject function = toolCall.getAsJsonObject("function");
                        
                        response.setHasToolCall(true);
                        ChatResponse.ToolCall tc = new ChatResponse.ToolCall();
                        tc.setName(function.get("name").getAsString());
                        tc.setArguments(function.get("arguments").getAsString());
                        response.setToolCall(tc);
                    }
                } else {
                    // 普通文本响应
                    String content = message.has("content") ? message.get("content").getAsString() : "";
                    response.setContent(content);
                }
            }
        } catch (Exception e) {
            response.setContent("解析LLM响应失败: " + e.getMessage());
        }
        
        return response;
    }
    
    private String getSystemPrompt() {
        return """
            你是FEBS Cloud微服务权限管理系统的智能助手。你可以帮助用户：
            
            1. 用户管理：查询、新增、修改、删除用户信息
            2. 权限管理：查询角色、查看角色详情、分配权限
            3. 定时任务：查询、创建、暂停、恢复、执行定时任务
            4. 安全审计：分析登录日志、权限变更、生成安全报告
            5. 代码生成：列出数据库表、生成CRUD代码
            6. 系统监控：查看服务健康状态、系统性能指标
            
            请使用提供的工具来执行用户的请求。如果用户的请求不明确，请先询问确认。
            回复时使用中文，格式清晰易读。
            """;
    }
}