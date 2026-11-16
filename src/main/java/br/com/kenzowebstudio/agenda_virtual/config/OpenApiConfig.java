package br.com.kenzowebstudio.agenda_virtual.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        String securitySchemeName = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Agenda Virtual Escolar API")
                        .version("1.0")
                        .description(
                                "API REST para gerenciamento da Agenda Virtual Escolar. "
                                + "Autenticação via cookie HttpOnly e proteção CSRF."))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName))
                .components(
                        new Components().addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access_token")));
    }
}
