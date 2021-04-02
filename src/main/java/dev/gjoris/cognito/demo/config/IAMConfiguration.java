package dev.gjoris.cognito.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iam")
@Data
public class IAMConfiguration {

    private String roleToAssume;

}
