package com.minh.momo_vn

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import vn.momo.momo_partner.AppMoMoLib

class MomoVnPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {

    private var channel: MethodChannel? = null
    private var activity: Activity? = null
    private var binding: ActivityPluginBinding? = null
    private var pendingResult: Result? = null

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, MomoVnConfig.CHANNEL_NAME)
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        this.binding = binding
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        binding?.removeActivityResultListener(this)
        activity = null
        binding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            MomoVnConfig.METHOD_REQUEST_PAYMENT -> {
                openCheckout(call.arguments, result)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    private fun openCheckout(momoRequestPaymentData: Any?, result: Result) {
        if (activity == null) {
            result.error("ACTIVITY_NULL", "Activity is null", null)
            return
        }

        pendingResult = result

        AppMoMoLib.getInstance().apply {
            setAction(AppMoMoLib.ACTION.PAYMENT)
            setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)

            val paymentInfo = momoRequestPaymentData as? HashMap<String, Any> ?: run {
                result.error("INVALID_ARGUMENT", "Invalid payment data", null)
                return
            }

            val isTestMode = paymentInfo["isTestMode"] as? Boolean ?: false
            setEnvironment(
                if (isTestMode) AppMoMoLib.ENVIRONMENT.DEVELOPMENT
                else AppMoMoLib.ENVIRONMENT.PRODUCTION
            )

            requestMoMoCallBack(activity, paymentInfo)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
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
}