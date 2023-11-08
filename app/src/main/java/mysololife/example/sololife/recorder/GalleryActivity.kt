package mysololife.example.sololife.recorder

import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityGalleryBinding

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class GalleryActivity : AppCompatActivity() , OnItemClickListener {
    private  lateinit var records : ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db : AppDatabase

    private var allChecked = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding : ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbar.setNavigationIconTint(Color.BLACK)
        records = ArrayList()


        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        db = Room.databaseBuilder(
            this,AppDatabase::class.java, "audioRecords").build()


        mAdapter = Adapter(records,this)
        binding.recyclerview.apply{
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var query = p0.toString()
                searchDatabase("%$query%")
            }

        })

        binding.btnClose.setOnClickListener {
            leaveEditMode()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.btnSelectAll.setOnClickListener {
            allChecked = !allChecked
            records.map{it.isChecked = allChecked}
            mAdapter.notifyDataSetChanged()

            if(allChecked){
                disableRename()
                enableDelete()
            }
            else{
                disableDelete()
                disableRename()
            }
        }

        binding.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("강의 녹음 삭제")
            val nbRecords = records.count({it.isChecked})
            builder.setMessage("${nbRecords}개의 강의 녹음본을 삭제하시겠습니까?")

            builder.setPositiveButton("삭제"){_,_->
                val toDelete = records.filter{it.isChecked}.toTypedArray()
                GlobalScope.launch {
                    db.audioRecorDao().delete(toDelete)
                    runOnUiThread {
                        records.removeAll(toDelete)
                        mAdapter.notifyDataSetChanged()
                        leaveEditMode()
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
            builder.setNegativeButton("취소"){_,_->
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecorDao().getAll()
            records.addAll(queryResult)

            mAdapter.notifyDataSetChanged()
        }

        binding.btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.rename_layout,null)
            builder.setView(dialogView)
            val dialog = builder.create()

            val record = records.filter{it.isChecked}.get(0)
            val textInput = dialogView.findViewById<TextInputEditText>(R.id.filenameInput)
            textInput.setText(record.filename)
            builder.show()
            dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                val input = textInput.text.toString()
                if(input.isEmpty()){
                    Toast.makeText(this,"변경할 이름을 작성하세요.",Toast.LENGTH_SHORT).show()
                }
                else{
                    record.filename = input
                    GlobalScope.launch {
                        db.audioRecorDao().update(record)
                        runOnUiThread {
                            mAdapter.notifyItemChanged(records.indexOf(record))
                            dialog.dismiss()
                            onBackPressed()
                            leaveEditMode()
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }
            }

            val btnCanCel = dialogView.findViewById<Button>(R.id.btnCancel)

           btnCanCel.setOnClickListener {
               dialog.dismiss()
               onBackPressed()
           }

        }
    }

    override fun onItemClickListener(position: Int) {
        var audioRecord = records[position]

        if (mAdapter.isEditMode()) {
            records[position].isChecked = !records[position].isChecked
            mAdapter.notifyItemChanged(position)

            var nbSelected = records.count{it.isChecked}
                when(nbSelected) {
                    0 ->{
                        disableRename()
                        disableDelete()
                    }
                    1 ->{
                        enableRename()
                        enableDelete()
                    }
                    else ->{
                        disableRename()
                        enableDelete()
                    }
                }

        } else {
            var intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }
    }

    private fun searchDatabase(query: String){
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecorDao().searchDatabase("%$query%")
            records.addAll(queryResult)

            runOnUiThread{
            mAdapter.notifyDataSetChanged()
        }}
    }

    private fun leaveEditMode(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // show relative layout
        binding.editBar.visibility = View.GONE
        records.map{it.isChecked = false}
        mAdapter.setEditMode(false)
    }
    private fun disableRename(){
        binding.btnEdit.isClickable = false
        binding.btnEdit.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled,theme)
        binding.tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled,theme))
    }

    private fun disableDelete(){
        binding.btnDelete.isClickable = false
        binding.btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled,theme)
        binding.tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDarkDisabled,theme))
    }

    private fun enableRename(){
        binding.btnEdit.isClickable = true
        binding.btnEdit.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDark,theme)
        binding.tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDark,theme))
    }

    private fun enableDelete(){
        binding.btnDelete.isClickable = true
        binding.btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources,
            R.color.grayDark,theme)
        binding.tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources,
            R.color.grayDark,theme))
    }

    override fun onItemLongClickListener(position: Int) {
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        if(mAdapter.isEditMode() && binding.editBar.visibility == View.GONE){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
            binding.editBar.visibility = View.VISIBLE

            enableRename()
            enableDelete()
        }

    }
}