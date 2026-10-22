package com.preyansh.ridesharing.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideShare Backend API",
                version = "1.0",
                description = "REST API for users, drivers, vehicles, and ride lifecycle management."
        )
)
public class OpenApiConfig {
}
