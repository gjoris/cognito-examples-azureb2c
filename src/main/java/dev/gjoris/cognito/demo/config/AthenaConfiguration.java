package dev.gjoris.cognito.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "athena")
@Data
public class AthenaConfiguration {

    private String database;
    private String outputLocation;

}
