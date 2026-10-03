package config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {

    private static final String BASE_URL = "https://dummyjson.com";

    private RequestSpecFactory() {
        // Prevent creating instances of this utility class
    }

    public static RequestSpecification baseSpec() {
        return createSpec(null);
    }

    public static RequestSpecification authenticatedSpec(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }

        return createSpec(token);
    }

    private static RequestSpecification createSpec(String token) {

        LogConfig logConfig = LogConfig.logConfig()
                .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                .blacklistHeader("Authorization");

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setConfig(RestAssuredConfig.config().logConfig(logConfig));

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        return builder.build();
    }
}
