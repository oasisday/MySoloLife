package mysololife.example.sololife.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.islandparadise14.mintable.model.ScheduleEntity


@Entity(tableName="Info")
data class InfoEntity(
    var scheduleName: String,
    var roomInfo: String,
    var scheduleDay: Int,
    var startTime: String,
    var endTime: String,
    var backgroundColor: String = "#dddddd",
    var textColor: String = "#ffffff",
    @PrimaryKey(autoGenerate = true) val originId: Int=0,
)
fun InfoEntity.toScheduleEntity(): ScheduleEntity {
    return ScheduleEntity(
        scheduleName = this.scheduleName, // InfoEntity의 scheduleName 속성을 ScheduleEntity의 name으로 복사
        roomInfo = this.roomInfo,     // InfoEntity의 roomInfo 속성을 ScheduleEntity의 room으로 복사
        // 나머지 속성들도 동일한 방식으로 복사
        scheduleDay = this.scheduleDay,
        startTime = this.startTime,
        endTime = this.endTime,
        backgroundColor = this.backgroundColor,
        textColor = this.textColor,
        originId = this.originId
    )
}