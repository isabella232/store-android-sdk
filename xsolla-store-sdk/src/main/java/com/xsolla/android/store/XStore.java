package com.xsolla.android.store;

import com.xsolla.android.store.api.StoreApi;
import com.xsolla.android.store.api.XStoreCallback;
import com.xsolla.android.store.entity.request.cart.CartRequestOptions;
import com.xsolla.android.store.entity.request.items.ItemsRequestOptions;
import com.xsolla.android.store.entity.response.cart.CartResponse;
import com.xsolla.android.store.entity.response.items.PhysicalItemsResponse;
import com.xsolla.android.store.entity.response.items.VirtualCurrencyPackageResponse;
import com.xsolla.android.store.entity.response.items.VirtualCurrencyResponse;
import com.xsolla.android.store.entity.response.items.VirtualItemsResponse;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class XStore {

    private RequestExecutor requestExecutor;

    private static XStore instance;

    private XStore(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    private static XStore getInstance() {
        if (instance == null) {
            throw new IllegalStateException("XLogin SDK not initialized. You should call \"XStore.init(project-id)\" first.");
        }
        return instance;
    }

    private static RequestExecutor getRequestExecutor() {
        return getInstance().requestExecutor;
    }

    public static void init(int projectId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://store.xsolla.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StoreApi storeApi = retrofit.create(StoreApi.class);

        RequestExecutor requestExecutor = new RequestExecutor(projectId, storeApi);
        instance = new XStore(requestExecutor);
    }

    // Virtual items
    public static void getVirtualItems(XStoreCallback<VirtualItemsResponse> callback) {
        getRequestExecutor().getVirtualItems(null, callback);
    }

    public static void getVirtualItems(ItemsRequestOptions options, XStoreCallback<VirtualItemsResponse> callback) {
        getRequestExecutor().getVirtualItems(options, callback);
    }

    // Virtual currency
    public static void getVirtualCurrency(XStoreCallback<VirtualCurrencyResponse> callback) {
        getRequestExecutor().getVirtualCurrency(null, callback);
    }

    public static void getVirtualCurrency(ItemsRequestOptions options, XStoreCallback<VirtualCurrencyResponse> callback) {
        getRequestExecutor().getVirtualCurrency(options, callback);
    }

    // Virtual currency package
    public static void getVirtualCurrencyPackage(XStoreCallback<VirtualCurrencyPackageResponse> callback) {
        getRequestExecutor().getVirtualCurrencyPackage(null, callback);
    }

    public static void getVirtualCurrencyPackage(ItemsRequestOptions options, XStoreCallback<VirtualCurrencyPackageResponse> callback) {
        getRequestExecutor().getVirtualCurrencyPackage(options, callback);
    }

    // Items by specified group
    public static void getItemsBySpecifiedGroup(XStoreCallback<VirtualItemsResponse> callback) {
        getRequestExecutor().getItemsBySpecifiedGroup(null, callback);
    }

    public static void getItemsBySpecifiedGroup(ItemsRequestOptions options, XStoreCallback<VirtualItemsResponse> callback) {
        getRequestExecutor().getItemsBySpecifiedGroup(options, callback);
    }

    // Physical items
    public static void getPhysicalItems(XStoreCallback<PhysicalItemsResponse> callback) {
        getRequestExecutor().getPhysicalItems(null, callback);
    }

    public static void getPhysicalItems(ItemsRequestOptions options, XStoreCallback<PhysicalItemsResponse> callback) {
        getRequestExecutor().getPhysicalItems(options, callback);
    }

    // Cart
    public static void getCartById(String cartId, XStoreCallback<CartResponse> callback) {
        getRequestExecutor().getCardById(cartId, null, callback);
    }

    public static void getCartById(String cartId, CartRequestOptions options, XStoreCallback<CartResponse> callback) {
        getRequestExecutor().getCardById(cartId, options, callback);
    }

    public static void getCurrentCart(XStoreCallback<CartResponse> callback) {
        getRequestExecutor().getCurrentCart(null, callback);
    }

    public static void getCurrentCart(CartRequestOptions options, XStoreCallback<CartResponse> callback) {
        getRequestExecutor().getCurrentCart(options, callback);
    }

    public static void clearCartById(String cartId, XStoreCallback<Void> callback) {
        getRequestExecutor().clearCartById(cartId, callback);
    }

    public static void clearCurrentCart(XStoreCallback<Void> callback) {
        getRequestExecutor().clearCurrentCart(callback);
    }

    public static void updateItemFromCartByCartId(String cartId, String itemSku, int quantity, XStoreCallback<Void> callback) {
        getRequestExecutor().updateItemFromCartByCartId(cartId, itemSku, quantity, callback);
    }

    public static void updateItemFromCurrentCart(String itemSku, int quantity, XStoreCallback<Void> callback) {
        getRequestExecutor().updateItemFromCurrentCart(itemSku, quantity, callback);
    }

}
