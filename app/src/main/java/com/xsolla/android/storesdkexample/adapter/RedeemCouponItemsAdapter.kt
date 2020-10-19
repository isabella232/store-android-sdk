package com.xsolla.android.storesdkexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xsolla.android.store.entity.response.items.RedeemCouponResponse
import com.xsolla.android.storesdkexample.R
import kotlinx.android.synthetic.main.item_inventory.view.consumeButton
import kotlinx.android.synthetic.main.item_inventory.view.itemExpiration
import kotlinx.android.synthetic.main.item_inventory.view.itemIcon
import kotlinx.android.synthetic.main.item_inventory.view.itemName
import kotlinx.android.synthetic.main.item_inventory.view.itemQuantity

class RedeemCouponItemsAdapter(
    private val items: List<RedeemCouponResponse.Item>
) : RecyclerView.Adapter<RedeemCouponItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: RedeemCouponResponse.Item) {
            Glide.with(itemView.context).load(item.imageUrl).into(itemView.itemIcon)
            itemView.itemName.text = item.name
            itemView.itemQuantity.text = item.quantity.toString()
            item.inventoryOption?.expirationPeriod?.value?.let { itemView.itemExpiration.text = it.toString() } ?: run {
                itemView.itemExpiration.isGone
            }

            itemView.consumeButton.isGone = true
        }
    }
}