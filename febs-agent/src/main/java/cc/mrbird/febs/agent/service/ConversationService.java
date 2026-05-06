package cc.mrbird.febs.agent.service;

import cc.mrbird.febs.agent.dto.ChatRequest;
import cc.mrbird.febs.agent.entity.Conversation;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务
 * 
 * @author mrbird
 */
@Service
public class ConversationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private Gson gson;
    
    private static final String CONVERSATION_KEY_PREFIX = "febs:agent:conversation:";
    private static final long CONVERSATION_EXPIRE_MINUTES = 30;
    
    /**
     * 创建或获取会话
     */
    public Conversation getOrCreateConversation(String userId, String sessionId) {
        String key = CONVERSATION_KEY_PREFIX + sessionId;
        Object cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            Conversation conversation = gson.fromJson(cached.toString(), Conversation.class);
            conversation.setLastActiveTime(new Date());
            saveConversation(conversation);
            return conversation;
        }
        
        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setSessionId(sessionId);
        conversation.setUserId(userId);
        conversation.setMessages(new ArrayList<>());
        conversation.setCreateTime(new Date());
        conversation.setLastActiveTime(new Date());
        conversation.setStatus("ACTIVE");
        
        saveConversation(conversation);
        return conversation;
    }
    
    /**
     * 保存会话
     */
    public void saveConversation(Conversation conversation) {
        String key = CONVERSATION_KEY_PREFIX + conversation.getSessionId();
        redisTemplate.opsForValue().set(key, gson.toJson(conversation), CONVERSATION_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 添加消息到会话
     */
    public void addMessage(String sessionId, ChatRequest.ChatMessage message) {
        Conversation conversation = getConversation(sessionId);
        if (conversation != null) {
            conversation.getMessages().add(message);
            // 保持最近50条消息
            if (conversation.getMessages().size() > 50) {
                conversation.setMessages(new ArrayList<>(conversation.getMessages().subList(conversation.getMessages().size() - 50, conversation.getMessages().size())));
            }
            conversation.setLastActiveTime(new Date());
            saveConversation(conversation);
        }
    }
    
    /**
     * 获取会话
     */
    public Conversation getConversation(String sessionId) {
        String key = CONVERSATION_KEY_PREFIX + sessionId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return gson.fromJson(cached.toString(), Conversation.class);
        }
        return null;
    }
    
    /**
     * 清理会话
     */
    public void clearConversation(String sessionId) {
        String key = CONVERSATION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}