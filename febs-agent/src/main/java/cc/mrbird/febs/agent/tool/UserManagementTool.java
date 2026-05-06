package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户管理工具
 * 提供用户查询、新增、修改、删除等功能
 * 
 * @author mrbird
 */
@Component
public class UserManagementTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    // 实际项目中这里应该注入Feign客户端，调用FEBS-Server-System的API
    // @Autowired
    // private UserServiceClient userServiceClient;
    
    @Override
    public String getName() {
        return "user_management";
    }
    
    @Override
    public String getDescription() {
        return "用户管理工具，可以查询、新增、修改、删除用户信息";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list", "get", "create", "update", "delete"],
                        "description": "操作类型：list-列表查询，get-详情查询，create-新增，update-修改，delete-删除"
                    },
                    "username": {
                        "type": "string",
                        "description": "用户名"
                    },
                    "userId": {
                        "type": "integer",
                        "description": "用户ID"
                    },
                    "deptId": {
                        "type": "integer",
                        "description": "部门ID"
                    },
                    "params": {
                        "type": "object",
                        "description": "其他参数"
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
                    return listUsers(json);
                case "get":
                    return getUser(json);
                case "create":
                    return createUser(json);
                case "update":
                    return updateUser(json);
                case "delete":
                    return deleteUser(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行用户管理操作失败: " + e.getMessage();
        }
    }
    
    private String listUsers(JsonObject json) {
        // 模拟数据，实际应该调用远程API
        Map<String, Object> result = Map.of(
            "code", 200,
            "message", "查询成功",
            "data", Map.of(
                "total", 100,
                "records", java.util.List.of(
                    Map.of("userId", 1, "username", "mrbird", "deptId", 1, "status", "1"),
                    Map.of("userId", 2, "username", "scott", "deptId", 2, "status", "1")
                )
            )
        );
        return gson.toJson(result);
    }
    
    private String getUser(JsonObject json) {
        return """
            {"code": 200, "message": "查询成功", "data": {"userId": 1, "username": "mrbird", "deptId": 1, "status": "1"}}
            """;
    }
    
    private String createUser(JsonObject json) {
        return """
            {"code": 200, "message": "用户创建成功", "data": {"userId": 3}}
            """;
    }
    
    private String updateUser(JsonObject json) {
        return """
            {"code": 200, "message": "用户更新成功"}
            """;
    }
    
    private String deleteUser(JsonObject json) {
        return """
            {"code": 200, "message": "用户删除成功"}
            """;
    }
}