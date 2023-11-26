package mysololife.example.sololife.auth

data class UserDataModel (
    val uid : String? = null,
    val nickname : String? = null,
    val gender : String? = null,
    var info : String? = "자기를 소개 해주세요:)",
    val token : String? = null,
    var ischecked: Boolean = false
)
