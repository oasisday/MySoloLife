package mysololife.example.sololife

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibrationHelper(private val context: Context) {

    fun vibrateOnce(duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 안드로이드 Oreo(26) 이상 버전에서는 VibrationEffect를 사용
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {
            // 안드로이드 Oreo 이하에서는 vibrate 메서드를 사용
            vibrator.vibrate(duration)
        }
    }
}