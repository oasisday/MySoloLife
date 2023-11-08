package mysololife.example.sololife.recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs){

    private var paint = Paint()
    private val amplitudes = ArrayList<Float>()
    private val spikes = ArrayList<RectF>()
    private var radius = 6f
    private var w = 9f
    private var d = 6f
    private var sw = 0f
    private var sh = 600f

    private var maxSpikes = 0
    init{
        paint.color = Color.rgb(244,81,30)
        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (sw/(w+d)).toInt()
    }

    fun addAmplitude(amp: Float) {
        var norm = Math.min(amp.toInt()/30,600).toFloat()
        amplitudes.add(norm)

        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices) {
            var left = sw - i*(w+d)
            var top = sh/2-amps[i]/2
            var right = left + w
            var bottom = top+amps[i]
            spikes.add(RectF(left, top, right, bottom))
        }

        invalidate()
    }

    fun clear():ArrayList<Float>{
        var amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        spikes.clear()
        invalidate()
        return amps
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }
}