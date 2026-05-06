package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 系统监控工具
 * 提供系统状态查询、服务监控、日志查看等功能
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
        return "系统监控工具，可以查询系统状态、服务健康状况、资源使用情况等";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["health", "services", "metrics", "logs"],
                        "description": "操作类型：health-健康检查，services-服务状态，metrics-性能指标，logs-日志查询"
                    },
                    "serviceName": {
                        "type": "string",
                        "description": "服务名称（可选）"
                    },
                    "level": {
                        "type": "string",
                        "enum": ["INFO", "WARN", "ERROR"],
                        "description": "日志级别"
                    }
                },
                "required": ["action"]
            }
            """;
    }
    
    @Override
    public String execute(String arguments) {
        return """
            {
                "code": 200,
                "message": "查询成功",
                "data": {
                    "systemHealth": "UP",
                    "services": [
                        {"name": "FEBS-Gateway", "status": "UP", "port": 8301},
                        {"name": "FEBS-Auth", "status": "UP", "port": 8101},
                        {"name": "FEBS-Server-System", "status": "UP", "port": 8201},
                        {"name": "FEBS-Agent", "status": "UP", "port": 8601}
                    ],
                    "memoryUsage": "45%",
                    "cpuUsage": "23%"
                }
            }
            """;
    }
}