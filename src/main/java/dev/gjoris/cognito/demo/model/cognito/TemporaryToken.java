package dev.gjoris.cognito.demo.model.cognito;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TemporaryToken {

    private final String accessToken;
    private final String secretKey;
    private final String sessionToken;

}
