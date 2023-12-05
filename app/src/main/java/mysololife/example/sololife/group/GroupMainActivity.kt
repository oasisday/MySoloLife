package mysololife.example.sololife.group

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityGroupMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard

class GroupMainActivity : AppCompatActivity() {

    private val TAG = GroupMainActivity::class.java.simpleName
    private lateinit var binding : ActivityGroupMainBinding

    private lateinit var key:String
    private lateinit var gname:String

    val myUid = FBAuth.getUid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_group_main)

        key = intent.getStringExtra("key").toString()
        getBoardData(key)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_main)


        binding.groupboardBtn.setOnClickListener{
            //startActivity(Intent(this, GroupQnAActivity::class.java))
            val intent = Intent(this, GroupQnAActivity::class.java)
            intent.putExtra("gname",gname)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        //로그 아웃 기능//
        binding.groupLogout.setOnClickListener{
            //logout_group(myUid)
            showDialog(myUid)
        }
    }

    private fun getBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)

                    binding.studyName.text = dataModel!!.classname
                    binding.studyInfo.text = dataModel!!.classinfo

                    gname = dataModel!!.classname.toString()
                }
                catch (e:Exception){
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)
    }
    private fun logoutGroup(uid : String) {
        val memberRef = FBboard.boardInfoRef.child(key).child("member")
        Log.d("delll", memberRef.child(uid).toString())

        var arr: MutableList<String>? = ArrayList()
        var checked = true

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)
                    arr = dataModel!!.member

                    if (dataModel!!.leader.equals(myUid)) checked = false
                } catch (e: Exception) {
                    Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)

        if (checked) {
            arr?.remove(uid)
            memberRef.setValue(arr)
        }
        else{
            Toast.makeText(this,"hi",Toast.LENGTH_SHORT).show()
            FBboard.boardInfoRef.child(key).removeValue()
        }
    }
    private fun showDialog(uid : String){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_del, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("정말 "+gname+" 에서 탈퇴 하시겠습니까?")

        val alertDialog = mBuilder.show()

        //탈퇴//
        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener{
            Toast.makeText(this, "\""+gname+"\""+" 그룹에서 탈퇴되었습니다.", Toast.LENGTH_LONG).show()
            logoutGroup(uid)
            finish()
        }

        //안함//
        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener{
            finish()
        }
        //mBuilder.show()
    }

}