package cc.mrbird.febs.agent.tool;

/**
 * Agent工具接口
 * 所有工具必须实现此接口，以便Agent可以调用
 * 
 * @author mrbird
 */
public interface AgentTool {
    
    /**
     * 获取工具名称
     * 
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * 
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具参数描述（JSON Schema格式）
     * 
     * @return 参数描述
     */
    String getParameters();
    
    /**
     * 执行工具
     * 
     * @param arguments 工具参数
     * @return 执行结果
     */
    String execute(String arguments);
}