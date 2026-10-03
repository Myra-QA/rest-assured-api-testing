package helpers;

import config.RequestSpecFactory;
import java.util.Map;
import static io.restassured.RestAssured.given;

public final class AuthHelper {

    private AuthHelper() {
        // Prevent creating instances of this utility class
    }

    public static String getAuthToken(String username, String password) {
        Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
        );

        String token = given()
                .spec(RequestSpecFactory.baseSpec())
                .body(credentials)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authentication returned an empty token");
        }

        return token;
    }
}