package com.plain.backend.api.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 화면(prototype 폴더)과 서버를 이어주는 설정입니다.
 *
 * 1) prototype 폴더를 /prototype/** 주소로 그대로 서비스합니다.
 *    화면과 API가 같은 주소(localhost:8080)에서 뜨므로 브라우저 CORS 문제가 아예 생기지 않습니다.
 * 2) 다른 개발 서버(VS Code Live Server 등)로 화면을 여는 경우를 위해 CORS 허용 목록도 둡니다.
 *
 * 설정값은 application.yml을 고치지 않아도 되도록 모두 기본값을 갖고 있습니다.
 * 바꾸고 싶으면 실행할 때 -Dplain.ai.base-url=... 처럼 넘기거나 yml에 plain.* 을 추가하면 됩니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String prototypePath;
    private final String[] corsOrigins;

    public WebConfig(
            @Value("${plain.prototype-path:../prototype/}") String prototypePath,
            @Value("${plain.cors-origins:http://localhost:5500,http://127.0.0.1:5500,http://localhost:4173,http://127.0.0.1:4173}")
            String[] corsOrigins) {
        this.prototypePath = prototypePath;
        this.corsOrigins = corsOrigins;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/prototype/**")
                .addResourceLocations("file:" + prototypePath);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트로 들어오면 화면 목록으로 보냅니다.
        registry.addRedirectViewController("/", "/prototype/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsOrigins)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
