package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.dto.ChatResponse;
import cc.mrbird.febs.agent.tool.AgentTool;
import cc.mrbird.febs.agent.tool.ToolRegistry;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent服务
 * 核心服务类，负责协调LLM和工具调用
 * 实现自然语言理解、意图识别、工具调用编排
 * 
 * @author mrbird
 */
@Service
public class AgentService {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    @Autowired
    private Gson gson;
    
    // 意图识别正则模式
    private static final Pattern USER_PATTERN = Pattern.compile("(用户|账号|登录|密码|注册|scott|mrbird|jane|admin)");
    private static final Pattern ROLE_PATTERN = Pattern.compile("(角色|权限|授权|分配|管理员|普通用户|访客)");
    private static final Pattern JOB_PATTERN = Pattern.compile("(定时|任务|调度|备份|同步|清理|cron|Quartz)");
    private static final Pattern SECURITY_PATTERN = Pattern.compile("(安全|审计|登录日志|异常|攻击|黑名单|封禁|暴力破解)");
    private static final Pattern GENERATOR_PATTERN = Pattern.compile("(代码生成|生成代码|CRUD|表结构|代码模板)");
    private static final Pattern SYSTEM_PATTERN = Pattern.compile("(系统|服务|状态|健康|监控|性能|资源)");
    private static final Pattern LIST_PATTERN = Pattern.compile("(查询|列出|查看|显示|有哪些|列表|所有)");
    private static final Pattern CREATE_PATTERN = Pattern.compile("(新增|添加|创建|新建)");
    private static final Pattern UPDATE_PATTERN = Pattern.compile("(修改|更新|编辑|变更)");
    private static final Pattern DELETE_PATTERN = Pattern.compile("(删除|移除|取消)");
    private static final Pattern PAUSE_PATTERN = Pattern.compile("(暂停|停止|停用)");
    private static final Pattern RESUME_PATTERN = Pattern.compile("(恢复|重启|启用)");
    
    /**
     * 处理用户消息
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse handleMessage(ChatRequest request) {
        String message = request.getMessage();
        
        // 1. 意图识别
        String intent = recognizeIntent(message);
        
        // 2. 参数提取
        Map<String, Object> params = extractParameters(message);
        
        // 3. 工具选择和调用
        if (!"general".equals(intent)) {
            return executeTool(intent, params);
        }
        
        // 4. 通用对话
        return generateGeneralResponse(message);
    }
    
    /**
     * 意图识别
     */
    private String recognizeIntent(String message) {
        if (SECURITY_PATTERN.matcher(message).find()) {
            return "security";
        }
        if (JOB_PATTERN.matcher(message).find()) {
            return "job";
        }
        if (GENERATOR_PATTERN.matcher(message).find()) {
            return "generator";
        }
        if (SYSTEM_PATTERN.matcher(message).find()) {
            return "system";
        }
        if (ROLE_PATTERN.matcher(message).find()) {
            return "permission";
        }
        if (USER_PATTERN.matcher(message).find()) {
            return "user";
        }
        return "general";
    }
    
