package mysololife.example.sololife.timetable

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [InfoEntity::class],version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun infoDao(): InfoDao
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "timetable-database.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}