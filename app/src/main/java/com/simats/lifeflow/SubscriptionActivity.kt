package com.simats.lifeflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : BaseActivity(), PurchasesUpdatedListener {

    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnSkipForNow: MaterialButton
    private lateinit var tvPrice: TextView
    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null

    companion object {
        private const val TAG = "SubscriptionActivity"
        private const val SUBSCRIPTION_SKU = "lifeflow_premium_subscription"
        private const val TEST_SUBSCRIPTION_SKU = "android.test.purchased"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        initializeViews()
        setupBillingClient()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnSkipForNow = findViewById(R.id.btnSkipForNow)
        tvPrice = findViewById(R.id.tvPrice)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    querySubscriptionDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    private fun querySubscriptionDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TEST_SUBSCRIPTION_SKU)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Find either the real subscription or the test product
                val details = productDetailsList.find { it.productId == SUBSCRIPTION_SKU }
                    ?: productDetailsList.find { it.productId == TEST_SUBSCRIPTION_SKU }
                
                productDetails = details
                
                if (details != null) {
                    Log.d(TAG, "Product details retrieved: ${details.productId}")
                    updatePriceText(details)
                } else {
                    Log.e(TAG, "No matching products found in Play Store")
                }
            }
        }
    }

    private fun updatePriceText(details: ProductDetails) {
        runOnUiThread {
            val price = if (details.productType == BillingClient.ProductType.SUBS) {
                details.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice?.let {
                    "$it / month"
                }
            } else {
                details.oneTimePurchaseOfferDetails?.formattedPrice
            }
            
            if (price != null) {
                tvPrice.text = price
            }
        }
    }

    private fun setupClickListeners() {
        btnSkipForNow.setOnClickListener {
            navigateToMain()
        }
        btnSubscribe.setOnClickListener {
            launchSubscriptionFlow()
        }
    }

    private fun launchSubscriptionFlow() {
        if (!billingClient.isReady) {
            Toast.makeText(this, "Billing service not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val details = productDetails
        if (details != null) {
            val productDetailsParamsList = if (details.productType == BillingClient.ProductType.SUBS) {
                val offerToken = details.subscriptionOfferDetails?.getOrNull(0)?.offerToken ?: ""
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            } else {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        } else {
            Toast.makeText(this, "Subscription details not loaded", Toast.LENGTH_SHORT).show()
            // Try to query again if not loaded
            querySubscriptionDetails()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                saveSubscriptionAndFinish()
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                Toast.makeText(this, "Error: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        saveSubscriptionAndFinish()
                    }
                }
            } else {
                saveSubscriptionAndFinish()
            }
        }
    }

    private fun saveSubscriptionAndFinish() {
        getSharedPreferences("subscription_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_premium_user", true)
            .apply()
        
        runOnUiThread {
            Toast.makeText(this, "Premium activated successfully!", Toast.LENGTH_LONG).show()
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val role = sharedPrefs.getString("user_role", "")
        val intent = when (role) {
            "donor" -> Intent(this, DonorDashboardActivity::class.java)
            "patient" -> Intent(this, PatientDashboardActivity::class.java)
            else -> Intent(this, RoleSelectionActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}
