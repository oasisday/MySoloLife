package mysololife.example.sololife.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysololife.databinding.ItemChatroomBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseRef
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(private val onClick : (ChatRoomItem) -> Unit) : ListAdapter<ChatRoomItem, ChatListAdapter.ViewHolder>(differ) {
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    inner class ViewHolder(private val binding: ItemChatroomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoomItem) {
            binding.chatTime.text = simpleDateFormat.format(Date(item.time))
            binding.nicknameTextView.text = item.otherUserName
            binding.lastMessageTextView.text = item.lastMessage

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.getValue(UserDataModel::class.java)


                    val storageReference = Firebase.storage.reference.child(item.otherUserId + ".png")
                    storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        if(task.isSuccessful) {

                            binding.root.context?.let {
                                if( it!= null) {
                                    if (binding.profileImg != null) {
                                        Glide.with(it)
                                            .load(task.result)
                                            .into(binding.profileImg)
                                    }
                                }
                            }

                        } else {

                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                }
            }
            if (item.otherUserId != null) {
                FirebaseRef.userInfoRef.child(item.otherUserId).addValueEventListener(postListener)
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatroomBinding.inflate(
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
        val differ = object : DiffUtil.ItemCallback<ChatRoomItem>() {
            override fun areItemsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
                return oldItem.chatRoomId == newItem.chatRoomId
            }

            override fun areContentsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
                return oldItem == newItem
            }

        }
    }
}