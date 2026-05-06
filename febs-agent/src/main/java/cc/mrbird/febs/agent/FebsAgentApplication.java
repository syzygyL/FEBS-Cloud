package cc.mrbird.febs.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * FEBS Cloud 智能客服Agent服务
 * 
 * @author mrbird
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FebsAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FebsAgentApplication.class, args);
    }
}