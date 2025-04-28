package br.edu.iff.ccc.bsi.gardensBoard.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garden's Board (Task Section)")
                        .version("1.0.0")
                        .description("Doc for Task Section of Garden's Board")
                        .contact(new Contact()
                                .name("Gabriel Silveira")
                                .email("gardengab@outlook.com")
                                .url("https://github.com/gabgarden"))
                );
    }
}
