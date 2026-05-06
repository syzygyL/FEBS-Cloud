package cc.mrbird.febs.agent.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

/**
 * Swagger配置
 * 
 * @author mrbird
 */
@Configuration
@EnableSwagger2WebFlux
public class SwaggerConfigure {
    
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cc.mrbird.febs.agent.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("FEBS Cloud 智能客服Agent API")
                .description("FEBS Cloud 微服务权限系统 - 智能客服Agent接口文档")
                .contact(new Contact("mrbird", "", ""))
                .version("1.0.0")
                .build();
    }
}