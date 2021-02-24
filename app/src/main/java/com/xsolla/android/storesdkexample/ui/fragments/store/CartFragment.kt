package com.xsolla.android.storesdkexample.ui.fragments.store

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xsolla.android.appcore.utils.AmountUtils
import com.xsolla.android.payments.XPayments.Companion.createIntentBuilder
import com.xsolla.android.payments.XPayments.Result.Companion.fromResultIntent
import com.xsolla.android.payments.data.AccessToken
import com.xsolla.android.storesdkexample.BuildConfig
import com.xsolla.android.storesdkexample.R
import com.xsolla.android.storesdkexample.adapter.CartAdapter
import com.xsolla.android.appcore.databinding.FragmentCartBinding
import com.xsolla.android.storesdkexample.listener.CartChangeListener
import com.xsolla.android.storesdkexample.ui.fragments.base.BaseFragment
import com.xsolla.android.storesdkexample.ui.vm.VmCart
import java.math.BigDecimal

class CartFragment : BaseFragment(), CartChangeListener {
    private val binding: FragmentCartBinding by viewBinding()

    private val vmCart: VmCart by activityViewModels()

    private lateinit var promocodeArrowEndIconDrawable: Drawable
    private lateinit var promocodeSuccessEndIconDrawable: Drawable

    private var orderId = 0

    override fun getLayout() = R.layout.fragment_cart

    override fun initUI() {
        promocodeArrowEndIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_forward_white_24)!!
        promocodeSuccessEndIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_black_24)!!.apply {
            setTint(Color.WHITE)
        }

        val cartAdapter = CartAdapter(mutableListOf(), vmCart, this)
        with(binding.recycler) {
            setHasFixedSize(true)
            val linearLayoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation).apply {
                ContextCompat.getDrawable(context, R.drawable.item_divider)?.let { setDrawable(it) }
            })
            layoutManager = linearLayoutManager
            adapter = cartAdapter
        }

        vmCart.cartContent.observe(viewLifecycleOwner, Observer { items ->
            if (items.isEmpty()) {
                findNavController().navigateUp()
                return@Observer
            }

            cartAdapter.items.clear()
            cartAdapter.items.addAll(items)
            cartAdapter.notifyDataSetChanged()
            binding.checkoutButton.isEnabled = items.isNotEmpty()

            val currency = items[0].price!!.currency

            val itemsWithoutBonus = items.filter { it.price != null }
            val sumWithoutDiscount = itemsWithoutBonus.map { item -> item.price!!.getAmountWithoutDiscountDecimal()!! * item.quantity.toBigDecimal() }.fold(BigDecimal.ZERO, BigDecimal::add)
            val sumWithDiscount = itemsWithoutBonus.map { item -> item.price!!.getAmountDecimal()!! * item.quantity.toBigDecimal() }.fold(BigDecimal.ZERO, BigDecimal::add)
            val discount = sumWithoutDiscount.minus(sumWithDiscount)

            val hasDiscount = discount.toDouble() != 0.0
            binding.subtotalLabel.isVisible = hasDiscount
            binding.subtotalValue.isVisible = hasDiscount
            binding.discountLabel.isVisible = hasDiscount
            binding.discountValue.isVisible = hasDiscount

            binding.subtotalValue.text = AmountUtils.prettyPrint(sumWithoutDiscount, currency!!)
            binding.discountValue.text = "- ${AmountUtils.prettyPrint(discount, currency)}"
            binding.totalValue.text = AmountUtils.prettyPrint(sumWithDiscount, currency)
        })

        binding.clearButton.setOnClickListener {
            vmCart.clearCart { result ->
                showSnack(result)
                findNavController().navigateUp()
            }
        }

        binding.checkoutButton.setOnClickListener {
            vmCart.createOrder { error -> showSnack(error) }
        }

        binding.continueButton.setOnClickListener { findNavController().navigateUp() }

        vmCart.paymentToken.observe(viewLifecycleOwner, Observer {
            val intent = createIntentBuilder(requireContext())
                .accessToken(AccessToken(it))
                .useWebview(true)
                .isSandbox(BuildConfig.IS_SANDBOX)
                .build()
            startActivityForResult(intent, RC_PAYSTATION)
        })

        vmCart.orderId.observe(viewLifecycleOwner, Observer {
            orderId = it
        })

        setupPromocodeInput()
    }

    override fun onResume() {
        super.onResume()
        vmCart.updateCart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            orderId = savedInstanceState.getInt("orderId")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("orderId", orderId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PAYSTATION) {
            val (status, invoiceId) = fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                vmCart.checkOrder(orderId) { error -> showSnack(error) }
            }
        }
    }

    override fun onChange(result: String) {
        showSnack(result)
    }

    private fun setupPromocodeInput() {
        binding.input.setEndIconOnClickListener {
            hideKeyboard()

            vmCart.redeemPromocode(
                    binding.inputEdit.text.toString(),
                onSuccess = { binding.input.endIconDrawable = promocodeSuccessEndIconDrawable },
                onError = { message ->
                    binding.input.isErrorEnabled = true
                    binding.input.error = message
                }
            )
        }
        binding.inputEdit.addTextChangedListener {
            binding.input.isErrorEnabled = false

            if (!it.isNullOrBlank()) {
                binding.input.endIconDrawable = promocodeArrowEndIconDrawable
            } else {
                binding.input.endIconDrawable = null
            }
        }
    }

    companion object {
        const val RC_PAYSTATION = 1

        @JvmStatic
        fun newInstance() = CartFragment().apply {
                arguments = Bundle()
            }
    }
}