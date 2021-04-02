package dev.gjoris.cognito.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cognito")
@Data
public class CognitoConfiguration {

    private String logoutUrl;
    private String rootUrl;
    private String cognitoUri;
    private String identityPoolId;

}
