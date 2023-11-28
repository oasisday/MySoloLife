package mysololife.example.sololife.message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseRef

class ListViewAdapter(val context : Context, val items : MutableList<UserDataModel>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        if(convertView == null){
            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.list_view_item,parent, false)

        }

        val nickname = convertView!!.findViewById<TextView>(R.id.listViewItemNickname)
        nickname.text = items[position].nickname
        val checkBox: CheckBox = convertView!!.findViewById<CheckBox>(R.id.likeCheckbox)
        checkBox.isChecked = items[position].ischecked
        checkBox.setOnCheckedChangeListener { _,isChecked ->
            items[position].ischecked= isChecked
        }
        val profile = convertView?.findViewById<ImageView>(R.id.profileImg)


        val uid = items[position].uid


        ///////////////////

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                nickname!!.text = data!!.nickname

                val storageReference = Firebase.storage.reference.child(uid + ".png")
                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if(task.isSuccessful) {

                        convertView?.context?.let {
                            if(context != null) {
                                if (profile != null) {
                                    Glide.with(it)
                                        .load(task.result)
                                        .into(profile)
                                }
                            }
                        }

                    } else {

                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        if (uid != null) {
            FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
        }
        return convertView!!
    }

    fun getSelectedItems(): List<UserDataModel>{
        return items.filter {it.ischecked}
    }

}