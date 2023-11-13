package mysololife.example.sololife.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBboard {
    companion object {
        val database = Firebase.database
        val boardInfoRef = database.getReference("boardInfo")

        val insideboardRef = database.getReference("insideBoard")
    }

}
