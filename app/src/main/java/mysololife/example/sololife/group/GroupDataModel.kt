package mysololife.example.sololife.group

import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.comment.CommentModel
import java.util.Vector

data class GroupDataModel (
    var groupnum : String? = null,
    var leader : String? = null,
    var classname : String? = null,
    var classinfo : String? = null,
    var member : MutableList<String>? = ArrayList(),
    var board : MutableList<BoardModel>? = ArrayList(),
    var comment : MutableList<CommentModel>? = ArrayList()
)