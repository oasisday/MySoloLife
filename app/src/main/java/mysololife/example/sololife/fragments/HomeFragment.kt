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
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentHomeBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import mysololife.example.sololife.CameraActivity
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.auth.UserInfoModel
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupMainActivity
import mysololife.example.sololife.group.GroupboardLVAdapter
import mysololife.example.sololife.message.MyLikeListActivity
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.TimeTableActivity
import mysololife.example.sololife.translator.TranslateActivity
import mysololife.example.sololife.ui.OnItemClickListener
import mysololife.example.sololife.ui.StudyTeamAdapter
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(),OnItemClickListener{
    lateinit var binding: FragmentHomeBinding
    private val uid = FirebaseAuthUtils.getUid()
    private val boardDataList = mutableListOf<GroupDataModel>()
    private val boardKeyList = mutableListOf<String>()
    private lateinit var studyteamAdapter: StudyTeamAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        getFBBoardData()
        binding.makestudyBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_myLikeListFragment)
        }
        binding.profileImage.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_mypageFragment)
        }
        binding.matchingBtn.setOnClickListener{
            val intent = Intent(context, Matching::class.java)
            startActivity(intent)
        }
        binding.addfriendBtn.setOnClickListener {
            addDialog()
        }
        return binding.root
    }

    private fun getFBBoardData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d("homefragment", dataModel.toString())
                    val item = dataModel.getValue(GroupDataModel::class.java)

                    if (item != null) {
                        if(item.member?.contains(FBAuth.getUid()) == true){
                            boardDataList.add(item!!)
                            boardKeyList.add(dataModel.key.toString())
                        }
                    }

                }

                boardKeyList.reverse()
                boardDataList.reverse()
                Log.d("homefragment", boardDataList.toString())
                studyteamAdapter = StudyTeamAdapter(boardDataList,this@HomeFragment)
                binding.studyteamrecyclerView.apply {
                    adapter = studyteamAdapter
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("homefragment", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.addValueEventListener(postListener)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVoiceRecorder.setOnClickListener {
            Intent(getActivity(),RecorderMainActivity::class.java).apply{
                startActivity(this)
            }
        }
        binding.btneveryoneboard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_groupMakeFragment)
        }

        binding.btnTimeTable.setOnClickListener {
            Intent(getActivity(), TimeTableActivity::class.java).apply{
                startActivity(this)
            }
        }

        binding.btnCamera.setOnClickListener {
            Intent(getActivity(), CameraActivity::class.java).apply{
                startActivity(this)
            }
        }

        binding.btnTranslate.setOnClickListener {
            Intent(getActivity(), TranslateActivity::class.java).apply{
                startActivity(this)
            }
        }
        getMyData()
    }

    private fun getMyData() {
        val myImage = binding.profileImage
        val myNickname = binding.nicknameTextView
        val requestOptions = RequestOptions().circleCrop()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)
                myNickname.text = data!!.nickname+" 님"
                val storageRef = Firebase.storage.reference.child(data!!.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(getActivity() !=null){
                            Glide.with(this@HomeFragment)
                                .load(task.result)
                                .apply(requestOptions)
                                .into(myImage)}
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    override fun onItemClickListener(position: Int) {
        val bundle = bundleOf("amount" to boardKeyList[position])
        view?.findNavController()?.navigate(R.id.action_homeFragment_to_groupMainFragment,bundle)
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

                        if(email == data!!.email){
                            FirebaseRef.userLikeRef.child(currentUser.toString()).child(data!!.uid.toString()).setValue("true")

                            userLikeOtherUser(currentUser.toString(),data!!.uid.toString())

                            Log.d("aaaa", data!!.nickname.toString())
                            name = data!!.nickname.toString()
                            check = false
                        }
                    }

                    if(check) Toast.makeText(activity,"존재하지 않는 E-mail 입니다.", Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(activity,name+"님을 친구로 추가하였습니다.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
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