package mysololife.example.sololife.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentChatlistBinding
import com.example.mysololife.databinding.FragmentUserlistBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ChatListFragment : Fragment() {

    private lateinit var binding: FragmentChatlistBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentChatlistBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chatListAdapter = ChatListAdapter()

        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
//        val currentUserId = Firebase.auth.currentUser?.uid ?: return
//        val chatRoomsDB = Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(currentUserId)
//
//        chatRoomsDB.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val chatRoomList = snapshot.children.map {
//                    it.getValue(ChatRoomItem::class.java)
//                }
//                chatListAdapter.submitList(chatRoomList)
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
    }
}