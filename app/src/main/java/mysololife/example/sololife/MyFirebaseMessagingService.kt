package mysololife.example.sololife

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.resources.Compatibility.Api18Impl.setAutoCancel
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mysololife.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mysololife.example.sololife.chatlist.ChatActivity2
import mysololife.example.sololife.map.MapActivity2
import okhttp3.internal.notify
import org.json.JSONObject

class MyFirebaseMessagingService: FirebaseMessagingService() {
    @SuppressLint("NewApi")
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("testAPI","!"+message.toString())
        super.onMessageReceived(message)
        val name = "채팅 알림"
        val descriptionText = "채팅 알림입니다."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(getString(R.string.default_notification_channel_id), name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val body = message.notification?.body ?: ""
        val notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.round_chat_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(body)
            .setSound(null)

        if (message.data.containsKey("location")) {
            val vibrateValue = message.data["location"]
            Log.d("hihihi",vibrateValue.toString())
            val shouldVibrate = vibrateValue?.toString()?:""
            if (!shouldVibrate.isNullOrEmpty()) {
                val intent = Intent(applicationContext, MapActivity2::class.java)
                intent.putExtra(ChatActivity2.EXTRA_CHAT_ROOM_ID, shouldVibrate)
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.setContentIntent(pendingIntent)
                notificationBuilder.setAutoCancel(true)
            }
            notificationBuilder.setSmallIcon(R.drawable.round_gps_fixed_24)
        }

        if (message.data.containsKey("vibrate")) {
            val vibrateValue = message.data["vibrate"]
            val shouldVibrate = vibrateValue?.toBoolean() ?: false
            if (shouldVibrate) {
                val vibrationHelper = VibrationHelper(applicationContext)
// 원하는 시간(밀리초) 동안 진동 실행
                vibrationHelper.vibrateOnce(500) // 1000 밀리초(1초) 동안 진동
                notificationBuilder.setAutoCancel(true)
            }
            notificationBuilder.setSmallIcon(R.drawable.clickbtn)
            notificationBuilder.setContentTitle("\"경고: 찔림 감지, 빨리 확인하세요\"")
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(applicationContext).notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}