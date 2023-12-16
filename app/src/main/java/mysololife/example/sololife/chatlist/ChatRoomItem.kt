package mysololife.example.sololife.chatlist

data class ChatRoomItem(
    val chatRoomId: String? = null,
    val lastMessage: String? = null,
    val otherUserName: String? = null,
    val otherUserId: String? = null,
    val time: Long = 0,
)