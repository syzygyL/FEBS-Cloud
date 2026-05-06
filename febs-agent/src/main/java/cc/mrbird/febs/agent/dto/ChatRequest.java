package cc.mrbird.febs.agent.dto;

import lombok.Data;
import java.util.List;

/**
 * 聊天请求DTO
 * 
 * @author mrbird
 */
@Data
public class ChatRequest {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 消息内容
     */
    private String message;
    
    /**
     * 对话历史
     */
    private List<ChatMessage> history;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    @Data
    public static class ChatMessage {
        /**
         * 角色：user/assistant/system
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
    }
}