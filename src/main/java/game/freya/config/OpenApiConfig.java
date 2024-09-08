package game.freya.config;

import game.freya.api.GMOnly;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Value("${spring.application.version}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Freya game API")
                        .description("Freya the game")
                        .version(appVersion)
                        .summary("Multiverse-39")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                        .contact(new Contact()
                                .name("Freya`s developer")
                                .email("angelicalis39@mail.ru")
                                .url("https://vk.com/neverendingsky")))
                .externalDocs(new ExternalDocumentation()
                        .description("Freya Wiki")
                        .url("https://github.com/KiraLis39/theFreya/blob/main/WIKI.md"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**")
                .displayName("Freya game API")
                .addOpenApiMethodFilter(method -> !method.isAnnotationPresent(GMOnly.class))
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("game-master")
                .pathsToMatch("/**")
                .displayName("Freya GM API")
//                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(GMOnly.class))
                .build();
    }
}
