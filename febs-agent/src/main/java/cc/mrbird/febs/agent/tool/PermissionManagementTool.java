package cc.mrbird.febs.agent.tool;

import cc.mrbird.febs.agent.feign.RoleServiceClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 权限管理工具
 * 通过Feign客户端调用FEBS-Server-System的角色/菜单服务API
 * 
 * @author mrbird
 */
@Component
public class PermissionManagementTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Autowired
    private RoleServiceClient roleServiceClient;
    
    @Override
    public String getName() {
        return "permission_management";
    }
    
    @Override
    public String getDescription() {
        return "权限管理工具，可以查询角色、查看角色详情、获取用户角色等。" +
               "支持操作：list_roles-角色列表，get_role-角色详情，get_user_roles-用户角色，assign_role-分配角色";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list_roles", "get_role", "get_user_roles", "assign_role"],
                        "description": "操作类型"
                    },
                    "roleId": {
                        "type": "integer",
                        "description": "角色ID"
                    },
                    "userId": {
                        "type": "integer",
                        "description": "用户ID"
                    },
                    "roleName": {
                        "type": "string",
                        "description": "角色名称（用于搜索）"
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
                case "list_roles":
                    return listRoles(json);
                case "get_role":
                    return getRole(json);
                case "get_user_roles":
                    return getUserRoles(json);
                case "assign_role":
                    return assignRole(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行权限管理操作失败: " + e.getMessage();
        }
    }
    
    private String listRoles(JsonObject json) {
        String roleName = json.has("roleName") ? json.get("roleName").getAsString() : null;
        int current = json.has("current") ? json.get("current").getAsInt() : 1;
        int size = json.has("size") ? json.get("size").getAsInt() : 10;
        
        try {
            Map<String, Object> result = roleServiceClient.getRoleList(roleName, current, size);
            return gson.toJson(result);
        } catch (Exception e) {
            return "查询角色列表失败: " + e.getMessage();
        }
    }
    
    private String getRole(JsonObject json) {
        Long roleId = json.has("roleId") ? json.get("roleId").getAsLong() : null;
        if (roleId == null) {
            return "错误：缺少roleId参数";
        }
        
        try {
            Map<String, Object> result = roleServiceClient.getRoleById(roleId);
            return gson.toJson(result);
        } catch (Exception e) {
            return "查询角色详情失败: " + e.getMessage();
        }
    }
    
    private String getUserRoles(JsonObject json) {
        Long userId = json.has("userId") ? json.get("userId").getAsLong() : null;
        if (userId == null) {
            return "错误：缺少userId参数";
        }
        
        try {
            Map<String, Object> result = roleServiceClient.getUserRoles(userId);
            return gson.toJson(result);
        } catch (Exception e) {
            return "查询用户角色失败: " + e.getMessage();
        }
    }
    
    private String assignRole(JsonObject json) {
        // 分配角色需要调用UserRoleService
        return """
            {"code": 200, "message": "角色分配需要通过管理界面执行，或提供userId和roleIds参数"}
            """;
    }
}