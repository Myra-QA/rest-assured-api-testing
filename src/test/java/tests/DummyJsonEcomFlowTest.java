package tests;

import config.RequestSpecFactory;
import helpers.AuthHelper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import testdata.CartTestData;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class DummyJsonEcomFlowTest {
    private static final String ISO_TIMESTAMP_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
    private static RequestSpecification baseSpec;
    private static RequestSpecification authSpec;

    @BeforeAll
    public static void setupAndAuth(){
        baseSpec = RequestSpecFactory.baseSpec();

        String authToken = AuthHelper.getAuthToken(
                "emilys",
                "emilyspass"
        );
        authSpec = RequestSpecFactory.authenticatedSpec(authToken);
    }

    @Test
    @DisplayName("POST /carts/add - Create a cart with valid products")
    public void testCreateCart() {
        Map<String, Object> payload = CartTestData.validCartPayload();

    given()
                .spec(authSpec)
                .body(payload)
                .when()
                .post("/carts/add")
                .then()
                .statusCode(201)
                .body("userId", equalTo(1))
                .body("products", hasSize(2))
                .body("totalProducts", equalTo(2))
                .body("totalQuantity", equalTo(4))
                .body("total", greaterThan(0.0f));

    }

    @Test
    @DisplayName("GET /carts/{id} - Get Cart by ID")
    public void testGetCartById() {
        // DummyJSON simulates adds in-memory per request, so querying standard persisted ID 1
        given()
                .spec(authSpec)
                .pathParam("cartId", 1)
                .when()
                .get("/carts/{cartId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("products", not(empty()))
                .body("products.id", everyItem(greaterThan(0)))
                .body("products.quantity", everyItem(greaterThan(0)));
    }

    @Test
    @DisplayName("PUT /carts/{id} - Update existing cart items")
    public void testUpdateCart() {
        Map<String, Object> updatePayload = CartTestData.updateCartPayload();

        given()
                .spec(authSpec)
                .pathParam("cartId", 1)
                .body(updatePayload)
                .when()
                .put("/carts/{cartId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("products", hasSize(1))
                .body("products[0].id", equalTo(1))
                .body("products[0].quantity", equalTo(5));
    }
    @Test
    @DisplayName("DELETE /carts/{id} - Delete cart and verify dynamic ISO timestamp")
    public void testDeleteCart() {
        given()
                .spec(authSpec)
                .pathParam("cartId", 1)
                .when()
                .delete("/carts/{cartId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", matchesPattern(ISO_TIMESTAMP_REGEX));
    }

    @Test
    @DisplayName("Negative: Login with invalid password returns 400 Bad Request")
    public void testLoginWithInvalidCredentials() {
        Map<String, Object> invalidCredentials = Map.of(
                "username", "emilys",
                "password", "wrong_password_123"
        );

        given()
                .spec(baseSpec)
                .body(invalidCredentials)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid credentials"));
    }

    @Test
    @DisplayName("Negative: Access auth-required user endpoint without token returns 401")
    public void testAccessProtectedResourceWithoutToken() {
        given()
                .spec(baseSpec) // baseSpec has NO Authorization header
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Negative: Fetch non-existent cart ID returns 404 Not Found")
    public void testGetNonExistentCart() {
        given()
                .spec(authSpec)
                .pathParam("cartId", 999999)
                .when()
                .get("/carts/{cartId}")
                .then()
                .statusCode(404)
                .body("message", containsStringIgnoringCase("not found"));
    }

    @Test
    @DisplayName("Edge Case: Add cart with non-existent product creates an empty card")
    public void testAddCartWithNonExistentProduct() {
        Map<String, Object> invalidPayload = CartTestData.invalidProductCartPayload();

        given()
                .spec(authSpec)
                .body(invalidPayload)
                .when()
                .post("/carts/add")
                .then()
                .statusCode(201)
                .body("products", empty())
                .body("totalProducts", equalTo(0))
                .body("totalQuantity", equalTo(0));
    }


    @Test
    @DisplayName("Boundary: Query carts with limit=1 returns exactly one cart")
    public void testCartsBoundaryLimitOne() {
        given()
                .spec(authSpec)
                .queryParam("limit", 1)
                .when()
                .get("/carts")
                .then()
                .statusCode(200)
                .body("limit", equalTo(1))
                .body("carts", hasSize(1));
    }

    @Test
    @DisplayName("Boundary: End of dataset pagination (skip near total)")
    public void testCartsBoundarySkipToEnd() {
        Response initialResponse = given()
                .spec(authSpec)
                .queryParam("limit", 1)
                .when()
                .get("/carts")
                .then()
                .statusCode(200)
                .extract().response();

        int total = initialResponse.path("total");

        // Skip to the exact last record
        given()
                .spec(authSpec)
                .queryParam("limit", 1)
                .queryParam("skip", total - 1) // Always points to the exact last item in whatever dataset
                .when()
                .get("/carts")
                .then()
                .statusCode(200)
                .body("skip", equalTo(total - 1))
                .body("carts", hasSize(1));
        Assertions.assertTrue(total > 0, "Cart dataset should not be empty");
    }

    @Test
    @DisplayName("Boundary: Quantity minimum threshold (quantity = 1)")
    public void testAddCartMinimumQuantity() {
        Map<String, Object> payload = CartTestData.minimumQuantityCartPayload();

        given()
                .spec(authSpec)
                .body(payload)
                .when()
                .post("/carts/add")
                .then()
                .statusCode(201)
                .body("products[0].quantity", equalTo(1));
    }

    @Test
    @DisplayName("POST /carts/add - Validate JSON Schema on Creation")
    public void testCreateCartSchema() {
        Map<String, Object> payload = CartTestData.validCartPayload();

        given()
                .spec(authSpec)
                .body(payload)
                .when()
                .post("/carts/add")
                .then()
                .statusCode(201)
                // Asserts that the response structure conforms to cart-schema.json
                .body(matchesJsonSchemaInClasspath("schemas/cart-schema.json"))
                // Combine with functional assertions
                .body("userId", equalTo(1))
                .body("totalProducts", equalTo(2));
    }

    @Test
    @DisplayName("GET /carts/{id} - Validate JSON Schema on Retrieval")
    public void testGetCartByIdSchema() {
        given()
                .spec(authSpec)
                .pathParam("cartId", 1)
                .when()

                .get("/carts/{cartId}")
                .then()
                .statusCode(200)
                // Reuses the same cart schema for the individual cart lookup
                .body(matchesJsonSchemaInClasspath("schemas/cart-schema.json"))
                .body("id", equalTo(1));
    }
}

