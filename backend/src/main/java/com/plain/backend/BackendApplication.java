package com.plain.backend;

import org.springframework.boot.SpringApplication; 
import org.springframework.boot.autoconfigure.SpringBootApplication; 

// =========================================================================================
// [Backend Application]
// Spring Boot 애플리케이션의 구동을 담당하는 최상위(Main) 진입점 클래스입니다.
// =========================================================================================

// @SpringBootApplication
// 내부적으로 @Configuration, @EnableAutoConfiguration, @ComponentScan 어노테이션들을 포함하는 메타 어노테이션입니다.
// - @ComponentScan: 지정된 패키지 이하의 @Component, @Service, @Controller 빈들을 검색하여 IoC 컨테이너에 등록합니다.
// - @EnableAutoConfiguration: 사전에 정의된 조건(Condition)에 따라 Spring Boot의 내장 웹 서버(Tomcat 등) 및 
//   다양한 자동 설정 Bean들을 애플리케이션 컨텍스트에 로드합니다.
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // 애플리케이션 컨텍스트(ApplicationContext)를 생성 및 초기화하고, 내장 WAS를 기동합니다.
        SpringApplication.run(BackendApplication.class, args);
    }
}
