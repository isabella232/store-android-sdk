package com.xsolla.android.store.entity.request.cart

data class FillCartWithItemsRequestBody(val items: List<FillCartItem>)

data class FillCartItem(
    val sku: String,
    val quantity: Int
)