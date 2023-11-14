package mysololife.example.sololife.boardUI.adapter.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.example.mysololife.R
import mysololife.example.sololife.boardUI.WaterfallItemModel
import java.lang.ref.WeakReference

class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val view = WeakReference(itemView)

    private var imageView: ImageView? = null
    private var textView: TextView? = null

    var model: WaterfallItemModel? = null

    init {
        imageView = view.get()?.findViewById(R.id.imageView)
        textView = view.get()?.findViewById(R.id.textView)

        view.get()?.setOnClickListener{
            model?.let { model ->
                Toast.makeText(it.context, model.title, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun updateView() {

        model?.let { model ->

            imageView?.let {
                Glide.with(it.context).asBitmap().load(model.imageURL)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(it)
            }

            textView?.text = model.title
        }
    }

}