package cc.mrbird.febs.agent.entity;

import cc.mrbird.febs.agent.dto.ChatRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 会话实体
 * 
 * @author mrbird
 */
@Data
public class Conversation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 对话历史
     */
    private List<ChatRequest.ChatMessage> messages;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 最后活跃时间
     */
    private Date lastActiveTime;
    
    /**
     * 会话状态
     */
    private String status;
}