# FEBS Cloud 智能客服Agent服务

基于FEBS Cloud微服务架构的AI智能客服Agent服务，提供自然语言交互能力，用户可以通过对话方式管理系统。

## 功能特性

- **智能对话**：基于大语言模型的自然语言理解
- **工具调用**：支持用户管理、权限管理、系统监控等工具调用
- **会话管理**：基于Redis的对话历史维护
- **权限集成**：集成FEBS Cloud的OAuth2认证体系
- **API文档**：集成Swagger/Knife4j自动生成API文档

## 系统架构

```
用户请求 → FEBS-Gateway → FEBS-Agent → LLM服务
                ↓               ↓
            OAuth2认证      工具调用 → FEBS-Server-System
                                    → 其他微服务
```

## 模块说明

### 1. 工具系统（Tool System）
- `AgentTool`：工具接口，所有工具必须实现此接口
- `UserManagementTool`：用户管理工具
- `PermissionManagementTool`：权限管理工具  
- `SystemMonitorTool`：系统监控工具
- `ToolRegistry`：工具注册器，管理所有可用工具

### 2. LLM服务（LLM Service）
- `LLMService`：大语言模型服务接口
- `LLMServiceImpl`：模拟实现，实际项目中需替换为真实LLM API

### 3. Agent服务（Agent Service）
- `AgentService`：核心服务，协调LLM和工具调用
- `ConversationService`：会话管理服务
- `AgentController`：REST API接口

## 部署配置

### 1. 添加依赖

在父模块pom.xml中添加febs-agent模块：

```xml
<modules>
    ...
    <module>../febs-agent</module>
</modules>
```

### 2. 配置网关路由

在Nacos配置中添加Agent服务路由：

```yaml
spring.cloud.gateway.routes:
  - id: febs-agent
    uri: lb://FEBS-Agent
    predicates:
      - Path=/agent/**
    filters:
      - StripPrefix=0
```

### 3. 配置认证

在认证服务器中添加Agent服务的客户端配置：

```yaml
security:
  oauth2:
    client:
      client-id: agent-client
      client-secret: agent-secret
      scope: agent
      authorized-grant-types: password,authorization_code,refresh_token
      auto-approve-scopes: agent
```

### 4. 环境变量

```bash
# OpenAI API配置
OPENAI_API_KEY=your-api-key-here
OPENAI_MODEL=gpt-3.5-turbo

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379

# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/febs_cloud
DB_USERNAME=root
DB_PASSWORD=123456
```

## API接口

### 1. 发送消息给Agent

```http
POST /agent/chat
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "userId": "mrbird",
    "message": "查询所有用户列表",
    "sessionId": "optional-session-id"
}
```

### 2. 健康检查

```http
GET /agent/health
```

### 3. 清空会话

```http
DELETE /agent/conversation/{sessionId}
Authorization: Bearer {access_token}
```

## 开发指南

### 1. 添加新工具

实现`AgentTool`接口：

```java
@Component
public class MyCustomTool implements AgentTool {
    
    @Override
    public String getName() {
        return "my_custom_tool";
    }
    
    @Override
    public String getDescription() {
        return "我的自定义工具描述";
    }
    
    @Override
    public String getParameters() {
        return "{\"type\": \"object\", \"properties\": {}}";
    }
    
    @Override
    public String execute(String arguments) {
        // 工具执行逻辑
        return "执行结果";
    }
}
```

### 2. 替换LLM服务

修改`LLMServiceImpl`，集成真实的LLM API：

```java
@Service
public class LLMServiceImpl implements LLMService {
    
    @Value("${openai.api-key}")
    private String apiKey;
    
    @Value("${openai.base-url}")
    private String baseUrl;
    
    @Override
    public ChatResponse chatWithTools(ChatRequest request, List<Map<String, Object>> toolDefinitions) {
        // 调用OpenAI API或其他LLM服务
        // 实现Function Calling逻辑
        return callOpenAIWithTools(request, toolDefinitions);
    }
    
    private ChatResponse callOpenAIWithTools(ChatRequest request, List<Map<String, Object>> toolDefinitions) {
        // 实现具体的API调用逻辑
        return new ChatResponse();
    }
}
```

### 3. 配置Feign客户端

配置Feign客户端调用其他微服务API：

```java
@FeignClient(name = "FEBS-Server-System", path = "/user")
public interface UserServiceClient {
    @GetMapping("/users")
    Map<String, Object> getUserList(@RequestParam Map<String, Object> params);
}
```

## 监控与运维

### 1. 服务监控

Agent服务集成了Spring Boot Actuator，可以通过以下端点查看服务状态：

- `/agent/actuator/health`：健康状态
- `/agent/actuator/metrics`：性能指标
- `/agent/actuator/info`：服务信息

### 2. 日志配置

日志文件位于`logs/febs-agent.log`，日志级别为DEBUG。

## 注意事项

1. **安全性**：确保Agent API接口受OAuth2保护
2. **性能**：LLM API调用可能有延迟，建议使用异步处理
3. **成本**：使用商业LLM API会产生费用，建议配置预算监控
4. **扩展性**：工具系统支持动态注册，可以根据需要添加新工具

## 后续规划

1. **多模型支持**：支持OpenAI、Anthropic、本地LLM等多种模型
2. **插件系统**：支持插件化工具扩展
3. **对话优化**：增加对话历史压缩和上下文管理
4. **安全增强**：添加敏感信息过滤和操作确认机制
5. **性能优化**：增加缓存、批量处理等优化