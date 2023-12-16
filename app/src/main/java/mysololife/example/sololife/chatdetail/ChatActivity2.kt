package mysololife.example.sololife.chatlist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.databinding.ActivityChatdetailBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.chatdetail.ChatAdapter2
import mysololife.example.sololife.chatdetail.ChatItem
import mysololife.example.sololife.message.UserItem

class ChatActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityChatdetailBinding
    private lateinit var chatAdapter: ChatAdapter2
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return

        if(otherUserId=="전체 채팅방") {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Person")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // dataSnapshot.childrenCount에 데이터 개수가 포함되어 있습니다.
                    val numberOfPersons = dataSnapshot.childrenCount
                    // 이제 numberOfPersons를 사용할 수 있습니다.
                    supportActionBar?.title = otherUserId+ " ("+numberOfPersons.toString()+"명)"
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 데이터 읽기 중 오류가 발생한 경우 처리
                    Log.e("FirebaseData", "데이터 읽기 중 오류 발생", databaseError.toException())
                }
            })
        }
        else{
            supportActionBar?.title = otherUserId+" 스터디룸 채팅방"
        }
        myUserId = Firebase.auth.currentUser?.uid ?: ""
        chatAdapter = ChatAdapter2()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""
                getChatData()
            }

        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatAdapter.itemCount
                )
            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            var otherUsername: String? = null
            var myUserID : String? =null

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "빈 메시지를 전송할 수는 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId,
                timestamp = System.currentTimeMillis(),
                userName = myUserName
            )

            Firebase.database.reference.child(Key.DB_ALLCHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }
            binding.messageEditText.text.clear()
        }
    }

    private fun getChatData() {
        chatAdapter.myUserID = myUserId
        Firebase.database.reference.child(Key.DB_ALLCHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return
                    chatItemList.add(chatItem)
                    chatAdapter.submitList(chatItemList.toMutableList())
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }
}