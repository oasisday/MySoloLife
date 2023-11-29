package mysololife.example.sololife.timetable

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface InfoDao {
    @Query("SELECT * from Info ORDER BY originid DESC")
    fun getAll(): List<InfoEntity>

    @Query("SELECT * FROM Info GROUP BY scheduleName")
    fun getAllLecture(): List<InfoEntity>
    @Insert
    fun insert(info: InfoEntity)

    @Query("UPDATE Info SET backgroundColor = :newColor WHERE scheduleName = :scheduleName")
    fun updateColor(scheduleName: String, newColor: String)

    @Query("DELETE FROM Info WHERE scheduleName = :name")
    fun deleteByScheduleName(name: String)
    @Delete
    fun delete(info: InfoEntity)

    @Update
    fun update(info: InfoEntity)
}