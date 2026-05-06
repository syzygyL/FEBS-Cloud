package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 权限管理工具
 * 提供角色管理、菜单管理、权限分配等功能
 * 
 * @author mrbird
 */
@Component
public class PermissionManagementTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Override
    public String getName() {
        return "permission_management";
    }
    
    @Override
    public String getDescription() {
        return "权限管理工具，可以查询角色、菜单、权限分配等信息";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list_roles", "get_role", "list_menus", "assign_role", "get_user_roles"],
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
                    "menuIds": {
                        "type": "array",
                        "items": {"type": "integer"},
                        "description": "菜单ID列表"
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
                    return listRoles();
                case "get_role":
                    return getRole(json);
                case "list_menus":
                    return listMenus();
                case "assign_role":
                    return assignRole(json);
                case "get_user_roles":
                    return getUserRoles(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行权限管理操作失败: " + e.getMessage();
        }
    }
    
    private String listRoles() {
        return """
            {
                "code": 200,
                "message": "查询成功",
                "data": [
                    {"roleId": 1, "roleName": "管理员", "roleDesc": "系统管理员"},
                    {"roleId": 2, "roleName": "普通用户", "roleDesc": "普通用户角色"},
                    {"roleId": 3, "roleName": "访客", "roleDesc": "访客角色"}
                ]
            }
            """;
    }
    
    private String getRole(JsonObject json) {
        return """
            {
                "code": 200,
                "message": "查询成功",
                "data": {
                    "roleId": 1,
                    "roleName": "管理员",
                    "roleDesc": "系统管理员",
                    "menuIds": [1, 2, 3, 4, 5]
                }
            }
            """;
    }
    
    private String listMenus() {
        return """
            {
                "code": 200,
                "message": "查询成功",
                "data": [
                    {"menuId": 1, "menuName": "系统管理", "parentId": 0},
                    {"menuId": 2, "menuName": "用户管理", "parentId": 1},
                    {"menuId": 3, "menuName": "角色管理", "parentId": 1},
                    {"menuId": 4, "menuName": "菜单管理", "parentId": 1}
                ]
            }
            """;
    }
    
    private String assignRole(JsonObject json) {
        return """
            {"code": 200, "message": "角色分配成功"}
            """;
    }
    
    private String getUserRoles(JsonObject json) {
        return """
            {
                "code": 200,
                "message": "查询成功",
                "data": [
                    {"roleId": 1, "roleName": "管理员"}
                ]
            }
            """;
    }
}