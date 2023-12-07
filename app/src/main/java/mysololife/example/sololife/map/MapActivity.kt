package mysololife.example.sololife.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var trackingPersonId: String = ""
    private val markerMap = hashMapOf<String, Marker>()

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
        binding.currentLocationSaveButton.setOnClickListener {
            Toast.makeText(this, "현재 위치를 저장했습니다.", Toast.LENGTH_SHORT)
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
        val lastEmoji = mutableMapOf<String, Any>()
        val uid = Firebase.auth.currentUser?.uid.orEmpty()
        binding.emojiLottieAnimationView.setOnClickListener {
            if (trackingPersonId != "") {
                lastEmoji["type"] = "emoji"
                lastEmoji["lastModifier"] = System.currentTimeMillis()
                Firebase.database.reference.child("Emoji").child(trackingPersonId)
                    .updateChildren(lastEmoji)
            }
            DummyAnimation(binding.dummyLottieAnimationView)
            binding.emojiLottieAnimationView.playAnimation()
        }
        binding.heartLottieAnimationView.setOnClickListener {
            if (trackingPersonId != "") {
                lastEmoji["type"] = "heart"
                lastEmoji["lastModifier"] = System.currentTimeMillis()
                Firebase.database.reference.child("Emoji").child(trackingPersonId)
                    .updateChildren(lastEmoji)
            }
            binding.heartLottieAnimationView.playAnimation()
            DummyAnimation(binding.dummyheartLottieAnimationView)
        }
        binding.togetherLottieAnimationView.setOnClickListener {
            if (trackingPersonId != "") {
                lastEmoji["type"] = "together"
                lastEmoji["lastModifier"] = System.currentTimeMillis()
                Firebase.database.reference.child("Emoji").child(trackingPersonId)
                    .updateChildren(lastEmoji)
            }
            binding.togetherLottieAnimationView.playAnimation()
            DummyAnimation(binding.dummytogetherLottieAnimationView)
        }
        binding.emojiLottieAnimationView.speed = 3f
        binding.centerLottieAnimationView.speed = 3f
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
                        val emoji = snapshot.getValue(Emoji::class.java)?.type ?: return
                        type = emoji
                        Log.d("testemoji", type)
                    } catch (e: Exception) {
                        Log.e("testing", e.toString())
                    }
                    val animationResId = when (type) {
                        "emoji" -> R.raw.emoji_star
                        "together" -> R.raw.walk_together
                        "heart" -> R.raw.heart
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

        //tag 설정
        marker.tag = uid

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
                    resource?.let {
                        runOnUiThread {
                            marker.setIcon(
                                BitmapDescriptorFactory.fromBitmap(resource)
                            )
                        }
                    }
                    return true
                }
            }).submit()
        return marker
    }



    override fun onMarkerClick(marker: Marker): Boolean {
        trackingPersonId = marker.tag as? String ?: ""

        val bottomSheetBitmap = BottomSheetBehavior.from(binding.emojiBottomSheetLayout)
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
}