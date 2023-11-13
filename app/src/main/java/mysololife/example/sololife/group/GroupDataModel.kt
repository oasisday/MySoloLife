package mysololife.example.sololife.group

import mysololife.example.sololife.board.BoardModel
import mysololife.example.sololife.comment.CommentModel
import org.w3c.dom.Comment
import java.util.Vector

data class GroupDataModel (
    var groupnum : String? = null,
    var leader : String? = null,
    var classname : String? = null,
    var classinfo : String? = null,
    var member : MutableList<String>? = ArrayList(),
    //var members : Map<String, Boolean>? = null,
    var boards : Map<String, BoardModel>? = null,
    var comments : Map<String, CommentModel>? = null
)