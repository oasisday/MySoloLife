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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mysololife.example.sololife.chatlist.ChatActivity
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

        val body = message.data["body"]
        val title = message.data["title"]
        val notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.round_chat_24)
            .setContentTitle(title)
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
        if(message.data.containsKey("chatroomId")&&message.data.containsKey("myUserId")){
            val chatroomid = message.data["chatroomId"]
            val otheruser = message.data["myUserId"]
            Log.d("testing",chatroomid+otheruser)
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID,chatroomid)
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID,otheruser)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
            notificationBuilder.setAutoCancel(true)

        }

        if (message.data.containsKey("vibrate")) {
            val vibrateValue = message.data["vibrate"]
            val shouldVibrate = vibrateValue?.toBoolean() ?: false
            if (shouldVibrate) {
                val vibrationHelper = VibrationHelper(applicationContext)
// 원하는 시간(밀리초) 동안 진동 실행
                vibrationHelper.vibrateOnce(500) // 1000 밀리초(1초) 동안 진동
            }
            notificationBuilder.setSmallIcon(R.drawable.clickbtn)
            notificationBuilder.setContentTitle("\"경고: 찔림 감지, 빨리 확인하세요\"")
            val intent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val vibrationPattern = longArrayOf(0, 500)
            notificationBuilder.setVibrate(vibrationPattern)
            notificationBuilder.setContentIntent(pendingIntent)
            notificationBuilder.setAutoCancel(true)
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
        Log.d("FCM Token", "Refreshed token: $token")

        // 서버로 토큰을 전송하는 작업을 수행 62297780
        sendTokenToServer(token)
    }
    private fun sendTokenToServer(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseDatabase.getInstance().getReference("Users/"+user!!.uid)
        userRef.child("fcmToken").setValue(token)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 업데이트 성공
                    Log.d("Firebase", "FCM 토큰 업데이트 성공")
                } else {
                    // 업데이트 실패
                    Log.e("Firebase", "FCM 토큰 업데이트 실패", task.exception)
                }
            }
    }
}