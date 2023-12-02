package mysololife.example.sololife.userlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.databinding.ItemChatIntroductionBinding

class UserAdapter: ListAdapter<UserItem, UserAdapter.ViewHolder>(differ) {

    inner class ViewHolder(private val binding: ItemChatIntroductionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserItem) {
            binding.nicknameTextView.text = item.username
            binding.descriptionTextView.text = item.description

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatIntroductionBinding.inflate(
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
        val differ = object : DiffUtil.ItemCallback<UserItem>() {
            override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem.userId == newItem.userId
            }
            override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}