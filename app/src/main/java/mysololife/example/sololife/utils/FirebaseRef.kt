package mysololife.example.sololife.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRef {
    companion object {

        val database = Firebase.database

        val userInfoRef = database.getReference("userInfo")
        val userLikeRef = database.getReference("userLike")
        val userMsgRef = database.getReference("userMsg")

        val userBothRef = database.getReference("userBoth")

        val userDataRef = database.getReference("userData")
        val personRef = database.getReference("Person")
    }

}