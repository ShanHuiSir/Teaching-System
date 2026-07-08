package com.teachingeval.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private final SwaggerUiConfigProperties swaggerUiProps;

    public OpenApiConfig(SwaggerUiConfigProperties swaggerUiProps) {
        this.swaggerUiProps = swaggerUiProps;
    }

    @PostConstruct
    void configureSwaggerUiGroups() {
        Set<SwaggerUrl> urls = new LinkedHashSet<>();
        SwaggerUrl javaUrl = new SwaggerUrl();
        javaUrl.setName("Java 教学系统");
        javaUrl.setUrl("/v3/api-docs");
        urls.add(javaUrl);

        SwaggerUrl aiUrl = new SwaggerUrl();
        aiUrl.setName("AI 服务");
        aiUrl.setUrl("/v3/ai-docs");
        urls.add(aiUrl);

        swaggerUiProps.setUrls(urls);
        swaggerUiProps.setUrlsPrimaryName("Java 教学系统");
    }

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
