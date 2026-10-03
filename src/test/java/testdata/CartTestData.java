package testdata;

import java.util.List;
import java.util.Map;

public final class CartTestData {

    private CartTestData(){}

    public static Map<String, Object> validCartPayload(){
        return Map.of(
                "userId", 1,
                "products", List.of(
                        Map.of("id", 144, "quantity", 3),
                        Map.of("id", 98, "quantity", 1)
                )

        );
    }

    public static Map<String, Object> minimumQuantityCartPayload() {
        return Map.of(
                "userId", 1,
                "products", List.of(
                        Map.of("id", 1, "quantity", 1)
                )
        );
    }

    public static Map<String, Object> invalidProductCartPayload() {
        return Map.of(
                "userId", 1,
                "products", List.of(
                        Map.of("id", 999999, "quantity", 1)
                )
        );
    }

    public static Map<String, Object> updateCartPayload() {
        return Map.of(
                "merge", false,
                "products", List.of(
                        Map.of("id", 1, "quantity", 5)
                )
        );
    }

}
