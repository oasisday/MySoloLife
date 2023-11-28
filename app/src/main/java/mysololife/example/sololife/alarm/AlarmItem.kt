package mysololife.example.sololife.alarm

import com.google.gson.JsonPrimitive

data class AlarmItem(
    val requestId: Long,
    val hour: Int,
    val minute: Int,
    val weekDays: List<String>,
    val isActive: Boolean
)
const val RECORD_NAME: String = "SubjectPreferences"