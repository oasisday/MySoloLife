package mysololife.example.sololife.timetable

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.firebase.auth.FirebaseAuth

@Database(entities = [InfoEntity::class],version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun infoDao(): InfoDao
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {

                val uid = getCurrentFirebaseUserUid()
                Log.d("hihihi",uid)
                synchronized(AppDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "timetable.db"
                    ).build()
                }
            }
            return INSTANCE
        }

        private fun getCurrentFirebaseUserUid(): String {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            return currentUser?.uid ?: "default_user_uid"
        }
    }
}