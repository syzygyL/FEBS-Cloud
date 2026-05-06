package cc.mrbird.febs.agent.configure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gson配置
 * 
 * @author mrbird
 */
@Configuration
public class GsonConfigure {
    
    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }
}