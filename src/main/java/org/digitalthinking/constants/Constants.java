package org.digitalthinking.constants;

public final class Constants {

    public static final String PRODUCT_FIELDS = "id code name description";

    public static final String ALL_PRODUCTS = "{ allProducts { " + PRODUCT_FIELDS + " } }";

    public static final String PRODUCT_BY_ID =
            "query($productId: BigInteger) { productById(productId: $productId) { " + PRODUCT_FIELDS + " } }";

    public static final String CREATE_PRODUCT =
            "mutation($product: ProductInput) { createProduct(product: $product) { " + PRODUCT_FIELDS + " } }";

    public static final String UPDATE_PRODUCT =
            "mutation($product: ProductInput) { updateProduct(product: $product) { " + PRODUCT_FIELDS + " } }";

    public static final String DELETE_PRODUCT =
            "mutation($productId: BigInteger) { deletProduct(productId: $productId) { " + PRODUCT_FIELDS + " } }";
}
