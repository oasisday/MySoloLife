package mysololife.example.sololife.chatdetail

import com.example.mysololife.databinding.ItemChatBinding
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.L
import com.example.mysololife.R
import mysololife.example.sololife.message.UserItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatItem, ChatAdapter.ViewHolder>(differ) {
    var otherUserItem: UserItem? = null
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem) {
            val chatTimeParams = binding.chatTime.layoutParams as LinearLayout.LayoutParams
            val chatinfoParams = binding.chatinfo.layoutParams as LinearLayout.LayoutParams
            if (item.userId == otherUserItem?.userId) {
                binding.usernameTextView.isVisible = true
                binding.usernameTextView.text = otherUserItem?.username
                binding.messageTextView.text = item.message
                chatinfoParams.gravity = Gravity.START
                binding.chatinfo.layoutParams = chatinfoParams
                binding.chatTime.text = simpleDateFormat.format(Date(item.timestamp))
                binding.messageTextView.gravity = Gravity.START
                chatTimeParams.gravity = Gravity.START
                binding.chatTime.layoutParams = chatTimeParams

            } else {
                binding.usernameTextView.isVisible = false
                binding.messageTextView.text = item.message
                binding.chatTime.text = simpleDateFormat.format(Date(item.timestamp))
                chatinfoParams.gravity = Gravity.END
                binding.chatinfo.layoutParams = chatinfoParams
                chatTimeParams.gravity = Gravity.END
                binding.chatTime.layoutParams = chatTimeParams
            }
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