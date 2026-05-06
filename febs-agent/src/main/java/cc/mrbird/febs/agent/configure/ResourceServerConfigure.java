package cc.mrbird.febs.agent.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 资源服务器配置
 * 
 * @author mrbird
 */
@Configuration
public class ResourceServerConfigure extends ResourceServerConfigurerAdapter {
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers(
                "/agent/health",
                "/agent/doc.html",
                "/agent/webjars/**",
                "/agent/swagger-resources",
                "/agent/v2/api-docs"
            ).permitAll()
            .anyRequest().authenticated()
            .and()
            .csrf().disable();
    }
}