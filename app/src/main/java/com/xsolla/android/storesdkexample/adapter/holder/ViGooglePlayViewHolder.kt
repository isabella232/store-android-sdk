package com.xsolla.android.storesdkexample.adapter.holder

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.xsolla.android.appcore.databinding.ItemViRealPriceBinding
import com.xsolla.android.appcore.utils.AmountUtils
import com.xsolla.android.store.entity.response.common.ExpirationPeriod
import com.xsolla.android.storesdkexample.R
import com.xsolla.android.storesdkexample.ui.fragments.store.ViFragmentDirections
import com.xsolla.android.storesdkexample.ui.fragments.store.VirtualItemUiEntity
import com.xsolla.android.storesdkexample.ui.vm.VmGooglePlay

class ViGooglePlayViewHolder(
    inflater: LayoutInflater,
    private val parent: ViewGroup,
    private val vmGooglePay: VmGooglePlay
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_vi_real_price, parent, false)) {
    private val binding = ItemViRealPriceBinding.bind(itemView)

    fun bind(item: VirtualItemUiEntity) {
        Glide.with(itemView).load(item.imageUrl).into(binding.itemIcon)
        binding.itemName.text = item.name
        bindPurchasedPlaceholderIfNeed(item)
        bindItemPrice(item)
        bindExpirationPeriod(item.inventoryOption?.expirationPeriod)
        bindBundlePlaceholder(item)
        (binding.addToCartButton as MaterialButton).icon = ContextCompat.getDrawable(parent.context, R.drawable.ic_buy_button_icon)
    }

    private fun bindBundlePlaceholder(item: VirtualItemUiEntity) {
        if (item.sku == "premium_pack" || item.sku == "starter_pack"||
            item.sku == "lootbox_pack_1" || item.sku == "lootbox_pack_2") {
            binding.preview.visibility = View.VISIBLE
            binding.preview.setOnClickListener {
                it.findNavController().navigate(ViFragmentDirections.actionNavViToBundleFragment(item))
            }
        }
        else{
            binding.preview.visibility = View.INVISIBLE
        }
    }

    private fun bindPurchasedPlaceholderIfNeed(item: VirtualItemUiEntity) {
        if (item.hasInInventory) {
            if (item.inventoryOption?.consumable == null && item.inventoryOption?.expirationPeriod == null) {
                binding.purchasedPlaceholder.isVisible = true
                binding.addToCartButton.isVisible = false
                binding.itemPrice.isVisible = false
                binding.itemOldPrice.isVisible = false
                binding.itemSaleLabel.isVisible = false
            } else {
                binding.purchasedPlaceholder.isVisible = false
                binding.addToCartButton.isVisible = true
                binding.itemPrice.isVisible = true
                binding.itemOldPrice.isVisible = true
                binding.itemSaleLabel.isVisible = true
            }
        } else {
            binding.purchasedPlaceholder.isVisible = false
            binding.addToCartButton.isVisible = true
            binding.itemPrice.isVisible = true
            binding.itemOldPrice.isVisible = true
            binding.itemSaleLabel.isVisible = true
        }
    }

    private fun bindItemPrice(item: VirtualItemUiEntity) {
        val price = item.price
        if (price!!.getAmountDecimal() == price.getAmountWithoutDiscountDecimal() || price.getAmountWithoutDiscountDecimal() == null) {
            binding.itemPrice.text = AmountUtils.prettyPrint(price.getAmountDecimal(), price.currency)
            binding.itemOldPrice.visibility = View.INVISIBLE
            binding.itemSaleLabel.visibility = View.INVISIBLE
        } else {
            if (binding.itemOldPrice.isVisible && binding.itemSaleLabel.isVisible) {
                val discount = AmountUtils.calculateDiscount(price.getAmountDecimal()!!, price.getAmountWithoutDiscountDecimal()!!)

                binding.itemPrice.text = AmountUtils.prettyPrint(price.getAmountDecimal(), price.currency)
                binding.itemSaleDiscount.text = "-${discount}%"
                binding.itemOldPrice.text = AmountUtils.prettyPrint(price.getAmountWithoutDiscountDecimal())
                binding.itemOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        binding.addToCartButton.setOnClickListener {
            when (item.virtualItemType) {
                null -> vmGooglePay.startPurchase(item.sku!!) // bundle
                else -> vmGooglePay.startPurchase(item.sku!!, item.virtualItemType)
            }
        }
    }

    private fun bindExpirationPeriod(expirationPeriod: ExpirationPeriod?) {
        if (expirationPeriod == null) {
            binding.itemAdditionalInfo.visibility = View.GONE
        } else {
            binding.itemAdditionalInfo.visibility = View.VISIBLE
            val sb = StringBuilder()
            sb.append("Expiration: ")
            sb.append(expirationPeriod.value)
            sb.append(' ')
            sb.append(expirationPeriod.type.name.toLowerCase())
            if (expirationPeriod.value != 1) {
                sb.append('s')
            }
            binding.itemAdditionalInfo.text = sb
        }
    }

}