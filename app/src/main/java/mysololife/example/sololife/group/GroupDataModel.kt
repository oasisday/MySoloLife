package mysololife.example.sololife.group

import java.util.Vector

data class GroupDataModel (
    var groupnum : String? = null,
    var leader : String? = null,
    var member : Vector<String>? = null
)