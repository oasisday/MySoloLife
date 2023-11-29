package mysololife.example.sololife.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityMainDashboardBinding
import com.islandparadise14.mintable.model.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import mysololife.example.sololife.dashboard.DashboardAdapter
import mysololife.example.sololife.dashboard.LectureMainActivity
import mysololife.example.sololife.dashboard.OnItemClickListener
import mysololife.example.sololife.timetable.AppDatabase
import mysololife.example.sololife.timetable.InfoEntity
import mysololife.example.sololife.timetable.LectureInitActivity
import mysololife.example.sololife.timetable.toScheduleEntity

class MainDashboardFragment : Fragment(), OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainDashboardBinding
    private var lectureList = mutableListOf<InfoEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMainDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.plusBtn.setOnClickListener {
            view?.findNavController()
                ?.navigate(R.id.action_maindashboardFragment_to_lectureInitFragment)

        }
    }

    private fun initializeRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = DashboardAdapter(lectureList, this)
        recyclerView.adapter?.notifyDataSetChanged()

        if (lectureList.isEmpty()) {
            binding.animationView.visibility = View.VISIBLE
            binding.animationView.setAnimation(R.raw.nofile_animation)
            binding.animationView.playAnimation();
            binding.textView3.text = "아이콘을 눌러 강의를 등록하세요"
            binding.textView3.textSize = 16f
            binding.animationView.setOnClickListener {
                view?.findNavController()
                    ?.navigate(R.id.action_maindashboardFragment_to_lectureInitFragment)
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
                AppDatabase.getInstance(requireContext())?.infoDao()?.getAllLecture() ?: emptyList()
            lectureList.addAll(infoEntities)

            activity?.runOnUiThread {
                initializeRecyclerView()
            }
        }.start()
    }

    override fun onItemClickListener(position: Int) {
        val bundle = bundleOf("lecturename" to lectureList[position].scheduleName)
        view?.findNavController()
            ?.navigate(R.id.action_maindashboardFragment_to_lectureMainFragment, bundle)
    }

    override fun onButtonClickListener(position: Int, itemName: String, itemView: View) {
        val scheduleList: ArrayList<ScheduleEntity> = ArrayList()
        Thread {
            val infoEntities =
                AppDatabase.getInstance(requireContext())?.infoDao()?.getAll() ?: emptyList()
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
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.secondary_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.editBtn -> {
                    // 편집 동작 수행
                    editName(name)
                    true
                }

                R.id.deletebtn -> {
                    // 삭제 동작 수행
                    val nameToDelete = name // 삭제하고 싶은 아이템의 이름
                    GlobalScope.launch {
                        // 비동기적으로 데이터베이스에서 아이템을 삭제
                        delete(nameToDelete)
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
                            AppDatabase.getInstance(requireContext())?.infoDao()?.getAllLecture() ?: emptyList()
                        lectureList.addAll(infoEntities)

                        activity?.runOnUiThread {
                            initializeRecyclerView()
                        }
                    }.start()
                        // 데이터베이스에서 아이템을 다시 불러오기
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun delete(lecturename: String) {
        Thread {
            AppDatabase.getInstance(requireContext())?.infoDao()?.deleteByScheduleName(lecturename)
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "과목 삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun editName(name: String) {
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())
        editText.setText(name)

        builder.setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val editedName = editText.text.toString()
                // 여기에 실제 이름 수정에 대한 작업을 추가하세요.
                Log.d("EditName", "Editing name from $name to $editedName")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}

