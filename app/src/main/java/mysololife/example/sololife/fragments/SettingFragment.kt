package mysololife.example.sololife.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivitySettingBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import mysololife.example.sololife.auth.Key
import mysololife.example.sololife.auth.LoginActivity
import mysololife.example.sololife.auth.UserDataModel
import mysololife.example.sololife.utils.FirebaseRef
import java.io.ByteArrayOutputStream

class SettingFragment : Fragment() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth : FirebaseAuth

    private var nickname = ""
    private var gender = ""
    private var info = ""

    //이미 쓴 게 있으면//
    private lateinit var writerUid : String
    private lateinit var key:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivitySettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        val user = auth.currentUser
        key = user?.uid.toString()
        getBoardData(key)
        getImageData(key)

        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                binding.profileImage.setImageURI(uri)
            }
        )

        binding.profileImage.setOnClickListener{
            getAction.launch("image/*")
        }

        binding.LogoutBtn.setOnClickListener{
            auth.signOut()
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_LONG).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }


        binding.SettingBtn.setOnClickListener{
            try{
                gender = binding.genderArea.text.toString()
                nickname = binding.nicknameArea.text.toString()
                info = binding.infoArea.text.toString()

                val currentUserId = Firebase.auth.currentUser?.uid ?:""
                val currentUserDB = Firebase.database.reference.child(Key.DB_USERS).child(currentUserId)


                val userModel = UserDataModel(
                    uid = currentUserId,
                    nickname = nickname,
                    gender =gender,
                    info = info
                )

                val user = mutableMapOf<String,Any>()
                user["username"] = nickname
                user["descriotion"] = info
                currentUserDB.updateChildren(user)

                FirebaseRef.userInfoRef.child(currentUserId).setValue(userModel)

                uploadImage(currentUserId)

                Toast.makeText(requireContext(),"파이어베이스에 사진이 올라갈 때 약간의 시간이 소요됩니다.\n 새로고침시 적용됩니다 :)", Toast.LENGTH_LONG).show()
                view?.findNavController()?.navigate(R.id.action_settingFragment_to_mypageFragment)
                requireActivity().supportFragmentManager.beginTransaction()
                    .remove(this)
                    .commit()
            }

            catch (e:Exception){
            }
        }

    }
    private fun uploadImage(uid : String){

        val storage = Firebase.storage
        val storageRef = storage.reference.child("$uid.png")

        // Get the data from an ImageView as bytes
        binding.profileImage.isDrawingCacheEnabled = true
        binding.profileImage.buildDrawingCache()
        val bitmap = (binding.profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
        }.addOnSuccessListener { taskSnapshot ->

        }


    }

    private fun getImageData(uid : String){
        val storageReference = Firebase.storage.reference.child("$uid.png")
        val imageViewFromFB = binding.profileImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })
    }

    private fun getBoardData(uid : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val dataModel = dataSnapshot.getValue(UserDataModel::class.java)

                binding.nicknameArea.setText(dataModel?.nickname)
                binding.genderArea.setText((dataModel?.gender))
                binding.infoArea.setText((dataModel?.info))
                writerUid = dataModel!!.uid.toString()

            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}