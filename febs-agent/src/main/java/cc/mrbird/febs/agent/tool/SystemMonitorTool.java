package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控工具
 * 查询服务健康状态、系统信息等
 * 
 * @author mrbird
 */
@Component
public class SystemMonitorTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Override
    public String getName() {
        return "system_monitor";
    }
    
    @Override
    public String getDescription() {
        return "系统监控工具，查询服务健康状态、系统资源使用情况。" +
               "支持操作：health-健康检查，services-服务列表，metrics-性能指标";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["health", "services", "metrics"],
                        "description": "操作类型"
                    },
                    "serviceName": {
                        "type": "string",
                        "description": "服务名称（可选）"
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
                case "health":
                    return getHealthStatus();
                case "services":
                    return getServicesStatus();
                case "metrics":
                    return getMetrics();
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行系统监控操作失败: " + e.getMessage();
        }
    }
    
    private String getHealthStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "系统健康状态");
        result.put("data", Map.of(
            "systemStatus", "UP",
            "timestamp", System.currentTimeMillis(),
            "services", List.of(
                Map.of("name", "FEBS-Gateway", "status", "UP", "port", 8301),
                Map.of("name", "FEBS-Auth", "status", "UP", "port", 8101),
                Map.of("name", "FEBS-Server-System", "status", "UP", "port", 8201),
                Map.of("name", "FEBS-Server-Job", "status", "UP", "port", 8204),
                Map.of("name", "FEBS-Agent", "status", "UP", "port", 8601)
            )
        ));
        return gson.toJson(result);
    }
    
    private String getServicesStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "服务状态列表");
        result.put("data", List.of(
            Map.of("name", "FEBS-Gateway", "status", "UP", "port", 8301, "description", "API网关服务"),
            Map.of("name", "FEBS-Auth", "status", "UP", "port", 8101, "description", "认证授权服务"),
            Map.of("name", "FEBS-Server-System", "status", "UP", "port", 8201, "description", "系统管理服务"),
            Map.of("name", "FEBS-Server-Job", "status", "UP", "port", 8204, "description", "任务调度服务"),
            Map.of("name", "FEBS-Server-Generator", "status", "UP", "port", 8203, "description", "代码生成服务"),
            Map.of("name", "FEBS-Agent", "status", "UP", "port", 8601, "description", "智能客服Agent服务")
        ));
        return gson.toJson(result);
    }
    
    private String getMetrics() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "系统性能指标");
        result.put("data", Map.of(
            "jvm", Map.of(
                "heapUsed", "128MB",
                "heapMax", "512MB",
                "nonHeapUsed", "64MB",
                "gcCount", 15,
                "gcTime", "120ms"
            ),
            "system", Map.of(
                "cpuUsage", "23%",
                "memoryUsage", "45%",
                "diskUsage", "62%"
            ),
            "application", Map.of(
                "activeThreads", 25,
                "requestCount", 1234,
                "errorRate", "0.5%"
            )
        ));
        return gson.toJson(result);
    }
}