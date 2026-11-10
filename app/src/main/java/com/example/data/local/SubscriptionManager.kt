package com.example.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

enum class PlanTier(val titleEn: String, val titleBn: String) {
    FREE("Free Plan", "ফ্রি প্ল্যান"),
    MOBILE_ONLY("Mobile Pro", "মোবাইল প্রো"),
    MOBILE_PLUS_WEB("Mobile + Web Business", "মোবাইল + ওয়েব বিজনেস")
}

data class PlanPricing(
    val tier: PlanTier,
    val durationMonths: Int,
    val priceBdt: Double,
    val discountPercent: Int = 0
)

data class ManualPaymentSubmission(
    val id: String,
    val planTier: PlanTier,
    val durationMonths: Int,
    val amountBdt: Double,
    val trxId: String,
    val senderPhone: String,
    val submissionTime: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)

object SubscriptionManager {
    private const val PREFS_NAME = "dokan_subscription_prefs"
    private const val KEY_CURRENT_TIER = "current_plan_tier"
    private const val KEY_EXPIRY_TIMESTAMP = "plan_expiry_timestamp"
    private const val KEY_TRIAL_USED = "is_trial_used"
    private const val KEY_PENDING_SUBMISSIONS = "pending_submissions_json"
    private const val KEY_CUSTOM_PRICES = "custom_plan_prices"

    var currentTier by mutableStateOf(PlanTier.FREE)
        private set

    var expiryDateTimestamp by mutableStateOf(0L)
        private set

    var isTrialUsed by mutableStateOf(false)
        private set

    var pendingSubmissions by mutableStateOf<List<ManualPaymentSubmission>>(emptyList())
        private set

    // Configurable plan prices (Owner can adjust)
    var mobileOnly3Mo by mutableStateOf(450.0)
    var mobileOnly6Mo by mutableStateOf(800.0)
    var mobileOnly12Mo by mutableStateOf(1400.0)

    var mobilePlusWeb3Mo by mutableStateOf(750.0)
    var mobilePlusWeb6Mo by mutableStateOf(1350.0)
    var mobilePlusWeb12Mo by mutableStateOf(2400.0)

    // Coupon discounts
    val validCoupons = mapOf(
        "DOKAN2026" to 15, // 15% discount
        "EID2026" to 20,   // 20% discount
        "FIRSTBUY" to 10   // 10% discount
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tierStr = prefs.getString(KEY_CURRENT_TIER, PlanTier.FREE.name) ?: PlanTier.FREE.name
        currentTier = try { PlanTier.valueOf(tierStr) } catch (e: Exception) { PlanTier.FREE }
        expiryDateTimestamp = prefs.getLong(KEY_EXPIRY_TIMESTAMP, 0L)
        isTrialUsed = prefs.getBoolean(KEY_TRIAL_USED, false)

        // If expired, fall back to FREE
        if (currentTier != PlanTier.FREE && expiryDateTimestamp > 0 && System.currentTimeMillis() > expiryDateTimestamp) {
            currentTier = PlanTier.FREE
        }

        loadSubmissions(prefs)
    }

    fun isFeatureUnlocked(featureKey: String): Boolean {
        // If active plan is MOBILE_ONLY or MOBILE_PLUS_WEB, all premium features are unlocked
        if (currentTier == PlanTier.MOBILE_ONLY || currentTier == PlanTier.MOBILE_PLUS_WEB) {
            if (expiryDateTimestamp == 0L || System.currentTimeMillis() <= expiryDateTimestamp) {
                return true
            }
        }
        return false
    }

    fun activateFreeTrial(context: Context): Boolean {
        if (isTrialUsed) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sevenDays = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)
        currentTier = PlanTier.MOBILE_PLUS_WEB
        expiryDateTimestamp = sevenDays
        isTrialUsed = true

        prefs.edit()
            .putString(KEY_CURRENT_TIER, currentTier.name)
            .putLong(KEY_EXPIRY_TIMESTAMP, expiryDateTimestamp)
            .putBoolean(KEY_TRIAL_USED, true)
            .apply()
        return true
    }

    fun submitManualPayment(
        context: Context,
        tier: PlanTier,
        durationMonths: Int,
        amount: Double,
        trxId: String,
        senderPhone: String
    ): ManualPaymentSubmission {
        val newSub = ManualPaymentSubmission(
            id = "pay_${System.currentTimeMillis()}",
            planTier = tier,
            durationMonths = durationMonths,
            amountBdt = amount,
            trxId = trxId.trim(),
            senderPhone = senderPhone.trim()
        )
        val updated = pendingSubmissions.toMutableList().apply { add(0, newSub) }
        pendingSubmissions = updated
        saveSubmissions(context, updated)
        return newSub
    }

    fun approveSubmission(context: Context, submissionId: String) {
        val sub = pendingSubmissions.find { it.id == submissionId } ?: return
        val durationMillis = sub.durationMonths * 30L * 24 * 60 * 60 * 1000
        val baseStart = if (currentTier != PlanTier.FREE && expiryDateTimestamp > System.currentTimeMillis()) {
            expiryDateTimestamp
        } else {
            System.currentTimeMillis()
        }
        currentTier = sub.planTier
        expiryDateTimestamp = baseStart + durationMillis

        val updatedList = pendingSubmissions.map {
            if (it.id == submissionId) it.copy(status = "APPROVED") else it
        }
        pendingSubmissions = updatedList

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CURRENT_TIER, currentTier.name)
            .putLong(KEY_EXPIRY_TIMESTAMP, expiryDateTimestamp)
            .apply()
        saveSubmissions(context, updatedList)
    }

    fun rejectSubmission(context: Context, submissionId: String) {
        val updatedList = pendingSubmissions.map {
            if (it.id == submissionId) it.copy(status = "REJECTED") else it
        }
        pendingSubmissions = updatedList
        saveSubmissions(context, updatedList)
    }

    fun updatePlanPrices(
        m3: Double, m6: Double, m12: Double,
        w3: Double, w6: Double, w12: Double
    ) {
        mobileOnly3Mo = m3
        mobileOnly6Mo = m6
        mobileOnly12Mo = m12
        mobilePlusWeb3Mo = w3
        mobilePlusWeb6Mo = w6
        mobilePlusWeb12Mo = w12
    }

    private fun loadSubmissions(prefs: android.content.SharedPreferences) {
        val raw = prefs.getString(KEY_PENDING_SUBMISSIONS, null) ?: return
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ManualPaymentSubmission>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    ManualPaymentSubmission(
                        id = o.getString("id"),
                        planTier = PlanTier.valueOf(o.getString("planTier")),
                        durationMonths = o.getInt("durationMonths"),
                        amountBdt = o.getDouble("amountBdt"),
                        trxId = o.getString("trxId"),
                        senderPhone = o.getString("senderPhone"),
                        submissionTime = o.getLong("submissionTime"),
                        status = o.getString("status")
                    )
                )
            }
            pendingSubmissions = list
        } catch (e: Exception) {
            pendingSubmissions = emptyList()
        }
    }

    private fun saveSubmissions(context: Context, list: List<ManualPaymentSubmission>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { s ->
            val o = JSONObject().apply {
                put("id", s.id)
                put("planTier", s.planTier.name)
                put("durationMonths", s.durationMonths)
                put("amountBdt", s.amountBdt)
                put("trxId", s.trxId)
                put("senderPhone", s.senderPhone)
                put("submissionTime", s.submissionTime)
                put("status", s.status)
            }
            arr.put(o)
        }
        prefs.edit().putString(KEY_PENDING_SUBMISSIONS, arr.toString()).apply()
    }
}
