package mysololife.example.sololife.message

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.Constants.Companion.LOGCHECK
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseRef
class ListViewAdapter(
    private val context: Context,
    private val items: MutableList<UserDataModel>,
    private val onClick: (UserDataModel) -> Unit
) : RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {

    var ischeck = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(items[position], onClick)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nickname: TextView = itemView.findViewById(R.id.listViewItemNickname)
        private val checkBox: CheckBox = itemView.findViewById(R.id.likeCheckbox)
        private val profile: ImageView? = itemView.findViewById(R.id.profileImg)
        fun bind(item: UserDataModel, onClick: (UserDataModel) -> Unit) {
            nickname.text = item.nickname
            if (ischeck) {
                checkBox.visibility = View.GONE
            }
            checkBox.isChecked = item.ischecked
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.ischecked = isChecked
            }

            val uid = item.uid

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.getValue(UserDataModel::class.java)
                    nickname.text = data!!.nickname

                    val storageReference = Firebase.storage.reference.child(uid + ".png")
                    storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            profile?.let {
                                Glide.with(context)
                                    .load(task.result)
                                    .into(it)
                            }
                        } else {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            }

            uid?.let {
                FirebaseRef.userInfoRef.child(it).addValueEventListener(postListener)
            }

            itemView.setOnClickListener {
                Log.d(LOGCHECK, adapterPosition.toString())
                onClick.invoke(item)
            }
        }
    }
    fun getSelectedItems(): List<UserDataModel> {
        return items.filter { it.ischecked }
    }
}
