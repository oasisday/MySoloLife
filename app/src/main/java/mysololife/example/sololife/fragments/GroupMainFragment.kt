package mysololife.example.sololife.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityGroupMainBinding
import com.example.mysololife.databinding.FragmentStudyteamBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import mysololife.example.sololife.MainActivity
import mysololife.example.sololife.auth.introActivity
import mysololife.example.sololife.board.BoardEditActivity
import mysololife.example.sololife.board.BoardInsideActivity
import mysololife.example.sololife.group.GroupDataModel
import mysololife.example.sololife.group.GroupQnAActivity
import mysololife.example.sololife.utils.FBAuth
import mysololife.example.sololife.utils.FBRef
import mysololife.example.sololife.utils.FBboard

class GroupMainFragment : Fragment() {

    private val TAG = GroupMainFragment::class.java.simpleName
    private lateinit var binding: FragmentStudyteamBinding

    private lateinit var key: String
    private lateinit var gname: String

    val myUid = FBAuth.getUid()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStudyteamBinding.inflate(inflater, container, false)

        key = arguments?.getString("amount").toString()
        getBoardData(key)

        binding.groupboardBtn.setOnClickListener {
            val intent = Intent(requireContext(), GroupQnAActivity::class.java)
            intent.putExtra("gname", gname)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        binding.groupOutBtn.setOnClickListener {
            showDialog(myUid)
        }

        return binding.root
    }

    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)

                    binding.studyNameTextView.text = dataModel!!.classname
                    binding.studyInfoTextView.text = dataModel!!.classinfo

                    gname = dataModel!!.classname.toString()
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)
    }

    private fun logoutGroup(uid: String) {
        val memberRef = FBboard.boardInfoRef.child(key).child("member")

        var arr: MutableList<String>? = ArrayList()
        var checked = true

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(GroupDataModel::class.java)
                    arr = dataModel!!.member

                    if (dataModel!!.leader.equals(myUid)) checked = false
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBboard.boardInfoRef.child(key).addValueEventListener(postListener)

        if (checked) {
            arr?.remove(uid)
            memberRef.setValue(arr)
        } else {
            FBboard.boardInfoRef.child(key).removeValue()
        }
    }

    private fun showDialog(uid: String) {
        val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_del, null)
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
            .setTitle("정말 \"$gname\" 그룹에서 탈퇴 하시겠습니까?")

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener {
            Toast.makeText(requireContext(), "\"$gname\" 그룹에서 탈퇴되었습니다.", Toast.LENGTH_LONG).show()
            logoutGroup(uid)
            alertDialog.dismiss()
            //requireActivity().finish()

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}