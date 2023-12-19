package mysololife.example.sololife.chatdetail

import com.example.mysololife.databinding.ItemChatBinding
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter2 : ListAdapter<ChatItem, ChatAdapter2.ViewHolder>(differ) {
    var myUserID : String? =null
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var previousItem: ChatItem? = null
    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem) {
            val chatTimeParams = binding.chatTime.layoutParams as LinearLayout.LayoutParams
            val chatinfoParams = binding.chatinfo.layoutParams as LinearLayout.LayoutParams
            if (item.userId == myUserID) {
                binding.usernameTextView.isVisible = false
                binding.messageTextView.text = item.message
                chatinfoParams.gravity = Gravity.END
                binding.chatinfo.layoutParams = chatinfoParams
                chatTimeParams.gravity = Gravity.END
                binding.chatTime.layoutParams = chatTimeParams

            } else {
                binding.usernameTextView.isVisible = true
                binding.usernameTextView.text = item.userName
                binding.messageTextView.text = item.message
                chatinfoParams.gravity = Gravity.START
                binding.chatinfo.layoutParams = chatinfoParams
                binding.messageTextView.gravity = Gravity.START
                chatTimeParams.gravity = Gravity.START
                binding.chatTime.layoutParams = chatTimeParams
            }

            // 이전 아이템과 현재 아이템의 timestamp를 비교하여 같을 경우에만 시간을 표시
            if (previousItem != null && simpleDateFormat.format(Date(item.timestamp)) == simpleDateFormat.format(Date(previousItem!!.timestamp))) {
                binding.chatTime.visibility = View.GONE
            } else {
                binding.chatTime.visibility = View.VISIBLE
                binding.chatTime.text = simpleDateFormat.format(Date(item.timestamp))
            }

            // bind 메서드가 호출될 때마다 previousItem 업데이트
            previousItem = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }
            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}