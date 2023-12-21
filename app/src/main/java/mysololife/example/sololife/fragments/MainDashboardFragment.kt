package mysololife.example.sololife.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainDashboardBinding
import com.islandparadise14.mintable.model.ScheduleEntity
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogAnimation
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.aestheticdialogs.OnDialogClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mysololife.example.sololife.dashboard.DashboardAdapter
import mysololife.example.sololife.dashboard.OnItemClickListener
import mysololife.example.sololife.timetable.AppDatabase
import mysololife.example.sololife.timetable.InfoEntity
import mysololife.example.sololife.timetable.toScheduleEntity
import java.util.Random


class MainDashboardFragment : Fragment(), OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainDashboardBinding
    private var lectureList = mutableListOf<InfoEntity>()
    private lateinit var mContext:Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMainDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findNavController()?.popBackStack(R.id.maindashboardFragment, false)
        binding.plusBtn.setOnClickListener {
            view?.findNavController()
                ?.navigate(com.example.mysololife.R.id.action_maindashboardFragment_to_lectureInitFragment)
        }
    }

    private fun initializeRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(mContext, 2)
        recyclerView.adapter = DashboardAdapter(lectureList, this)
        recyclerView.adapter?.notifyDataSetChanged()

        if (lectureList.isEmpty()) {
            binding.animationView.visibility = View.VISIBLE
            binding.animationView.setAnimation(com.example.mysololife.R.raw.nofile_animation)
            binding.animationView.playAnimation();
            binding.textView3.text = "아이콘을 눌러 강의를 등록하세요"
            binding.textView3.textSize = 16f
            binding.animationView.setOnClickListener {
                view?.findNavController()
                    ?.navigate(com.example.mysololife.R.id.action_maindashboardFragment_to_lectureInitFragment)
            }
        } else {
            binding.animationView.visibility = View.GONE
            binding.textView3.text = "과목"
            binding.textView3.textSize = 32f
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        Log.d("testhi","here")
        lectureList.clear()
        Thread {
            val infoEntities =
                AppDatabase.getInstance(mContext)?.infoDao()?.getAllLecture() ?: emptyList()
            lectureList.addAll(infoEntities)
            activity?.runOnUiThread {
                initializeRecyclerView()
            }
        }.start()
    }

    override fun onItemClickListener(position: Int) {
        val bundle = bundleOf("lecturename" to lectureList[position].scheduleName)
        view?.findNavController()
            ?.navigate(com.example.mysololife.R.id.action_maindashboardFragment_to_lectureMainFragment, bundle)
    }

    override fun onButtonClickListener(position: Int, itemName: String, itemView: View) {
        val scheduleList: ArrayList<ScheduleEntity> = ArrayList()
        Thread {
            val infoEntities =
                AppDatabase.getInstance(mContext)?.infoDao()?.getAll() ?: emptyList()
            val scheduleEntities = infoEntities.map { it.toScheduleEntity() }
            scheduleList.addAll(scheduleEntities.asReversed())

            if (position < scheduleList.size) {
                requireActivity().runOnUiThread {
                    showPopupMenu(itemView, itemName)
                    Log.d("deletetest", "$itemName 클릭")
                }
            } else {
                Log.e("deletetest", "Invalid position: $position")
            }
        }.start()
    }


    private fun showPopupMenu(view: View, name: String) {
        val popupMenu = PopupMenu(mContext, view)
        popupMenu.menuInflater.inflate(com.example.mysololife.R.menu.secondary_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.editbutton -> {

                        val randomHexCode = generateRandomHexCode()
                        Thread {
                                AppDatabase.getInstance(mContext)?.infoDao()?.updateColor(name,randomHexCode)
                        }.start()
                    Thread.sleep(500)
                    lectureList.clear()
                    Thread {
                        val infoEntities =
                            AppDatabase.getInstance(mContext)?.infoDao()?.getAllLecture() ?: emptyList()
                        lectureList.addAll(infoEntities)

                        activity?.runOnUiThread {
                            initializeRecyclerView()
                        }
                    }.start()
                    true
                }

                R.id.deletebtn -> {
                    val nameToDelete = name // 삭제하고 싶은 아이템의 이름
                    delete(nameToDelete)
                        // 데이터베이스에서 아이템을 다시 불러오기
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun delete(lecturename: String) {
        requireActivity().runOnUiThread {
            AestheticDialog.Builder(requireActivity(), DialogStyle.FLAT, DialogType.WARNING)
                .setTitle("해당 과목을 삭제하시겠습니까?")
                .setMessage("만약 삭제하시려면 해당 과목의 모든 녹음 예약 알림을 먼저 직접 삭제한 후 확인 버튼을 누르시고, 과목 삭제를 진행해주세요!")
                .setCancelable(true)
                .setDarkMode(false)
                .setGravity(Gravity.CENTER)
                .setAnimation(DialogAnimation.SHRINK)
                .setOnClickListener(object : OnDialogClickListener {
                    override fun onClick(dialog: AestheticDialog.Builder) {
                        dialog.dismiss()
                        // Perform database operation in a background thread
                        GlobalScope.launch(Dispatchers.IO) {
                            // Access the database on a background thread
                            AppDatabase.getInstance(mContext)?.infoDao()?.deleteByScheduleName(lecturename)
                            // Update UI on the main thread
                            launch(Dispatchers.Main) {
                                Toast.makeText(mContext, "과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                        lectureList.clear()
                        Thread{
                            activity?.runOnUiThread {
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                        }.start()
                        Thread.sleep(500)
                        Thread {
                            val infoEntities =
                                AppDatabase.getInstance(mContext)?.infoDao()?.getAllLecture() ?: emptyList()
                            lectureList.addAll(infoEntities)

                            activity?.runOnUiThread {
                                initializeRecyclerView()
                            }
                        }.start()
                    }
                })
                .show()
        }
    }
    fun generateRandomHexCode(): String {
        val chars = "0123456789ABCDEF"
        val random = Random()
        val hex = StringBuilder()

        for (i in 0 until 6) {
            val index = random.nextInt(chars.length)
            hex.append(chars[index])
        }

// Ensure that there are no 'O' characters in the generated hex string
        val sanitizedHex = hex.toString().replace("O", "0")

        return "#" + sanitizedHex
    }
}

