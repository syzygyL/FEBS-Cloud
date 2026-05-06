package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全审计工具
 * 分析登录日志、操作日志，检测异常行为，生成安全报告
 * 
 * @author mrbird
 */
@Component
public class SecurityAuditTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Override
    public String getName() {
        return "security_audit";
    }
    
    @Override
    public String getDescription() {
        return "安全审计工具，分析登录日志、操作日志，检测异常行为，生成安全报告。" +
               "支持操作：login_analysis-登录异常分析，permission_audit-权限变更审计，security_report-安全报告，blacklist_check-黑名单检查";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["login_analysis", "permission_audit", "security_report", "blacklist_check"],
                        "description": "操作类型"
                    },
                    "username": {
                        "type": "string",
                        "description": "用户名（可选）"
                    },
                    "days": {
                        "type": "integer",
                        "description": "分析天数，默认7天"
                    },
                    "ip": {
                        "type": "string",
                        "description": "IP地址（可选）"
                    }
                },
                "required": ["action"]
            }
            """;
    }
    
    @Override
    public String execute(String arguments) {
        try {
            JsonObject json = gson.fromJson(arguments, JsonObject.class);
            String action = json.get("action").getAsString();
            
            switch (action) {
                case "login_analysis":
                    return analyzeLoginLogs(json);
                case "permission_audit":
                    return auditPermissionChanges(json);
                case "security_report":
                    return generateSecurityReport(json);
                case "blacklist_check":
                    return checkBlacklist(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行安全审计操作失败: " + e.getMessage();
        }
    }
    
    private String analyzeLoginLogs(JsonObject json) {
        String username = json.has("username") ? json.get("username").getAsString() : null;
        int days = json.has("days") ? json.get("days").getAsInt() : 7;
        
        // 模拟数据，实际应调用 LoginLogService
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录日志分析报告（近" + days + "天）");
        result.put("data", Map.of(
            "totalLogins", 156,
            "failedLogins", 12,
            "uniqueUsers", 45,
            "uniqueIPs", 23,
            "suspiciousActivities", List.of(
                Map.of(
                    "type", "暴力破解尝试",
                    "ip", "192.168.1.100",
                    "count", 8,
                    "timeRange", "2024-01-15 09:00-09:30",
                    "targetUser", "admin"
                ),
                Map.of(
                    "type", "异地登录",
                    "username", "scott",
                    "ip", "10.0.0.50",
                    "location", "北京",
                    "usualLocation", "上海",
                    "time", "2024-01-14 22:15"
                )
            ),
            "recommendations", List.of(
                "建议对IP 192.168.1.100 添加黑名单",
                "建议提醒用户 scott 确认是否本人登录",
                "建议启用双因素认证"
            )
        ));
        return gson.toJson(result);
    }
    
    private String auditPermissionChanges(JsonObject json) {
        String username = json.has("username") ? json.get("username").getAsString() : null;
        int days = json.has("days") ? json.get("days").getAsInt() : 7;
        
        // 模拟数据，实际应调用 LogService
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "权限变更审计报告（近" + days + "天）");
        result.put("data", Map.of(
            "totalPermissionChanges", 15,
            "roleAssignments", 8,
            "menuModifications", 5,
            "userDeletions", 2,
            "highRiskChanges", List.of(
                Map.of(
                    "type", "超级管理员角色分配",
                    "operator", "mrbird",
                    "target", "newuser",
                    "time", "2024-01-13 14:30",
                    "riskLevel", "HIGH"
                )
            ),
            "summary", "本周权限变更正常，无异常操作"
        ));
        return gson.toJson(result);
    }
    
    private String generateSecurityReport(JsonObject json) {
        int days = json.has("days") ? json.get("days").getAsInt() : 7;
        
        // 模拟数据，实际应综合调用多个服务
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "安全综合报告（近" + days + "天）");
        result.put("data", Map.of(
            "overallRisk", "LOW",
            "riskScore", 25,
            "summary", Map.of(
                "totalLogins", 156,
                "failedAttempts", 12,
                "blockedIPs", 3,
                "permissionChanges", 15,
                "anomaliesDetected", 2
            ),
            "riskAreas", List.of(
                Map.of("area", "登录安全", "risk", "LOW", "description", "登录失败率低于5%"),
                Map.of("area", "权限管理", "risk", "MEDIUM", "description", "发现1次高风险角色分配"),
                Map.of("area", "网络安全", "risk", "LOW", "description", "已封禁3个可疑IP")
            ),
            "recommendations", List.of(
                "建议定期审查用户权限",
                "建议启用操作日志实时告警",
                "建议配置IP白名单限制管理后台访问"
            )
        ));
        return gson.toJson(result);
    }
    
    private String checkBlacklist(JsonObject json) {
        String ip = json.has("ip") ? json.get("ip").getAsString() : null;
        
        // 模拟数据，实际应调用 BlackListService
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "黑名单检查结果");
        result.put("data", Map.of(
            "ip", ip != null ? ip : "未指定",
            "inBlacklist", false,
            "totalBlacklisted", 3,
            "recentBlocks", List.of(
                Map.of("ip", "192.168.1.100", "reason", "暴力破解", "blockedAt", "2024-01-15 09:30"),
                Map.of("ip", "10.0.0.99", "reason", "异常访问", "blockedAt", "2024-01-14 15:20")
            )
        ));
        return gson.toJson(result);
    }
}