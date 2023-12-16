package mysololife.example.sololife.chatdetail

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatItem(
    var chatId: String? = null,
    val userId: String? = null,
    val message: String? = null,
    val timestamp: Long = 0,
    val userName: String? = null
)