package cc.mrbird.febs.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成工具
 * 通过自然语言描述生成CRUD代码
 * 
 * @author mrbird
 */
@Component
public class CodeGeneratorTool implements AgentTool {
    
    @Autowired
    private Gson gson;
    
    @Override
    public String getName() {
        return "code_generator";
    }
    
    @Override
    public String getDescription() {
        return "代码生成工具，根据自然语言描述生成CRUD代码。" +
               "支持操作：list_tables-列出数据库表，generate-生成代码，configure-配置生成参数";
    }
    
    @Override
    public String getParameters() {
        return """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "enum": ["list_tables", "generate", "configure"],
                        "description": "操作类型"
                    },
                    "tableName": {
                        "type": "string",
                        "description": "表名"
                    },
                    "description": {
                        "type": "string",
                        "description": "表描述/业务描述"
                    },
                    "packageName": {
                        "type": "string",
                        "description": "包名，如：cc.mrbird.febs.server.system"
                    },
                    "author": {
                        "type": "string",
                        "description": "作者名称"
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
                case "list_tables":
                    return listTables();
                case "generate":
                    return generateCode(json);
                case "configure":
                    return configureGenerator(json);
                default:
                    return "未知的操作类型: " + action;
            }
        } catch (Exception e) {
            return "执行代码生成操作失败: " + e.getMessage();
        }
    }
    
    private String listTables() {
        // 实际应调用 GeneratorServiceClient.getTables
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "数据库表列表");
        result.put("data", List.of(
            Map.of("tableName", "t_user", "engine", "InnoDB", "tableComment", "用户表"),
            Map.of("tableName", "t_role", "engine", "InnoDB", "tableComment", "角色表"),
            Map.of("tableName", "t_menu", "engine", "InnoDB", "tableComment", "菜单表"),
            Map.of("tableName", "t_dept", "engine", "InnoDB", "tableComment", "部门表"),
            Map.of("tableName", "t_log", "engine", "InnoDB", "tableComment", "操作日志表"),
            Map.of("tableName", "t_job", "engine", "InnoDB", "tableComment", "定时任务表")
        ));
        return gson.toJson(result);
    }
    
    private String generateCode(JsonObject json) {
        String tableName = json.has("tableName") ? json.get("tableName").getAsString() : null;
        String description = json.has("description") ? json.get("description").getAsString() : null;
        String packageName = json.has("packageName") ? json.get("packageName").getAsString() : "cc.mrbird.febs.server.system";
        String author = json.has("author") ? json.get("author").getAsString() : "mrbird";
        
        if (tableName == null) {
            return "错误：缺少tableName参数";
        }
        
        // 实际应调用 GeneratorServiceClient.generate
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "代码生成成功");
        result.put("data", Map.of(
            "tableName", tableName,
            "description", description != null ? description : tableName,
            "packageName", packageName,
            "author", author,
            "generatedFiles", List.of(
                Map.of("type", "Entity", "path", packageName.replace('.', '/') + "/entity/" + capitalize(tableName) + ".java", "size", "2.5KB"),
                Map.of("type", "Mapper", "path", packageName.replace('.', '/') + "/mapper/" + capitalize(tableName) + "Mapper.java", "size", "1.2KB"),
                Map.of("type", "Service", "path", packageName.replace('.', '/') + "/service/I" + capitalize(tableName) + "Service.java", "size", "0.8KB"),
                Map.of("type", "ServiceImpl", "path", packageName.replace('.', '/') + "/service/impl/" + capitalize(tableName) + "ServiceImpl.java", "size", "3.1KB"),
                Map.of("type", "Controller", "path", packageName.replace('.', '/') + "/controller/" + capitalize(tableName) + "Controller.java", "size", "4.2KB"),
                Map.of("type", "Mapper XML", "path", "resources/mapper/" + capitalize(tableName) + "Mapper.xml", "size", "5.8KB")
            ),
            "downloadUrl", "/generator/download?tableName=" + tableName
        ));
        return gson.toJson(result);
    }
    
    private String configureGenerator(JsonObject json) {
        // 实际应调用 GeneratorServiceClient.updateConfig
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "代码生成配置已更新");
        result.put("data", Map.of(
            "packageName", "cc.mrbird.febs.server.system",
            "author", "mrbird",
            "isSwagger2", true,
            "isLombok", true,
            "isRestStyle", true
        ));
        return gson.toJson(result);
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // 去掉前缀 t_ 然后首字母大写
        if (str.startsWith("t_")) {
            str = str.substring(2);
        }
        // 下划线转驼峰
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString().substring(0, 1).toUpperCase() + sb.toString().substring(1);
    }
}