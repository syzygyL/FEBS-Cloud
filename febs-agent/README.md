# FEBS Cloud 智能客服Agent服务

基于FEBS Cloud微服务架构的AI智能客服Agent服务，支持多种LLM后端，提供自然语言交互能力。

## 功能特性

- **多LLM支持**：OpenAI API / 本地Ollama / 模拟模式
- **智能对话**：自然语言理解 + Function Calling
- **工具调用**：用户管理、权限管理、任务调度、安全审计、代码生成、系统监控
- **会话管理**：基于Redis的对话历史维护
- **权限集成**：集成FEBS Cloud的OAuth2认证体系
- **容器化部署**：支持Docker和Docker Compose部署

## 系统架构

```
用户请求 → FEBS-Gateway → FEBS-Agent → LLM服务（OpenAI/Ollama）
                ↓               ↓
            OAuth2认证      工具调用 → FEBS-Server-System
                                    → FEBS-Server-Job
                                    → 其他微服务
```

## 快速开始

### 1. 本地开发

```bash
# 进入项目目录
cd febs-agent

# 打包
mvn clean package -DskipTests

# 启动服务（模拟LLM模式）
java -jar target/febs-agent.jar --spring.profiles.active=dev

# 或者使用启动脚本
chmod +x start.sh
./start.sh
```

### 2. 使用OpenAI API

```bash
# 设置环境变量
export OPENAI_API_KEY=your-api-key
export OPENAI_MODEL=gpt-3.5-turbo

# 启动服务
java -jar target/febs-agent.jar \
  --spring.profiles.active=dev \
  --llm.provider=openai
```

### 3. 使用本地Ollama

```bash
# 先启动Ollama服务
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest

# 下载模型
docker exec ollama ollama pull qwen2.5:7b

# 启动Agent服务
java -jar target/febs-agent.jar \
  --spring.profiles.active=dev \
  --llm.provider=local \
  --local.llm.url=http://localhost:11434
```

### 4. Docker Compose部署

```bash
# 一键启动所有服务
docker-compose up -d

# 启动带本地LLM的服务
docker-compose --profile local-llm up -d

# 查看日志
docker-compose logs -f febs-agent
```

## 配置说明

### LLM配置

```yaml
llm:
  provider: mock  # 可选：openai / local / mock

openai:
  api-key: ${OPENAI_API_KEY:your-api-key}
  model: ${OPENAI_MODEL:gpt-3.5-turbo}
  base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}

local:
  llm:
    url: ${LOCAL_LLM_URL:http://localhost:11434}
    model: ${LOCAL_LLM_MODEL:qwen2.5:7b}
```

### 网关路由配置

在Nacos的 `febs-gateway.yaml` 中添加：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: febs-agent
          uri: lb://FEBS-Agent
          predicates:
            - Path=/agent/**
```

## API接口

### 发送消息

```http
POST /agent/chat
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "userId": "mrbird",
    "message": "查询所有用户",
    "sessionId": "optional-session-id"
}
```

### 健康检查

```http
GET /agent/health
```

### 获取工具列表

```http
GET /agent/tools
```

## 支持的自然语言指令

| 场景 | 示例指令 |
|------|---------|
| 用户管理 | "查询所有用户"、"查看scott的详情" |
| 权限管理 | "查看角色列表"、"查看管理员的权限" |
| 定时任务 | "查看任务列表"、"暂停数据同步任务" |
| 安全审计 | "这周有哪些异常登录"、"生成安全报告" |
| 代码生成 | "列出数据库表"、"生成用户管理的CRUD代码" |
| 系统监控 | "查看系统健康状态"、"查看服务列表" |

## 目录结构

```
febs-agent/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── start.sh
├── README.md
└── src/main/
    ├── java/cc/mrbird/febs/agent/
    │   ├── FebsAgentApplication.java
    │   ├── configure/
    │   │   ├── GsonConfigure.java
    │   │   ├── LLMConfigure.java
    │   │   ├── RedisConfigure.java
    │   │   ├── ResourceServerConfigure.java
    │   │   └── SwaggerConfigure.java
    │   ├── controller/
    │   │   └── AgentController.java
    │   ├── dto/
    │   │   ├── ChatRequest.java
    │   │   └── ChatResponse.java
    │   ├── entity/
    │   │   └── Conversation.java
    │   ├── feign/
    │   │   ├── UserServiceClient.java
    │   │   └── RoleServiceClient.java
    │   ├── service/
    │   │   ├── AgentService.java
    │   │   ├── ConversationService.java
    │   │   ├── LLMService.java
    │   │   ├── MockLLMServiceImpl.java
    │   │   ├── OpenAILLMServiceImpl.java
    │   │   └── LocalLLMServiceImpl.java
    │   └── tool/
    │       ├── AgentTool.java
    │       ├── ToolRegistry.java
    │       ├── UserManagementTool.java
    │       ├── PermissionManagementTool.java
    │       ├── SecurityAuditTool.java
    │       ├── JobManagementTool.java
    │       ├── CodeGeneratorTool.java
    │       └── SystemMonitorTool.java
    └── resources/
        ├── bootstrap.yml
        ├── application.yml
        ├── application-dev.yml
        └── gateway-route-config.yml
```

## 后续规划

- [ ] 接入更多LLM：Claude、Gemini、文心一言
- [ ] 流式对话：SSE/WebSocket实时响应
- [ ] 多轮对话优化：上下文压缩、记忆管理
- [ ] 工具扩展：更多业务工具、自定义工具
- [ ] 前端界面：对话式管理后台
- [ ] 性能优化：缓存、批量处理、异步调用