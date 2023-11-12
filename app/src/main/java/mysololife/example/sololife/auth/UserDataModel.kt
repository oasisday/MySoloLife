package mysololife.example.sololife.auth

data class UserDataModel (
    val uid : String? = null,
    val nickname : String? = null,
    val gender : String? = null,
    val token : String? = null,
    var ischecked: Boolean = false
)
