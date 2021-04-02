package dev.gjoris.cognito.demo.util;

import dev.gjoris.cognito.demo.config.CognitoConfiguration;
import dev.gjoris.cognito.demo.model.cognito.TemporaryToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CognitoExchanger {

    private final CognitoConfiguration cognitoConfiguration;

    private final CognitoIdentityClient cognitoIdentityClient;

    public TemporaryToken collectTokensForAuthentication(OAuth2AuthenticationToken authentication) {
        Map<String, String> logins = new HashMap<>();

        logins.put(
                cognitoConfiguration.getCognitoUri(),
                ((DefaultOidcUser) authentication.getPrincipal()).getIdToken().getTokenValue()
        );

        String id = cognitoIdentityClient.getId(
                GetIdRequest.builder()
                        .identityPoolId(cognitoConfiguration.getIdentityPoolId())
                        .logins(logins)
                        .build()
        ).identityId();

        GetCredentialsForIdentityRequest request = GetCredentialsForIdentityRequest.builder()
                .identityId(id)
                .logins(logins)
                .build();
        GetCredentialsForIdentityResponse credentialsForIdentity = cognitoIdentityClient.getCredentialsForIdentity(
                request
        );

        return new TemporaryToken(
                credentialsForIdentity.credentials().accessKeyId(),
                credentialsForIdentity.credentials().secretKey(),
                credentialsForIdentity.credentials().sessionToken()
        );
    }

}
