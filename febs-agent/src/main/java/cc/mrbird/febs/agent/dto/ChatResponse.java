package cc.mrbird.febs.agent.dto;

import lombok.Data;
import java.util.List;

/**
 * 聊天响应DTO
 * 
 * @author mrbird
 */
@Data
public class ChatResponse {
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 工具调用信息
     */
    private ToolCall toolCall;
    
    /**
     * 是否有工具调用
     */
    private boolean hasToolCall;
    
    @Data
    public static class ToolCall {
        /**
         * 工具名称
         */
        private String name;
        
        /**
         * 工具参数
         */
        private String arguments;
    }
}