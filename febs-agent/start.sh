#!/bin/bash

# FEBS Agent 启动脚本

echo "=========================================="
echo "  FEBS Cloud 智能客服Agent服务启动脚本"
echo "=========================================="

# 设置默认环境变量
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
export JAVA_MEM_OPTS=${JAVA_MEM_OPTS:-"-Xms256m -Xmx512m"}

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java环境，请先安装JDK 8+"
    exit 1
fi

# 检查jar包是否存在
JAR_FILE="target/febs-agent.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误：未找到 $JAR_FILE，请先执行 mvn package"
    exit 1
fi

echo "启动环境：$SPRING_PROFILES_ACTIVE"
echo "JVM参数：$JAVA_MEM_OPTS"
echo ""

# 启动服务
java $JAVA_MEM_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -jar $JAR_FILE \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE

echo "FEBS Agent 服务已停止"