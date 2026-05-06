FROM openjdk:8-jre-alpine

MAINTAINER mrbird

LABEL description="FEBS Cloud - 智能客服Agent服务"

ENV TZ=Asia/Shanghai \
    JAVA_MEM_OPTS=" -Xms256m -Xmx512m " \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR /febs

COPY target/febs-agent.jar app.jar

EXPOSE 8601

ENTRYPOINT ["sh", "-c", "java ${JAVA_MEM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]