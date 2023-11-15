package mysololife.example.sololife.boardUI.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import mysololife.example.sololife.boardUI.WaterfallItemModel
import mysololife.example.sololife.boardUI.adapter.item.ItemViewHolder

class TestAdapter: RecyclerView.Adapter<ItemViewHolder>() {

    private val list = mutableListOf<WaterfallItemModel>()

    var onLoadMore: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return  ItemViewHolder((
                LayoutInflater.from(parent.context).inflate(R.layout.view_holder_item, parent, false)
                ))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.model = list[position]
        holder.updateView()

        if(position == list.size -1) {
            onLoadMore?.let {
                it()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun reload(list: List<WaterfallItemModel>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMore(list: List<WaterfallItemModel>) {
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size - list.size +1, list.size)
    }
}