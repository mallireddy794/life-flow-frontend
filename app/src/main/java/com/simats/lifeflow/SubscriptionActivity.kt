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

        addDebugInformation()
        initializeViews()
        setupBillingClient()
        setupClickListeners()
    }

    private fun addDebugInformation() {
        Log.d(TAG, "=== DEBUG INFORMATION ===")
        Log.d(TAG, "Package name: ${packageName}")

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            Log.d(TAG, "Version code: ${packageInfo.longVersionCode}")
            Log.d(TAG, "Version name: ${packageInfo.versionName}")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to get package info: ${e.message}")
        }

        Log.d(TAG, "Product ID: $SUBSCRIPTION_SKU")
        Log.d(TAG, "Test Product ID: $TEST_SUBSCRIPTION_SKU")
        Log.d(TAG, "=========================")
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
        // First try to query real subscription
        querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS) { success ->
            if (!success) {
                Log.w(TAG, "Real subscription not found, trying test product...")
                querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP) { testSuccess ->
                    if (!testSuccess) {
                        Log.e(TAG, "Both real and test products failed")
                        runOnUiThread {
                            tvPrice.text = "Subscription unavailable"
                        }
                    }
                }
            }
        }
    }

    private fun querySpecificProduct(productId: String, productType: String, callback: (Boolean) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val details = productDetailsList[0]
                productDetails = details
                Log.d(TAG, "Product details retrieved: ${details.productId}")
                
                runOnUiThread {
                    val price = if (details.productType == BillingClient.ProductType.SUBS) {
                        details.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice?.let {
                            "$it / month"
                        }
                    } else {
                        details.oneTimePurchaseOfferDetails?.formattedPrice
                    }
                    tvPrice.text = price ?: "Price details not available"
                }
                callback(true)
            } else {
                Log.e(TAG, "Failed to query product details for $productId: ${billingResult.debugMessage}")
                callback(false)
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
            Toast.makeText(this, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        productDetails?.let { details ->
            val productDetailsParamsList = if (details.productType == BillingClient.ProductType.SUBS) {
                val subscriptionOfferDetails = details.subscriptionOfferDetails
                if (subscriptionOfferDetails.isNullOrEmpty()) {
                    Toast.makeText(this, "No subscription offers available", Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedOffer = subscriptionOfferDetails[0]
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(selectedOffer.offerToken)
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
        } ?: run {
            Toast.makeText(this, "Subscription details not loaded", Toast.LENGTH_SHORT).show()
            querySubscriptionDetails()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(this, "Purchase canceled", Toast.LENGTH_SHORT).show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                onSubscriptionSuccess()
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                Toast.makeText(this, "Purchase failed: ${billingResult.debugMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onSubscriptionSuccess()
                    }
                }
            } else {
                onSubscriptionSuccess()
            }
        }
    }

    private fun onSubscriptionSuccess() {
        Toast.makeText(this, "Subscription successful! Welcome to Premium!", Toast.LENGTH_LONG).show()

        getSharedPreferences("subscription_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_premium_user", true)
            .putLong("subscription_time", System.currentTimeMillis())
            .apply()

        navigateToMain()
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

