package com.minh.momo_vn

import android.app.Activity
import android.content.Intent
import io.flutter.plugin.common.MethodChannel.Result
import vn.momo.momo_partner.AppMoMoLib

class MomoVnPluginDelegate(private var activity: Activity?) {

    private var pendingResult: Result? = null

    fun openCheckout(momoRequestPaymentData: Any, result: Result) {
        this.pendingResult = result

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)

        val paymentInfo: HashMap<String, Any> = momoRequestPaymentData as HashMap<String, Any>
        val isTestMode: Boolean? = paymentInfo["isTestMode"] as? Boolean

        AppMoMoLib.getInstance().setEnvironment(
            if (isTestMode == true) AppMoMoLib.ENVIRONMENT.DEVELOPMENT
            else AppMoMoLib.ENVIRONMENT.PRODUCTION
        )

        activity?.let {
            AppMoMoLib.getInstance().requestMoMoCallBack(it, paymentInfo)
        } ?: run {
            result.error("ACTIVITY_NULL", "Activity is null", null)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != AppMoMoLib.getInstance().REQUEST_CODE_MOMO) return false

        val resultMap = mutableMapOf<String, Any>(
            "isSuccess" to false,
            "status" to 7,
            "phoneNumber" to "",
            "token" to "",
            "message" to "",
            "extra" to ""
        )

        if (resultCode == Activity.RESULT_OK && data != null) {
            val status = data.getIntExtra("status", -1)
            val isSuccess = status == MomoVnConfig.CODE_PAYMENT_SUCCESS

            resultMap["isSuccess"] = isSuccess
            resultMap["status"] = status
            resultMap["phoneNumber"] = data.getStringExtra("phonenumber") ?: ""
            resultMap["token"] = data.getStringExtra("data") ?: ""
            resultMap["message"] = data.getStringExtra("message") ?: ""
            resultMap["extra"] = data.getStringExtra("extra") ?: ""
        }

        pendingResult?.success(resultMap)
        pendingResult = null
        return true
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }
}
