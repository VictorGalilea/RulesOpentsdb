package restAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;

@Configuration
@EnableEurekaClient
@EnableDiscoveryClient
@EnableSwagger2
@ImportResource("classpath:Spring-Module.xml")
public class SwaggerSpringMvcConfig {
	
	// autowire the DataSource bean declared in datasource-config.xml
    @Autowired DataSource dataSource;
	
    //CORSConfig
    @Bean	
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
    
	/**
     * Publish a bean to generate swagger2 endpoints
     * @return a swagger configuration bean
     */
    @Bean
    public Docket postsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(userPaths())
                .build();
    }
    
    /**
     * Api info
     * @return ApiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Rules Microservice")
                .version("1.0")
                .license("Apache License Version 2.0")
                .description("This is the API and the model definition for the "
                		+ "FIWOO'S microservice of Rules.")
                .build();
    }


    /**
     * Config paths.
     *
     * @return the predicate
     */
    private Predicate<String> userPaths() {
        //return regex("/.*");
    	return Predicates.not(PathSelectors.regex("/error"));
    }
}
