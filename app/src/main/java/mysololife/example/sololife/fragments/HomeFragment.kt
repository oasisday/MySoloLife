package mysololife.example.sololife.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.mysololife.R
import com.example.mysololife.databinding.FragmentHomeBinding
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogAnimation
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.aestheticdialogs.OnDialogClickListener
import mysololife.example.sololife.CameraActivity
import mysololife.example.sololife.Constants
import mysololife.example.sololife.Constants.Companion.LOGCHECK
import mysololife.example.sololife.Constants.Companion.TUTORIAL
import mysololife.example.sololife.Constants.Companion.TUTORIAL_DONE
import mysololife.example.sololife.Matching
import mysololife.example.sololife.auth.LoginActivity
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.auth.UserInfoModel
import mysololife.example.sololife.chatlist.ChatActivity2
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.map.MapActivity
import mysololife.example.sololife.recorder.RecorderMainActivity
import mysololife.example.sololife.timetable.TimeTableActivity
import mysololife.example.sololife.translator.TranslateActivity
import mysololife.example.sololife.ui.OnItemClickListener
import mysololife.example.sololife.ui.PopupManager
import mysololife.example.sololife.ui.StudyTeamAdapter
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBboard
import mysololife.example.sololife.utils.FirebaseAuthUtils
import mysololife.example.sololife.utils.FirebaseRef


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
        binding.tutorialBtn.setOnClickListener {
            Toast.makeText(requireContext(),"튜토리얼을 시작합니다.", Toast.LENGTH_SHORT).show()
            with(requireActivity().getSharedPreferences(TUTORIAL, Context.MODE_PRIVATE).edit()) {
                putBoolean(TUTORIAL_DONE, false)
                apply()
            }
            PopupManager(this, binding).showViewTypePrompt()
        }
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
        binding.chatlistBtn.setOnClickListener {
            //addDialog()
            //친구목록 프래그먼트로 가기
            view?.findNavController()?.navigate(R.id.action_homeFragment_to_chatListFragment)
        }
        binding.locationshare.setOnClickListener {
            AestheticDialog.Builder(requireActivity(), DialogStyle.FLAT, DialogType.WARNING)
                .setTitle("위치 공유 기능 설명")
                .setMessage("버튼 클릭시 수동으로 기능이 활성화되며, 활성화된 사용자들 간에만 위치가 공유됩니다. 다른 화면으로 이동시 자동으로 비활성화됩니다.")
                .setCancelable(true)
                .setDarkMode(false)
                .setGravity(Gravity.CENTER)
                .setAnimation(DialogAnimation.SHRINK)
                .setOnClickListener(object : OnDialogClickListener {
                    override fun onClick(dialog: AestheticDialog.Builder) {
                        dialog.dismiss()

                        val intent = Intent(context,MapActivity::class.java)
                        startActivity(intent)
                    }
                })
                .show()
        }

        binding.chatBtn.setOnClickListener {
            val intent = Intent(context, ChatActivity2::class.java)
            intent.putExtra(ChatActivity2.EXTRA_CHAT_ROOM_ID,"allchatting_room")
            intent.putExtra(ChatActivity2.EXTRA_OTHER_USER_ID,"전체 채팅방")
            startActivity(intent)
        }
        return binding.root
    }
    private fun requestLocationPermission() {
        requestPermissionLauncher
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
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
                Log.d(LOGCHECK,boardDataList.toString())
                studyteamAdapter = StudyTeamAdapter(boardDataList,this@HomeFragment)
                binding.studyteamrecyclerView.apply {
                    adapter = studyteamAdapter
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("homefragment", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.addValueEventListener(postListener)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askNotificationPermission()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 버튼을 눌렀을 때 수행할 동작
                showExitConfirmation()

            }
        }

       PopupManager(this,binding).showViewTypePrompt()
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

    private fun showExitConfirmation() {
        AestheticDialog.Builder(requireActivity(), DialogStyle.FLAT, DialogType.INFO)
            .setTitle("앱 종료 확인")
            .setMessage("앱을 종료하려면 아래의 확인 버튼을 클릭하세요. 그렇지 않으면 화면의 다른 부분을 눌러주세요.")
            .setCancelable(true)
            .setDarkMode(false)
            .setGravity(Gravity.CENTER)
            .setAnimation(DialogAnimation.FADE)
            .setOnClickListener(object : OnDialogClickListener {
                override fun onClick(dialog: AestheticDialog.Builder) {
                    dialog.dismiss()
                    requireActivity().finish()
                }
            })
            .show()
    }

    private fun getMyData() {
        val myImage = binding.profileImage
        val myNickname = binding.nicknameTextView
        val requestOptions = RequestOptions().circleCrop()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    val data = dataSnapshot.getValue(UserDataModel::class.java)
                    if (data != null) {
                        myNickname.text = data.nickname + " 님"
                        val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                        storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                    if (getActivity() != null) {
                                        try {
                                            Glide.with(this@HomeFragment)
                                                .load(task.result)
                                                .apply(requestOptions)
                                                .listener(object : RequestListener<Drawable> {
                                                    override fun onLoadFailed(
                                                        e: GlideException?,
                                                        model: Any?,
                                                        target: Target<Drawable>,
                                                        isFirstResource: Boolean
                                                    ): Boolean {
                                                        binding.loadingProgressBar.visibility =
                                                            View.GONE // 로딩 실패 시 ProgressBar 숨김
                                                        return false
                                                    }

                                                    override fun onResourceReady(
                                                        resource: Drawable,
                                                        model: Any,
                                                        target: Target<Drawable>?,
                                                        dataSource: DataSource,
                                                        isFirstResource: Boolean
                                                    ): Boolean {
                                                        // target이 null이 아닌 경우에만 로딩 완료 시 ProgressBar를 숨김
                                                        if (target != null) {
                                                            binding.loadingProgressBar.visibility =
                                                                View.GONE
                                                        }
                                                        return false
                                                    }
                                                })
                                                .into(myImage)
                                        } catch (e: Exception) {
                                            binding.loadingProgressBar.visibility = View.GONE
                                        }
                                    }
                            }
                            else{
                                binding.loadingProgressBar.visibility = View.GONE
                            }
                        })
                    } else {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_LONG).show()

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    binding.loadingProgressBar.visibility = View.GONE
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
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
                }
            }

            FirebaseRef.userDataRef.addValueEventListener(postListener)
        }
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

                    return
                }
                this?.notify(123,builder.build())
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationalDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // 알림권한 없음
        }
    }
}