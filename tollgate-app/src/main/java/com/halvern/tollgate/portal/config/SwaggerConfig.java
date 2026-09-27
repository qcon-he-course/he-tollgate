// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger UI for the partner API at /swagger-ui/ (PI-21).
 *
 * @author Aisha Rahman
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket partnerApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(new ApiInfoBuilder()
                        .title("Tollgate partner API")
                        .description("Movement permit applications for Halvern Crossings. Authenticate with the X-Api-Key header.")
                        .version("2021")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.halvern.tollgate.portal"))
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }
}
