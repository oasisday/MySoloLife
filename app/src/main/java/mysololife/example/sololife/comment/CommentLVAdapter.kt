package mysololife.example.sololife.comment

import android.app.PendingIntent.getActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

class CommentLVAdapter(val commentList: MutableList<CommentModel>) : BaseAdapter() {


    override fun getCount(): Int {
        return commentList.size
    }

    override fun getItem(position: Int): Any {
        return commentList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if(view == null) {
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.comment_list_item, parent, false)
        }
        val time = view?.findViewById<TextView>(R.id.timeArea)
        val content = view?.findViewById<TextView>(R.id.contentArea)
        val nickname = view?.findViewById<TextView>(R.id.nicknameArea)
        val profile = view?.findViewById<ImageView>(R.id.profileImage)

        val uid = commentList[position].uid

        content!!.text = commentList[position].commentTitle
        time!!.text = commentList[position].commentCreatedTime
        //nickname!!.text = FirebaseRef.userInfoRef.child(FirebaseAuthUtils.getUid()).child("nickname").toString()

        /*
        val storageReference = Firebase.storage.reference.child(uid + ".png")

        // ImageView in your Activity

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                view?.context?.let {
                    if (profile != null) {
                        Glide.with(it)
                            .load(task.result)
                            .into(profile)
                    }
                }

            } else {
            }
        })
        */

        ////////////
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                nickname!!.text = data!!.nickname

                val storageReference = Firebase.storage.reference.child(uid + ".png")
                storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if(task.isSuccessful) {

                        view?.context?.let {
                            if (profile != null) {
                                Glide.with(it)
                                    .load(task.result)
                                    .into(profile)
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
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)



        return view!!
    }


}