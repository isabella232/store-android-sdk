package com.xsolla.android.store.callbacks

import com.xsolla.android.store.entity.response.order.OrderResponse

interface GetOrderCallback {
    fun onSuccess(response: OrderResponse)
    fun onError(throwable: Throwable?, errorMessage: String?)
}