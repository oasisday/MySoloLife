package mysololife.example.sololife.group

import java.util.Vector

data class GroupDataModel (
    var groupnum : String? = null,
    var leader : String? = null,
    var classname : String? = null,
    var classinfo : String? = null,
    var member : MutableList<String>? = ArrayList()
)