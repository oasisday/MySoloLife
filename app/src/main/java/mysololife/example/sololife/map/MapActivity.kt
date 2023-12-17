package mysololife.example.sololife.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.chatlist.ChatActivity
import mysololife.example.sololife.chatlist.ChatRoomItem
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var trackingPersonId: String = ""
    private val markerMap = hashMapOf<String, Marker>()
    private var myname =""
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // fine location 권한이 있다.
                getCurrentLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // coarse Location 권한이 있음
                getCurrentLocation()
            }

            else -> {
                //showLocationPermissionInfoDialog()
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 새로 요청된 위치 정보
            for (location in locationResult.locations) {
                Log.e(
                    "MapActivity",
                    "onLocationResult : ${location.latitude} ${location.longitude}"
                )
                val uid = Firebase.auth.currentUser?.uid.orEmpty()
                val locationMap = mutableMapOf<String, Any>()
                locationMap["latitude"] = location.latitude
                locationMap["longitude"] = location.longitude
                Firebase.database.reference.child("Person").child(uid).updateChildren(locationMap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()
        setupEmojiAnimationView()
        setupCurrentLocationView()
        setupFirebaseDatabase()

        binding.outBtn.setOnClickListener {
            finish()
        }

        binding.makeNotificationButton.setOnClickListener {
            if(trackingPersonId.isNullOrEmpty()){
                Toast.makeText(this,"상대방 아이콘을 선택한 후 버튼을 클릭하세요",Toast.LENGTH_SHORT).show()
            }
            else {
                val myUserId = Firebase.auth.currentUser?.uid ?: ""
                if (myUserId == trackingPersonId) {
                    Toast.makeText(
                        this,
                        "다른 사람의 아이콘을 선택해 주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    getUserTokenByUID(trackingPersonId) { token ->
                        if (token != null) {
                            Toast.makeText(this@MapActivity, "위치 공유를 요청하는 기능은 스터디 팀 내에서 사용하실 수 있습니다:)", Toast.LENGTH_SHORT).show()
//                            // 여기에서 원하는 동작 수행
//                            val client = OkHttpClient()
//                            val root = JSONObject()
//                            val notification = JSONObject()
//                            val message =
//                                "님이 위치공유 요청을 하였습니다.\n클릭을 통해 접속한 해당 액티비티에서만 위치 공유가 진행되고, 해당 액티비티를 벗어나면 어플에서 위치를 추적하지 않습니다."
//                            notification.put("title", "위치 공유 요청")
//                            notification.put("body", "\"$myname\"" + message)
//                            root.put("to", token)
//                            root.put("priority", "high")
//                            root.put("notification", notification)
//                            Log.d("testApi", root.toString())
//                            val requestBody =
//                                root.toString()
//                                    .toRequestBody("application/json; charset=utf-8".toMediaType())
//                            val request =
//                                Request.Builder().post(requestBody)
//                                    .url("https://fcm.googleapis.com/fcm/send")
//                                    .header(
//                                        "Authorization",
//                                        "key=${getString(R.string.fcm_server_key)}"
//                                    )
//                                    .build()
//                            Log.d("testApi", request.toString())
//                            client.newCall(request).enqueue(object : Callback {
//                                override fun onFailure(call: Call, e: IOException) {
//                                    e.stackTraceToString()
//                                }
//
//                                override fun onResponse(call: Call, response: Response) {
//                                }
//                            })
                        }
                    }
                }
            }
        }

        binding.sendMessageButton.setOnClickListener {
            if(trackingPersonId.isNullOrEmpty()){
                Toast.makeText(this,"상대방 아이콘을 선택한 후 버튼을 클릭하세요",Toast.LENGTH_SHORT).show()
            }
            else {
                val myUserId = Firebase.auth.currentUser?.uid ?: ""
                if (myUserId == trackingPersonId) {
                    Toast.makeText(
                        this,
                        "자신의 아이콘이 아닌 상대방의 아이콘을 선택한 후 버튼을 클릭해주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    var otherusername = ""
                    getUserNameByUID(trackingPersonId) { username ->
                        if (!username.isNullOrEmpty()) {
                            val chatRoomDB =
                                Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(myUserId)
                                    .child(trackingPersonId)

                            chatRoomDB.get().addOnSuccessListener {
                                var chatRoomId = ""
                                if (it.value != null) {
                                    //데이터가 존재
                                    val chatRoom = it.getValue(ChatRoomItem::class.java)
                                    chatRoomId = chatRoom?.chatRoomId ?: ""
                                } else {
                                    chatRoomId = UUID.randomUUID().toString()
                                    val newChatRoom = ChatRoomItem(
                                        chatRoomId = chatRoomId,
                                        otherUserName = otherusername,
                                        otherUserId = trackingPersonId,
                                    )
                                    chatRoomDB.setValue(newChatRoom)
                                }

                                val intent = Intent(this, ChatActivity::class.java)
                                intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomId)
                                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, trackingPersonId)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setMaxZoomPreference(20.0f)
        googleMap.setMinZoomPreference(10.0f)
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMapClickListener {
            trackingPersonId = ""
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Glide.with(this).pauseRequests()
    }

    private fun getCurrentLocation() {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5 * 1000)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        //권한이 있는 상태
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        moveLastLocation()
    }

    private fun setupEmojiAnimationView() {
        try {
            binding.emojiLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "emoji"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                DummyAnimation(binding.dummyLottieAnimationView)
                binding.emojiLottieAnimationView.playAnimation()
            }
            binding.heartLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "heart"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.heartLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummyheartLottieAnimationView)
            }
            binding.togetherLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "together"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummytogetherLottieAnimationView)
            }
            binding.chickenLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "chicken"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummychickenLottieAnimationView)
            }
            binding.fiveLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "five"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummyfiveLottieAnimationView)
            }
            binding.sixLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "six"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummysixLottieAnimationView)
            }
            binding.sevenLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "seven"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummysevenLottieAnimationView)
            }

            binding.eightLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "eight"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummyeightLottieAnimationView)
            }

            binding.nineLottieAnimationView.setOnClickListener {
                if (trackingPersonId != "") {
                    val EmojiChange = mutableMapOf<String, Any>()
                    EmojiChange["type"] = "nine"
                    EmojiChange["lastModifier"] = convertMillisToDateString(System.currentTimeMillis(), "MM월 dd일 HH시 mm분 ss.SSS초")
                    Firebase.database.reference.child("Emoji").child(trackingPersonId).child(trackingPersonId)
                        .updateChildren(EmojiChange)
                }
                binding.togetherLottieAnimationView.playAnimation()
                DummyAnimation(binding.dummynineLottieAnimationView)
            }

            binding.emojiLottieAnimationView.speed = 3f
            binding.centerLottieAnimationView.speed = 3f
        }catch (e:Exception){
            Log.d("testemoji",e.toString())
        }
    }

    private fun DummyAnimation(dummy: LottieAnimationView) {
        dummy.animate()
            .scaleX(3f)
            .scaleY(3f)
            .alpha(0f)
            .withStartAction {
                dummy.scaleX = 1f
                dummy.scaleY = 1f
                dummy.alpha = 1f
            }.withEndAction {
                dummy.scaleX = 1f
                dummy.scaleY = 1f
                dummy.alpha = 1f
            }.start()
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun moveLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16.0f)
            )
        }
    }

    private fun setupCurrentLocationView() {
        binding.currentLocationButton.setOnClickListener {
            trackingPersonId = ""
            moveLastLocation()
        }
    }

    private fun setupFirebaseDatabase() {
        Firebase.database.reference.child("Person")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val person = snapshot.getValue(Person::class.java) ?: return
                    val uid = person.uid ?: return
                    if (markerMap[uid] == null) {
                        markerMap[uid] = makeNewMarker(person, uid) ?: return
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val person = snapshot.getValue(Person::class.java) ?: return
                    val uid = person.uid ?: return

                    if (markerMap[uid] == null) {
                        markerMap[uid] = makeNewMarker(person, uid) ?: return
                    } else {
                        markerMap[uid]?.position =
                            LatLng(person.latitude ?: 0.0, person.longitude ?: 0.0)
                    }
                    if (uid == trackingPersonId) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(person.latitude ?: 0.0, person.longitude ?: 0.0))
                                    .zoom(16.0f)
                                    .build()
                            )
                        )
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })

        Firebase.database.reference.child("Emoji").child(Firebase.auth.currentUser?.uid ?: "")
            .addChildEventListener(object : ChildEventListener {
                private var currentAnimationResId: Int = 0
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    var type =""
                    try {
                        val emoji = snapshot.getValue(Emoji::class.java)
                        type = emoji?.type ?: return
                    } catch (e: Exception) {
                        Log.e("testing", e.toString())
                    }
                    val animationResId = when (type) {
                        "emoji" -> R.raw.emoji_star
                        "together" -> R.raw.walk_together
                        "heart" -> R.raw.heart
                        "chicken" -> R.raw.chicken
                        "five" -> R.raw.five_animation
                        "six" -> R.raw.six_animation
                        "seven" -> R.raw.seven_animation
                        "eight" -> R.raw.eight_animation
                        "nine" -> R.raw.nine_animation
                        else -> R.raw.emoji_star
                    }
                    if (currentAnimationResId != animationResId) {
                        // 애니메이션 리소스를 설정합니다.
                        binding.centerLottieAnimationView.setAnimation(animationResId)

                        // 현재 애니메이션 리소스 ID를 업데이트합니다.
                        currentAnimationResId = animationResId
                    }

                    binding.centerLottieAnimationView.playAnimation()
                    binding.centerLottieAnimationView.animate()
                        .scaleX(7f)
                        .scaleY(7f)
                        .alpha(0.3f)
                        .setDuration(binding.centerLottieAnimationView.duration / 3)
                        .withEndAction {
                            binding.centerLottieAnimationView.scaleX = 0f
                            binding.centerLottieAnimationView.scaleY = 0f
                            binding.centerLottieAnimationView.alpha = 1f
                        }.start()
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private fun makeNewMarker(person: Person, uid: String): Marker? {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(person.latitude ?: 0.0, person.longitude ?: 0.0))
                .title(person.name.orEmpty())
        ) ?: return null

        //이름 추가
        if(uid == Firebase.auth.currentUser?.uid ?: ""){
            myname = person.name.orEmpty()
        }
        //tag 설정
        marker.tag = uid



        if(!isFinishing()) {
            Glide.with(this).asBitmap()
                .load(person.profilePhoto)
                .transform(RoundedCorners(60))
                .override(150)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val resizedBitmap = Bitmap.createScaledBitmap(resource, 120, 120, false)
                        resource?.let {
                            runOnUiThread {
                                marker.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(resizedBitmap)
                                )
                            }
                        }
                        return true
                    }
                }).submit()
        }
        return marker
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        trackingPersonId = marker.tag as? String ?: ""

        val bottomSheetBitmap = BottomSheetBehavior.from(binding.horizontalView)
        bottomSheetBitmap.state = BottomSheetBehavior.STATE_EXPANDED
        return false
    }
    private fun showLocationPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("위치 권한을 허용하세요")
            setMessage("앱의 위치 공유 기능을 위해서 권한을 허용해야 합니다.")
            setNegativeButton("거절", null)
            setPositiveButton("동의") { _, _ ->
                requestLocationPermission()
            }
        }.show()
    }

    private fun convertMillisToDateString(millis: Long, pattern: String): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        val date = Date(millis)
        return formatter.format(date)
    }

    private fun getUserNameByUID(uid: String, callback: (String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java)
                Log.d("maptest", "$username check")
                callback(username)
            } else {
                callback(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("maptest", "Failed to read data from database: $exception")
            callback(null)
        }
    }

    private fun getUserTokenByUID(uid: String, callback: (String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val token = snapshot.child("fcmToken").getValue(String::class.java)
                callback(token)
            } else {
                callback(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("maptest", "Failed to read data from database: $exception")
            callback(null)
        }
    }
}