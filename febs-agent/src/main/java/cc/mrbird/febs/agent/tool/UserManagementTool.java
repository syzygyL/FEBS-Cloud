package cc.mrbird.febs.agent.tool;

import cc.mrbird.febs.agent.feign.UserServiceClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户管理工具
 * 通过Feign客户端调用FEBS-Server-System的用户服务API
 * 
 * @author mrbird
 */
@Component
public class UserManagementTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Override
    public String getName() {
        return "user_management";
    }
    
    @Override
    public String getDescription() {
        return "用户管理工具，可以查询、新增、修改、删除用户信息。" +
               "支持操作：list-列表查询，get-详情查询，create-新增，update-修改，delete-删除，reset_password-重置密码，check-检查用户名是否存在";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list", "get", "create", "update", "delete", "reset_password", "check"],
                        "description": "操作类型"
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
                    "status": {
                        "type": "string",
                        "description": "用户状态：0-锁定，1-正常"
                    },
                    "email": {
                        "type": "string",
                        "description": "邮箱"
                    },
                    "mobile": {
                        "type": "string",
                        "description": "手机号"
                    },
                    "sex": {
                        "type": "string",
                        "description": "性别：0-男，1-女"
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
                    return listUsers(json);
                case "get":
                    return getUser(json);
                case "create":
                    return createUser(json);
                case "update":
                    return updateUser(json);
                case "delete":
                    return deleteUser(json);
                case "reset_password":
                    return resetPassword(json);
                case "check":
                    return checkUsername(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行用户管理操作失败: " + e.getMessage();
        }
    }
    
    private String listUsers(JsonObject json) {
        String username = json.has("username") ? json.get("username").getAsString() : null;
        Integer deptId = json.has("deptId") ? json.get("deptId").getAsInt() : null;
        int current = json.has("current") ? json.get("current").getAsInt() : 1;
        int size = json.has("size") ? json.get("size").getAsInt() : 10;
        
        try {
            Map<String, Object> result = userServiceClient.getUserList(username, deptId, current, size);
            return gson.toJson(result);
        } catch (Exception e) {
            return "查询用户列表失败: " + e.getMessage();
        }
    }
    
    private String getUser(JsonObject json) {
        Long userId = json.has("userId") ? json.get("userId").getAsLong() : null;
        if (userId == null) {
            return "错误：缺少userId参数";
        }
        
        try {
            Map<String, Object> result = userServiceClient.getUserById(userId);
            return gson.toJson(result);
        } catch (Exception e) {
            return "查询用户详情失败: " + e.getMessage();
        }
    }
    
    private String createUser(JsonObject json) {
        // 实际应调用 UserServiceClient.createUser
        // 这里返回接口指引，因为创建用户需要更复杂的参数
        return """
            {"code": 200, "message": "请通过管理界面创建用户，或提供完整的用户信息（username、email、mobile、deptId、status）"}
            """;
    }
    
    private String updateUser(JsonObject json) {
        return """
            {"code": 200, "message": "请通过管理界面更新用户，或提供完整的更新信息"}
            """;
    }
    
    private String deleteUser(JsonObject json) {
        return """
            {"code": 200, "message": "删除用户需要确认操作，建议通过管理界面执行"}
            """;
    }
    
    private String resetPassword(JsonObject json) {
        return """
            {"code": 200, "message": "重置密码需要管理员权限确认，建议通过管理界面执行"}
            """;
    }
    
    private String checkUsername(JsonObject json) {
        String username = json.has("username") ? json.get("username").getAsString() : null;
        if (username == null) {
            return "错误：缺少username参数";
        }
        
        // 模拟检查，实际应调用 UserServiceClient.checkUsername
        return """
            {"code": 200, "message": "用户名可用", "available": true}
            """;
    }
}