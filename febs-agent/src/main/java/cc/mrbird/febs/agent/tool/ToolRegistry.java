package cc.mrbird.febs.agent.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工具注册器
 * 管理所有可用的Agent工具
 * 
 * @author mrbird
 */
@Component
public class ToolRegistry {
    
    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();
    
    @Autowired
    private List<AgentTool> toolList;
    
    @PostConstruct
    public void init() {
        for (AgentTool tool : toolList) {
            tools.put(tool.getName(), tool);
        }
    }
    
    /**
     * 获取所有工具
     * 
     * @return 工具列表
     */
    public List<AgentTool> getAllTools() {
        return List.copyOf(tools.values());
    }
    
    /**
     * 根据名称获取工具
     * 
     * @param name 工具名称
     * @return 工具实例
     */
    public AgentTool getTool(String name) {
        return tools.get(name);
    }
    
    /**
     * 获取工具的OpenAI Function Calling格式定义
     * 
     * @return 工具定义列表
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return toolList.stream()
                .map(tool -> {
                    Map<String, Object> definition = new java.util.HashMap<>();
                    definition.put("name", tool.getName());
                    definition.put("description", tool.getDescription());
                    definition.put("parameters", new com.google.gson.Gson().fromJson(tool.getParameters(), Map.class));
                    return definition;
                })
                .collect(Collectors.toList());
    }
}