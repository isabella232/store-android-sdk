package com.xsolla.android.storesdkexample.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xsolla.android.store.XStore
import com.xsolla.android.store.api.XStoreCallback
import com.xsolla.android.store.entity.response.inventory.InventoryResponse
import com.xsolla.android.store.entity.response.inventory.SubscriptionsResponse
import com.xsolla.android.storesdkexample.R
import com.xsolla.android.storesdkexample.adapter.InventoryAdapter
import com.xsolla.android.storesdkexample.listener.ConsumeListener
import kotlinx.android.synthetic.main.fragment_inventory.*

class InventoryFragment : Fragment(), ConsumeListener {

    private lateinit var inventoryAdapter: InventoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recycler) {
            val linearLayoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation).apply {
                ContextCompat.getDrawable(context, R.drawable.item_divider)?.let { setDrawable(it) }
            })
            layoutManager = linearLayoutManager
            goToStoreButton.setOnClickListener { findNavController().navigate(R.id.nav_vi) }
        }
        getItems()
    }

    private fun getItems() {
        XStore.getInventory(object : XStoreCallback<InventoryResponse>() {
            override fun onSuccess(response: InventoryResponse) {
                val virtualItems = response.items.filter { item -> item.type == InventoryResponse.Item.Type.VIRTUAL_GOOD }
                inventoryAdapter = InventoryAdapter(virtualItems, this@InventoryFragment)
                recycler.adapter = inventoryAdapter
                getSubscriptions()
            }

            override fun onFailure(errorMessage: String) {
                showSnack(errorMessage)
            }
        })
    }

    private fun getSubscriptions() {
        XStore.getSubscriptions(object : XStoreCallback<SubscriptionsResponse>() {
            override fun onSuccess(response: SubscriptionsResponse) {
                inventoryAdapter.setSubscriptions(response.items)
            }

            override fun onFailure(errorMessage: String) {
                showSnack(errorMessage)
            }
        })
    }

    override fun onConsume(item: InventoryResponse.Item) {
        val bundle = bundleOf(ConsumeFragment.ITEM_ARG to item)
//        findNavController().navigate(R.id.fragment_consume, bundle)
        consume(item)
    }

    override fun onSuccess() {
        showSnack("Item Consumed")
    }

    override fun onFailure(errorMessage: String) {
        showSnack(errorMessage)
    }

    private fun showSnack(message: String) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    private fun consume(item: InventoryResponse.Item) {
        XStore.consumeItem(item.sku, 1, null, object : XStoreCallback<Void>() {
            override fun onSuccess(response: Void?) {
                XStore.getInventory(object : XStoreCallback<InventoryResponse>() {
                    override fun onSuccess(response: InventoryResponse) {
                        inventoryAdapter.items = response.items.filter { item -> item.type == InventoryResponse.Item.Type.VIRTUAL_GOOD }
                        inventoryAdapter.notifyDataSetChanged()
                        showSnack("Item consumed")
                    }

                    override fun onFailure(errorMessage: String) {
                        showSnack(errorMessage)
                    }

                })
            }

            override fun onFailure(errorMessage: String) {
                showSnack(errorMessage)
            }
        })
    }

}