    /**
     * 提取参数
     */
    private Map<String, Object> extractParameters(String message) {
        Map<String, Object> params = new java.util.HashMap<>();
        
        // 提取用户名
        Pattern usernamePattern = Pattern.compile("(\\w+)的");
        Matcher usernameMatcher = usernamePattern.matcher(message);
        if (usernameMatcher.find()) {
            params.put("username", usernameMatcher.group(1));
        }
        
        // 提取数字ID
        Pattern idPattern = Pattern.compile("ID[：:]?\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(message);
        if (idMatcher.find()) {
            params.put("id", Integer.parseInt(idMatcher.group(1)));
        }
        
        // 提取操作类型
        if (LIST_PATTERN.matcher(message).find()) {
            params.put("action", "list");
        } else if (CREATE_PATTERN.matcher(message).find()) {
            params.put("action", "create");
        } else if (UPDATE_PATTERN.matcher(message).find()) {
            params.put("action", "update");
        } else if (DELETE_PATTERN.matcher(message).find()) {
            params.put("action", "delete");
        } else if (PAUSE_PATTERN.matcher(message).find()) {
            params.put("action", "pause");
        } else if (RESUME_PATTERN.matcher(message).find()) {
            params.put("action", "resume");
        }
        
        return params;
    }
    
    /**
     * 执行工具调用
     */
    private ChatResponse executeTool(String intent, Map<String, Object> params) {
        ChatResponse response = new ChatResponse();
        
        String toolName;
        String action;
        
        switch (intent) {
            case "user":
                toolName = "user_management";
                action = (String) params.getOrDefault("action", "list");
                break;
            case "permission":
                toolName = "permission_management";
                action = (String) params.getOrDefault("action", "list_roles");
                break;
            case "job":
                toolName = "job_management";
                action = (String) params.getOrDefault("action", "list");
                break;
            case "security":
                toolName = "security_audit";
                action = (String) params.getOrDefault("action", "security_report");
                break;
            case "generator":
                toolName = "code_generator";
                action = (String) params.getOrDefault("action", "list_tables");
                break;
            case "system":
                toolName = "system_monitor";
                action = (String) params.getOrDefault("action", "health");
                break;
            default:
                return generateGeneralResponse("无法识别的意图");
        }
        
        AgentTool tool = toolRegistry.getTool(toolName);
        if (tool != null) {
            String arguments = gson.toJson(params);
            String toolResult = tool.execute(arguments);
            
            response.setContent(formatToolResult(toolName, toolResult));
            response.setHasToolCall(true);
            
            ChatResponse.ToolCall toolCall = new ChatResponse.ToolCall();
            toolCall.setName(toolName);
            toolCall.setArguments(arguments);
            response.setToolCall(toolCall);
        } else {
            response.setContent("未找到对应的工具: " + toolName);
        }
        
        return response;
    }
    
    /**
     * 格式化工具执行结果
     */
    private String formatToolResult(String toolName, String toolResult) {
        try {
            // 尝试解析JSON并美化
            Map<String, Object> resultMap = gson.fromJson(toolResult, Map.class);
            String message = (String) resultMap.get("message");
            Object data = resultMap.get("data");
            
            StringBuilder sb = new StringBuilder();
            sb.append("✅ ").append(message != null ? message : "操作成功").append("\n\n");
            
            if (data != null) {
                sb.append(gson.toJson(data));
            }
            
            return sb.toString();
        } catch (Exception e) {
            // JSON解析失败，直接返回
            return "工具执行结果：\n" + toolResult;
        }
    }
    
    /**
     * 生成通用对话响应
     */
    private ChatResponse generateGeneralResponse(String message) {
        ChatResponse response = new ChatResponse();
        
        StringBuilder help = new StringBuilder();
        help.append("👋 您好！我是FEBS Cloud智能助手，可以帮您：\n\n");
        help.append("👤 **用户管理**\n");
        help.append("   - 查询用户列表、用户详情\n");
        help.append("   - 校验用户名是否存在\n\n");
        help.append("🔑 **权限管理**\n");
        help.append("   - 查询角色列表、角色详情\n");
        help.append("   - 查看用户角色\n\n");
        help.append("⏰ **定时任务**\n");
        help.append("   - 查看任务列表、任务日志\n");
        help.append("   - 创建、暂停、恢复、执行任务\n\n");
        help.append("🛡️ **安全审计**\n");
        help.append("   - 登录异常分析\n");
        help.append("   - 权限变更审计\n");
        help.append("   - 安全综合报告\n\n");
        help.append("💻 **代码生成**\n");
        help.append("   - 列出数据库表\n");
        help.append("   - 根据描述生成CRUD代码\n\n");
        help.append("🖥️ **系统监控**\n");
        help.append("   - 服务健康状态\n");
        help.append("   - 系统性能指标\n\n");
        help.append("💡 **示例指令**：\n");
        help.append("   - \"查询所有用户\"\n");
        help.append("   - \"查看角色列表\"\n");
        help.append("   - \"这周有哪些异常登录\"\n");
        help.append("   - \"查看系统健康状态\"\n");
        help.append("   - \"列出数据库表\"\n");
        
        response.setContent(help.toString());
        return response;
    }
}