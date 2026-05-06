package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 本地LLM服务实现
 * 支持Ollama、vLLM等本地部署的模型
 * 
 * @author mrbird
 */
@Service("localLLMService")
public class LocalLLMServiceImpl implements LLMService {
    
    @Value("${local.llm.url:http://localhost:11434}")
    private String localLlmUrl;
    
    @Value("${local.llm.model:qwen2.5:7b}")
    private String model;
    
    @Autowired
    private Gson gson;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", getSystemPrompt()));
        
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        
        return callOllama(messages);
    }
    
    @Override
    public ChatResponse chatWithTools(ChatRequest request, List<Map<String, Object>> toolDefinitions) {
        // Ollama不原生支持Function Calling，通过prompt注入工具信息
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", getSystemPromptWithTools(toolDefinitions)));
        
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        
        return callOllama(messages);
    }
    
    private ChatResponse callOllama(List<Map<String, String>> messages) {
        ChatResponse response = new ChatResponse();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("stream", false);
            
            JsonArray messagesArray = gson.toJsonTree(messages).getAsJsonArray();
            requestBody.add("messages", messagesArray);
            
            // 添加工具定义到options
            JsonObject options = new JsonObject();
            options.addProperty("temperature", 0.7);
            requestBody.add("options", options);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> ollamaResponse = restTemplate.exchange(
                localLlmUrl + "/api/chat",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return parseOllamaResponse(ollamaResponse.getBody());
            
        } catch (Exception e) {
            response.setContent("本地LLM调用失败: " + e.getMessage());
            return response;
        }
    }
    
    private ChatResponse parseOllamaResponse(String responseBody) {
        ChatResponse response = new ChatResponse();
        
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonObject message = json.getAsJsonObject("message");
            
            if (message != null && message.has("content")) {
                String content = message.get("content").getAsString();
                
                // 尝试解析工具调用JSON
                if (content.contains("\"tool\":") && content.contains("\"action\":")) {
                    try {
                        // 提取JSON部分
                        int start = content.indexOf("{");
                        int end = content.lastIndexOf("}") + 1;
                        if (start >= 0 && end > start) {
                            String jsonPart = content.substring(start, end);
                            JsonObject toolCall = gson.fromJson(jsonPart, JsonObject.class);
                            
                            if (toolCall.has("tool")) {
                                response.setHasToolCall(true);
                                ChatResponse.ToolCall tc = new ChatResponse.ToolCall();
                                tc.setName(toolCall.get("tool").getAsString());
                                tc.setArguments(jsonPart);
                                response.setToolCall(tc);
                                return response;
                            }
                        }
                    } catch (Exception ignored) {
                        // JSON解析失败，当作普通文本
                    }
                }
                
                response.setContent(content);
            }
        } catch (Exception e) {
            response.setContent("解析Ollama响应失败: " + e.getMessage());
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
            
            请使用工具来执行用户的请求。如果需要调用工具，请返回JSON格式：
            {"tool": "工具名", "action": "操作", "参数名": "参数值"}
            
            回复时使用中文，格式清晰易读。
            """;
    }
    
    private String getSystemPromptWithTools(List<Map<String, Object>> toolDefinitions) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrompt());
        sb.append("\n\n可用工具：\n");
        
        for (Map<String, Object> tool : toolDefinitions) {
            sb.append("- ").append(tool.get("name")).append(": ").append(tool.get("description")).append("\n");
        }
        
        sb.append("\n调用工具时请返回JSON格式：{\"tool\": \"工具名\", \"action\": \"操作\", ...其他参数}");
        
        return sb.toString();
    }
}