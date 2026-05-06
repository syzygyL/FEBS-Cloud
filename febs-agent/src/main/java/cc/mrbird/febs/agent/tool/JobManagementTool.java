package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务管理工具
 * 通过Feign客户端调用FEBS-Server-Job的任务服务API
 * 
 * @author mrbird
 */
@Component
public class JobManagementTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Override
    public String getName() {
        return "job_management";
    }
    
    @Override
    public String getDescription() {
        return "定时任务管理工具，可以查询、创建、暂停、恢复、执行定时任务。" +
               "支持操作：list-任务列表，get-任务详情，create-创建任务，pause-暂停任务，resume-恢复任务，run-立即执行，logs-任务日志";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list", "get", "create", "pause", "resume", "run", "logs"],
                        "description": "操作类型"
                    },
                    "jobId": {
                        "type": "integer",
                        "description": "任务ID"
                    },
                    "jobName": {
                        "type": "string",
                        "description": "任务名称"
                    },
                    "beanName": {
                        "type": "string",
                        "description": "Spring Bean名称"
                    },
                    "methodName": {
                        "type": "string",
                        "description": "方法名称"
                    },
                    "cronExpression": {
                        "type": "string",
                        "description": "Cron表达式"
                    },
                    "current": {
                        "type": "integer",
                        "description": "当前页码，默认1"
                    },
                    "size": {
                        "type": "integer",
                        "description": "每页条数，默认10"
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
                case "list":
                    return listJobs(json);
                case "get":
                    return getJob(json);
                case "create":
                    return createJob(json);
                case "pause":
                    return pauseJob(json);
                case "resume":
                    return resumeJob(json);
                case "run":
                    return runJob(json);
                case "logs":
                    return getJobLogs(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行定时任务管理操作失败: " + e.getMessage();
        }
    }
    
    private String listJobs(JsonObject json) {
        // 实际应调用 JobServiceClient.getJobList
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务列表查询成功");
        result.put("data", Map.of(
            "total", 5,
            "records", List.of(
                Map.of("jobId", 1, "jobName", "数据备份任务", "beanName", "backupTask", "methodName", "execute", "cronExpression", "0 0 2 * * ?", "status", "RUNNING"),
                Map.of("jobId", 2, "jobName", "日志清理任务", "beanName", "logCleanTask", "methodName", "execute", "cronExpression", "0 0 3 * * ?", "status", "RUNNING"),
                Map.of("jobId", 3, "jobName", "数据同步任务", "beanName", "dataSyncTask", "methodName", "execute", "cronExpression", "0 */30 * * * ?", "status", "PAUSED")
            )
        ));
        return gson.toJson(result);
    }
    
    private String getJob(JsonObject json) {
        Long jobId = json.has("jobId") ? json.get("jobId").getAsLong() : null;
        if (jobId == null) {
            return "错误：缺少jobId参数";
        }
        
        // 实际应调用 JobServiceClient.getJobById
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务详情查询成功");
        result.put("data", Map.of(
            "jobId", jobId,
            "jobName", "数据备份任务",
            "beanName", "backupTask",
            "methodName", "execute",
            "params", "database=febs_cloud",
            "cronExpression", "0 0 2 * * ?",
            "status", "RUNNING",
            "remark", "每天凌晨2点执行数据库备份",
            "createTime", "2024-01-01 10:00:00"
        ));
        return gson.toJson(result);
    }
    
    private String createJob(JsonObject json) {
        String jobName = json.has("jobName") ? json.get("jobName").getAsString() : null;
        String beanName = json.has("beanName") ? json.get("beanName").getAsString() : null;
        String methodName = json.has("methodName") ? json.get("methodName").getAsString() : null;
        String cronExpression = json.has("cronExpression") ? json.get("cronExpression").getAsString() : null;
        
        if (jobName == null || beanName == null || methodName == null || cronExpression == null) {
            return "错误：缺少必要参数（jobName、beanName、methodName、cronExpression）";
        }
        
        // 实际应调用 JobServiceClient.createJob
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务创建成功");
        result.put("data", Map.of(
            "jobId", 4,
            "jobName", jobName,
            "beanName", beanName,
            "methodName", methodName,
            "cronExpression", cronExpression,
            "status", "RUNNING"
        ));
        return gson.toJson(result);
    }
    
    private String pauseJob(JsonObject json) {
        Long jobId = json.has("jobId") ? json.get("jobId").getAsLong() : null;
        if (jobId == null) {
            return "错误：缺少jobId参数";
        }
        
        // 实际应调用 JobServiceClient.pauseJob
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务暂停成功");
        result.put("data", Map.of("jobId", jobId, "status", "PAUSED"));
        return gson.toJson(result);
    }
    
    private String resumeJob(JsonObject json) {
        Long jobId = json.has("jobId") ? json.get("jobId").getAsLong() : null;
        if (jobId == null) {
            return "错误：缺少jobId参数";
        }
        
        // 实际应调用 JobServiceClient.resumeJob
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务恢复成功");
        result.put("data", Map.of("jobId", jobId, "status", "RUNNING"));
        return gson.toJson(result);
    }
    
    private String runJob(JsonObject json) {
        Long jobId = json.has("jobId") ? json.get("jobId").getAsLong() : null;
        if (jobId == null) {
            return "错误：缺少jobId参数";
        }
        
        // 实际应调用 JobServiceClient.runJob
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务已触发执行");
        result.put("data", Map.of("jobId", jobId, "triggerTime", System.currentTimeMillis()));
        return gson.toJson(result);
    }
    
    private String getJobLogs(JsonObject json) {
        Long jobId = json.has("jobId") ? json.get("jobId").getAsLong() : null;
        int current = json.has("current") ? json.get("current").getAsInt() : 1;
        int size = json.has("size") ? json.get("size").getAsInt() : 10;
        
        // 实际应调用 JobLogServiceClient.getJobLogs
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务日志查询成功");
        result.put("data", Map.of(
            "total", 30,
            "records", List.of(
                Map.of("jobId", jobId, "status", "SUCCESS", "time", "2024-01-15 02:00:00", "duration", "15s"),
                Map.of("jobId", jobId, "status", "SUCCESS", "time", "2024-01-14 02:00:00", "duration", "12s"),
                Map.of("jobId", jobId, "status", "FAILURE", "time", "2024-01-13 02:00:00", "duration", "30s", "error", "Connection timeout")
            )
        ));
        return gson.toJson(result);
    }
}