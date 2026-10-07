package com.zhixue.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger 文档配置
 * <p>
 * 为 Swagger UI 配置全局 JWT 认证入口，所有接口默认需要 Authorization Bearer Token。
 * </p>
 */
@Configuration
public class SpringDocConfig {

    /** JWT 安全方案名称，与 SecurityRequirement 中引用的名称保持一致 */
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    /**
     * 配置 OpenAPI 文档信息及全局安全认证
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 接口文档基本信息
                .info(new Info()
                        .title("智学云 API 文档")
                        .version("v1.0.0")
                        .description("智学云后台管理系统接口文档"))
                // 注册 JWT Bearer Token 认证方式
                .schemaRequirement(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                // 所有接口默认需要携带该 Token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
