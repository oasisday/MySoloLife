package mysololife.example.sololife.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMyPageBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.auth.UserInfoModel
import mysololife.example.sololife.message.MyLikeListActivity
import mysololife.example.sololife.message.MyMsgActivity
import mysololife.example.sololife.setting.SettingActivity
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef
import mysololife.example.sololife.utils.FirebaseRef.Companion.userLikeRef

class MyPageFragment : Fragment() {

    private val TAG = "MyPageFragment"
    private val uid = FirebaseAuthUtils.getUid()

    private lateinit var binding: ActivityMyPageBinding

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        binding.profileeditBtn.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.likelistBtn.setOnClickListener {
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }

        binding.msgBtn.setOnClickListener {
            val intent = Intent(activity, MyMsgActivity::class.java)
            startActivity(intent)
        }

        binding.plusBtn.setOnClickListener{
            /*
            val email = binding.getEmail.text.toString()

            val current_user = Firebase.auth.currentUser?.uid

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString())

                    for (dataModel in dataSnapshot.children){
                        val data = dataModel.getValue(UserInfoModel::class.java)

                        if(email == data!!.email){
                            userLikeRef.child(current_user.toString()).child(data!!.uid.toString()).setValue("true")
                            Log.d("aaaa", data!!.nickname.toString())
                            Toast.makeText(activity,data!!.nickname+"님을 친구로 추가하였습니다.",Toast.LENGTH_SHORT).show()
                        }

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }

            FirebaseRef.userDataRef.addValueEventListener(postListener)
*/

            addDialog()


        }

        getMyData()
    }

    private fun getMyData() {
        //val myUid = binding.myUid
        val myNickname = binding.myNickname
        val myGender = binding.myGender
        val myImage = binding.myImage
        val myInfo = binding.myInfo

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                //myUid.text = data!!.uid
                myNickname.text = data!!.nickname
                myGender.text = data!!.gender
                myInfo.text = data!!.info

                if(myInfo.text == ""){
                    myInfo.text = "자기소개를 해주세요:)"
                }

                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(getActivity() !=null){
                        Glide.with(this@MyPageFragment)
                            .load(task.result)
                            .into(myImage)}
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }


    private fun addDialog(){

        var check = true

        var name = ""

        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_add, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
            .setTitle("E-mail로 친구추가")

        val alertDialog = mBuilder.show()
        val getTxt = alertDialog.findViewById<EditText>(R.id.getEmail)

        alertDialog.findViewById<Button>(R.id.plusBtn)?.setOnClickListener{


            val email = getTxt.text.toString()

            val currentUser = Firebase.auth.currentUser?.uid

            Log.d("aaa",email)

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (dataModel in dataSnapshot.children){
                        val data = dataModel.getValue(UserInfoModel::class.java)
                        Log.d(TAG, data.toString())

                        if(email == data!!.email){
                            userLikeRef.child(currentUser.toString()).child(data!!.uid.toString()).setValue("true")

                            userLikeOtherUser(currentUser.toString(),data!!.uid.toString())

                            Log.d("aaaa", data!!.nickname.toString())
                            name = data!!.nickname.toString()
                            check = false
                        }
                    }

                    if(check) Toast.makeText(activity,"존재하지 않는 E-mail 입니다.", Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(activity,name+"님을 친구로 추가하였습니다.",Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }

            FirebaseRef.userDataRef.addValueEventListener(postListener)
            //finish()

        }



        //mBuilder.show()
    }
    private fun userLikeOtherUser(myUid : String, otherUid : String){
        FirebaseRef.userLikeRef.child(uid).child(otherUid).setValue("true")

        getOtherUserLikeList(otherUid)
    }
    private fun getOtherUserLikeList(otherUid: String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    val likeUserKey = dataModel.key.toString()
                    if(likeUserKey.equals(uid)){
                        Toast.makeText(activity,"matching success!!", Toast.LENGTH_SHORT).show()
                        createNotificationChannel()
                        sendNotification()
                    }

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_ch", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(){
        var builder = activity?.let {
            NotificationCompat.Builder(it, "Test_ch")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Study Matching")
                .setContentText("새로운 스터디원과 연결되었습니다. 확인해보세요!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        if (builder != null) {
            with(activity?.let { NotificationManagerCompat.from(it) }){
                if (activity?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                this?.notify(123,builder.build())
            }
        }
    }
}