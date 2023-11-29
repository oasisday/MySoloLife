package mysololife.example.sololife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mysololife.R
import com.example.mysololife.databinding.ActivityListViewBinding

class ListViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityListViewBinding
    private lateinit var userArrayList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageId = intArrayOf(

            R.drawable.img_1, R.drawable.img_2, R.drawable.img_3, R.drawable.img_4, R.drawable.img_5,
            R.drawable.img_6, R.drawable.img_7, R.drawable.img_8, R.drawable.img_9

        )

        val name = arrayOf(

            "민기", "형우", "재용", "태문", "두현",
            "혜진", "호신", "정식", "정욱"
        )

        val lastMessage = arrayOf(

            "ㅎㅇ", "뭐함", "어디서 볼까", "점메추", "가야됨",
            "수업중", "확인", "ㄱㄱ", "주말에 뭐함?"

        )

        val lastmsgTime = arrayOf(

            "8:45 pm", "9:00 am", "7:34 pm", "6:32 am", "5:76 am",
            "5:00 am", "7:34 pm", "2:32 am", "7:06 am"

        )

        val phoneNo = arrayOf(

            "1056865546", "1051258625", "1045057515", "1025555458", "1008125541",
            "1087781548", "1040547404", "1021231842", "1075455814"
        )

        val country = arrayOf(

            "United States", "Russia", "India", "Korea", "Germany", "Japan", "Canada", "France", "Norway"

        )

        userArrayList = ArrayList()

        for( i in name.indices){
            val user = User(name[i], lastMessage[i], lastmsgTime[i], phoneNo[i], country[i], imageId[i])
            userArrayList.add(user)

        }

        binding.listview.isClickable = true
        binding.listview.adapter = MyAdapter(this, userArrayList)
        binding.listview.setOnItemClickListener { parent, view, position, id ->

            val name = name[position]
            val phone = phoneNo[position]
            val country = country[position]
            val imageId = imageId[position]
            Toast.makeText(this,"해당 기능은 준비 중 입니다 :)",Toast.LENGTH_SHORT).show()

        }

    }
}