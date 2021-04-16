package com.xsolla.android.store.callbacks

import com.xsolla.android.store.entity.response.cart.CartResponse

interface GetCartByIdCallback {
    fun onSuccess(response: CartResponse)
    fun onError(throwable: Throwable?, errorMessage: String?)
}