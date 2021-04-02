package dev.gjoris.cognito.demo.factory.s3;

import dev.gjoris.cognito.demo.model.cognito.TemporaryToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3ClientFactory {

    public static S3Client createWithSessionToken(TemporaryToken temporaryToken) {
        return S3Client.builder().credentialsProvider(
                StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(
                                temporaryToken.getAccessToken(),
                                temporaryToken.getSecretKey(),
                                temporaryToken.getSessionToken()
                        )
                )
        ).build();
    }
}
