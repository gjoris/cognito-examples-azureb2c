package dev.gjoris.cognito.demo.factory.athena;

import dev.gjoris.cognito.demo.model.cognito.TemporaryToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.athena.AthenaClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AthenaClientFactory {


    public static AthenaClient createWithSessionToken(TemporaryToken temporaryToken) {
        return AthenaClient.builder().credentialsProvider(
                StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(
                                temporaryToken.getAccessToken(),
                                temporaryToken.getSecretKey(),
                                temporaryToken.getSessionToken()
                        ))
        ).build();
    }
}
