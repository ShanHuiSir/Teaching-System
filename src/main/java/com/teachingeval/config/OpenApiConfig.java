package com.teachingeval.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class OpenApiConfig {

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public OpenAPI teachingEvaluationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("教学评价系统 API")
                        .description("教师端作品评价系统接口文档")
                        .version("1.0.0"))
                .addServersItem(new Server().url("/").description("Default Server URL"));
    }
}
