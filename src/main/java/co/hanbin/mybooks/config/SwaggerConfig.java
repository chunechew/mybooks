package co.hanbin.mybooks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
// import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
// @EnableWebMvc
public class SwaggerConfig {
    @Bean
    public Docket restAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(true) //기본 응답 메시지 표시 여부
                .select()
                .apis(RequestHandlerSelectors.basePackage("co.hanbin.mybooks")) //swagger탐색 대상 패키지
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MyBooks Spring Boot REST API")
                .version("1.0.0")
                .description("MyBooks의 API 명세서입니다.")
                .build();
    }
}
