package dev.gjoris.cognito.demo.controller;

import dev.gjoris.cognito.demo.config.AthenaConfiguration;
import dev.gjoris.cognito.demo.config.IAMConfiguration;
import dev.gjoris.cognito.demo.factory.athena.AthenaClientFactory;
import dev.gjoris.cognito.demo.factory.s3.S3ClientFactory;
import dev.gjoris.cognito.demo.model.cognito.TemporaryToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    private final StsClient stsClient;

    private final AthenaConfiguration athenaConfiguration;

    private final IAMConfiguration iamConfiguration;

    @GetMapping
    public String getIndexPage(Model model, OAuth2AuthenticationToken authentication) throws InterruptedException {
        if (authentication != null && authentication.isAuthenticated()) {
//            This is the way to retrieve the access token:
//
//            OAuth2AccessToken accessToken = oAuth2AuthorizedClientService.loadAuthorizedClient(
//                    authentication.getAuthorizedClientRegistrationId(),
//                    authentication.getName()
//            ).getAccessToken();

            DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();
            String tokenValue = principal.getIdToken().getTokenValue();
            model.addAttribute("name", principal.getUserInfo().getGivenName());

//            AssumeRoleWithWebIdentity

            AssumeRoleWithWebIdentityResponse assumeRoleWithWebIdentityResponse = stsClient.assumeRoleWithWebIdentity(AssumeRoleWithWebIdentityRequest.builder()
                    .roleArn(iamConfiguration.getRoleToAssume())
                    .roleSessionName("AssumedRoleFromAzure")
                    .webIdentityToken(tokenValue)
                    .build());

            Credentials credentials = assumeRoleWithWebIdentityResponse.credentials();
            TemporaryToken temporaryToken = new TemporaryToken(credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken());

            model.addAttribute("accessKey", temporaryToken.getAccessToken());
            model.addAttribute("secretKey", temporaryToken.getSecretKey());

//            S3

            S3Client s3Client = S3ClientFactory.createWithSessionToken(temporaryToken);

            addBucketsToModel(model, s3Client);

//            Athena

            AthenaClient athenaClient = AthenaClientFactory.createWithSessionToken(temporaryToken);
//
            String queryExecutionId = performQuery(athenaClient);
//
            waitForQueryToFinish(athenaClient, queryExecutionId);
//
            printResults(athenaClient, queryExecutionId, model);

        }

        return "home";
    }

    private void addBucketsToModel(Model model, S3Client s3Client) {
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();

        model.addAttribute("buckets", listBucketsResponse.buckets());
    }

    private String performQuery(AthenaClient athenaClient) {
        QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                .database(athenaConfiguration.getDatabase()).build();

        ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(athenaConfiguration.getOutputLocation())
                .build();

        //TODO Adapt query as necessary. The example was based on: https://docs.aws.amazon.com/athena/latest/ug/getting-started.html .

        StartQueryExecutionResponse response = athenaClient.startQueryExecution(StartQueryExecutionRequest.builder()
                .queryString("SELECT os, COUNT(*) count " +
                        "FROM cloudfront_logs " +
                        "WHERE date BETWEEN date '2014-07-05' AND date '2014-08-05' " +
                        "GROUP BY os;")
                .queryExecutionContext(queryExecutionContext)
                .resultConfiguration(resultConfiguration)
                .build()
        );

        return response.queryExecutionId();
    }

    private void waitForQueryToFinish(AthenaClient athenaClient, String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId).build();

        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("The Amazon Athena query failed to run with error message: " + getQueryExecutionResponse
                        .queryExecution().status().stateChangeReason());
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("The Amazon Athena query was cancelled.");
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                // Sleep an amount of time before retrying again
                Thread.sleep(1000);
            }
            LOGGER.info("The current status is: " + queryState);
        }
    }

    private void printResults(AthenaClient athenaClient, String queryExecutionId, Model model) {
        // Max Results can be set but if its not set,
        // it will choose the maximum page size
        GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();

        GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);

        List<Row> allResults = new ArrayList<>();

        for (GetQueryResultsResponse result : getQueryResultsResults) {
            List<Row> results = result.resultSet().rows();
            allResults.addAll(results);
            for (Row myRow : results) {
                List<Datum> allData = myRow.data();
                for (Datum data : allData) {
                    LOGGER.info("The value of the column is " + data.varCharValue());
                }
            }
        }

        model.addAttribute("athena", allResults);
    }

}
